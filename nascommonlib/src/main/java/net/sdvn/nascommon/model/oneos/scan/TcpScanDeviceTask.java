package net.sdvn.nascommon.model.oneos.scan;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class TcpScanDeviceTask extends AsyncTask<Void, String, String[]> {
    private static final String TAG = TcpScanDeviceTask.class.getSimpleName();

    private static final String TCP_SCAN_CMD = "CMD:check\r\nSESSION:xxx\r\nPOSITION:0\r\nLENGTH:0\r\nFULLNAME:storage/scan\r\nMD5:0\r\nTHUMBNAIL:0\r\n";

    /**
     * Device Map<Mac, IP>
     */
    @NonNull
    private Map<String, String> mDeviceMap = new HashMap<String, String>();
    private boolean isInterrupt = false;
    @Nullable
    private OnScanDeviceListener mListener;
    @Nullable
    private String topIp = null;
    private Context context;

    public TcpScanDeviceTask(OnScanDeviceListener mListener, Context context) {
        this.mListener = mListener;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener != null) {
            mListener.onScanStart();
        }
        mDeviceMap.clear();
        this.isInterrupt = false;

        topIp = getLocalTopIP(context);
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "---TCP Scan Start, Top Ip = " + topIp);
    }

    @Nullable
    @Override
    protected String[] doInBackground(Void... params) {
        if (topIp == null) {
            Logger.LOGE(TAG, "TCP Scan Over, Top Ip is NULL");
            return null;
        }

        tcpScanDevice();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);

        if (mListener != null) {
            mListener.onScanOver(mDeviceMap, isInterrupt, false);
        }
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "---TCP Scan Over, count: " + mDeviceMap.size());
    }

    private void tcpScanDevice() {
        final byte[] cmd = TCP_SCAN_CMD.getBytes();

        for (int i = 2; i < 254; i++) {
            if (isInterrupt) {
                break;
            }

            final String ip = topIp + i;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    doTcpScanDevice(ip, cmd);
                }
            }).start();
        }
    }

    private void doTcpScanDevice(String ip, @NonNull byte[] cmd) {
        Socket socket = new Socket();
        OutputStream socketOutStream = null;
        BufferedReader bReader = null;

        try {
            socket.connect(new InetSocketAddress(ip, OneOSAPIs.OneOS_UPLOAD_SOCKET_PORT), 1000);
            socketOutStream = socket.getOutputStream();
            socketOutStream.write(cmd);

            String result = null;
            bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!socket.isClosed()) {
                result = bReader.readLine();
                if (result != null && result.startsWith("CMD")) {
                    Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "TCP Scan Result: " + result + "---- IP: " + ip);
                    mDeviceMap.put("", ip);
                }
                break;
            }
        } catch (IOException e) {
            // Logd.e(TAG, "TCP Scan Exception: " + e);
        } finally {
            try {
                if (bReader != null) {
                    bReader.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logger.LOGE(TAG, "close stream exception");
            }
        }
    }

    public void stopScan() {
        this.mListener = null;
        this.isInterrupt = true;
    }

    @Nullable
    private String getLocalTopIP(@Nullable Context context) {
        if (context == null) {
            return null;
        }

        String ip = null;
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = null;
            if (wifiManager != null) {
                wifiInfo = wifiManager.getConnectionInfo();
                int ipInt = wifiInfo.getIpAddress();

                StringBuilder sb = new StringBuilder();
                sb.append(ipInt & 0xFF).append(".");
                sb.append((ipInt >> 8) & 0xFF).append(".");
                sb.append((ipInt >> 16) & 0xFF).append(".");
                // sb.append((ipInt >> 24) & 0xFF);

                ip = sb.toString();
            }
        } catch (Exception e) {
            Logger.LOGE(TAG, "Get Local IP Exception: " + e);
            ip = null;
        }

        return ip;
    }

}
