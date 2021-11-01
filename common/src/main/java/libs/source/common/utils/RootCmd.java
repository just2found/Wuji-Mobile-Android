package libs.source.common.utils;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import timber.log.Timber;

public final class RootCmd {

    private static final String TAG = "RootCmd";
    private static boolean mHaveRoot = false;

    private static boolean checkRootBinaries() {
        String[] paths = {
                "/data/local/bin/su",
                "/data/local/su",
                "/data/local/xbin/su",
                "/sbin/su",
                "/su/bin/su",
                "/system/app/Superuser.apk",
                "/system/bin/failsafe/su",
                "/system/bin/su",
                "/system/sd/xbin/su",
                "/system/xbin/su"
                // ...Additional binaries/APKs... //
        };
        boolean binaryFound = false;
        for (String path : paths) {
            File bin = new File(path);
            if (bin.exists()) {
                binaryFound = true;
                break;
            }
        }
        return binaryFound;
    }

    // 判断机器Android是否已经root，即是否获取root权限 
    public static boolean haveRoot() {
        if (!mHaveRoot) {
            boolean ret = checkRootBinaries(); // 通过执行测试命令来检测
            if (ret) {
                Timber.i("have root!");
                mHaveRoot = true;
            } else {
                Timber.i("not root!");
            }
            if (!mHaveRoot) {
                boolean writeable = SystemPartition.isWriteable();
                if (writeable) {
                    Timber.i("have root!");
                    mHaveRoot = true;
                } else {
                    Timber.i("not root!");
                }
            }
        } else {
            Timber.i("mHaveRoot = true, have root!");
        }
        return mHaveRoot;
    }

    // 执行命令并且输出结果 
    public static String execRootCmd(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            Process p = Runtime.getRuntime().exec("/system/bin/sh");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            Timber.i(cmd);
            dos.writeBytes("su \n");
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                Timber.d(line);
                result += line;
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    // 执行命令但不关注结果输出 
    public static int execRootCmdSilent(String cmd) {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("/system/bin/sh");
            dos = new DataOutputStream(p.getOutputStream());

            Log.i(TAG, cmd);
            dos.writeBytes("su \n");
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
} 