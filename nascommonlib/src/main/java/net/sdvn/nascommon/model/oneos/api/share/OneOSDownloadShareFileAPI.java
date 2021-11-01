package net.sdvn.nascommon.model.oneos.api.share;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.common.internet.OkHttpClientIns;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.model.oneos.transfer.OnTransferFileListener;
import net.sdvn.nascommon.model.oneos.transfer.ShareElement;
import net.sdvn.nascommon.model.oneos.transfer.TransferException;
import net.sdvn.nascommon.model.oneos.transfer.TransferState;
import net.sdvn.nascommon.utils.SDCardUtils;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OneOSDownloadShareFileAPI {
    private static final String TAG = "DownloadShareFile";
    private static final int HTTP_BUFFER_SIZE = 1024 * 16;

    @Nullable
    protected String url = null;

    private OnTransferFileListener<ShareElement> listener;
    private ShareElement shareElement;
    private boolean isInterrupt = false;
    private String downloadPath;

    public OneOSDownloadShareFileAPI(ShareElement element) {
        this.shareElement = element;
        downloadPath = element.getToPath();
    }

    public void setOnDownloadFileListener(OnTransferFileListener<ShareElement> listener) {
        this.listener = listener;
    }

    public boolean download() {
        url = OneOSAPIs.getShareDownloadUrl(shareElement.getSourceIp(), shareElement.getDownloadToken());

        if (null != listener) {
            listener.onStart(url, shareElement);
        }

//        if (LoginManage.getInstance().isHttp()) {
        doHttpDownload();
//        } else {
//            // TODO: 2018/5/25 UDP下载暂不处理
//            throw new RuntimeException();
//        }

        if (null != listener) {
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "download over");
            listener.onComplete(url, shareElement);
        }

        return shareElement.getState() == TransferState.COMPLETE;
    }

    public void stopDownload() {
        isInterrupt = true;
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "Upload Stopped");
    }

    private void doHttpDownload() {
        // set element download state to start
        isInterrupt = false;
        shareElement.setState(TransferState.START);
        try {
            Map<String, String> headers = new ConcurrentHashMap<>();
//            HttpGet httpGet = new HttpGet(url);
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "Download file: " + url);
            String tmpPath = downloadPath + File.separator + shareElement.getShareToken() +
                    AppConstants.TMP;
            File tmpFile = new File(tmpPath);
            if (!tmpFile.getParentFile().exists()) tmpFile.getParentFile().mkdirs();
            if (tmpFile.exists()) {
                if (tmpFile.length() < shareElement.getFileSize())
                    shareElement.setOffset(tmpFile.length());
                else if (tmpFile.length() == shareElement.getFileSize()) {
                    completeDownload(tmpPath, shareElement.getFileSize());
                } else {
                    tmpFile.delete();
                    shareElement.setOffset(0);
                }
            } else {
                shareElement.setOffset(0);
            }
            if (shareElement.getOffset() < 0) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "error position, position must greater than or equal zero");
                shareElement.setOffset(0);
            }

            if (shareElement.getOffset() > 0) {
//                httpGet.setHeader("Range", "bytes=" + String.valueOf(shareElement.getOffset()) + "-");
                headers.put("Range", "bytes=" + shareElement.getOffset() + "-");
            }
            OkHttpClient okHttpClient = OkHttpClientIns.getTransmitClient();
            Request.Builder builder = new Request.Builder();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }

            Request request = builder.url(url).build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            int code = response.code();
            if (code != 200 && code != 206) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "ERROR: status code=" + code);
                setStateError();
                if (code == 404) {
                    shareElement.setException(TransferException.SERVER_FILE_NOT_FOUND);
                } else {
                    shareElement.setException(TransferException.FAILED_REQUEST_SERVER);
                }
                return;
            }

            long fileLength = response.body() != null ? response.body().contentLength() : 0;
            if (fileLength < 0) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "ERROR: content length=" + fileLength);
                setStateError();
                call.cancel();
                shareElement.setException(TransferException.FAILED_REQUEST_SERVER);
                return;
            } else if (fileLength > SDCardUtils.getDeviceAvailableSize(downloadPath)) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "SD Available Size Insufficient");
                setStateError();
                call.cancel();
                shareElement.setException(TransferException.LOCAL_SPACE_INSUFFICIENT);
                return;
            }

            saveData(response.body().byteStream(), call);

        } catch (@NonNull UnsupportedEncodingException | IllegalArgumentException e) {
            setStateError();
            shareElement.setException(TransferException.ENCODING_EXCEPTION);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            setStateError();
            shareElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (IOException e) {
            setStateError();
            shareElement.setException(TransferException.IO_EXCEPTION);
            e.printStackTrace();
        } catch (Exception e) {
            setStateError();
            shareElement.setException(TransferException.UNKNOWN_EXCEPTION);
            e.printStackTrace();
        }
    }

    private void saveData(@NonNull InputStream input, @NonNull Call call) {
        RandomAccessFile outputFile = null;
        long downloadLen = shareElement.getOffset();
        try {
            File dir = new File(downloadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
//            shareElement.setDownloadState(ShareElement.START_CHANNEL);
            String tmpPath = downloadPath + File.separator + shareElement.getShareToken() + AppConstants.TMP;
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "tmpPath" + tmpPath);
            outputFile = new RandomAccessFile(tmpPath, "rw");
            outputFile.seek(shareElement.getLength());
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
                shareElement.setLength(downloadLen);
                callback++;
                if (null != listener && callback == 32) {
                    // callback every 512KB
                    listener.onTransmission(url, shareElement);
                    callback = 0;
                }
            }
            completeDownload(tmpPath, downloadLen);
            if (isInterrupt && downloadLen < shareElement.getSize())
                call.cancel();
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "Shut down http connection");
        } catch (FileNotFoundException e) {
            setStateError();
            shareElement.setException(TransferException.FILE_NOT_FOUND);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            setStateError();
            shareElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (SocketException e) {
            setStateError();
            shareElement.setException(TransferException.SOCKET_TIMEOUT);
            e.printStackTrace();
        } catch (IOException e) {
            setStateError();
            shareElement.setException(TransferException.IO_EXCEPTION);
            e.printStackTrace();
        } catch (Exception e) {
            setStateError();
            shareElement.setException(TransferException.UNKNOWN_EXCEPTION);
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
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "Input/Output Stream closed error");
                e.printStackTrace();
            }
        }
    }

    private void completeDownload(@NonNull String tmpPath, long downloadLen) {
        if (isInterrupt) {
            shareElement.setShareState(ShareElement.STATE_SHARE_DOWNLOADING);
            shareElement.setState(TransferState.PAUSE);
            Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "Download interrupt");
        } else {
            if (shareElement.getFileSize() > 0 && downloadLen != shareElement.getFileSize()) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, String.format("Download file length[%d] is not equals file real length[%d]", downloadLen, shareElement.getFileSize()));
                setStateError();
                shareElement.setException(TransferException.UNKNOWN_EXCEPTION);
            } else {
                String toName = shareElement.getFileName();
                File toFile = new File(downloadPath + File.separator + toName);
                int addition = 1;
                while (toFile.exists()) {
                    String name = toFile.getName();
                    int index = name.indexOf(".");
                    if (index >= 0) {
                        String prefix = name.substring(0, index);
                        String suffix = name.substring(index);
                        toName = prefix + "_" + addition + suffix;
                    } else {
                        toName = name + "_" + addition;
                    }
                    toFile = new File(downloadPath + File.separator + toName);
                    addition++;
                }
                shareElement.setToName(toName);
                File tmpFile = new File(tmpPath);
                tmpFile.renameTo(toFile);
                shareElement.setState(TransferState.COMPLETE);
                shareElement.setShareState(ShareElement.STATE_SHARE_COMPLETED);
            }
        }
    }


    private void setStateError() {
        shareElement.setShareState(ShareElement.STATE_SHARE_ERROR);
//        DLShareElemsKeeper.update(shareElement);
    }
}
