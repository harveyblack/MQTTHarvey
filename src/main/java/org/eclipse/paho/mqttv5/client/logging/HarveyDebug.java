package org.eclipse.paho.mqttv5.client.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HarveyDebug {

    public static void d(String info){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");//设置日期格式
        String date = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
        System.out.println(date + " " + info);
    }

    public static void d(){
        System.out.println("");
    }

}
