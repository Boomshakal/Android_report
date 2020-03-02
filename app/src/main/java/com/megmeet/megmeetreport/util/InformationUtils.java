package com.megmeet.megmeetreport.util;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class InformationUtils {
    private static InformationUtils informationUtils;
    private static Context mContext;

    private InformationUtils() {

    }

    public static InformationUtils getInformationUtils(Context context) {
        mContext = context;
        if (informationUtils == null) {
            informationUtils = new InformationUtils();
        }
        return informationUtils;
    }


    /**
     * 获取MAC地址
     *
     * @return
     */
    public String getMacAddress() {
        String mac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault();
            Log.e("len", "mesmes:" + mac);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacFromFile();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = getMacFromHardware();
        }
        return mac;
    }

    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     *
     * @return
     */
    private String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    /**
     * Android 6.0（包括） - Android 7.0（不包括）
     *
     * @return
     */
    private String getMacFromFile() {
        String WifiAddress = "02:00:00:00:00:00";
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (WifiAddress.equals("02:00:00:00:00:00")) {
            WifiAddress = getLowMacAddress();
        }
        return WifiAddress;
    }

//    /**
//     * Android  6.0 之前（不包括6.0）
//     * 必须的权限  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
//     * @param context
//     * @return
//     */
//    private String getMacDefault(Context context) {
//        String mac = "02:00:00:00:00:00";
//        if (context == null) {
//            return mac;
//        }
//
//        WifiManager wifi = (WifiManager) context.getApplicationContext()
//                .getSystemService(Context.WIFI_SERVICE);
//        if (wifi == null) {
//            return mac;
//        }
//        WifiInfo info = null;
//        try {
//            info = wifi.getConnectionInfo();
//        } catch (Exception e) {
//        }
//        if (info == null) {
//            return null;
//        }
//        mac = info.getMacAddress();
//        if (!TextUtils.isEmpty(mac)) {
//            mac = mac.toUpperCase(Locale.ENGLISH);
//        }
//        return mac;
//    }

    /**
     * 获取MAC地址
     *
     * @return
     */
    public String getMacDefault() {
        String strMacAddr = null;
        try {
            InetAddress ip = getLocalInetAddress();
            byte[] b = NetworkInterface.getByInetAddress(ip)
                    .getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toUpperCase();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strMacAddr;
    }

    /**
     * 获取移动设备本地IP
     *
     * @return
     */
    protected static InetAddress getLocalInetAddress() {

        InetAddress ip = null;
        try {
            //列举
            Enumeration en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {//是否还有元素
                NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();//得到下一个元素
                Enumeration en_ip = ni.getInetAddresses();//得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = (InetAddress) en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }
                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }


    /**
     * 获取设备的MAC地址
     */
    public String getLowMacAddress() {
        String strMacAddr = null;
        InetAddress localInetAddress = getLocalInetAddress();
        if (localInetAddress == null) {
            return "as";
        }
        try {
            byte[] hardwareAddress = NetworkInterface.getByInetAddress(localInetAddress).getHardwareAddress();
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < hardwareAddress.length; i++) {
                if (i != 0) {
                    stringBuffer.append(":");
                }
                String str = Integer.toHexString(hardwareAddress[i] & 0xFF);
                stringBuffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = stringBuffer.toString().toUpperCase();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return strMacAddr;
    }

    /**
     * 获取设备的IP地址
     */
//    protected InetAddress getLocalInetAddress() {
//        InetAddress ip = null;
//        try {
//            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//            while (networkInterfaces.hasMoreElements()) {      //判断是否还有元素
//                NetworkInterface networkInterface = networkInterfaces.nextElement();
//                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
//                Log.e("len", inetAddresses.nextElement().getHostAddress());
//                while (inetAddresses.hasMoreElements()) {
//                    Log.e("len", inetAddresses.nextElement().getHostAddress());
//                    ip = inetAddresses.nextElement();
//                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
//                        break;
//                    } else {
//                        ip = null;
//                    }
//                    if (ip != null) {
//                        break;
//                    }
//                }
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//        return ip;
//    }
}
