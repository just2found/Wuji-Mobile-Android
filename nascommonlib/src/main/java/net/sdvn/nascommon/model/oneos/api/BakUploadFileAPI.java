//package net.sdvn.nascommon.model.oneos.api;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import net.sdvn.common.internet.OkHttpClientIns;
//import net.sdvn.nascommon.constant.OneOSAPIs;
//import net.sdvn.nascommon.model.http.RequestBody;
//import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener;
//import net.sdvn.nascommon.model.oneos.transfer.TransferException;
//import net.sdvn.nascommon.model.oneos.transfer.TransferState;
//import net.sdvn.nascommon.model.oneos.transfer.UploadElement;
//import net.sdvn.nascommon.model.oneos.user.LoginSession;
//import net.sdvn.nascommon.utils.IOUtils;
//import net.sdvn.nascommon.utils.log.Logged;
//import net.sdvn.nascommon.utils.log.Logger;
//
//import org.json.JSONObject;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.RandomAccessFile;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//import okhttp3.Call;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
///**
// * OneSpace OS Upload File API
// * <p/>
// * Created by gaoyun@eli-tech.com on 2016/02/14.
// * <p>
// * todo 需要优化                   -pause -resume -cancel
// * 文件上传  选择文件 -> 加入队列 -> 开始上传 ->上传完成
// */
//public class BakUploadFileAPI extends BaseAPI {
//    private static final String TAG = "Transfer--upload ";
//    private static final int HTTP_UPLOAD_TIMEOUT = 30 * 1000;
//    private static final int HTTP_UPLOAD_RENAME_TIMES = 100;
//    private static final int HTTP_UPLOAD_RETRY_TIMES = 3;
//    public static final int HTTP_BUFFER_SIZE = 1024 * 16;
//    private static final double SSUDP_CHUNK_SIZE = 1024 * 1024;
//    /**
//     * chuck block size: 2mb
//     */
//    private static final long HTTP_BLOCK_SIZE = 1024 * 1024 * 2;
//
//    private OnTransferFileListener<UploadElement> listener;
//    private UploadElement uploadElement;
//    private LoginSession loginSession;
//    @Nullable
//    private String pathPosition = null;
//    private final OkHttpClient okHttpClient;
//    private Call call;
//    //    private RandomAccessFile inputStream;
//    private static final String SERVER_TMP = ".tmpdata";
//
//
//    public BakUploadFileAPI(@NonNull LoginSession loginSession, UploadElement element) {
//        super(loginSession, OneOSAPIs.FILE_UPLOAD);
//        this.loginSession = loginSession;
//        this.uploadElement = element;
//        okHttpClient = OkHttpClientIns.getTransmitClient();
//    }
//
//    public void setOnUploadFileListener(OnTransferFileListener<UploadElement> listener) {
//        this.listener = listener;
//    }
//
//    public boolean upload() {
//        if (null != listener) {
//            listener.onStart(url(), uploadElement);
//        }
//        uploadElement.setState(TransferState.START);
//        if (uploadElement.isCheck()) {
//            long check = checkExist(uploadElement.getToPath() + uploadElement.getSrcName());
//            if (uploadElement.getState() == TransferState.START) {
//                if (check == uploadElement.getSize()) {
//                    uploadElement.setState(TransferState.COMPLETE);
//                } else if (check != uploadElement.getSize() && check > 0) {
//                    duplicateRename(uploadElement.getToPath() + uploadElement.getSrcName(), uploadElement.getSrcName());
//                } else {
//                    doHttpUpload();
//                }
//            }
//        } else {
//            doHttpUpload();
//        }
//
//        if (null != listener) {
//            listener.onComplete(url(), uploadElement);
//        }
//
//        return uploadElement.getState() == TransferState.COMPLETE;
//    }
//
//    //
//    private void doHttpUpload() {
//        oneOsRequest.setAction(OneOSAPIs.FILE_UPLOAD);
////        HttpUtils.log(TAG, url, null);
//
//        String session = loginSession.getSession();
//        String srcPath = uploadElement.getSrcPath();
//        String targetPath = uploadElement.getToPath();
//
//        File uploadFile = new File(srcPath);
//        if (!uploadFile.exists()) {
//            Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "upload file does not exist");
//            uploadElement.setState(TransferState.FAILED);
//            uploadElement.setException(TransferException.FILE_NOT_FOUND);
//            return;
//        }
//
//        final long fileLen = uploadFile.length();
//        long uploadPosition = uploadElement.getLength();
//        if (uploadPosition == fileLen) {
//            uploadElement.setState(TransferState.COMPLETE);
//            return;
//        }
//        if (uploadPosition < 0 || uploadPosition > uploadElement.getSize()
//            /* || uploadPosition != checkExist(uploadElement.getToPath() + File.separator + uploadElement.getSrcName() + SERVER_TMP)*/
//        ) {
//            uploadPosition = 0;
//        }
//        // Modified to new position, to make sure anim_progress is correct
//        uploadElement.setLength(uploadPosition);
//        uploadElement.setOffset(uploadPosition);
//        if (uploadElement.getState() != TransferState.START) {
//            return;
//        }
//        String PREFIX = "--";
//        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
//        String LINE_END = "\r\n";
//        String CONTENT_TYPE = "multipart/form-data"; // 内容类型
//
//        long retry = 0; // exception retry times
//        long uploadLen = 0;
//        long chunkNum = (long) Math.ceil((double) fileLen / (double) HTTP_BLOCK_SIZE);
//        long chunkIndex = (uploadPosition / HTTP_BLOCK_SIZE);
//
//        for (; chunkIndex < chunkNum; chunkIndex++) {
//            Logger.p(Logger.Level.DEBUG, Logged.UPLOAD, TAG, "=====>>> BlockIndex:" + chunkIndex + ", BlockNum:" + chunkNum + ", BlockSize:" + HTTP_BLOCK_SIZE);
//            long blockUpLen = 0;
//            OutputStream outStream = null;
//            RandomAccessFile inputStream = null;
//            try {
//                URL mUrl = new URL(url());
//                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
//                if (uploadElement.getState() != TransferState.START) {
//                    break;
//                }
//                conn.setReadTimeout(HTTP_UPLOAD_TIMEOUT);
//                conn.setConnectTimeout(HTTP_UPLOAD_TIMEOUT);
//                conn.setDoInput(true); // 允许输入流
//                conn.setDoOutput(true); // 允许输出流
//                conn.setUseCaches(false); // 不允许使用缓存
//                conn.setRequestMethod("POST"); // 请求方式
//                conn.setRequestProperty("Charset", "UTF-8"); // 设置编码
//                conn.setRequestProperty("connection", "keep-alive");
//                conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
//                // conn.connect();
//
//                outStream = new BufferedOutputStream(conn.getOutputStream());
//                if (uploadElement.getState() != TransferState.START) {
//                    break;
//                }
//                StringBuilder sb = new StringBuilder();
//
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"session\"").append(LINE_END);
//                sb.append(LINE_END);
//                sb.append(session);
//                sb.append(LINE_END);
////                outStream.write(sb.toString().getBytes());
////                outStream.flush();
////
////                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"todir\"").append(LINE_END);
//                sb.append(LINE_END);
//                sb.append(targetPath);
//                sb.append(LINE_END);
////                outStream.write(sb.toString().getBytes());
////                outStream.flush();
//
//                if (uploadElement.isOverwrite()) {
////                    sb = new StringBuilder();
//                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                    sb.append("Content-Disposition: form-data; name=\"overwrite\"").append(LINE_END);
//                    sb.append(LINE_END);
//                    sb.append("1");
//                    sb.append(LINE_END);
////                    outStream.write(sb.toString().getBytes());
////                    outStream.flush();
//                }
//
////                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"chunks\"").append(LINE_END);
//                sb.append(LINE_END);
//                sb.append(chunkNum);
//                sb.append(LINE_END);
////                outStream.write(sb.toString().getBytes());
////                outStream.flush();
//
////                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"chunk\"").append(LINE_END);
//                sb.append(LINE_END);
//                sb.append(chunkIndex);
//                sb.append(LINE_END);
////                outStream.write(sb.toString().getBytes());
////                outStream.flush();
//
////                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"name\"").append(LINE_END);
//                sb.append(LINE_END);
//                sb.append(uploadFile.getName());
//                sb.append(LINE_END);
////                outStream.write(sb.toString().getBytes());
////                outStream.flush();
//
////                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"size\"").append(LINE_END);
//                sb.append(LINE_END);
//                sb.append(uploadFile.length());
//                sb.append(LINE_END);
////                outStream.write(sb.toString().getBytes());
////                outStream.flush();
//
////                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(uploadFile.getName()).append("\"").append(LINE_END);
//                sb.append("Content-Type: application/octet-stream;charset=UTF-8").append(LINE_END);
//                sb.append(LINE_END);
//                outStream.write(sb.toString().getBytes());
//                outStream.flush();
//                if (uploadElement.getState() != TransferState.START) {
//                    break;
//                }
//                inputStream = new RandomAccessFile(uploadFile, "r");
//                inputStream.seek(chunkIndex * HTTP_BLOCK_SIZE);
//                byte[] bytes = new byte[HTTP_BUFFER_SIZE];
//                int len;
//                int callback = 0; // for upload progress callback
//                while (uploadElement.getState() == TransferState.START
//                        && (len = inputStream.read(bytes)) != -1) {
//                    outStream.write(bytes, 0, len);
//                    outStream.flush();
//                    blockUpLen += len;
//                    uploadLen += len;
//                    uploadElement.setLength(uploadLen);
//                    callback++;
//                    if (null != listener && callback == 64) {
//                        // callback every 512KB
//                        listener.onTransmission(url(), uploadElement);
//                        callback = 0;
//                    }
//                    if (blockUpLen >= HTTP_BLOCK_SIZE) {
//                        break;
//                    }
//                }
//                inputStream.close();
//
//                if (uploadElement.getState() == TransferState.START) {
//                    outStream.write(LINE_END.getBytes());
//                    byte[] end = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
//                    outStream.write(end);
//                    outStream.flush();
//                    outStream.close();
//
//                    int code = conn.getResponseCode();
//                    if (code != HttpURLConnection.HTTP_OK) {
//                        Logger.p(Logger.Level.ERROR, Logged.UPLOAD, TAG, "Http Response Error, code = " + code);
//                        uploadElement.setException(TransferException.FAILED_REQUEST_SERVER);
//                        retry++;
//                    } else {
//                        retry = 0;
//                    }
//                } else {
//                    outStream.close();
//                    Logger.p(Logger.Level.DEBUG, Logged.UPLOAD, TAG, "End, file upload interrupt");
//                    break;
//                }
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//                retry++;
//                uploadElement.setException(TransferException.FAILED_REQUEST_SERVER);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                retry++;
//                uploadElement.setException(TransferException.FILE_NOT_FOUND);
//            } catch (IOException e) {
//                e.printStackTrace();
//                retry++;
//                uploadElement.setException(TransferException.IO_EXCEPTION);
//            } finally {
//                if (uploadElement.getState() == TransferState.START && retry > 0) {
//                    try {
//                        chunkIndex--;
//                        uploadLen -= blockUpLen;
//                        uploadElement.setLength(uploadLen);
//                        Thread.sleep(retry * retry * 100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (uploadElement.getState() == TransferState.START && retry > HTTP_UPLOAD_RETRY_TIMES) {
//                    Logger.p(Logger.Level.WARN, Logged.UPLOAD, TAG, "Upload exception: Retry " + HTTP_UPLOAD_RETRY_TIMES + " times, Exit...");
//                    break;
//                }
//                IOUtils.close(outStream);
//                IOUtils.close(inputStream);
//            }
//        }
//
//        Logger.p(Logger.Level.DEBUG, Logged.UPLOAD, TAG, "The end of the file upload: FileLen = " + fileLen + ", UploadLen = " + uploadLen);
//        if (uploadElement.getState() == TransferState.CANCELED) {
//            Logger.p(Logger.Level.DEBUG, Logged.UPLOAD, TAG, "canceled");
//            return;
//        }
//        if (uploadElement.getState() == TransferState.START) {
//            if (fileLen == uploadLen) {
//                uploadElement.setState(TransferState.COMPLETE);
//            } else {
//                uploadElement.setState(TransferState.FAILED);
//            }
//        }
//    }
//   /* private void doHttpUploadSingle() {
//        oneOsRequest.setAction(OneOSAPIs.FILE_UPLOAD);
////        HttpUtils.log(TAG, url, null);
//        isInterrupt = false;
//        uploadElement.setShareState(TransferState.START_CHANNEL);
//        String session = loginSession.getSession();
//        String srcPath = uploadElement.getSrcPath();
//        String targetPath = uploadElement.getToPath();
//
//        File uploadFile = new File(srcPath);
//        if (!uploadFile.exists()) {
//            Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "upload file does not exist");
//            uploadElement.setShareState(TransferState.FAILED);
//            uploadElement.setException(TransferException.FILE_NOT_FOUND);
//            return;
//        }
//
//        final long fileLen = uploadFile.length();
//        long uploadPosition = uploadElement.getLength();
//        if (uploadPosition == fileLen) {
//            uploadElement.setShareState(TransferState.COMPLETE);
//            return;
//        }
////        if (uploadPosition < 0 || uploadPosition > uploadElement.getSize()) {
//        uploadPosition = 0;
////        }
//        // Modified to new position, to make sure anim_progress is correct
//        uploadElement.setLength(uploadPosition);
//        uploadElement.setOffset(uploadPosition);
//
//        int retry = 0; // exception retry times
//        final long[] uploadLen = {uploadPosition};
//        while (!isInterrupt) {
//            try {
//                uploadLen[0] = 0;
//                uploadElement.setOffset(0);
//                uploadElement.setLength(0);
//                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
//                map.put("session", session);
//                if (!TextUtils.isEmpty(targetPath))
//                    map.put("todir", targetPath);
//                map.put("size", uploadFile.length());
//                int overwrite = uploadElement.isOverwrite() ? 1 : 0;
//                map.put("overwrite", overwrite);
//                map.put("file", uploadFile);
//                map.put("name", uploadFile.getName());
//                okhttp3.RequestBody body = httpRequest.buildMultipartBody(map,
//                        new ProgressRequestBody.ProgressInterceptor() {
//                            int callback = 0; // for upload progress callback
//
//                            @Override
//                            public void progress(long contentLength, long byteCount) {
//                                callback++;
//                                uploadLen[0] += byteCount;
//                                uploadElement.setLength(uploadLen[0]);
//                                if (null != listener && callback == 64) {
//                                    // callback every 512KB
//                                    Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "contentLength : "
//                                            + contentLength
//                                            + " uploadLen : " + uploadLen[0]);
//                                    listener.onTransmission(url(), uploadElement);
//                                    callback = 0;
//                                }
//                            }
//                        });
////                conn.setRequestProperty("Charset", "UTF-8"); // 设置编码
////                conn.setRequestProperty("connection", "keep-alive");
//                Request request = new Request.Builder()
//                        .url(url())
//                        .addHeader("Charset", "UTF-8")
//                        .addHeader("connection", "keep-alive")
//                        .post(body)
//                        .build();
//                final Call call = okHttpClient.newCall(request);
//                addObserver(call);
//
//                uploadElement.setShareState(TransferState.START_CHANNEL);
//                Response response = call.execute();
//                if (response.isSuccessful()) {
//                    Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, response.body().string());
//                    retry = 0;
//                    break;
//                } else {
//                    Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "Http Response Error, code = " + response.code());
//                    Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "Http Response Error, msg = " + response.message());
//                    uploadElement.setException(TransferException.FAILED_REQUEST_SERVER);
//                    retry++;
//                }
//            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
//                e.printStackTrace();
//                uploadElement.setException(TransferException.ENCODING_EXCEPTION);
//                e.printStackTrace();
//            } catch (SocketTimeoutException e) {
//                e.printStackTrace();
//                uploadElement.setException(TransferException.SOCKET_TIMEOUT);
//                e.printStackTrace();
//            } catch (IllegalStateException | ClosedChannelException e) {
//                e.printStackTrace();
//                retry++;
//                uploadElement.setException(TransferException.IO_EXCEPTION);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                retry++;
//                uploadElement.setException(TransferException.FILE_NOT_FOUND);
//            } catch (IOException e) {
//                e.printStackTrace();
//                retry++;
//                uploadElement.setException(TransferException.IO_EXCEPTION);
//            } catch (Exception e) {
//                e.printStackTrace();
//                retry++;
//                uploadElement.setException(TransferException.UNKNOWN_EXCEPTION);
//            } finally {
//                if (isInterrupt || retry > 0) {
//                    try {
//                        Thread.sleep(retry * retry * 100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (!isInterrupt && retry > HTTP_UPLOAD_RETRY_TIMES) {
//                    Logger.p(Logger.Level.WARN, Logger.Logd.UPLOAD, TAG, "Upload exception: Retry " + HTTP_UPLOAD_RETRY_TIMES + " times, Exit...");
//                    break;
//                }
//            }
//        }
//        Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "The end of the file upload: FileLen = " + fileLen + ", UploadLen = " + uploadLen[0]);
//        if (fileLen == uploadLen[0]) {
//            uploadElement.setShareState(TransferState.COMPLETE);
//        } else if (uploadLen[0] < fileLen) {
//            if (isInterrupt) {
//                uploadElement.setShareState(TransferState.PAUSE);
//            } else {
//                uploadElement.setShareState(TransferState.FAILED);
//            }
//        } else {
//            uploadElement.setShareState(TransferState.FAILED);
//        }
//        File file = new File(getContext().getCacheDir().getAbsolutePath(),
//                uploadFile.getName() + AppConstants.TMP);
//        if (file.exists()) {
//            file.delete();
//        }
//    }*/
//
//
//    int count = 1;
//    int index = 1;
//    @Nullable
//    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
//
//    private void duplicateRename(final String path, final String srcName) {
//        // String newName = genDuplicateName(srcName, index);
//        Map<String, Object> params = new HashMap<>();
//        params.put("cmd", "rename");
//        params.put("path", path);
//        RequestBody requestBody = new RequestBody("manage", loginSession.getSession(), params);
//        String jsonString = requestBody.jsonString();
//
////        String url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
//        try {
//
//            Request.Builder builder = new Request.Builder();
//            okhttp3.RequestBody body = okhttp3.RequestBody.create(MEDIA_TYPE_JSON, jsonString);
//            builder.post(body)
//                    .url(url());
//
//            Request request = builder.build();
//            call = okHttpClient.newCall(request);
//            Response execute = call.execute();
////            String result = (String) httpUtils.postSync(url, params);
//            if (execute.isSuccessful()) {
//                String result = execute.body().string();
//                Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "File Attr: " + result);
//                JSONObject json = new JSONObject(result);
//                boolean ret = json.getBoolean("result");
//                if (ret) {
//                    Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "======Duplicate Rename Success");
//                    doHttpUpload();
//                } else {
//                    Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "======Duplicate Rename Failed");
//                    if (count <= HTTP_UPLOAD_RENAME_TIMES) {
//                        count++;
//                        // index = (int) Math.pow(2, count);
//                        index = count;
//                        if (uploadElement.getState() == TransferState.START)
//                            duplicateRename(path, srcName);
//                    } else {
//                        Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "======Duplicate Rename " + count + " Times, Skip...");
//                        uploadElement.setState(TransferState.FAILED);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "****Upload file not exist on server: " + path/*, e*/);
//        }
//    }
//
////    private String genDuplicateName(String srcName, int index) {
////        int pos = srcName.lastIndexOf(".");
////        if (pos == -1) {
////            return srcName + "_" + index;
////        }
////
////        String name = srcName.substring(0, pos);
////        return name + "_" + index + srcName.substring(pos, srcName.length());
////    }
//
//    public void stopUpload() {
//        if (call != null && !call.isCanceled())
//            call.cancel();
//        Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "Upload Stopped");
//    }
//
//    /**
//     * check if file exist in server
//     *
//     * @param path file server path
//     * @return 1: exist and do not needs to upload; -1: needs rename old file then upload; 0: file do not exist
//     */
//    private long checkExist(String path) {
//        //String url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
//        Map<String, Object> params = new HashMap<>();
//        //params.put("session", session);
//        params.put("cmd", "attributes");
//        params.put("path", path);
//        setParams(params);
//        setMethod("manage");
//        oneOsRequest.setAction(OneOSAPIs.FILE_API);
//
//        try {
//            Request.Builder builder = new Request.Builder();
//            okhttp3.RequestBody body = okhttp3.RequestBody.create(MEDIA_TYPE_JSON, oneOsRequest.params());
//            builder.post(body)
//                    .url(url());
//
//            Request request = builder.build();
//            call = okHttpClient.newCall(request);
//            Response execute = call.execute();
////            String result = (String) httpUtils.postSync(url, params);
//            if (execute.isSuccessful()) {
//                String result = null;
//                if (execute.body() != null) {
//                    result = execute.body().string();
//                }
//
////                String url = genOneOSAPIUrl(OneOSAPIs.FILE_API);
////                params.put("path", path);
////                String result = (String) httpUtils.postSync(url, new RequestBody("manage", session, params));
//
//                Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "File Attr: " + result);
//                JSONObject json = new JSONObject(result);
//                boolean ret = json.getBoolean("result");
//                if (ret) {
//                    return json.getJSONObject("data").getLong("size");
////                    if (size == srcSize) {
////                        Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "****Upload file exist on server: " + path);
////                        return 1; // exist and do not needs to upload
////                    } else {
////                        return -1; // needs rename old file
////                    }
//                }
//            }
//        } catch (Exception e) {
//            // e.printStackTrace();
//            Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "****Upload file not exist on server: " + path/*, e*/);
//        }
//
//        return 0; // file do not exist
//    }
//
//   /* private void doHttpUpload() {
//        oneOsRequest.setAction(OneOSAPIs.FILE_UPLOAD);
//        //        HttpUtils.log(TAG, url, null);
//
//        uploadElement.setState(TransferState.START_CHANNEL);
////        String session = loginSession.getSession();
//        String srcPath = uploadElement.getSrcPath();
//        String targetPath = uploadElement.getToPath();
//
//        File uploadFile = new File(srcPath);
//        if (!uploadFile.exists()) {
//            Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "upload file does not exist");
//            uploadElement.setState(TransferState.FAILED);
//            uploadElement.setException(TransferException.FILE_NOT_FOUND);
//            return;
//        }
//
//        final long fileLen = uploadFile.length();
//        if (uploadElement.getException() == TransferException.SERVER_FILE_NOT_FOUND) {
//            uploadElement.setLength(0);
//        }
//        long uploadPosition = uploadElement.getLength();
//        // Modified to new position, to make sure anim_progress is correct
//        if (uploadPosition < 0 || uploadPosition > fileLen)
//            uploadPosition = 0;
//        uploadElement.setLength(uploadPosition);
//        uploadElement.setOffset(uploadPosition);
//
//        String PREFIX = "--";
//        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
//        String LINE_END = "\r\n";
//        String CONTENT_TYPE = "multipart/form-data"; // 内容类型
//
//        long retry = 0; // exception retry times
//        long uploadLen = uploadPosition;
//        long chunks = (long) Math.ceil((double) fileLen / (double) HTTP_BLOCK_SIZE);
//        long chunk = (uploadPosition / HTTP_BLOCK_SIZE);
//        long timestamp = System.currentTimeMillis();
//        a:
//        for (; chunk < chunks; chunk++) {
//            if (isInterrupt) {
//                Logger.p(Logger.Level.WARN, Logger.Logd.UPLOAD, TAG, "Upload exception: isInterrupt, Exit...");
//                break;
//            }
//            if (retry > HTTP_UPLOAD_RETRY_TIMES) {
//                Logger.p(Logger.Level.WARN, Logger.Logd.UPLOAD, TAG, "Upload exception: Retry "
//                        + HTTP_UPLOAD_RETRY_TIMES + " times, Exit...");
//                break;
//            }
//            Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "=====>>> BlockIndex:" + chunk +
//                    ", BlockNum:" + chunks + ", url:" + url() + " uploadPosition%HTTP_BLOCK_SIZE :"
//                    + uploadPosition % HTTP_BLOCK_SIZE);
//            long blockUpLen = 0;
//            DataOutputStream outStream = null;
//            RandomAccessFile inputStream = null;
//            try {
//                URL mUrl = new URL(url());
//                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
//                conn.setReadTimeout(HTTP_UPLOAD_TIMEOUT);
//                conn.setConnectTimeout(HTTP_UPLOAD_TIMEOUT);
//                conn.setDoInput(true); // 允许输入流
//                conn.setDoOutput(true); // 允许输出流
//                conn.setUseCaches(false); // 不允许使用缓存
//                conn.setRequestMethod("POST"); // 请求方式
//                conn.setRequestProperty("Charset", "UTF-8"); // 设置编码
//                conn.setRequestProperty("connection", "keep-alive");
//                conn.setRequestProperty("Content-Type", CONTENT_TYPE + "; boundary=" + BOUNDARY);
//                // conn.connect();
//
//                outStream = new DataOutputStream(conn.getOutputStream());
//                StringBuilder sb = new StringBuilder();
//
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"session\"").append(LINE_END);
//                sb.append(LINE_END);
//                sb.append(loginSession.getSession());
//                sb.append(LINE_END);
//                outStream.write(sb.toString().getBytes());
//                outStream.flush();
//
//                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"todir\"").append(LINE_END);
//                sb.append(LINE_END);
//                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "targetPath====" + targetPath);
//                sb.append(targetPath);
//                sb.append(LINE_END);
//                outStream.write(sb.toString().getBytes());
//                outStream.flush();
//
//                if (uploadElement.isOverwrite()) {
//                    sb = new StringBuilder();
//                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                    sb.append("Content-Disposition: form-data; name=\"overwrite\"").append(LINE_END);
//                    sb.append(LINE_END);
//                    sb.append(1);
//                    sb.append(LINE_END);
//                    outStream.write(sb.toString().getBytes());
//                    outStream.flush();
//                }
//                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"chunks\"").append(LINE_END);
//                sb.append(LINE_END);
//                sb.append(chunks);
//                sb.append(LINE_END);
//                outStream.write(sb.toString().getBytes());
//                outStream.flush();
//
//                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"chunk\"").append(LINE_END);
//                sb.append(LINE_END);
//                sb.append(chunk);
//                sb.append(LINE_END);
//                outStream.write(sb.toString().getBytes());
//                outStream.flush();
//
//                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"name\"").append(LINE_END);
//                sb.append(LINE_END);
//                sb.append(uploadFile.getName());
//                sb.append(LINE_END);
//                outStream.write(sb.toString().getBytes());
//                outStream.flush();
//
//                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"size\"").append(LINE_END);
//                sb.append(LINE_END);
//                sb.append(fileLen);
//                sb.append(LINE_END);
//                outStream.write(sb.toString().getBytes());
//                outStream.flush();
//
//                sb = new StringBuilder();
//                sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
//                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(uploadFile.getName()).append("\"").append(LINE_END);
//                sb.append("Content-Type: application/octet-stream; charset=UTF-8").append(LINE_END);
//                sb.append(LINE_END);
//                outStream.write(sb.toString().getBytes());
//                outStream.flush();
//
//                inputStream = new RandomAccessFile(uploadFile, "r");
//                inputStream.seek(uploadLen);
//                byte[] bytes = new byte[HTTP_BUFFER_SIZE];
//                int len;
//                int callback = 0; // for upload progress callback
//                while (!isInterrupt && (len = inputStream.read(bytes)) != -1) {
//                    outStream.write(bytes, 0, len);
//                    outStream.flush();
//                    blockUpLen += len;
//                    uploadLen += len;
//                    uploadElement.setLength(uploadLen);
//                    callback++;
//                    long timeMillis = System.currentTimeMillis();
//                    if (null != listener && (timeMillis - timestamp >= 980 || callback == 32)) {
////                         callback every 512KB
//                        listener.onTransmission(url(), uploadElement);
//                        callback = 0;
//                        timestamp = timeMillis;
//
//                    }
//                    if (blockUpLen >= HTTP_BLOCK_SIZE) {
//                        break;
//                    }
//                }
//                inputStream.close();
//
//                if (blockUpLen == HTTP_BLOCK_SIZE || !isInterrupt) {
//                    outStream.write(LINE_END.getBytes());
//                    byte[] end = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
//                    outStream.write(end);
//                    outStream.flush();
//                    outStream.close();
//
//                    int code = conn.getResponseCode();
//                    if (code != HttpURLConnection.HTTP_OK) {
//                        Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "Http Response Error, code = " + code);
//                        String responseMessage = conn.getResponseMessage();
//                        Logger.p(Logger.Level.ERROR, Logger.Logd.UPLOAD, TAG, "Http Response Error, msg = " + responseMessage);
//                        uploadElement.setException(TransferException.FAILED_REQUEST_SERVER);
////                        {"result":false, "error":{"code":xx,"msg":"xxxx"}}
//                        Type type = new TypeToken<BaseResultModel>() {
//                        }.getType();
//                        BaseResultModel resultModel = GsonUtils.decodeJSON(responseMessage, type);
//                        if (resultModel != null) {
//                            BaseResultModel.ErrorBean error = resultModel.getError();
//                            switch (error.getCode()) {
//                                case HttpErrorNo.ERR_ONE_FILE_UPLOAD_CHUNK:
//                                    String msg = error.getMsg();
//                                    long offset = Long.parseLong(msg);
//                                    uploadElement.setLength(offset);
//                                    uploadElement.setException(TransferException.IO_EXCEPTION);
//                                    break a;
//                                case HttpErrorNo.ERR_ONE_USER_SPACE:
//                                    uploadElement.setException(TransferException.SERVER_SPACE_INSUFFICIENT);
//                                    break a;
//                                case HttpErrorNo.ERR_ONE_NO_FOUND:
//                                    uploadElement.setException(TransferException.SERVER_FILE_NOT_FOUND);
//                                    break a;
//                            }
//                        }
//                        retry++;
//                    } else {
//                        Logger.p(Logger.Level.WARN, Logger.Logd.UPLOAD, TAG, "Http Response , msg = " + conn.getResponseMessage());
//                        retry = 0;
//                    }
//                } else {
//                    outStream.close();
//                    Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "End, file upload interrupt");
//                    break;
//                }
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//                retry++;
//                uploadElement.setException(TransferException.FAILED_REQUEST_SERVER);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                retry++;
//                uploadElement.setException(TransferException.FILE_NOT_FOUND);
//            } catch (IOException e) {
//                e.printStackTrace();
//                retry++;
//                uploadElement.setException(TransferException.IO_EXCEPTION);
//            } catch (Exception e) {
//                e.printStackTrace();
//                retry++;
//                uploadElement.setException(TransferException.UNKNOWN_EXCEPTION);
//            } finally {
//                IOUtils.close(inputStream);
//                IOUtils.close(outStream);
//                if (retry > 0) {
//                    try {
//                        if (blockUpLen < HTTP_BLOCK_SIZE) {
//                            chunk--;
//                            uploadLen -= blockUpLen;
//                            uploadElement.setLength(uploadLen);
//                        }
//                        Thread.sleep(retry * retry * 100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//        }
//
//        Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "The end of the file upload: FileLen = " + fileLen + ", UploadLen = " + uploadLen);
//        if (fileLen == uploadLen) {
//            uploadElement.setState(TransferState.COMPLETE);
//        } else {
//            if (isInterrupt) {
//                uploadElement.setState(TransferState.PAUSE);
//            } else {
//                uploadElement.setState(TransferState.FAILED);
//            }
//        }
//    }
//*/
//
//
///*
//    private void getServerPhotoList(String url, Map<String, String> map, List<ScanningAlbumThread.TmpElemet> photoList) {
//
//        List<NameValuePair> params = new ArrayList<NameValuePair>();
//        Set<String> keys = map.keySet();
//        for (String key : keys) {
//            params.add(new BasicNameValuePair(key, map.get(key)));
//        }
//
//        try {
//            HttpPost httpRequest = new HttpPost(url);
//            Logger.p(Level.DEBUG, Logd.DEBUG, TAG, "Url: " + url);
//            DefaultHttpClient httpClient = new DefaultHttpClient();
//
//            httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 5000);
//            httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
//
//            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//            HttpResponse httpResponse = httpClient.execute(httpRequest);
//            // Logger.p(Level.DEBUG, Logd.DEBUG,TAG, "Response Code: " +
//            // httpResponse.getStatusLine().getStatusCode());
//            if (httpResponse.getStatusLine().getStatusCode() == 200) {
//                String resultStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
//                httpRequest.abort();
//
//                // Logger.p(Level.DEBUG, Logd.DEBUG,TAG, "Response: " + resultStr);
//                JSONObject jsonObj = new JSONObject(resultStr);
//                JSONArray jsonArray = null;
//                boolean isRequested = jsonObj.getBoolean("result");
//                if (isRequested) {
//                    String fileStr = jsonObj.getString("files");
//                    if (!fileStr.equals("{}")) {
//                        jsonArray = (JSONArray) jsonObj.get("files");
//                        for (int i = 0; i < jsonArray.length(); ++i) {
//                            JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            ScanningAlbumThread.TmpElemet mElemet = new ScanningAlbumThread.TmpElemet();
//                            mElemet.setFullName(jsonObject.getString("fullname"));
//                            mElemet.setLength(jsonObject.getLong("size"));
//                            boolean isDir = jsonObject.getString("type").equals("dir");
//                            if (isDir) {
//                                Map<String, String> tmpMap = new HashMap<String, String>();
//                                tmpMap.put("path", mElemet.getFullName());
//                                LoginSession loginSession = LoginManage.getInstance().getLoginSession();
//                                tmpMap.put("session", loginSession.toString());
//                                Logger.p(Level.DEBUG, Logd.DEBUG, TAG, "List Path: " + mElemet.getFullName());
//                                getServerPhotoList(url, tmpMap, photoList);
//                            } else {
//                                photoList.add(mElemet);
//                            }
//                        }
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }*/
//
//}
