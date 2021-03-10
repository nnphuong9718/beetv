package com.example.android.architecture.blueprints.beetv.util;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;

import com.example.android.architecture.blueprints.beetv.BeeTVApplication;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class Utils {

    public static void print(Object msg) {
        print("BEETV Log", msg);
    }

    public static void print(String tag, Object msg) {
        Log.d(tag, msg != null ? msg.toString() : "NULL");
    }

    public static String getDuration(int timeInSeconds) {
        int seconds = (int) (timeInSeconds % 60);
        int minutes = (int) (timeInSeconds / 60);
        int hours = minutes / 60;
        String times = (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
        if (hours > 0) {
            minutes = minutes % 60;
            times = hours + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
        }
        return times;
    }

    public static String getDurationFromLong(long timeInMillisec){

        int timeInSeconds = (int) (timeInMillisec / 1000);
        int seconds = (int) (timeInSeconds % 60);
        int minutes = (int) (timeInSeconds / 60);
        int hours = minutes / 60;
        String times =(minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
        if (hours > 0) {
            minutes = minutes % 60;
            times
                    =hours + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
        }
        return times;
    }

    public static long getDurationFromString(String duration){
        String[] spDuration = duration.split(":");
        if(spDuration.length == 3){
            return (3600 + (Integer.parseInt(spDuration[1]) * 60) + Integer.parseInt(spDuration[2]))*1000;
            // minute
        }else if(spDuration.length == 2){
            return ((Integer.parseInt(spDuration[0]) * 60) + Integer.parseInt(spDuration[1]))*1000;
        }else{
            return  Integer.parseInt(spDuration[0])*1000;
        }
    }

    public static String getMacAddress2() {
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
                    // res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:",b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

//    @Deprecated
//    public static String getMacAddress() {
//        WifiManager wimanager = (WifiManager) BeeTVApplication.context.getSystemService(WIFI_SERVICE);
//        String macAddress = wimanager.getConnectionInfo().getMacAddress();
//        if (macAddress == null) {
//            macAddress = "Device don't have mac address or wi-fi is disabled";
//        }
//        return macAddress;
//    }

    public static  String getIPAddress(Context context){
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }


    public static String deviceID() {
        return Settings.Secure.getString(BeeTVApplication.context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

}
