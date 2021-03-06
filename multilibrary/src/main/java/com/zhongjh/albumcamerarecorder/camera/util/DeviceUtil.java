package com.zhongjh.albumcamerarecorder.camera.util;

import android.os.Build;

/**
 * 手机相关信息
 */
public class DeviceUtil {

    private static String[] huaweiRongyao = {
            "hwH60",    //荣耀6
            "hwPE",     //荣耀6 plus
            "hwH30",    //3c
            "hwHol",    //3c畅玩版
            "hwG750",   //3x
            "hw7D",      //x1
            "hwChe2",      //x1
    };

    public static String getDeviceInfo() {
        return "手机型号：" + Build.DEVICE +
                "\n系统版本：" + Build.VERSION.RELEASE +
                "\nSDK版本：" + Build.VERSION.SDK_INT;
    }

    public static String getDeviceModel() {
        return Build.DEVICE;
    }

    public static boolean isHuaWeiRongyao() {
        int length = huaweiRongyao.length;
        for (String aHuaweiRongyao : huaweiRongyao) {
            if (aHuaweiRongyao.equals(getDeviceModel())) {
                return true;
            }
        }
        return false;
    }
}
