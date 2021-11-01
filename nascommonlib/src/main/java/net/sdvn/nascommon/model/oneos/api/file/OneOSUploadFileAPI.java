package net.sdvn.nascommon.model.oneos.api.file;

import android.text.TextUtils;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.fileserver.constants.SharePathType;
import net.sdvn.nascommon.model.http.ByteBufferRequestBody;
import net.sdvn.nascommon.model.http.ByteBufferStream;
import net.sdvn.nascommon.model.http.ProgressRequestBody;
import net.sdvn.nascommon.model.http.RequestBody;
import net.sdvn.nascommon.model.oneos.BaseResultModel;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener;
import net.sdvn.nascommon.model.oneos.transfer.TransferException;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.model.oneos.transfer.UploadElement;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.IOUtils;
import net.sdvn.nascommon.utils.log.Logger;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.weline.repo.SessionCache;
import io.weline.repo.api.V5HttpErrorNoKt;
import io.weline.repo.files.constant.AppConstants;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static net.sdvn.nascommon.model.oneos.transfer.TransferElement.REFRESH_TIME;

/**
 * OneSpace OS Upload File API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/14.
 * <p>
 * 注意：因V5新接口没有绝对路径，这里的path得用全路径，也就是带public,并在上传时，path去掉public
 * 文件上传  选择文件 -> 加入队列 -> 开始上传 ->上传完成
 */
public class OneOSUploadFileAPI extends BaseAPI {
    private static final String TAG = "Transfer--upload ";
    private static final int HTTP_UPLOAD_TIMEOUT = 30 * 1000;
    private static final int HTTP_UPLOAD_RENAME_TIMES = 100;
    private static final int HTTP_UPLOAD_RETRY_TIMES = 3;
    public static final int HTTP_BUFFER_SIZE = 1024 * 16;
    private static final double SSUDP_CHUNK_SIZE = 1024 * 1024;
    private long lastRefreshTime;
    private int callback = 0; // for upload progress callback
    /**
     * chuck block size: 2mb
     */
    private static final long HTTP_BLOCK_SIZE = 1024 * 1024 * 2;

    private OnTransferFileListener<UploadElement> listener;
    private UploadElement uploadElement;
    //    private boolean isInterrupt = false;
    private LoginSession loginSession;
    @Nullable
    private String pathPosition = null;
    private final OkHttpClient okHttpClient;
    private Call call;
    //    private RandomAccessFile inputStream;
    private static final String SERVER_TMP = ".tmpdata";


    public OneOSUploadFileAPI(@NonNull LoginSession loginSession, UploadElement element) {
        super(loginSession, OneOSAPIs.FILE_UPLOAD);
        this.loginSession = loginSession;
        this.uploadElement = element;
        okHttpClient = OkHttpClientIns.getTransmitClient();
    }

    public void setOnUploadFileListener(OnTransferFileListener<UploadElement> listener) {
        this.listener = listener;
    }

    public boolean upload() {
        if (null != listener) {
            listener.onStart(url(), uploadElement);
        }
        uploadElement.setState(TransferState.START);
        if (uploadElement.isCheck()) {
            long check = checkExist(uploadElement.getToPath() + File.separator + uploadElement.getSrcName());
            if (uploadElement.getState() == TransferState.START) {
                if (check == uploadElement.getSize()) {
                    uploadElement.setState(TransferState.COMPLETE);
                } else if (check != uploadElement.getSize() && check > 0) {
                    duplicateRename(uploadElement.getToPath() + File.separator + uploadElement.getSrcName(), uploadElement.getSrcName());
                } else {
                    doHttpUpload();
                }
            }
        } else {
            doHttpUpload();
        }

        if (null != listener) {
            listener.onComplete(url(), uploadElement);
        }

        return uploadElement.getState() == TransferState.COMPLETE;
    }

