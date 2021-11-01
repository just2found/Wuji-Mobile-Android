package net.sdvn.nascommon.model.oneos.backup.file;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.db.objecbox.BackupFile;
import net.sdvn.nascommon.model.oneos.backup.BackupType;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/24.
 */
public class ScanningAlbumThread implements Runnable {
    private static final String TAG = ScanningAlbumThread.class.getSimpleName();

    private List<BackupFile> mBackupList;
    private OnScanFileListener mListener;
    private boolean isInterrupt = false;

    public ScanningAlbumThread(@NonNull List<BackupFile> mBackupList, OnScanFileListener mScanListener) {
        this.mBackupList = mBackupList;
        this.mListener = mScanListener;
        if (EmptyUtils.isEmpty(mBackupList)) {
            Logger.p(Logger.Level.ERROR, Logger.Logd.BACKUP_ALBUM, TAG, "BackupFile List is Empty");
            isInterrupt = true;
        }
        Logger.p(Logger.Level.DEBUG, Logger.Logd.BACKUP_ALBUM, TAG, "Backup List Size: " + mBackupList.size());
    }

    @NonNull
    private ArrayList<BackupElement> scanningBackupFiles(BackupFile info) {
        final long lastBackupTime = info.getTime();
        boolean isFirstBackup = lastBackupTime <= 0;
        boolean isBackupAlbum = (info.getType() == BackupType.ALBUM);  // 相册备份
        File backupDir = new File(info.getPath());
        ArrayList<File> fileList = new ArrayList<>();
        // 遍历备份目录文件
        listFiles(fileList, backupDir, isBackupAlbum, lastBackupTime);
        ArrayList<BackupElement> backupElements = new ArrayList<>();
        if (null != fileList) {
            for (File file : fileList) {
                BackupElement element = new BackupElement(info, file, true);
                backupElements.add(element);
                Logger.p(Logger.Level.DEBUG, Logger.Logd.BACKUP_ALBUM, TAG, "Add Backup Element: " + element.toString());
            }
        }

        Collections.sort(backupElements, new Comparator<BackupElement>() {
            @Override
            public int compare(@NonNull BackupElement elem1, @NonNull BackupElement elem2) {
                return Long.compare(elem1.getFile().lastModified(), elem2.getFile().lastModified());
            }
        });
        return backupElements;
    }

    @Override
    public void run() {
        if (!isInterrupt) {
            Logger.p(Logger.Level.DEBUG, Logger.Logd.BACKUP_ALBUM, TAG, "======Start Sort Backup Task=====");
            Collections.sort(mBackupList, new Comparator<BackupFile>() {
                @Override
                public int compare(@NonNull BackupFile info1, @NonNull BackupFile info2) {
                    // priority 1 is max
                    if (info1.getPriority() < info2.getPriority()) {
                        return 1;
                    } else if (info1.getPriority() > info2.getPriority()) {
                        return -1;
                    }
                    return 0;
                }
            });
            Logger.p(Logger.Level.DEBUG, Logger.Logd.BACKUP_ALBUM, TAG, "======Complete Sort Backup Task=====");

            Logger.p(Logger.Level.DEBUG, Logger.Logd.BACKUP_ALBUM, TAG, ">>>>>>Start Scanning Directory=====");
            ArrayList<BackupElement> backupElements = new ArrayList<>();
            for (BackupFile info : mBackupList) {
                Logger.p(Logger.Level.DEBUG, Logger.Logd.BACKUP_ALBUM, TAG, "------Scanning: " + info.getPath());
                ArrayList<BackupElement> files = scanningBackupFiles(info);

                backupElements.addAll(files);
                info.setCount(info.getCount() + 1);
            }
            Logger.p(Logger.Level.DEBUG, Logger.Logd.BACKUP_ALBUM, TAG, ">>>>>>Complete Scanning Directory: " + backupElements.size());

            if (mListener != null) {
                mListener.onComplete(backupElements);
            }
        }
    }

    private void listFiles(@NonNull ArrayList<File> list, File dir, boolean isBackupAlbum, long lastBackupTime) {
        Logger.p(Logger.Level.DEBUG, Logger.Logd.BACKUP_ALBUM, TAG, "######List Dir: " + dir.getAbsolutePath() + ", LastTime: " + lastBackupTime);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles(new BackupFileFilter(isBackupAlbum, lastBackupTime));
            if (null != files) {
                for (File file : files) {
                    listFiles(list, file, isBackupAlbum, lastBackupTime);
                }
            }
        } else {
            list.add(dir);
        }
    }

    //
//    /* private void getServerPhotoList(String url, Map<String, String> map, List<TmpElemet> mServerList) {
//
//         List<NameValuePair> params = new ArrayList<NameValuePair>();
//         Set<String> keys = map.keySet();
//         for (String key : keys) {
//             params.add(new BasicNameValuePair(key, map.get(key)));
//         }
//
//         try {
//             HttpPost httpRequest = new HttpPost(url);
//             Logger.p(Level.DEBUG, Logd.DEBUG,TAG, "Url: " + url);
//             DefaultHttpClient httpClient = new DefaultHttpClient();
//
//             httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 5000);
//             httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
//
//             httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//             HttpResponse httpResponse = httpClient.execute(httpRequest);
//             Logger.p(Level.DEBUG, Logd.DEBUG,TAG, "Response Code: " + httpResponse.getStatusLine().getStatusCode());
//             if (httpResponse.getStatusLine().getStatusCode() == 200) {
//                 String resultStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
//                 httpRequest.abort();
//
//                 JSONObject jsonObj = new JSONObject(resultStr);
//                 JSONArray jsonArray = null;
//                 boolean isRequested = jsonObj.getBoolean("result");
//                 if (isRequested) {
//                     String fileStr = jsonObj.getString("files");
//                     if (!fileStr.equals("{}")) {
//                         jsonArray = (JSONArray) jsonObj.get("files");
//                         for (int i = 0; i < jsonArray.length(); ++i) {
//                             JSONObject jsonObject = jsonArray.getJSONObject(i);
//                             TmpElemet mElemet = new TmpElemet();
//                             mElemet.setFullName(jsonObject.getString("fullname"));
//                             mElemet.setLength(jsonObject.getLong("size"));
//                             boolean isDir = jsonObject.getString("type").equals("dir");
//                             if (isDir) {
//                                 Map<String, String> tmpMap = new HashMap<String, String>();
//                                 tmpMap.put("path", mElemet.getFullName());
//                                 LoginSession loginSession = LoginManage.getInstance().getLoginSession();
//                                 tmpMap.put("session", loginSession.getSession());
//                                 Logger.p(Level.DEBUG, Logd.DEBUG,TAG, "List Path: " + mElemet.getFullName());
//                                 getServerPhotoList(url, tmpMap, mServerList);
//                             } else {
//                                 mServerList.add(mElemet);
//                             }
//                         }
//                     } else {
//                          Log.e(TAG, "====>> " + map.get("path") + " isEmpty");
//                     }
//                 } else {
//                      Log.e(TAG, "====>> " + map.get("path") + " Requestresult: " + isRequested);
//                 }
//             }
//
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
//
// */
    public void stopScanThread() {
        this.isInterrupt = true;
    }

    public static class TmpElemet {
        @Nullable
        private String fullName;
        private long length;

        public TmpElemet() {
            this.fullName = null;
            this.length = 0;
        }

        @Nullable
        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

    }

    public interface OnScanFileListener {
        void onComplete(ArrayList<BackupElement> backupList);
    }
}
