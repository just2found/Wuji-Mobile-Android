package libs.source.common.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import timber.log.Timber;

public final class SystemPartition {
    private static final String TAG = "SystemMount";
    private static String TMP_PATH = "/sdcard/mount.txt";
    private static String mMountPiont = null;
    private static boolean mWriteable = false;

    private SystemPartition() {
        Timber.i("new SystemMount()");
    }

    public static String getSystemMountPiont() {
        DataInputStream dis = null;
        if (mMountPiont == null) {
            try {
                RootCmd.execRootCmd("mount > " + TMP_PATH);
//              Runtime.getRuntime().exec("mount > " + TMP_PATH);

                dis = new DataInputStream(new FileInputStream(TMP_PATH));

                String line = null;
                int index = -1;
                while ((line = dis.readLine()) != null) {
                    index = line.indexOf(" /system ");
                    if (index > 0) {
                        mMountPiont = line.substring(0, index);
                        if (line.indexOf(" rw") > 0) {
                            mWriteable = true;
                            Timber.i("/system is writeable !");
                        } else {
                            mWriteable = false;
                            Timber.i("/system is readonly !");
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (dis != null) {
                    try {
                        dis.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    dis = null;
                }

                File f = new File(TMP_PATH);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        if (mMountPiont != null) {
            Timber.i("/system mount piont: %s", mMountPiont);
        } else {
            Timber.i("get /system mount piont failed !!!");
        }

        return mMountPiont;
    }

    public static boolean isWriteable() {
        mMountPiont = null;
        getSystemMountPiont();
        return mWriteable;
    }

    public static void remountSystem(boolean writeable) {
        String cmd = null;
        getSystemMountPiont();
        if (mMountPiont != null && RootCmd.haveRoot()) {
            if (writeable) {
                cmd = "mount -o remount,rw " + mMountPiont + " /system";
            } else {
                cmd = "mount -o remount,ro " + mMountPiont + " /system";
            }
            RootCmd.execRootCmdSilent(cmd);

            isWriteable();
        }
    }

    public SystemPartition getInstance() {
        return SystemPartitionHolder.instance;
    }

    private static class SystemPartitionHolder {
        private static SystemPartition instance = new SystemPartition();
    }
} 