    //
    private void doHttpUpload() {
        oneOsRequest.setAction(OneOSAPIs.FILE_UPLOAD);
//        HttpUtils.log(TAG, url, null);
//        isInterrupt = false;
        boolean isV5 = SessionCache.Companion.getInstance().isV5OrSynchRequest(uploadElement.getDevId(), loginSession.getIp());
        String url = url();
        if (isV5 || loginSession.isV5()) {
            url = String.format("http://%s:%s/file/upload", loginSession.getIp(), AppConstants.HS_ANDROID_TV_PORT);
        }
        doHttpUploadNext(url);
    }

    private void doHttpUploadNext(String url) {
        String srcPath = uploadElement.getSrcPath();
        String targetPath = uploadElement.getToPath();
        boolean isV5 = SessionCache.Companion.getInstance().isV5(uploadElement.getDevId());
        String session = /*isV5 ? SessionCache.Companion.getInstance().getOrSynchRequest(uploadElement.getDevId(),
                loginSession.getIp(), LoginTokenUtil.getToken()).getSession()
                : */loginSession.getSession();
        File uploadFile = new File(srcPath);
        if (!uploadFile.exists()) {
            Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "upload file does not exist");
            uploadElement.setState(TransferState.FAILED);
            uploadElement.setException(TransferException.FILE_NOT_FOUND);
            return;
        }

        final long fileLen = uploadFile.length();
        long uploadPosition = uploadElement.getLength();
        if (uploadPosition == fileLen) {
            uploadElement.setState(TransferState.COMPLETE);
            return;
        }
        if (uploadPosition < 0 || uploadPosition > uploadElement.getSize()
            /* || uploadPosition != checkExist(uploadElement.getToPath() + File.separator + uploadElement.getSrcName() + SERVER_TMP)*/
        ) {
            uploadPosition = 0;
        }
        // Modified to new position, to make sure anim_progress is correct
        uploadElement.setLength(uploadPosition);
        uploadElement.setOffset(uploadPosition);

        int retry = 0; // exception retry times

        long chunks = (long) Math.ceil((double) fileLen / (double) HTTP_BLOCK_SIZE);
        long chunk = (uploadPosition / HTTP_BLOCK_SIZE);
        final long[] uploadLen = {HTTP_BLOCK_SIZE * chunk};
        final Long[] blockUpLen = new Long[1];
        final ProgressRequestBody.ProgressInterceptor progressInterceptor = new ProgressRequestBody.ProgressInterceptor() {

            @Override
            public void progress(long contentLength, long byteCount) throws IOException {
                if (uploadLen[0] <= fileLen) {
                    blockUpLen[0] += byteCount;
                    uploadLen[0] += byteCount;
                    uploadElement.setLength(uploadLen[0]);
                    long currentTime = System.currentTimeMillis();
                    boolean isNotify = (currentTime - lastRefreshTime) >= REFRESH_TIME;
                    if (null != listener && (isNotify)) {//|| callback == 32 || callback == 0
                        // callback every 512KB
                        if (false) {
                            Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "contentLength : "
                                    + contentLength + " blockUpLen : " + blockUpLen[0]
                                    + " uploadLen : " + uploadLen[0]);
                        }
                        listener.onTransmission(uploadElement.getTag(), uploadElement);
                        lastRefreshTime = currentTime;
                        callback = 1;
                    }
                    callback++;
                    if (uploadElement.getState() == TransferState.CANCELED) {
//                        isInterrupt = true;
                        if (call != null && !call.isCanceled()) {
                            call.cancel();
                        }
                    }
                } else {
                    throw new IOException("Upload: uploadLen > fileLen ");
                }
            }
        };
        for (; chunk < chunks; chunk++) {
            // 取消
            if (uploadElement.getState() != TransferState.START) {
                break;
            }
            // 上传大小超过 文件大小  异常
            if (uploadLen[0] >= fileLen) {
                Logger.p(Logger.Level.WARN, Logger.Logd.UPLOAD, TAG, "Upload exception: overSize "
                        + "fileLen :" + fileLen + " uploadLen :" + uploadLen[0]);
                break;
            }
            if (retry > HTTP_UPLOAD_RETRY_TIMES) {
                Logger.p(Logger.Level.WARN, Logger.Logd.UPLOAD, TAG, "Upload exception: Retry " + HTTP_UPLOAD_RETRY_TIMES + " times, Exit...");
                break;
            }
            //初始化

            FileChannel inChannel = null;
            FileChannel outChannel = null;

            blockUpLen[0] = 0L;

            // 计算块大小
            long chunksize;
            if (chunk == chunks - 1) {
                chunksize = fileLen - ((chunks - 1) * HTTP_BLOCK_SIZE);
            } else chunksize = HTTP_BLOCK_SIZE;

            Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "=====>>> BlockIndex:" + chunk + ", BlockNum:" + chunks + ", BlockSize:" + HTTP_BLOCK_SIZE +
                    " chunksize:" + chunksize);
