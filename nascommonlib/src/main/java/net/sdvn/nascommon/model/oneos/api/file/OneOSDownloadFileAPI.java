package net.sdvn.nascommon.model.oneos.api.file;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.oneos.BaseResultModel;
import net.sdvn.nascommon.model.oneos.api.BaseAPI;
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener;
import net.sdvn.nascommon.model.oneos.transfer.TransferElement;
import net.sdvn.nascommon.model.oneos.transfer.TransferException;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.utils.GsonUtils;
import net.sdvn.nascommon.utils.SDCardUtils;
import net.sdvn.nascommon.utils.log.Logger;

import org.view.libwidget.log.L;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import io.weline.repo.SessionCache;
import io.weline.repo.api.V5HttpErrorNoKt;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static net.sdvn.nascommon.model.oneos.transfer.TransferElement.REFRESH_TIME;

/**
 * OneSpace OS Download File API
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/02/14.
 */
public class OneOSDownloadFileAPI extends BaseAPI implements TransferElement.TransferStateObserver {
    private static final String TAG = OneOSDownloadFileAPI.class.getSimpleName();
    private static final int HTTP_BUFFER_SIZE = 1024 * 16;
    private static final double SSUDP_CHUNK_SIZE = 1024 * 1024;
    private long lastRefreshTime;
    private OnTransferFileListener<DownloadElement> listener;
    private DownloadElement downloadElement;
    private boolean isInterrupt = false;
    private LoginSession loginSession;

    public OneOSDownloadFileAPI(@NonNull LoginSession loginSession, DownloadElement element) {
        super(loginSession, OneOSAPIs.FILE_DOWNLOAD);
        this.loginSession = loginSession;
        this.downloadElement = element;
        this.downloadElement.addTransferStateObserver(this);
    }

    public void setOnDownloadFileListener(OnTransferFileListener<DownloadElement> listener) {
        this.listener = listener;
    }

    public boolean download() {
        if (null != listener) {
            listener.onStart(url(), downloadElement);
        }
        doHttpDownload();
        if (null != listener) {
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DOWNLOAD, TAG, "download over");
            listener.onComplete(url(), downloadElement);
        }

