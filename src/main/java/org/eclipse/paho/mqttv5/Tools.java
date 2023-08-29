package org.eclipse.paho.mqttv5;

public class Tools {

    /**
     * 数字转换二进制字符串
     * @param num
     * @return
     */
    public static String toBinaryString(int num){
        String binaryResult = Integer.toBinaryString(num);
        for(int m = binaryResult.length(); m < 8; m++){
            binaryResult = "0" + binaryResult;
        }
        return binaryResult;
    }

}