//            File file = null;
            ByteBufferStream byteBufferStream = null;
            try {
                try {
                    RandomAccessFile inputStream = new RandomAccessFile(uploadFile, "r");
                    inChannel = inputStream.getChannel();
                    MappedByteBuffer inByteBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY,
                            chunk * HTTP_BLOCK_SIZE, chunksize);
                    byteBufferStream = new ByteBufferStream(inByteBuffer);
                } finally {
                    IOUtils.close(inChannel);
                }
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                builder.addFormDataPart("session", session);
                if (!TextUtils.isEmpty(targetPath)) {
                    String toDir = targetPath;
                    if (isV5 || loginSession.isV5()) {
                        toDir = OneOSAPIs.getV5Path(targetPath);
                    }
                    builder.addFormDataPart("todir", toDir);
                }
                if (isV5) {

                    int type = OneOSAPIs.getSharePathType(targetPath);
                    builder.addFormDataPart("share_path_type", String.valueOf(type));
//                    builder.addFormDataPart("md5", Md5Utils.getFileMD5(uploadFile));
                    builder.addFormDataPart("hashcode", String.valueOf(uploadElement.getTag().hashCode()));

                    builder.addFormDataPart("chunksize", String.valueOf(HTTP_BLOCK_SIZE));
                    Long groupId = uploadElement.getGroupId();
                    if (groupId != null) {
                        builder.addFormDataPart("groupid", String.valueOf(groupId));
                    }
                } else if (loginSession.isV5()) {//如果是android tv nas 1.0

                    builder.addFormDataPart("share_path_type", String.valueOf(SharePathType.PUBLIC.getType()));

                }
                builder.addFormDataPart("size", String.valueOf(uploadFile.length()));
                if (uploadElement.isOverwrite())
                    builder.addFormDataPart("overwrite", String.valueOf(1));
                if (chunk >= 0)
                    builder.addFormDataPart("chunk", String.valueOf(chunk));
                if (chunks > 0)
                    builder.addFormDataPart("chunks", String.valueOf(chunks));
                builder.addFormDataPart("name", uploadFile.getName());

                Timber.d(TAG + " params session :" + session);
                builder.addFormDataPart("file", uploadFile.getName(),
                        new ProgressRequestBody(new ByteBufferRequestBody(MediaType.parse("application/octet-stream"),
                                byteBufferStream),
                                progressInterceptor));
                //创建RequestBody
                okhttp3.RequestBody body = builder.build();
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Charset", "UTF-8")
                        .addHeader("connection", "keep-alive")
                        .post(body)
                        .build();
                if (uploadElement.getState() == TransferState.START) {
                    call = okHttpClient.newCall(request);
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        final String responseMessage = response.body().string();
                        Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "isSuccessful :" + responseMessage);
                        Logger.p(Logger.Level.INFO, Logger.Logd.UPLOAD, TAG, "Http Response result, msg = " + responseMessage);
