package net.linkmate.app.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NetWork Utils
 * <ul>
 * <strong>Attentions</strong>
 * <li>You should add <strong>android.permission.ACCESS_NETWORK_STATE</strong> in manifest, to get network status.</li>
 * </ul>
 *
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2014-11-03
 */
public class NetworkUtils {

    public static final String NETWORK_TYPE_WIFI = "wifi";
    public static final String NETWORK_TYPE_3G = "eg";
    public static final String NETWORK_TYPE_2G = "2g";
    public static final String NETWORK_TYPE_WAP = "wap";
    public static final String NETWORK_TYPE_UNKNOWN = "unknown";
    public static final String NETWORK_TYPE_DISCONNECT = "disconnect";

    /**
     * Get network type
     *
     * @param context
     * @return
     */
    public static int getNetworkType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        return networkInfo == null ? -1 : networkInfo.getType();
    }

    public static boolean isWifi(Context context) {
        switch (getNetworkType(context)) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_ETHERNET:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check network
     *
     * @param context
     * @return
     */
    public static boolean checkNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Get network type name
     *
     * @param context
     * @return
     */
    public static String getNetworkTypeName(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo;
        String type = NETWORK_TYPE_DISCONNECT;
        if (manager == null || (networkInfo = manager.getActiveNetworkInfo()) == null) {
            return type;
        }

        if (networkInfo.isConnected()) {
            String typeName = networkInfo.getTypeName();
            if ("WIFI".equalsIgnoreCase(typeName)) {
                type = NETWORK_TYPE_WIFI;
            } else if ("MOBILE".equalsIgnoreCase(typeName)) {
                String proxyHost = android.net.Proxy.getDefaultHost();
                type = TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(context) ? NETWORK_TYPE_3G : NETWORK_TYPE_2G)
                        : NETWORK_TYPE_WAP;
            } else {
                type = NETWORK_TYPE_UNKNOWN;
            }
        }
        return type;
    }

    /**
     * Whether is fast mobile network
     *
     * @param context
     * @return
     */
    private static boolean isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return false;
        }

        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_IDEN:
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                return false;
        }
    }


    public static void printReachableIP(InetAddress remoteAddr, int port) {
        String retIP = null;
        Enumeration netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();

                Enumeration<InetAddress> localAddrs = ni.getInetAddresses();
                while (localAddrs.hasMoreElements()) {
                    InetAddress localAddr = localAddrs.nextElement();

                    if (remoteAddr instanceof Inet4Address && localAddr instanceof Inet4Address)
                        if (isReachable(localAddr, remoteAddr, port, 5000)) {
                            retIP = localAddr.getHostAddress();
                            break;
                        }
                    if (remoteAddr instanceof Inet6Address && localAddr instanceof Inet6Address) {
                        String hostAddress = localAddr.getHostAddress();
                        try {
                            InetAddress local2 = Inet6Address.getByName(hostAddress.substring(0, hostAddress.indexOf("%")));

                            if (isReachable(local2, remoteAddr, port, 5000)) {
                                retIP = localAddr.getHostAddress();
                                break;
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("networkutils >> Error occurred while listing all the local network addresses.");
        }
        if (retIP == null) {
            System.out.println("networkutils >> NULL reachable local IP is found !");
        } else {
            System.out.println("networkutils >> Reachable local IP is found, it is  " + retIP);
        }
    }

    public static boolean isReachable(InetAddress localInetAddr, InetAddress remoteInetAddr, int port, int timeout) {
        boolean isReachable = false;
        Socket socket = null;
        DatagramSocket datagramSocket;
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.connect(remoteInetAddr, 0);
            sendDateByUdp(remoteInetAddr, 5, port);

            socket = new Socket(); // 端口号设置为 0 表示在本地挑选一个可用端口进行连接
            SocketAddress localSocketAddr = new InetSocketAddress(localInetAddr, 0);
            socket.bind(localSocketAddr);
            InetSocketAddress endpointSocketAddr = new InetSocketAddress(remoteInetAddr, port);
            socket.connect(endpointSocketAddr, timeout);
            System.out.println("networkutils >> SUCCESS - connection established !Local:" + localInetAddr.getHostAddress() + " remote: " +
                    remoteInetAddr.getHostAddress() + " port" + port + datagramSocket.isConnected());
            isReachable = socket.isConnected();
        } catch (IOException e) {
            System.out.println("networkutils >> FAILRE - CAN not connect! Local: " + localInetAddr.getHostAddress() + " remote: "
                    + remoteInetAddr.getHostAddress() + " port " + port);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("networkutils >> Error occurred while closing socket..");
                }
            }
        }
        return isReachable;
    }

    private static void sendDateByUdp(InetAddress loc, int MAXNUM, int port) throws IOException {
        String str_send = "Hello UDPserver";
        byte[] buf = new byte[1024];
        //客户端在port端口监听接收到的数据
        DatagramSocket ds = new DatagramSocket(port);

        //定义用来发送数据的DatagramPacket实例
        DatagramPacket dp_send = new DatagramPacket(str_send.getBytes(), str_send.length(), loc, port);
        //定义用来接收数据的DatagramPacket实例
        DatagramPacket dp_receive = new DatagramPacket(buf, 1024);
        //数据发向本地3000端口
        ds.setSoTimeout(50);              //设置接收数据时阻塞的最长时间
        int tries = 0;                         //重发数据的次数
        boolean receivedResponse = false;     //是否接收到数据的标志位
        //直到接收到数据，或者重发次数达到预定值，则退出循环
        while (!receivedResponse && tries < MAXNUM) {
            //发送数据
            ds.send(dp_send);
            try {
                //接收从服务端发送回来的数据
                ds.receive(dp_receive);
                Thread.sleep(50);
                //如果接收到的数据不是来自目标地址，则抛出异常
                if (!dp_receive.getAddress().equals(loc)) {
                    throw new IOException("Received packet from an umknown source");
                }
                //如果接收到数据。则将receivedResponse标志位改为true，从而退出循环
                receivedResponse = true;
            } catch (InterruptedIOException e) {
                //如果接收数据时阻塞超时，重发并减少一次重发的次数
                tries += 1;
                System.out.println("Time out," + (MAXNUM - tries) + " more tries...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (receivedResponse) {
            //如果收到数据，则打印出来
            System.out.println("client received data from server：");
            String str_receive = new String(dp_receive.getData(), 0, dp_receive.getLength()) +
                    " from " + dp_receive.getAddress().getHostAddress() + ":" + dp_receive.getPort();
            System.out.println(str_receive);
            //由于dp_receive在接收了数据之后，其内部消息长度值会变为实际接收的消息的字节数，
            //所以这里要将dp_receive的内部消息长度重新置为1024
            dp_receive.setLength(1024);
        } else {
            //如果重发MAXNUM次数据后，仍未获得服务器发送回来的数据，则打印如下信息
            System.out.println("No response -- give up.");
        }
        ds.close();
    }

    public static boolean canConnect() {
        Socket socket = new Socket();
        InetAddress remoteAddr = null;
        try {
            String host = IPv4long2ip(0x08080808L);
            System.out.println("networkutils >>" + host);
            remoteAddr = Inet4Address.getByName(host);
            printReachableIP(remoteAddr, 0xFFFF);
            remoteAddr = Inet6Address.getByAddress(new byte[]{0x20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
            printReachableIP(remoteAddr, 0xFFFF);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * @param cidr1 192.168.1.11/26
     * @param cidr2 192.168.1.111/26
     * @return 是否包含
     */
    public static boolean isInSubnet(String cidr1, String cidr2) {
        SubnetInfo subnetInfo1 = genSubnetInfo(cidr1);
        SubnetInfo subnetInfo2 = genSubnetInfo(cidr2);
        return !(subnetInfo1.netId > subnetInfo2.broadcastIp
                || subnetInfo1.broadcastIp < subnetInfo2.netId);
    }

    /**
     * @param ip   192.168.1.11
     * @param cidr 192.168.1.111/26
     * @return 是否在范围内
     */
    public static boolean isInRange(String ip, String cidr) {
        int ipAddr = ip2Int(ip);
        System.out.println(Integer.toBinaryString(ipAddr));
        int type = Integer.parseInt(cidr.replaceAll(".*/", ""));
        int mask = 0xFFFFFFFF << (32 - type);
//        System.out.println(Integer.toBinaryString(mask));
        String cidrIp = cidr.replaceAll("/.*", "");
        int cidrIpAddr = ip2Int(cidrIp);
//        System.out.println(Integer.toBinaryString(cidrIpAddr));
        return (ipAddr & mask) == (cidrIpAddr & mask);
    }

    private static int ip2Int(String ip) {
        String[] ips = ip.split("\\.");
        return (Integer.parseInt(ips[0]) << 24) | (Integer.parseInt(ips[1]) << 16)
                | (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
    }

    public static String IPv4long2ip(long ip) {
        return String.valueOf((int) ((ip >> 24) & 0xff)) +
                '.' +
                (int) ((ip >> 16) & 0xff) +
                '.' +
                (int) ((ip >> 8) & 0xff) +
                '.' +
                (int) (ip & 0xff);
    }

    public static SubnetInfo genSubnetInfo(String cidr) {
        int type = Integer.parseInt(cidr.replaceAll(".*/", ""));
        int mask = 0xFFFFFFFF << (32 - type);
        String cidrIp = cidr.replaceAll("/.*", "");
        int cidrIpAddr = ip2Int(cidrIp);
        int netId = cidrIpAddr & mask;
        SubnetInfo subnetInfo = new SubnetInfo();
        subnetInfo.netId = (netId);
        int availableNum = (1 << (32 - type)) - 2;
        int broadcastId = netId + availableNum + 1;
        subnetInfo.broadcastIp = (broadcastId);
        subnetInfo.startIp = (netId + 1);
        subnetInfo.endIp = (broadcastId - 1);
        subnetInfo.availableIps = availableNum;
//        System.out.println(subnetInfo.toString());
        return subnetInfo;
    }

    public static void getNetmask() {
        try {
            final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class SubnetInfo {
        public int availableIps;
        public int netId;
        public int startIp;
        public int endIp;
        public int broadcastIp;

        @Override
        public String toString() {
            return "SubnetInfo{" +
                    "availableIps=" + availableIps +
                    ", netId='" + netId + '\'' +
                    ", startIp='" + startIp + '\'' +
                    ", endIp='" + endIp + '\'' +
                    ", broadcastIp='" + broadcastIp + '\'' +
                    '}';
        }
    }

    public static String getNetmaskByN(int prflen) {
        int shft = 0xffffffff << (32 - prflen);
        int oct1 = ((byte) ((shft & 0xff000000) >> 24)) & 0xff;
        int oct2 = ((byte) ((shft & 0x00ff0000) >> 16)) & 0xff;
        int oct3 = ((byte) ((shft & 0x0000ff00) >> 8)) & 0xff;
        int oct4 = ((byte) (shft & 0x000000ff)) & 0xff;
        return oct1 + "." + oct2 + "." + oct3 + "." + oct4;
    }

    public static boolean isNetmask(String netmask) {
        String mask = "^(254|252|248|240|224|192|128|0)\\.0\\.0\\.0|255\\.(254|252|248|240|224|192|128|0)\\.0\\.0|255\\.255\\.(254|252|248|240|224|192|128|0)\\.0|255\\.255\\.255\\.(255|254|252|248|240|224|192|128|0)$";
        Pattern pattern = Pattern.compile(mask);
        Matcher matcher = pattern.matcher(netmask);
        return matcher.matches();
    }
}