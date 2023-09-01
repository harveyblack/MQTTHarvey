package com.harvey.mqtt;

import org.eclipse.paho.mqttv5.client.logging.HarveyDebug;

import java.io.ByteArrayOutputStream;

/**
 *
 * MQTT5.0 : https://docs.oasis-open.org/mqtt/mqtt/v5.0/mqtt-v5.0.pdf
 * 参见 "1.5.5 Variable Byte Integer" 内容
 *
 */
public class ExampleVariableByteInteger {

    public static void main(String[] args) {

        ByteArrayOutputStream bais = new ByteArrayOutputStream();

        int [] inNum = {125, 128, 16385, 2097152};
        int x = 16385;

        StringBuilder sbInNum = new StringBuilder();
        sbInNum.append("编码前的数字序列为：");
        StringBuilder sb = new StringBuilder();

        for (int k : inNum){
            x = k;
            sbInNum.append(x).append(" ");

            do{
                int encodeByte = x % 128;
                x = x / 128;

                if ( x > 0){
                    encodeByte = (encodeByte | 128);
                }
                bais.write(encodeByte);

                sb.append(Integer.toBinaryString(encodeByte)).append(" ");
            } while ( x > 0);

        }

        HarveyDebug.d(sbInNum.toString());
        HarveyDebug.d("编码后二进制: " + sb.toString());

        HarveyDebug.d();

        StringBuilder sbDecodeNum = new StringBuilder();
        sbDecodeNum.append("解码后的数字序列为：");

        byte [] covert = bais.toByteArray();
        int index = 0;

        do{
            int multiplier = 1;
            int value = 0;
            int encodedByte = 0;
            do{
                encodedByte = covert[index];
                value += (encodedByte & 127) * multiplier;

                if(multiplier > 128*128*128){
                    System.err.println("非法编码");
                    break;
                }
                multiplier *= 128;
                index++;
            } while ((encodedByte & 128) != 0);

            sbDecodeNum.append(value).append(" ");

        } while (index < covert.length);

        HarveyDebug.d(sbDecodeNum.toString());
    }

}
