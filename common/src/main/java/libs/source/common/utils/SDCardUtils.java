package libs.source.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.EnvironmentCompat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import timber.log.Timber;


public class SDCardUtils {

    private static final String TAG = SDCardUtils.class.getSimpleName();

    /**
     * create local download store path
     */
    public static String createDefaultDownloadPath(String user) {
        String savePath;
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        if (SDCardUtils.checkSDCard()) {
            savePath = Environment.getExternalStorageDirectory() + path;
        } else {
            savePath = Environment.getDownloadCacheDirectory().getAbsolutePath() + path;
        }
        File dir = new File(savePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

       Timber.i( "Create default download path: " + savePath);
        return savePath;
    }

    // /** get directory available size */
    public static long getDeviceTotalSize(Context context,@Nullable String path) {
        if (path == null) {
            return -1;
        }

        List<File> mSDCardList = SDCardUtils.getSDCardList(context);
        if (null != mSDCardList && mSDCardList.size() > 0) {
            String sdPath = null;
            for (File root : mSDCardList) {
                String rootPath = root.getAbsolutePath();
                if (path.startsWith(rootPath)) {
                    sdPath = rootPath;
                    break;
                }
            }

            if (null != sdPath) {
                StatFs sf = new StatFs(sdPath);
                long blockCount = sf.getBlockCountLong();
                long blockSize = sf.getBlockSizeLong();
                return blockCount * blockSize;
            }
        }

        return -1;
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public static long getDeviceAvailableSize(String path) {
        if (new File(path).exists()) {
            StatFs stat = new StatFs(path);
            return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        }
        return 0;
    }


    // /** get directory available size */
//    public static long getDeviceAvailableSize(String path) {
//        if (path == null) {
//            return -1;
//        }
//
//        List<File> mSDCardList = SDCardUtils.getSDCardList();
//        if (null != mSDCardList && mSDCardList.size() > 0) {
//            String sdPath = null;
//            for (File root : mSDCardList) {
//                String rootPath = root.getAbsolutePath();
//                if (path.startsWith(rootPath)) {
//                    sdPath = rootPath;
//                    break;
//                }
//            }
//
//            if (null != sdPath) {
//                StatFs sf = new StatFs(sdPath);
//                long blockSize = sf.getBlockSize();
//                long freeBlocks = sf.getAvailableBlocks();
//                return (freeBlocks * blockSize);
//            }
//        }
//
//        return -1;
//    }


    // /** Get Sd card total size */
    public static long getSDTotalSize(Context context,String downloadPath) {
        if (EmptyUtils.isEmpty(downloadPath)) {
            File file = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(file.getPath());
            long blockCount = statFs.getBlockCountLong();
            long blockSize = statFs.getBlockSizeLong();
            return blockCount * blockSize;
        } else {
            return getDeviceTotalSize(context,downloadPath);
        }
    }

    /**
     * Get free space of SD card
     **/
    public static long getSDAvailableSize(String downloadPath) {
        if (EmptyUtils.isEmpty(downloadPath)) {
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(path.getPath());
            long blockSize = sf.getBlockSizeLong();
            long freeBlocks = sf.getAvailableBlocksLong();
            return (freeBlocks * blockSize);
        } else {
            return getDeviceAvailableSize(downloadPath);
        }
    }

//    public static File getExternalSDCard() {
//        ArrayList<File> sdcards = getSDCardList();
//
//        if (null != sdcards && sdcards.size() > 0) {
//
//            String interSDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//            for (File sd : sdcards) {
//                if (!sd.getAbsolutePath().equals(interSDPath)) {
//                    return sd;
//                }
//            }
//        }
//
//        return null;
//    }

    /**
     * 获取SD卡路径列表
     * <p>
     * 兼容Android6.0以上版本
     */
    @NonNull
    @SuppressLint("ObsoleteSdkInt")
    public static ArrayList<File> getSDCardList(Context context) {
       Timber.d( "==========================Android M +==============================");
        Set<String> sdcardPaths = new HashSet<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = context.getApplicationContext().getExternalFilesDirs(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                for (File file : externalDirs) {
                    if (null != file && file.exists()) {
                        if (Environment.isExternalStorageRemovable(file)) {
                            String path = file.getPath().split("/Android")[0];
                           Timber.d( ">>>>>1 Add path: " + path);
                            sdcardPaths.add(path);
                        }
                    }
                }
            } else {
                for (File file : externalDirs) {
                    if (null != file && file.exists()) {
                        if (Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file))) {
                            String path = file.getPath().split("/Android")[0];
                           Timber.d( ">>>>>2 Add path: " + path);
                            sdcardPaths.add(path);
                        }
                    }
                }
            }
        }