        return downloadElement.getState() == TransferState.COMPLETE;
    }

    public void stopDownload() {
        isInterrupt = true;
        downloadElement.setState(TransferState.PAUSE);
        L.i("TransferState.PAUSE", "stopDownload", "OneOSDownloadFileAPI", "nwq", "2021/3/22");
        Logger.p(Logger.Level.DEBUG, Logger.Logd.UPLOAD, TAG, "download Stopped");
    }

    private void doHttpDownload() {
//        url = OneOSAPIs.genDownloadUrl(loginSession, downloadElement.getFile());

        // set element download state to start
        downloadElement.setState(TransferState.START);
        isInterrupt = false;
        String session = loginSession.getSession();
        try {
//            HttpGet httpGet = new HttpGet(url);
//            Map<String, String> map = new ConcurrentHashMap<>();
            Map<String, String> headers = new ConcurrentHashMap<>();
//            map.put("session", session);
//            map.put("path", downloadElement.getFile().getPath());
//            Logger.p(Level.DEBUG, Logd.DOWNLOAD, TAG, "Download tmpFile: " + url());
            String tmpPath = downloadElement.getToPath() + File.separator + downloadElement.getTmpName();
            File tmpFile = new File(tmpPath);
            if (!tmpFile.getParentFile().exists()) tmpFile.getParentFile().mkdirs();
            if (tmpFile.exists()) {
                if (tmpFile.length() <= downloadElement.getSize())
                    downloadElement.setOffset(tmpFile.length());
                else {
                    tmpFile.delete();
                    downloadElement.setOffset(0);
                }
            } else {
                downloadElement.setOffset(0);
            }
            if (downloadElement.getOffset() < 0) {
                Logger.p(Logger.Level.WARN, Logger.Logd.DOWNLOAD, TAG, "error position, position must greater than or equal zero");
                downloadElement.setOffset(0);
            }
//            httpGet.setHeader("Cookie", "session=" + session);
            headers.put("Cookie", "session=" + session);

            if (downloadElement.getOffset() > 0 && downloadElement.getOffset() < downloadElement.getSize()) {
//                httpGet.setHeader("Range", "bytes=" + String.valueOf(downloadElement.getOffset()) + "-");
                headers.put("Range", "bytes=" + downloadElement.getOffset() + "-");
            }
//            HttpClient httpClient = new DefaultHttpClient();
//            httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
//            HttpResponse httpResponse = httpClient.execute(httpGet);

            OkHttpClient okHttpClient = OkHttpClientIns.getTransmitClient();

//            HttpEntity entity = httpResponse.getEntity();
//            int code = httpResponse.getStatusLine().getStatusCode();

            Request.Builder builder = new Request.Builder();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
            String url;
            if (SessionCache.Companion.getInstance().isV5(downloadElement.getSrcDevId())) {
                url = OneOSAPIs.genDownloadUrlV5(loginSession, downloadElement.getSrcPath());
                Long groupId = downloadElement.getGroupId();
                if (groupId != null){
                   url =  HttpUrl.parse(url).newBuilder()
                            .addQueryParameter("groupid", String.valueOf(groupId))
                            .build().toString();

                }
            } else {
                url = OneOSAPIs.genDownloadUrl(loginSession, downloadElement.getSrcPath());
            }
//            String url = url() + "?" + httpRequest.paramsToStr(map);
            Request request = builder.url(url).build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            int code = response.code();
            if (code != 200 && code != 206) {
                Logger.p(Logger.Level.ERROR, Logger.Logd.DOWNLOAD, TAG, "ERROR: status code=" + code);
                downloadElement.setState(TransferState.FAILED);
                if (code == 404) {
                    downloadElement.setException(TransferException.SERVER_FILE_NOT_FOUND);
                } else {
                    downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                }
                return;
            }
            String header = response.header("Content-Type");
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DOWNLOAD, TAG, "Header: Content-Type" + header);
            if (Objects.equals(header, "application/octet-stream")) {
                long fileLength = response.body() != null ? response.body().contentLength() : 0;
                if (fileLength <= 0) {
                    Logger.p(Logger.Level.ERROR, Logger.Logd.DOWNLOAD, TAG, "ERROR: content length=" + fileLength);
                    downloadElement.setState(TransferState.FAILED);
                    downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                    return;
                } else if (downloadElement.isCheck() && fileLength > SDCardUtils.getDeviceAvailableSize(downloadElement.getToPath())) {
                    Logger.p(Logger.Level.ERROR, Logger.Logd.DOWNLOAD, TAG, "SD Available Size Insufficient");
                    downloadElement.setState(TransferState.FAILED);
                    downloadElement.setException(TransferException.LOCAL_SPACE_INSUFFICIENT);
                    return;
                }
//                fileLength += downloadElement.getOffset();
//                downloadElement.setTotalFileLength(fileLength);

                saveData(response.body().byteStream(), call);
            } else {
                String responseMessage = response.body().string();
                Type type = new TypeToken<BaseResultModel>() {
                }.getType();
                BaseResultModel resultModel = GsonUtils.decodeJSON(responseMessage, type);
                if (resultModel != null) {
                    if (!resultModel.isSuccess()) {
                        BaseResultModel.ErrorBean error = resultModel.getError();
                        if (error != null) {
                            switch (error.getCode()) {
                                case HttpErrorNo.ERR_ONE_NO_FOUND:
                                case V5HttpErrorNoKt.V5_ERR_FILE_NOT_EXISTED:
                                    downloadElement.setException(TransferException.SERVER_FILE_NOT_FOUND);
                                    break;
                                case HttpErrorNo.ERR_ONE_NO_LOGIN:
                                case V5HttpErrorNoKt.V5_ERR_SESSION_EXP:
                                default:
                                    downloadElement.setException(TransferException.AUTH_EXP);
                                    SessionManager.getInstance().removeSession(downloadElement.getSrcDevId());
                            }
                            downloadElement.setState(TransferState.FAILED);
                        }
                    } else {
                        downloadElement.setException(TransferException.NONE);
                    }
                } else {
                    downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                }
            }

        } catch (SocketTimeoutException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (IOException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.IO_EXCEPTION);
            e.printStackTrace();
        } catch (Exception e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.UNKNOWN_EXCEPTION);
            e.printStackTrace();
        }
    }

