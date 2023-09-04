package org.eclipse.paho.mqttv5.client.logging;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HarveyDebug {

    public final static int DEBUG_MODE = 1; //0: 默认模式 1: 解析模式

    private static String date(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
    }
    public static void d(String info){
        System.out.println(date() + " " + info);
    }

    public static void d(){
        System.out.println("");
    }

    public static void e(String info){
        System.err.println(date() + " " + info);
    }

    public static void parseHeader(byte [] datas){

        if(datas == null || datas.length == 0){
            return;
        }

        d("--------------------Header---------------------------");

        int currentByteIndex = 0;

        int packetType = (datas[currentByteIndex] >> 4) & 0xf;
        int QoS = (datas[currentByteIndex]&0x6) >> 1;
        int dup = (datas[currentByteIndex]&0x8) >> 3;

        currentByteIndex++;
        int remainingLength = datas[currentByteIndex];

        if(packetType == 3){ //PUBLISH
            d("类型 ：PUBLISH，" + "QoS:" + QoS + ", DUP:"+dup);

            currentByteIndex++;

            int topicNameLength = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++];

            byte [] topicNameB = new byte[topicNameLength];
            for(int k = 0; k < topicNameLength; k++){
                topicNameB[k] = datas[4+k];
                currentByteIndex = 4+k;
            }
            currentByteIndex++;

            String topicName = new String(topicNameB, StandardCharsets.UTF_8);

            d("主题: " + topicName);

            if(QoS > 0){
                int packetIdentifier = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex];
                currentByteIndex++;
                d("包标识: " + packetIdentifier);
            }

            //属性长度
            int propertyLength = datas[currentByteIndex++];

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length){

                if(propertyType == 1){ // Payload Format Indicator
                    currentByteIndex++;
                    int payloadIdentifier = datas[currentByteIndex];
                    currentByteIndex++;
                    d("负荷标识: " + payloadIdentifier);

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 2) { //Message Expiry Interval`
                    currentByteIndex++;
                    int messageExpiryInterval = (datas[currentByteIndex++] << 24)&0xff000000 | (datas[currentByteIndex++] << 16)&0xff0000 | (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    d("消息过期时间: " +messageExpiryInterval);

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 35) { //Topic Alias
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    d("主题别名: " +  value);

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 8) { //Response Topic
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    byte [] responseTopicB = new byte[value];

                    for (int k = 0; k < value; k++){
                        responseTopicB[k] = datas[currentByteIndex++];
                    }

                    d("回应主题: " + new String(responseTopicB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 9) { //Correlation Data
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] correlationDataB = new byte[value];

                    for (int k = 0; k < value; k++){
                        correlationDataB[k] = datas[currentByteIndex++];
                    }
                    d("对比数据: " + new String(correlationDataB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 38) { //User Property
                    currentByteIndex++;
                    int key = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] keyB = new byte[key];

                    for (int k = 0; k < key; k++){
                        keyB[k] = datas[currentByteIndex++];
                    }

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] valueB = new byte[value];

                    for (int k = 0; k < value; k++){
                        valueB[k] = datas[currentByteIndex++];
                    }

                    d("属性: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];

                }

                if(propertyType == 11) { //Subscription Identifier
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] contentTypeB = new byte[value];

                    for (int k = 0; k < value; k++){
                        contentTypeB[k] = datas[currentByteIndex++];
                    }
                    d("订阅标识: " + new String(contentTypeB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 3) { //Content Type
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] contentTypeB = new byte[value];

                    for (int k = 0; k < value; k++){
                        contentTypeB[k] = datas[currentByteIndex++];
                    }
                    d("内容类型: " + new String(contentTypeB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

            }

        } else if(packetType == 1){//CONNECT
            d("类型 ：CONNECT，" + "QoS:" + QoS + ", DUP:"+dup);

            currentByteIndex++;
            int protocalNameLength = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++];
            byte [] protocalNameB = new byte[protocalNameLength];
            for(int k = 0; k < protocalNameLength; k++){
                protocalNameB[k] = datas[4+k];
                currentByteIndex = 4+k;
            }

            currentByteIndex++;

            int protocalVersion = datas[currentByteIndex];

            String topicName = new String(protocalNameB, StandardCharsets.UTF_8);

            d("协议: " + topicName + " V" +protocalVersion);

            currentByteIndex++;
            d("ConnectFlags");
            //Connect Flags
            System.out.printf("%12s %12s %11s %8s %9s %10s %8s",
                    "UserNameFlag", "PasswordFlag", "WillRetain",
                    "WillQoS",
                    "WillFlag","CleanStart","Reserved");
            System.out.println();
            System.out.format("%12d %12d %11d %8s %9d %10d %8d",
                    (datas[currentByteIndex]&0x80)>>6, (datas[currentByteIndex]&0x40)>>5, (datas[currentByteIndex]&0x20)>>4,
                    ((datas[currentByteIndex]&0x10)>>4) + "" + ((datas[currentByteIndex]&0x8)>>3),
                    (datas[currentByteIndex]&0x4)>>2, (datas[currentByteIndex]&0x02)>>1, datas[currentByteIndex]&0x1);

            System.out.println();

            currentByteIndex++;

            int keepLiveLength = (datas[currentByteIndex++]<<8)&0xff00 | datas[currentByteIndex++];
            d("心跳间隙: " + keepLiveLength + " 秒");

            int propertyLength = datas[currentByteIndex++];

            if(currentByteIndex >= datas.length){
                return;
            }

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length){

                if(propertyType == 17){ //Session Expiry Interval
                    currentByteIndex++;
                    int sessionExpiry = (datas[currentByteIndex++] << 24)&0xff000000 | (datas[currentByteIndex++] << 16)&0xff0000 | (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    d("session过期时间: " + sessionExpiry + "秒");

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 33){ //Receive Maximum
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("最大接收 : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 39){ //Maximum Packet Size
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 24)&0xff000000 | (datas[currentByteIndex++] << 16)&0xff0000 | (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    d("包最大尺寸: " + value);

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 34){ //Topic Alias Maximum
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("主题别名最大值 : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 25){ //Request Response Information
                    currentByteIndex++;

                    int value = datas[currentByteIndex++]&0xff;

                    d("Identifier of the Request Response Information : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 23){ //Request Problem Information
                    currentByteIndex++;

                    int value = datas[currentByteIndex++]&0xff;

                    d("Identifier of the Request Problem Information : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 38) { //User Property
                    currentByteIndex++;
                    int key = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] keyB = new byte[key];

                    for (int k = 0; k < key; k++){
                        keyB[k] = datas[currentByteIndex++];
                    }

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] valueB = new byte[value];

                    for (int k = 0; k < value; k++){
                        valueB[k] = datas[currentByteIndex++];
                    }

                    d("属性: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];

                }

                if(propertyType == 21){ //Authentication Method
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] contentTypeB = new byte[value];

                    for (int k = 0; k < value; k++){
                        contentTypeB[k] = datas[currentByteIndex++];
                    }
                    d("Authentication Method: " + new String(contentTypeB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 22){ //Authentication Data
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] contentTypeB = new byte[value];

                    for (int k = 0; k < value; k++){
                        contentTypeB[k] = datas[currentByteIndex++];
                    }
                    d("Authentication Data: " + new String(contentTypeB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

            }

        } else if(packetType == 2) {//CONNACK
            d("类型 ：CONNACK，" + "QoS:" + QoS + ", DUP:" + dup);

            //Connect Acknowledge Flags, Connect Reason Code, and Properties
            currentByteIndex++;
            int sessionPresent = datas[currentByteIndex++]&0x1;
            d("sessionPresent: " + sessionPresent);

            int connectReasonCode = datas[currentByteIndex++]&0xff;

            String reason = "unknow";
            if(connectReasonCode == 0){
                reason = "Success";
            } else if(connectReasonCode == 0x80){
                reason = "Unspecified error";
            } else if(connectReasonCode == 0x81){
                reason = "UMalformed Packet";
            } else if(connectReasonCode == 0x82){
                reason = "Protocol Error";
            } else if(connectReasonCode == 0x83){
                reason = "Implementation specific error";
            } else if(connectReasonCode == 0x84){
                reason = "Unsupported Protocol Version";
            } else if(connectReasonCode == 0x85){
                reason = "UClient Identifier not valid";
            } else if(connectReasonCode == 0x86){
                reason = "Bad User Name or Password";
            } else if(connectReasonCode == 0x87){
                reason = "Not authorized";
            } else if(connectReasonCode == 0x88){
                reason = "Server unavailable";
            } else if(connectReasonCode == 0x89){
                reason = "Server busy";
            } else if(connectReasonCode == 0x8a){
                reason = "Banned";
            } else if(connectReasonCode == 0x8c){
                reason = "Bad authentication method";
            } else if(connectReasonCode == 0x90){
                reason = "Topic Name invalid";
            } else if(connectReasonCode == 0x95){
                reason = "Packet too large";
            } else if(connectReasonCode == 0x97){
                reason = "Quota exceeded";
            } else if(connectReasonCode == 0x99){
                reason = "Payload format invalid";
            } else if(connectReasonCode == 0x9a){
                reason = "Retain not supported ";
            } else if(connectReasonCode == 0x9b){
                reason = "QoS not supported";
            } else if(connectReasonCode == 0x9c){
                reason = "Use another server";
            } else if(connectReasonCode == 0x9d){
                reason = "Server moved";
            } else if(connectReasonCode == 0x9f){
                reason = "Connection rate exceeded";
            }
            d("连接结果 : " + reason);

            int propertyLength = datas[currentByteIndex++];

            if(currentByteIndex >= datas.length){
                return;
            }

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length) {

                if (propertyType == 17) { //Session Expiry Interval
                    currentByteIndex++;
                    int sessionExpiry = (datas[currentByteIndex++] << 24) & 0xff000000 | (datas[currentByteIndex++] << 16) & 0xff0000 | (datas[currentByteIndex++] << 8) & 0xff00 | datas[currentByteIndex++] & 0xff;
                    d("session过期时间: " + sessionExpiry + "秒");

                    if (currentByteIndex >= datas.length) {
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 33){ //Receive Maximum
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("最大接收 : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 36){ // Maximum QoS
                    currentByteIndex++;

                    int value = datas[currentByteIndex++]&0xff;

                    d("Maximum QoS : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 37){ // Retain Available
                    currentByteIndex++;

                    int value = datas[currentByteIndex++]&0xff;

                    d("Retain Available : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 39){ //Maximum Packet Size
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 24)&0xff000000 | (datas[currentByteIndex++] << 16)&0xff0000 | (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    d("包最大尺寸: " + value);

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 18){ //Assigned Client Identifier
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] contentTypeB = new byte[value];

                    for (int k = 0; k < value; k++){
                        contentTypeB[k] = datas[currentByteIndex++];
                    }
                    d("Assigned Client Identifier: " + new String(contentTypeB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 34){ //Topic Alias Maximum
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("主题别名最大值 : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 31){ //Reason String
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("Reason String : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 38) { //User Property
                    currentByteIndex++;
                    int key = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] keyB = new byte[key];

                    for (int k = 0; k < key; k++){
                        keyB[k] = datas[currentByteIndex++];
                    }

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] valueB = new byte[value];

                    for (int k = 0; k < value; k++){
                        valueB[k] = datas[currentByteIndex++];
                    }

                    d("属性: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];

                }

                if(propertyType == 40){ // Wildcard Subscription Available
                    currentByteIndex++;

                    int value = datas[currentByteIndex++]&0xff;

                    d("Wildcard Subscription Available : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 41){ // Subscription Identifiers Available
                    currentByteIndex++;

                    int value = datas[currentByteIndex++]&0xff;

                    d("Subscription Identifiers Available : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 42){ // Shared Subscription Available
                    currentByteIndex++;

                    int value = datas[currentByteIndex++]&0xff;

                    d("Shared Subscription Available : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 19){ //Server Keep Alive
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("Server Keep Alive : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 26) { //Response Information
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    byte [] responseTopicB = new byte[value];

                    for (int k = 0; k < value; k++){
                        responseTopicB[k] = datas[currentByteIndex++];
                    }

                    d("Response Information: " + new String(responseTopicB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 28) { //Server Reference
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    byte [] responseTopicB = new byte[value];

                    for (int k = 0; k < value; k++){
                        responseTopicB[k] = datas[currentByteIndex++];
                    }

                    d("Server Reference: " + new String(responseTopicB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 21){ //Authentication Method
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] contentTypeB = new byte[value];

                    for (int k = 0; k < value; k++){
                        contentTypeB[k] = datas[currentByteIndex++];
                    }
                    d("Authentication Method: " + new String(contentTypeB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 22){ //Authentication Data
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] contentTypeB = new byte[value];

                    for (int k = 0; k < value; k++){
                        contentTypeB[k] = datas[currentByteIndex++];
                    }
                    d("Authentication Data: " + new String(contentTypeB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

            }

        } else if(packetType == 4 || packetType == 5 || packetType == 6 || packetType == 7) {//PUBACK / PUBREC / PUBREL / PUBCOMP
            if(packetType == 4){
                d("类型 ：PUBACK，" + "QoS:" + QoS + ", DUP:" + dup);
            } else if(packetType == 5){
                d("类型 ：PUBREC，" + "QoS:" + QoS + ", DUP:" + dup);
            } else if(packetType == 6){
                d("类型 ：PUBREL，" + "QoS:" + QoS + ", DUP:" + dup);
            } else {
                d("类型 ：PUBCOMP，" + "QoS:" + QoS + ", DUP:" + dup);
            }

            currentByteIndex++;
            int identifier = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;
            d("Packet Identifier: " + identifier);

            if(currentByteIndex >= datas.length){
                return;
            }

            int code = datas[currentByteIndex++]&0xff;
            String reason = "";
            if(code == 0x00){
                reason = "Success";
            } else if(code == 0x10){
                reason = "No matching subscribers";
            } else if(code == 0x80){
                reason = "Unspecified error";
            } else if(code == 0x83){
                reason = "Implementation specific error";
            } else if(code == 0x87){
                reason = "Not authorized";
            } else if(code == 0x90){
                reason = "Topic Name invalid";
            } else if(code == 0x91){
                reason = "Packet identifier in use";
            }  else if(code == 0x92){
                reason = "Packet Identifier not found";
            } else if(code == 0x97){
                reason = "Quota exceeded";
            } else if(code == 0x99){
                reason = "Payload format invalid";
            }
            d("发布主题结果 : " + reason);

            int propertyLength = datas[currentByteIndex++];

            if(currentByteIndex >= datas.length){
                return;
            }

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length) {

                if(propertyType == 31){ //Reason String
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("Reason String : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 38) { //User Property
                    currentByteIndex++;
                    int key = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] keyB = new byte[key];

                    for (int k = 0; k < key; k++){
                        keyB[k] = datas[currentByteIndex++];
                    }

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] valueB = new byte[value];

                    for (int k = 0; k < value; k++){
                        valueB[k] = datas[currentByteIndex++];
                    }

                    d("属性: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];

                }

            }

        } else if(packetType == 8){//SUBSCRIBE
            d("类型 ：SUBSCRIBE，" + "QoS:" + QoS + ", DUP:" + dup);
            currentByteIndex++;
            int identifier = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;
            d("Packet Identifier: " + identifier);

            int propertyLength = datas[currentByteIndex++];

            if(currentByteIndex >= datas.length){
                return;
            }

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length){

                if(propertyType == 11) { //Subscription Identifier
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] contentTypeB = new byte[value];

                    for (int k = 0; k < value; k++){
                        contentTypeB[k] = datas[currentByteIndex++];
                    }
                    d("订阅标识: " + new String(contentTypeB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }


                if(propertyType == 38) { //User Property
                    currentByteIndex++;
                    int key = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] keyB = new byte[key];

                    for (int k = 0; k < key; k++){
                        keyB[k] = datas[currentByteIndex++];
                    }

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] valueB = new byte[value];

                    for (int k = 0; k < value; k++){
                        valueB[k] = datas[currentByteIndex++];
                    }

                    d("属性: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];

                }

            }

        } else if(packetType == 9){//SUBACK
            d("类型 ：SUBACK，" + "QoS:" + QoS + ", DUP:" + dup);
            currentByteIndex++;
            int identifier = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;
            d("Packet Identifier: " + identifier);

            int propertyLength = datas[currentByteIndex++];

            if(currentByteIndex >= datas.length){
                return;
            }

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length){

                if(propertyType == 31){ //Reason String
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("Reason String : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 38) { //User Property
                    currentByteIndex++;
                    int key = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] keyB = new byte[key];

                    for (int k = 0; k < key; k++){
                        keyB[k] = datas[currentByteIndex++];
                    }

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] valueB = new byte[value];

                    for (int k = 0; k < value; k++){
                        valueB[k] = datas[currentByteIndex++];
                    }

                    d("属性: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];

                }

            }

        } else if(packetType == 10 || packetType == 11) {//UNSUBSCRIBE / UNSUBACK
            if(packetType == 10){
                d("类型 ：UNSUBSCRIBE，" + "QoS:" + QoS + ", DUP:" + dup);
            } else {
                d("类型 ：UNSUBACK，" + "QoS:" + QoS + ", DUP:" + dup);
            }

            currentByteIndex++;
            int identifier = (datas[currentByteIndex++] << 8) & 0xff00 | datas[currentByteIndex++] & 0xff;
            d("Packet Identifier: " + identifier);

            int propertyLength = datas[currentByteIndex++];

            if (currentByteIndex >= datas.length) {
                return;
            }

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length) {

                if(propertyType == 38) { //User Property
                    currentByteIndex++;
                    int key = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] keyB = new byte[key];

                    for (int k = 0; k < key; k++){
                        keyB[k] = datas[currentByteIndex++];
                    }

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] valueB = new byte[value];

                    for (int k = 0; k < value; k++){
                        valueB[k] = datas[currentByteIndex++];
                    }

                    d("属性: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];

                }
            }
        } else if(packetType == 12 || packetType == 13) {//PINGREQ / PINGRESP
            if(packetType == 12){
                d("类型 ：PINGREQ，" + "QoS:" + QoS + ", DUP:" + dup);
            } else {
                d("类型 ：PINGRESP，" + "QoS:" + QoS + ", DUP:" + dup);
            }
        } else if(packetType == 14) {//DISCONNECT
            d("类型 ：DISCONNECT，" + "QoS:" + QoS + ", DUP:" + dup);

            currentByteIndex++;
            int code = datas[currentByteIndex++]&0xff;
            String reason = "";
            if(code == 0x00){
                reason = "Normal disconnection";
            } else if(code == 0x04){
                reason = "Disconnect with Will Message";
            } else if(code == 0x80){
                reason = "Unspecified error";
            } else if(code == 0x81){
                reason = "Malformed Packet";
            } else if(code == 0x82){
                reason = "Protocol Error";
            } else if(code == 0x83){
                reason = "Implementation specific error";
            } else if(code == 0x87){
                reason = "Not authorized";
            } else if(code == 0x89){
                reason = "Server busy";
            } else if(code == 0x8B){
                reason = "Server shutting down";
            } else if(code == 0x8D){
                reason = "Keep Alive timeout";
            } else if(code == 0x8E){
                reason = "Session taken over";
            } else if(code == 0x8F){
                reason = "Topic Filter invalid";
            } else if(code == 0x90){
                reason = "Topic Name invalid";
            } else if(code == 0x93){
                reason = "Receive Maximum exceeded";
            } else if(code == 0x94){
                reason = "Topic Alias invalid";
            } else if(code == 0x95){
                reason = "Packet too large";
            } else if(code == 0x96){
                reason = "Message rate too high";
            } else if(code == 0x97){
                reason = "Quota exceeded";
            } else if(code == 0x98){
                reason = "Administrative action";
            } else if(code == 0x99){
                reason = "Payload format invalid";
            } else if(code == 0x9A){
                reason = "Retain not supported";
            } else if(code == 0x9B){
                reason = "QoS not supported";
            } else if(code == 0x9C){
                reason = "Use another server";
            } else if(code == 0x9D){
                reason = "Server moved";
            } else if(code == 0x9E){
                reason = "Shared Subscriptions not supported";
            } else if(code == 0x9F){
                reason = "Connection rate exceeded";
            } else if(code == 0xA0){
                reason = "Maximum connect time";
            } else if(code == 0xA1){
                reason = "Subscription Identifiers not supported";
            } else if(code == 0xA2){
                reason = "Wildcard Subscriptions not supported";
            }
            d("断开结果 : " + reason);
            int propertyLength = datas[currentByteIndex++];

            if(currentByteIndex >= datas.length){
                return;
            }

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length){

                if(propertyType == 17){ //Session Expiry Interval
                    currentByteIndex++;
                    int sessionExpiry = (datas[currentByteIndex++] << 24)&0xff000000 | (datas[currentByteIndex++] << 16)&0xff0000 | (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    d("session过期时间: " + sessionExpiry + "秒");

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 31){ //Reason String
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("Reason String : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 38) { //User Property
                    currentByteIndex++;
                    int key = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] keyB = new byte[key];

                    for (int k = 0; k < key; k++){
                        keyB[k] = datas[currentByteIndex++];
                    }

                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    byte [] valueB = new byte[value];

                    for (int k = 0; k < value; k++){
                        valueB[k] = datas[currentByteIndex++];
                    }

                    d("属性: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];

                }

                if(propertyType == 28) { //Server Reference
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    byte [] responseTopicB = new byte[value];

                    for (int k = 0; k < value; k++){
                        responseTopicB[k] = datas[currentByteIndex++];
                    }

                    d("Server Reference: " + new String(responseTopicB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

            }
        }

    }

}