//                        {"result":false, "error":{"code":xx,"msg":"xxxx"}}

                        if (isV5) {
                            Type type = new TypeToken<BaseResultModel<UploadResult>>() {
                            }.getType();
                            BaseResultModel<UploadResult> resultModel = GsonUtils.decodeJSON(responseMessage, type);
                            if (resultModel != null) {
                                if (resultModel.isSuccess()) {
                                    if (resultModel.data != null && resultModel.data.isFastUpload()) {
                                        uploadElement.setException(TransferException.NONE);
                                        retry = 0;
                                        uploadElement.setState(TransferState.COMPLETE);
                                    } else {
                                        uploadElement.setException(TransferException.NONE);
                                        retry = 0;
                                    }
                                } else {
                                    BaseResultModel.ErrorBean error = resultModel.getError();
                                    if (error != null) {
                                        switch (error.getCode()) {
                                            case HttpErrorNo.ERR_ONE_FILE_UPLOAD_CHUNK:
                                                String msg = error.getMsg();
                                                long offset = Long.parseLong(msg);
                                                chunk = 0;
                                                uploadLen[0] = 0;
                                                retry++;
                                                uploadElement.setLength(offset);
                                                uploadElement.setException(TransferException.IO_EXCEPTION);
                                                break;
                                            case V5HttpErrorNoKt.V5_ERR_DENIED_PERMISSION:
                                                uploadElement.setException(TransferException.NO_PERM);
                                                break;
                                            case V5HttpErrorNoKt.V5_ERR_DISK_IS_FULL:
                                            case V5HttpErrorNoKt.V5_ERR_USER_SPACE_FULL:
                                                uploadElement.setException(TransferException.SERVER_SPACE_INSUFFICIENT);
                                                break;
                                            case V5HttpErrorNoKt.V5_ERR_FILE_NOT_EXISTED:
                                                chunk = 0;
                                                uploadLen[0] = 0;
                                                retry++;
                                                uploadElement.setException(TransferException.SERVER_FILE_NOT_FOUND);
                                                break;
                                            case V5HttpErrorNoKt.V5_ERR_ERROR_PARAMS:
                                            case V5HttpErrorNoKt.V5_ERR_OPERATOR:
                                                if (Objects.equals(resultModel.getError().getMsg(), "Temporary file not found")) {
                                                    chunk = 0;
                                                    uploadLen[0] = 0;
                                                    retry++;
                                                    uploadElement.setException(TransferException.TEMPORARY_FILE_NOT_FOUND);
                                                } else {
                                                    uploadElement.setException(TransferException.UNKNOWN_EXCEPTION);
                                                }
                                                break;
                                            case V5HttpErrorNoKt.V5_ERR_NOT_LOGIN:
                                            case V5HttpErrorNoKt.V5_ERR_SESSION_EXP:
                                                SessionManager.getInstance().removeSession(uploadElement.getToDevId());
                                                uploadElement.setException(TransferException.AUTH_EXP);
                                                break;
                                            default:
                                                uploadElement.setException(TransferException.AUTH_EXP);

                                        }
                                        uploadElement.setState(TransferState.FAILED);
                                    }
                                }
                            } else {
                                uploadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                                retry++;
                            }
                        } else {
                            Type type = new TypeToken<BaseResultModel>() {
                            }.getType();
                            BaseResultModel resultModel = GsonUtils.decodeJSON(responseMessage, type);
                            if (resultModel != null) {
                                if (!resultModel.isSuccess()) {
                                    BaseResultModel.ErrorBean error = resultModel.getError();
                                    if (error != null) {
                                        switch (error.getCode()) {
                                            case HttpErrorNo.ERR_ONE_FILE_UPLOAD_CHUNK:
                                                String msg = error.getMsg();
                                                long offset = Long.parseLong(msg);
                                                chunk = 0;
                                                uploadLen[0] = 0;
                                                retry++;
                                                uploadElement.setLength(offset);
                                                uploadElement.setException(TransferException.IO_EXCEPTION);
                                                break;
                                            case HttpErrorNo.ERR_ONE_USER_SPACE:
                                                uploadElement.setException(TransferException.SERVER_SPACE_INSUFFICIENT);
                                                break;
                                            case HttpErrorNo.ERR_ONE_NO_FOUND:
                                                chunk = 0;
                                                uploadLen[0] = 0;
                                                retry++;
                                                uploadElement.setException(TransferException.SERVER_FILE_NOT_FOUND);
                                                break;
                                            case HttpErrorNo.ERR_ONE_REQUEST:
                                                if (Objects.equals(resultModel.getError().getMsg(), "Temporary file not found")) {
                                                    chunk = 0;
                                                    uploadLen[0] = 0;
                                                    retry++;
                                                    uploadElement.setException(TransferException.TEMPORARY_FILE_NOT_FOUND);
                                                } else {
                                                    uploadElement.setException(TransferException.UNKNOWN_EXCEPTION);
                                                }
                                                break;
                                            case HttpErrorNo.ERR_ONE_PARAM:
                                            case V5HttpErrorNoKt.V5_ERR_DENIED_PERMISSION:
                                                uploadElement.setException(TransferException.NO_PERM);
                                                break;
                                            case HttpErrorNo.ERR_ONE_NO_LOGIN:
                                                SessionManager.getInstance().removeSession(uploadElement.getToDevId());
                                                uploadElement.setException(TransferException.AUTH_EXP);
                                            default:
                                                uploadElement.setException(TransferException.AUTH_EXP);

                                        }
                                        uploadElement.setState(TransferState.FAILED);
                                    }
                                } else {
                                    uploadElement.setException(TransferException.NONE);
                                    retry = 0;
//                                if (null != listener) {
//                                    listener.onComplete(url(), uploadElement);
//                                }
                                }
                            } else {
                                uploadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                                retry++;
                            }
                        }


                    } else {
                        final String responseMessage = response.body().string();
                        Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "Http Response Error, code = " + response.code());
                        Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "Http Response Error, msg = " + response.message());
                        Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "Http Response Error, url = " + response.request().url());
                        Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "Http Response Error, body = " + (responseMessage));
                        response.close();
                        uploadElement.setState(TransferState.FAILED);
                        if (response.code() == 400 && EmptyUtils.isNotEmpty(responseMessage)) {
                            Type type = new TypeToken<BaseResultModel>() {
                            }.getType();
                            BaseResultModel resultModel = GsonUtils.decodeJSON(responseMessage, type);
                            if (resultModel != null) {
                                if (!resultModel.isSuccess()) {
                                    BaseResultModel.ErrorBean error = resultModel.getError();
                                    if (error != null) {
                                        switch (error.getCode()) {
                                            case HttpErrorNo.ERR_ONE_FILE_UPLOAD_CHUNK:
                                                String msg = error.getMsg();
                                                long offset = Long.parseLong(msg);
                                                chunk = 0;
                                                uploadLen[0] = 0;
                                                retry++;
                                                uploadElement.setLength(offset);
                                                uploadElement.setException(TransferException.IO_EXCEPTION);
                                                break;
                                            case HttpErrorNo.ERR_ONE_USER_SPACE:
                                                uploadElement.setException(TransferException.SERVER_SPACE_INSUFFICIENT);
                                                break;
                                            case HttpErrorNo.ERR_ONE_NO_FOUND:
                                                chunk = 0;
                                                uploadLen[0] = 0;
                                                retry++;
                                                uploadElement.setException(TransferException.SERVER_FILE_NOT_FOUND);
                                                break;
                                            case HttpErrorNo.ERR_ONE_REQUEST:
                                                if (Objects.equals(resultModel.getError().getMsg(), "Temporary file not found")) {
                                                    chunk = 0;
                                                    uploadLen[0] = 0;
                                                    retry++;
                                                    uploadElement.setException(TransferException.TEMPORARY_FILE_NOT_FOUND);
                                                } else {
                                                    uploadElement.setException(TransferException.UNKNOWN_EXCEPTION);
                                                }
                                                break;
                                            case HttpErrorNo.ERR_ONE_PARAM:
                                            case V5HttpErrorNoKt.V5_ERR_DENIED_PERMISSION:
                                                uploadElement.setException(TransferException.NO_PERM);
                                                break;
                                            case HttpErrorNo.ERR_ONE_NO_LOGIN:
                                                SessionManager.getInstance().removeSession(uploadElement.getToDevId());
                                                uploadElement.setException(TransferException.AUTH_EXP);
                                            default:
                                                uploadElement.setException(TransferException.AUTH_EXP);

                                        }
                                        uploadElement.setState(TransferState.FAILED);
                                    }
                                } else {
                                    uploadElement.setException(TransferException.NONE);
                                    retry = 0;
//                                if (null != listener) {
//                                    listener.onComplete(url(), uploadElement);
//                                }
                                }
                            } else {
                                uploadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                                retry++;
                            }
                        } else if (response.code() == 507) {
                            uploadElement.setException(TransferException.SERVER_SPACE_INSUFFICIENT);
                        } else {
                            uploadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                        }
                        retry++;
                    }
                }
            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                e.printStackTrace();
                uploadElement.setException(TransferException.ENCODING_EXCEPTION);
                e.printStackTrace();
                retry++;
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                uploadElement.setException(TransferException.SOCKET_TIMEOUT);
                e.printStackTrace();
                retry++;
            } catch (IllegalStateException | ClosedChannelException e) {
                e.printStackTrace();
                retry++;
                uploadElement.setException(TransferException.IO_EXCEPTION);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                retry++;
                uploadElement.setException(TransferException.FILE_NOT_FOUND);
            } catch (IOException e) {
                e.printStackTrace();
                if (uploadElement.getState() == TransferState.START) {
                    retry++;
                    uploadElement.setException(TransferException.IO_EXCEPTION);
                }
            } catch (Exception e) {
                e.printStackTrace();
                retry++;
                uploadElement.setException(TransferException.UNKNOWN_EXCEPTION);
            } finally {
                IOUtils.close(byteBufferStream);
                if (uploadElement.getState() != TransferState.START || retry > 0
                        || uploadElement.getException() != TransferException.NONE) {
                    try {
                        chunk--;
                        uploadLen[0] -= blockUpLen[0];
                        if (uploadLen[0] < 0) uploadLen[0] = 0;
                        uploadElement.setLength(uploadLen[0]);
                        Thread.sleep(retry * retry * 100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "The end of the file upload: FileLen = " + fileLen + ", UploadLen = " + uploadLen[0]);
        if (uploadElement.getException() == TransferException.NONE) {
            if (fileLen == uploadLen[0]) {
                uploadElement.setState(TransferState.COMPLETE);
            }
        } else {
            uploadElement.setState(TransferState.FAILED);
        }
    }


    private int count = 1;
    private int index = 1;
    @Nullable
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private void duplicateRename(final String path, final String srcName) {
//        boolean isV5 = SessionCache.Companion.getInstance().isV5(uploadElement.getDevId());
//        if (isV5) {
//            SessionCache.Companion.getInstance().getOrSynchRequest(loginSession.getId(),
//                    loginSession.getIp(), LoginTokenUtil.getToken(), new Function<DataSessionUser, Void>() {
//                        @Override
//                        public Void apply(DataSessionUser input) {
//                            if (input != null) {
//                                String session = input.getSession();
//                                duplicateRenameNext(path, srcName, session);
//                            } else {
//                                duplicateRenameNext(path, srcName, "");
//                            }
//                            return null;
//                        }
//                    });
//        } else {
        duplicateRenameNext(path, srcName, loginSession.getSession());
//        }

    }

    private void duplicateRenameNext(final String path, String srcName, String session) {

        try {
            String newName = genDuplicateName(srcName, index);
            Map<String, Object> params = new HashMap<>();
            params.put("cmd", "rename");
            params.put("newname", newName);
            oneOsRequest.setAction(OneOSAPIs.FILE_API);
            String url = url();
            if (isV5Api()) {
                if (!TextUtils.isEmpty(path)) {
                    params.put("path", OneOSAPIs.getV5Path(path));
                    params.put("share_path_type", OneOSAPIs.getSharePathType(path));
                }
                url = String.format("http://%s:%s/file", loginSession.getIp(), AppConstants.HS_ANDROID_TV_PORT);
            }

            RequestBody requestBody = new RequestBody("manage", session, params);
            String jsonString = requestBody.jsonString();
            Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "File requestBody: " + jsonString);
            Request.Builder builder = new Request.Builder();
            okhttp3.RequestBody body = okhttp3.RequestBody.create(MEDIA_TYPE_JSON, jsonString);
            builder.post(body)
                    .url(url);

            Request request = builder.build();
            call = okHttpClient.newCall(request);
            Response execute = call.execute();
//            String result = (String) httpUtils.postSync(url, params);
            if (execute.isSuccessful()) {
                String result = execute.body().string();
                Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "File Attr: " + result);
                JSONObject json = new JSONObject(result);
                boolean ret = json.getBoolean("result");
                if (ret) {
                    Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "======Duplicate Rename Success");
                    doHttpUpload();
                } else {
                    Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "======Duplicate Rename Failed");
                    if (count <= HTTP_UPLOAD_RENAME_TIMES) {
                        count++;
                        // index = (int) Math.pow(2, count);
                        index = count;
                        if (uploadElement.getState() == TransferState.START)
                            duplicateRename(path, srcName);
                    } else {
                        Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "======Duplicate Rename " + count + " Times, Skip...");
                        uploadElement.setState(TransferState.FAILED);
                    }
                }
            }
        } catch (Exception e) {
            Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "****Upload file not exist on server: " + path/*, e*/);
        }
    }

    private boolean isV5Api() throws Exception {
        return SessionCache.Companion.getInstance().isV5(loginSession.getId()) || loginSession.isV5();
    }

    private String genDuplicateName(String srcName, int index) {
        int pos = srcName.lastIndexOf(".");
        if (pos == -1) {
            return srcName + "_" + index;
        }

        String name = srcName.substring(0, pos);
        return name + "_" + index + srcName.substring(pos);
    }

    public void stopUpload() {
        uploadElement.setState(TransferState.PAUSE);
//        isInterrupt = true;
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "Upload Stopped");
    }

    /**
     * check if file exist in server
     *
     * @param path file server path
     * @return 1: exist and do not needs to upload; -1: needs rename old file then upload; 0: file do not exist
     */
    private long checkExist(String path) {
        try {
            //String url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
            Map<String, Object> params = new HashMap<>();
            //params.put("session", session);
            params.put("cmd", "attributes");
            params.put("path", path);
            oneOsRequest.setAction(OneOSAPIs.FILE_API);
            String url = url();
            if (isV5Api()) {
                if (!TextUtils.isEmpty(path)) {
                    params.put("path", OneOSAPIs.getV5Path(path));
                    params.put("share_path_type", OneOSAPIs.getSharePathType(path));
                }
                url = String.format("http://%s:%s/file", loginSession.getIp(), AppConstants.HS_ANDROID_TV_PORT);
            }
            setParams(params);
            setMethod("manage");
            Request.Builder builder = new Request.Builder();
            okhttp3.RequestBody body = okhttp3.RequestBody.create(MEDIA_TYPE_JSON, oneOsRequest.params());
            Timber.tag(TAG).d(oneOsRequest.toString());
            builder.post(body)
                    .url(url);

            Request request = builder.build();
            call = okHttpClient.newCall(request);
            Response execute = call.execute();
//            String result = (String) httpUtils.postSync(url, params);
            if (execute.isSuccessful()) {
                String result = null;
                if (execute.body() != null) {
                    result = execute.body().string();
                    Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "File Attr: " + result);
                    JSONObject json = new JSONObject(result);
                    boolean ret = json.getBoolean("result");
                    if (ret) {
                        return json.getJSONObject("data").getLong("size");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "****Upload file not exist on server: " + path/*, e*/);
        }

        return 0; // file do not exist
    }

    @Keep
    static class UploadResult {
        @SerializedName("FastUpload")
        boolean FastUpload = false;
        @SerializedName("path")
        String path = null;


        public boolean isFastUpload() {
            return FastUpload;
        }

        public void setFastUpload(boolean fastUpload) {
            FastUpload = fastUpload;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

}