/*    private void httpPostDownload() {
        Context context = MyApplication.getAppContext();
        LoginSession loginSession = LoginManage.getInstance().getLoginSession();
        String session = loginSession.getSession();
        String url = downloadElement.getUrl();
        String srcPath = downloadElement.getSrcPath();

        if (session == null) {
            downloadElement.setShareState(TransferState.FAILED);
            downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
            Logger.p(Level.ERROR, Logd.DOWNLOAD, TAG, "Session is null");
            return;
        }

        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("session", session));
        param.add(new BasicNameValuePair("srcPath", srcPath));

        try {
            HttpPost httpRequest = new HttpPost(url);
            if (downloadElement.getOffset() > 0) {
                String tmpPath = downloadElement.getToPath() + File.separator + downloadElement.getTmpName();
                File tmpFile = new File(tmpPath);
                if (!tmpFile.exists()) {
                    Logger.p(Level.ERROR, Logd.DOWNLOAD, TAG, "Temporary file is missing, resetBackupAlbum download offset position");
                    downloadElement.setOffset(0);
                } else {
                    long tmpLen = tmpFile.length();
                    if (tmpLen != downloadElement.getOffset()) {
                        Logger.p(Level.WARN, Logd.DOWNLOAD, TAG, "Temporary file length not equals offset position, reset download offset position and delete temporary file");
                        downloadElement.setOffset(0);
                        tmpFile.delete();
                    }
                }

                Logger.p(Level.DEBUG, Logd.DOWNLOAD, TAG, "Download offset position: " + downloadElement.getOffset());
                httpRequest.setHeader("Range", "bytes=" + String.valueOf(downloadElement.getOffset()) + "-");
            } else if (downloadElement.getOffset() < 0) {
                Logger.p(Level.ERROR, Logd.DOWNLOAD, TAG, "Error offset position: " + downloadElement.getOffset() + ", reset position to 0");
                downloadElement.setOffset(0);
            }

            HttpClient httpClient = new DefaultHttpClient();
            // httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT,
            // 5000);
            httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
            httpRequest.setEntity(new UrlEncodedFormEntity(param, HTTP.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            HttpEntity entity = httpResponse.getEntity();
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code != 200 && code != 206) {
                Logger.p(Level.ERROR, Logd.DOWNLOAD, TAG, "ERROR: status code=" + code);
                downloadElement.setShareState(TransferState.FAILED);
                downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                return;
            }
            long fileLength = entity.getContentLength();
            // Logd.d(LOG_TAG, "download file length = " + fileLength);
            if (fileLength < 0) {
                Logger.p(Level.ERROR, Logd.DOWNLOAD, TAG, "ERROR: content length=" + fileLength);
                downloadElement.setShareState(TransferState.FAILED);
                downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
                return;
            } else if (fileLength > SDCardUtils.getDeviceAvailableSize(downloadElement.getToPath())) {
                Logger.p(Level.ERROR, Logd.DOWNLOAD, TAG, "SDCard Available Size Insufficient");
                downloadElement.setShareState(TransferState.FAILED);
                downloadElement.setException(TransferException.LOCAL_SPACE_INSUFFICIENT);
                return;
            }
//            Header header = httpResponse.getFirstHeader("Content-Ranges");
//            if (header != null) {
//                String contentRanges = header.getValue();
//                int last = contentRanges.lastIndexOf('/');
//                String totalString = contentRanges.substring(last + 1, contentRanges.length());
//                fileLength = Long.valueOf(totalString);
//                Logger.p(Level.ERROR, Logd.DOWNLOAD, TAG, "header name=" + header.getName() + ", value=" + header.getValue());
//            }
//            downloadElement.getFile().setSize(fileLength);

            // set element download state to start
            downloadElement.setShareState(TransferState.START_CHANNEL);
//            待变更
//            saveData(entity.getContent(), httpClient);

        } catch (HttpHostConnectException e) {
            downloadElement.setShareState(TransferState.FAILED);
            downloadElement.setException(TransferException.FAILED_REQUEST_SERVER);
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            downloadElement.setShareState(TransferState.FAILED);
            downloadElement.setException(TransferException.ENCODING_EXCEPTION);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            downloadElement.setShareState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (IOException e) {
            downloadElement.setShareState(TransferState.FAILED);
            downloadElement.setException(TransferException.IO_EXCEPTION);
            e.printStackTrace();
        }
    }*/

    @SuppressLint("DefaultLocale")
    private void completeDownload(@NonNull String tmpPath, long downloadLen) {
        if (isInterrupt) {
            downloadElement.setState(TransferState.PAUSE);
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DOWNLOAD, TAG, "Download interrupt");
        } else {
            if (downloadElement.getSize() > 0 && downloadLen != downloadElement.getSize()) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DOWNLOAD, TAG,
                        String.format("Download file length[%d] is not equals file real length[%d]",
                                downloadLen, downloadElement.getSize()));
                downloadElement.setState(TransferState.FAILED);
                downloadElement.setException(TransferException.UNKNOWN_EXCEPTION);
            } else {
                File toFile = new File(downloadElement.getToPath() + File.separator + downloadElement.getSrcName());
                String toName = downloadElement.getSrcName();
                int addition = 1;
                while (toFile.exists()) {
                    String name = toFile.getName();
                    int index = name.lastIndexOf(".");
                    if (index >= 0) {
                        String prefix = name.substring(0, index);
                        String suffix = name.substring(index);
                        toName = prefix + "_" + addition + suffix;
                    } else {
                        toName = name + "_" + addition;
                    }
                    toFile = new File(downloadElement.getToPath() + File.separator + toName);
                    addition++;
                }
                downloadElement.setToName(toName);
                File tmpFile = new File(tmpPath);
                tmpFile.renameTo(toFile);
                downloadElement.setState(TransferState.COMPLETE);
            }
        }
    }

    private void saveData(@NonNull InputStream input, @NonNull Call call) {
        RandomAccessFile outputFile = null;
        long downloadLen = downloadElement.getOffset();
        try {
            File dir = new File(downloadElement.getToPath());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String tmpPath = downloadElement.getToPath() + File.separator + downloadElement.getTmpName();
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DOWNLOAD, TAG, "tmpPath : " + tmpPath);
            outputFile = new RandomAccessFile(tmpPath, "rw");
            outputFile.seek(downloadElement.getOffset());
            byte[] buffer = new byte[HTTP_BUFFER_SIZE];
            int nRead;
            int callback = 0; // for download progress callback
            while (!isInterrupt) {
                nRead = input.read(buffer, 0, buffer.length);
                if (nRead < 0) {
                    break;
                }
                outputFile.write(buffer, 0, nRead);
                downloadLen += nRead;
                downloadElement.setLength(downloadLen);
                callback++;
                long currentTime = System.currentTimeMillis();
                boolean isNotify = (currentTime - lastRefreshTime) >= REFRESH_TIME;
                if (null != listener) {//&& callback == 32
                    // callback every 512KB
                    listener.onTransmission(url(), downloadElement);
                    callback = 0;
                    lastRefreshTime = currentTime;
                }
            }
            if (!isInterrupt) {
                completeDownload(tmpPath, downloadLen);
            }
//            HttpClient httpClient = AndroidHttpClient.newInstance("");
//            httpClient.getConnectionManager().shutdown();
            if (isInterrupt && downloadLen < downloadElement.getSize()) {
                downloadElement.setState(TransferState.PAUSE);
                call.cancel();
            }
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DOWNLOAD, TAG, "Shut down http connection");
        } catch (FileNotFoundException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.FILE_NOT_FOUND);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (IOException e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.IO_EXCEPTION);
            e.printStackTrace();
        } catch (Exception e) {
            downloadElement.setState(TransferState.FAILED);
            downloadElement.setException(TransferException.UNKNOWN_EXCEPTION);
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (outputFile != null) {
                    outputFile.close();
                }
            } catch (IOException e) {
                Logger.p(Logger.Level.ERROR, Logger.Logd.DOWNLOAD, TAG, "Input/Output Stream closed error");
                e.printStackTrace();
            }
        }
        downloadElement.setOffset(downloadElement.getLength());
    }

    @Override
    public void onChanged(Object tag) {
        if (!isInterrupt && downloadElement.getState() == TransferState.PAUSE) {
            stopDownload();
        }
    }
}