        if (sdcardPaths.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            StringBuilder output = new StringBuilder();
            InputStream is = null;
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold").redirectErrorStream(true).start();
                process.waitFor();
                is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output.append(new String(buffer));
                }
                is.close();
            } catch (@NonNull final Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!output.toString().trim().isEmpty()) {
                String[] devicePoints = output.toString().split("\n");
                for (String point : devicePoints) {
                   Timber.d( ">>>>>3 Add path: " + point.split(" ")[2]);
                    sdcardPaths.add(point.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        Iterator<String> iterator = sdcardPaths.iterator();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            while (iterator.hasNext()) {
                String sdcardPath = iterator.next();
                if (!sdcardPath.toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                   Timber.d( "<<<<<4" + sdcardPath + " might not be extSDcard, remove it!");
                    iterator.remove();
                }
            }
        } else {
            while (iterator.hasNext()) {
                String sdcardPath = iterator.next();
                if (!sdcardPath.toLowerCase().contains("ext") && !sdcardPath.toLowerCase().contains("sdcard")) {
                   Timber.d( "<<<<<5" + sdcardPath + " might not be extSDcard, remove it!");
                    iterator.remove();
                }
            }
        }

        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!sdcardPaths.contains(externalPath) && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
           Timber.d( ">>>>>6 Add path: " + externalPath);
            sdcardPaths.add(externalPath);
        }

        ArrayList<File> sdcardList = new ArrayList<>();
        for (String path : sdcardPaths) {
            sdcardList.add(new File(path));
        }
       Timber.d( "===================================================================");
        return sdcardList;
    }

    /**
     * 获取SD卡路径列表
     *
     * @deprecated 不兼容Android6.0以上版本
     */
    @NonNull
    public static ArrayList<File> _getSDCardList() {
        ArrayList<String> sdcardPaths = new ArrayList<String>();
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
        try {
            Process process = run.exec(cmd);// 启动另一个进程来执行命令
            BufferedInputStream in = new BufferedInputStream(process.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));

            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                // 获得命令执行后在控制台的输出信息
               Timber.d( "-->> " + lineStr);

                String[] temp = TextUtils.split(lineStr, " ");
                // 得到的输出的第二个空格后面是路径
                String result = temp[1];
                File file = new File(result);
                if (!result.endsWith("legacy") && file.isDirectory() && file.canRead() && file.canWrite() && !isSymbolicLink(file)) {
                    // Logged.d(TAG, "directory can read can write:" + file.getPath());
                    // 可读可写的文件夹未必是sdcard，我的手机的sdcard下的Android/obb文件夹也可以得到
                    sdcardPaths.add(result);
                    //Logger.LOGD(TAG, ">>>> Add Path: " + result);
                }

                // 检查命令是否执行失败。
                if (process.waitFor() != 0 && process.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                   Timber.d( "CommonUtil: GetSDCardPath Command fails!");
                }
            }
            inBr.close();
            in.close();
        } catch (Exception e) {
          Timber.e( e.toString());
        }

        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!sdcardPaths.contains(externalPath)) {
            sdcardPaths.add(externalPath);
        }

        optimize(sdcardPaths);

        ArrayList<File> sdcardList = new ArrayList<File>();
        for (Iterator<String> iterator = sdcardPaths.iterator(); iterator.hasNext(); ) {
            String path = iterator.next();
            sdcardList.add(new File(path));
        }

        return sdcardList;
    }

    /**
     * Check the state of SDcard, if exist return true, else return false
     */
    public static boolean checkSDCard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    private static void optimize(List<String> sdcardPaths) {
        if (sdcardPaths.size() == 0) {
            return;
        }
        int index = 0;
        while (true) {
            if (index >= sdcardPaths.size() - 1) {
                String lastItem = sdcardPaths.get(sdcardPaths.size() - 1);
                for (int i = sdcardPaths.size() - 2; i >= 0; i--) {
                    if (sdcardPaths.get(i).contains(lastItem)) {
                        sdcardPaths.remove(i);
                    }
                }
                return;
            }

            String containsItem = sdcardPaths.get(index);
            for (int i = index + 1; i < sdcardPaths.size(); i++) {
                if (sdcardPaths.get(i).contains(containsItem)) {
                    sdcardPaths.remove(i);
                    i--;
                }
            }

            index++;
        }
    }

    private static boolean isSymbolicLink(@Nullable File file) {
        if (null == file) {
            return true;
        }

        try {
            return !file.getAbsolutePath().equals(file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
