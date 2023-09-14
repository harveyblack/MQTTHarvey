package org.eclipse.paho.mqttv5.client.logging;

import org.eclipse.paho.mqttv5.common.packet.util.VariableByteInteger;

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

    public static void parsePayload(byte [] header, byte [] payload){
        if(payload == null || payload.length == 0){
            return;
        }

        int currentByteIndex = 0;

        int packetType = (header[currentByteIndex] >> 4) & 0xf;

        currentByteIndex++;

        {
            VariableByteInteger remainVariableByte = decodevariableByte(header, currentByteIndex);
            int remainingLength = remainVariableByte.getValue();
            currentByteIndex = currentByteIndex + remainVariableByte.getEncodedLength() - 1;
        }

        if(packetType == 1) { //CONNECT
            //解析头部
            currentByteIndex++;
            int protocalNameLength = (header[currentByteIndex++] << 8)&0xff00 | header[currentByteIndex++];
            byte [] protocalNameB = new byte[protocalNameLength];
            for(int k = 0; k < protocalNameLength; k++){
                protocalNameB[k] = header[4+k];
                currentByteIndex = 4+k;
            }

            currentByteIndex++;

            int protocalVersion = header[currentByteIndex];

            String topicName = new String(protocalNameB, StandardCharsets.UTF_8);

            currentByteIndex++;

            int willRetail =  (header[currentByteIndex]&0x20)>>5;
            int willQoS =    ((header[currentByteIndex]&0x10)>>4) + ((header[currentByteIndex]&0x8)>>3);
            int willFlag = (header[currentByteIndex]&0x4)>>2;

            //解析payload

            //3.1.3.1 Client Identifier (ClientID)
            int payloadIndex = 0;
            int clientIdentifierLength = (payload[payloadIndex] << 8)&0xff00 | payload[payloadIndex+1];
            payloadIndex = payloadIndex + 2;

            byte [] clientIdentifierB = new byte[clientIdentifierLength];
            for(int k = 0; k < clientIdentifierLength; k++){
                clientIdentifierB[k] = payload[payloadIndex + k];
            }

            payloadIndex = payloadIndex + clientIdentifierLength;

            String clientIdentifier = new String(clientIdentifierB, StandardCharsets.UTF_8);
            d("Client Identifier: " + clientIdentifier);

            //3.1.3.2 Will Properties
            if(willFlag==1){
                VariableByteInteger remainVariableByte = decodevariableByte(payload, payloadIndex);

                int propertyLength = remainVariableByte.getValue();

                payloadIndex = payloadIndex + remainVariableByte.getEncodedLength();

                int propertyType = payload[payloadIndex];

                int propertyIndex = propertyLength;

                while (true){

                    if(propertyIndex <= 0){
                        break;
                    }

                    if(propertyType == 24){ //3.1.3.2.2 Will Delay Interval
                        propertyIndex--;
                        propertyIndex = propertyIndex - 4;

                        payloadIndex++;
                        int sessionExpiry = (payload[payloadIndex] << 24)&0xff000000 | (payload[payloadIndex+1] << 16)&0xff0000 | (payload[payloadIndex+2] << 8)&0xff00 |  payload[payloadIndex+3]&0xff;
                        d("Will Delay Interval: " + sessionExpiry + "秒");

                        payloadIndex = payloadIndex + 4;

                        propertyType = payload[payloadIndex];

                    }

                    if(propertyType == 1){//3.1.3.2.3 Payload Format Indicator
                        propertyIndex--;
                        propertyIndex--;

                        payloadIndex++;
                        int value = payload[payloadIndex++]&0xff;

                        d("Payload Format Indicator: " + value);

                        propertyType = payload[payloadIndex];
                    }

                    if(propertyType == 2){//Message Expiry Interva
                        propertyIndex--;
                        propertyIndex = propertyIndex - 4;

                        payloadIndex++;
                        int sessionExpiry = (payload[payloadIndex++] << 24)&0xff000000 | (payload[payloadIndex++] << 16)&0xff0000 | (payload[payloadIndex++] << 8)&0xff00 |  payload[payloadIndex++]&0xff;
                        d("Message Expiry Interva: " + sessionExpiry + "秒");

                        if(payloadIndex >= payload.length){
                            break;
                        }
                        propertyType = payload[payloadIndex];
                    }

                    if(propertyType == 3){//3.1.3.2.5 Content Type

                        propertyIndex--;
                        propertyIndex = propertyIndex - 2;

                        payloadIndex++;
                        int contentTypeLength = (payload[payloadIndex++] << 8)&0xff00 | payload[payloadIndex++];
                        byte [] contentTypeB = new byte[contentTypeLength];
                        for(int k = 0; k < contentTypeLength; k++){
                            contentTypeB[k] = payload[payloadIndex+k];
                            propertyIndex--;
                        }

                        payloadIndex = payloadIndex+contentTypeLength;

                        String contentType = new String(contentTypeB, StandardCharsets.UTF_8);
                        d("Content Type: " + contentType);

                        if(payloadIndex >= payload.length){
                            break;
                        }
                        propertyType = payload[payloadIndex];

                    }

                    if(propertyType == 8){//3.1.3.2.6 Response Topic
                        propertyIndex--;
                        propertyIndex = propertyIndex - 2;

                        payloadIndex++;
                        int contentTypeLength = (payload[payloadIndex++] << 8)&0xff00 | payload[payloadIndex++];
                        byte [] contentTypeB = new byte[contentTypeLength];
                        for(int k = 0; k < contentTypeLength; k++){
                            contentTypeB[k] = payload[payloadIndex+k];
                            propertyIndex--;
                        }

                        payloadIndex = payloadIndex + contentTypeLength;

                        String contentType = new String(contentTypeB, StandardCharsets.UTF_8);
                        d("Response Topic: " + contentType);

                        propertyType = payload[payloadIndex];
                    }

                    if(propertyType == 9){//3.1.3.2.7 Correlation Data
                        propertyIndex--;
                        propertyIndex = propertyIndex - 2;

                        payloadIndex++;
                        int value = (payload[payloadIndex++] << 8)&0xff00 |  payload[payloadIndex++]&0xff;
                        byte [] correlationDataB = new byte[value];

                        for (int k = 0; k < value; k++){
                            correlationDataB[k] = payload[payloadIndex++];
                            propertyIndex--;
                        }

                        d("Correlation Data: " + new String(correlationDataB, StandardCharsets.UTF_8));

                        if(payloadIndex >= payload.length){
                            break;
                        }
                        propertyType = payload[payloadIndex];

                    }

                    if(propertyType == 38){//3.1.3.2.8 User Property
                        propertyIndex--;
                        propertyIndex = propertyIndex - 2;

                        payloadIndex++;
                        int key = (payload[payloadIndex++] << 8)&0xff00 |  payload[payloadIndex++]&0xff;
                        byte [] keyB = new byte[key];

                        for (int k = 0; k < key; k++){
                            keyB[k] = payload[payloadIndex++];
                            propertyIndex--;
                        }

                        propertyIndex = propertyIndex - 2;

                        int value = (payload[payloadIndex++] << 8)&0xff00 |  payload[payloadIndex++]&0xff;
                        byte [] valueB = new byte[value];

                        for (int k = 0; k < value; k++){
                            valueB[k] = payload[payloadIndex++];
                            propertyIndex--;
                        }

                        d("User Property: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

                        if(payloadIndex >= payload.length){
                            break;
                        }

                        propertyType = payload[payloadIndex];
                    }

                }

                //3.1.3.3 Will Topic
                {
                    int willTopicLength = (payload[payloadIndex++] << 8)&0xff00 | payload[payloadIndex++];
                    byte [] willTopicB = new byte[willTopicLength];
                    for(int k = 0; k < willTopicLength; k++){
                        willTopicB[k] = payload[payloadIndex+k];
                    }
                    payloadIndex = payloadIndex + willTopicLength;

                    String contentType = new String(willTopicB, StandardCharsets.UTF_8);
                    d("Will Topic: " + contentType);

                }

                //3.1.3.4 Will Payload
                {
                    int value = (payload[payloadIndex++] << 8)&0xff00 |  payload[payloadIndex++]&0xff;
                    byte [] willPayloadB = new byte[value];

                    for (int k = 0; k < value; k++){
                        willPayloadB[k] = payload[payloadIndex++];
                    }

                    d("Will Payload: " + new String(willPayloadB, StandardCharsets.UTF_8));
                }

            }

            //3.1.3.5 User Name
            {
                int willTopicLength = (payload[payloadIndex++] << 8)&0xff00 | payload[payloadIndex++];
                byte [] willTopicB = new byte[willTopicLength];
                for(int k = 0; k < willTopicLength; k++){
                    willTopicB[k] = payload[payloadIndex+k];
                }
                payloadIndex = payloadIndex + willTopicLength;

                String contentType = new String(willTopicB, StandardCharsets.UTF_8);
                d("User Name: " + contentType);
            }

            //3.1.3.6 Password
            {
                int value = (payload[payloadIndex++] << 8)&0xff00 |  payload[payloadIndex++]&0xff;
                byte [] willPayloadB = new byte[value];

                for (int k = 0; k < value; k++){
                    willPayloadB[k] = payload[payloadIndex++];
                }

                d("Password: " + new String(willPayloadB, StandardCharsets.UTF_8));
            }

        } else if(packetType == 3){ //PUBLISH
            d("发布内容：" + new String(payload, StandardCharsets.UTF_8));
        } else if(packetType == 8){ //SUBSCRIBE
            int payloadIndex = 0;

            while (payloadIndex < payload.length) {

                {
                    int willTopicLength = (payload[payloadIndex++] << 8) & 0xff00 | payload[payloadIndex++];
                    byte[] willTopicB = new byte[willTopicLength];
                    for (int k = 0; k < willTopicLength; k++) {
                        willTopicB[k] = payload[payloadIndex + k];
                    }
                    payloadIndex = payloadIndex + willTopicLength;

                    String contentType = new String(willTopicB, StandardCharsets.UTF_8);
                    d("Topic Filters: " + contentType);
                }

                //Reserved Retain Handling RAP NL QoS
                String Reserved = ((payload[payloadIndex] & 0xc0) >> 6) + "";
                String RetainHandling = ((payload[payloadIndex] & 0x30) >> 4) + "";
                String RAP = ((payload[payloadIndex] & 0x8) >> 3) + "";
                String NL = ((payload[payloadIndex] & 0x4) >> 2) + "";
                String QoS = (payload[payloadIndex] & 0x3) + "";

                d("Reserved:" + Reserved + " , " + "RetainHandling:" + RetainHandling + " , " + "RAP:" + RAP + " , " + "NL:" + NL + " , " + "QoS:" + QoS);

                payloadIndex++;

            }

        } else if(packetType == 9 || packetType == 11){
            StringBuilder sb = new StringBuilder();
            for(int code : payload){
                if(code == 0){
                  if(sb.toString().length() > 0){
                      sb.append(" , ");
                  }
                  sb.append("code:").append(code).append(":");

                  sb.append("Granted QoS 0");
                } else if(code == 0x1){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Granted QoS 1");
                } else if(code == 0x2){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Granted QoS 2");
                } else if(code == 0x80){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Unspecified error");
                } else if(code == 0x83){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Implementation specific error");
                } else if(code == 0x87){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Not authorized");
                } else if(code == 0x8F){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Topic Filter invalid");
                } else if(code == 0x91){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Packet Identifier in use");
                } else if(code == 0x97){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Quota exceeded");
                } else if(code == 0x9E){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Shared Subscriptions not supported");
                } else if(code == 0xA1){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Subscription Identifiers not supported");
                } else if(code == 0xA2){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Wildcard Subscriptions not supported");
                } else if(code == 0x11){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("No subscription existed");
                } else if(code == 0x87){
                    if(sb.toString().length() > 0){
                        sb.append(" , ");
                    }
                    sb.append("code:").append(code).append(":");
                    sb.append("Not authorized");
                }
            }

            d(sb.toString());
        } else if(packetType == 10){
            int payloadIndex = 0;

            while (payloadIndex < payload.length) {

                {
                    int willTopicLength = (payload[payloadIndex++] << 8) & 0xff00 | payload[payloadIndex++];
                    byte[] willTopicB = new byte[willTopicLength];
                    for (int k = 0; k < willTopicLength; k++) {
                        willTopicB[k] = payload[payloadIndex + k];
                    }
                    payloadIndex = payloadIndex + willTopicLength;

                    String contentType = new String(willTopicB, StandardCharsets.UTF_8);
                    d("Topic Filters: " + contentType);
                }

            }

        }

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
//        int remainingLength = datas[currentByteIndex];

        {
            VariableByteInteger remainVariableByte = decodevariableByte(datas,currentByteIndex);
            int remainingLength = remainVariableByte.getValue();
            currentByteIndex = currentByteIndex + remainVariableByte.getEncodedLength() - 1;
        }


        if(packetType == 3){ //PUBLISH
            d("类型 ：PUBLISH，" + "QoS:" + QoS + ", DUP:"+dup);

            currentByteIndex++;

            int topicNameLength = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++];

            byte [] topicNameB = new byte[topicNameLength];

            for(int k = 0; k < topicNameLength; k++){
                topicNameB[k] = datas[currentByteIndex+k];
            }
            currentByteIndex = currentByteIndex + topicNameLength;

            String topicName = new String(topicNameB, StandardCharsets.UTF_8);

            d("Topic Name: " + topicName);

            if(QoS > 0){
                int packetIdentifier = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex];
                currentByteIndex++;
                d("Packet Identifier: " + packetIdentifier);
            }

            //属性长度
            VariableByteInteger propertyVariableByte = decodevariableByte(datas,currentByteIndex);
            int propertyLength = propertyVariableByte.getValue();
            if(propertyLength == 0){
                return;
            }
            currentByteIndex = currentByteIndex + propertyVariableByte.getEncodedLength();

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length){

                if(propertyType == 0){
                    break;
                }
                if(propertyType == 1){ // Payload Format Indicator
                    currentByteIndex++;
                    int payloadIdentifier = datas[currentByteIndex];
                    currentByteIndex++;
                    d("Payload Format Indicator: " + payloadIdentifier);

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 2) { //Message Expiry Interval`
                    currentByteIndex++;
                    int messageExpiryInterval = (datas[currentByteIndex++] << 24)&0xff000000 | (datas[currentByteIndex++] << 16)&0xff0000 | (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    d("Message Expiry Interval: " +messageExpiryInterval);

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 35) { //Topic Alias
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    d("Topic Alias: " +  value);

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

                    d("Response Topic: " + new String(responseTopicB, StandardCharsets.UTF_8));

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
                    d("Correlation Data: " + new String(correlationDataB, StandardCharsets.UTF_8));

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

                    d("User Property: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];

                }

                if(propertyType == 11) { //Subscription Identifier
                    currentByteIndex++;

                    VariableByteInteger payloadVariableByte = decodevariableByte(datas,currentByteIndex);

                    int value = payloadVariableByte.getValue();
                    currentByteIndex = currentByteIndex + payloadVariableByte.getEncodedLength();

                    d("Subscription Identifier: " + value);

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
                    d("Content Type: " + new String(contentTypeB, StandardCharsets.UTF_8));

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
                protocalNameB[k] = datas[currentByteIndex+k];
            }

            currentByteIndex = currentByteIndex + protocalNameLength;

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
                    (datas[currentByteIndex]&0x80)>>7, (datas[currentByteIndex]&0x40)>>6, (datas[currentByteIndex]&0x20)>>5,
                    ((datas[currentByteIndex]&0x10)>>4) + "" + ((datas[currentByteIndex]&0x8)>>3),
                    (datas[currentByteIndex]&0x4)>>2, (datas[currentByteIndex]&0x02)>>1, datas[currentByteIndex]&0x1);

            System.out.println();

            currentByteIndex++;

            int keepLiveLength = (datas[currentByteIndex++]<<8)&0xff00 | datas[currentByteIndex++];
            d("Keep Live: " + keepLiveLength + " 秒");

            //属性长度
            VariableByteInteger propertyVariableByte = decodevariableByte(datas,currentByteIndex);
            int propertyLength = propertyVariableByte.getValue();
            if(propertyLength == 0){
                return;
            }
            currentByteIndex = currentByteIndex + propertyVariableByte.getEncodedLength();

            if(currentByteIndex >= datas.length){
                return;
            }

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length){

                if(propertyType == 17){ //Session Expiry Interval
                    currentByteIndex++;
                    int sessionExpiry = (datas[currentByteIndex++] << 24)&0xff000000 | (datas[currentByteIndex++] << 16)&0xff0000 | (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    d("Session Expiry Interval: " + sessionExpiry + "秒");

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 33){ //Receive Maximum
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("Receive Maximum : " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 39){ //Maximum Packet Size
                    currentByteIndex++;
                    int value = (datas[currentByteIndex++] << 24)&0xff000000 | (datas[currentByteIndex++] << 16)&0xff0000 | (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    d("Maximum Packet Size: " + value);

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 34){ //Topic Alias Maximum
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("Topic Alias Maximum : " + value);
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

                    d("User Property: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

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

            //属性长度
            VariableByteInteger propertyVariableByte = decodevariableByte(datas,currentByteIndex);
            int propertyLength = propertyVariableByte.getValue();
            if(propertyLength == 0){
                return;
            }
            currentByteIndex = currentByteIndex + propertyVariableByte.getEncodedLength();

            if(currentByteIndex >= datas.length){
                return;
            }

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length) {

                if (propertyType == 17) { //Session Expiry Interval
                    currentByteIndex++;
                    int sessionExpiry = (datas[currentByteIndex++] << 24) & 0xff000000 | (datas[currentByteIndex++] << 16) & 0xff0000 | (datas[currentByteIndex++] << 8) & 0xff00 | datas[currentByteIndex++] & 0xff;
                    d("Session Expiry Interval: " + sessionExpiry + "秒");

                    if (currentByteIndex >= datas.length) {
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 33){ //Receive Maximum
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("Receive Maximum: " + value);
                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 36){ // Maximum QoS
                    currentByteIndex++;

                    int value = datas[currentByteIndex++]&0xff;

                    d("Maximum QoS: " + value);
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
                    d("Maximum Packet Size: " + value);

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

                    d("Topic Alias Maximum: " + value);
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

                    d("User Property: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

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

            //属性长度
            VariableByteInteger propertyVariableByte = decodevariableByte(datas,currentByteIndex);
            int propertyLength = propertyVariableByte.getValue();
            if(propertyLength == 0){
                return;
            }
            currentByteIndex = currentByteIndex + propertyVariableByte.getEncodedLength();

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

                    d("User Property: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

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

            //属性长度
            VariableByteInteger propertyVariableByte = decodevariableByte(datas,currentByteIndex);
            int propertyLength = propertyVariableByte.getValue();
            if(propertyLength == 0){
                return;
            }
            currentByteIndex = currentByteIndex + propertyVariableByte.getEncodedLength();

            if(currentByteIndex >= datas.length){
                return;
            }

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length){

                if(propertyType == 11) { //Subscription Identifier
                    currentByteIndex++;
                    VariableByteInteger payloadVariableByte = decodevariableByte(datas,currentByteIndex);

                    int value = payloadVariableByte.getValue();
                    currentByteIndex = currentByteIndex + payloadVariableByte.getEncodedLength() ;
                    d("Subscription Identifier: " + value);

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

                    d("User Property: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

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

            //属性长度
            VariableByteInteger propertyVariableByte = decodevariableByte(datas,currentByteIndex);
            int propertyLength = propertyVariableByte.getValue();
            if(propertyLength == 0){
                return;
            }
            currentByteIndex = currentByteIndex + propertyVariableByte.getEncodedLength();

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

                    d("User Property: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

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

            //属性长度
            VariableByteInteger propertyVariableByte = decodevariableByte(datas,currentByteIndex);
            int propertyLength = propertyVariableByte.getValue();
            if(propertyLength == 0){
                return;
            }
            currentByteIndex = currentByteIndex + propertyVariableByte.getEncodedLength();

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

                    d("User Property: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

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
            //属性长度
            VariableByteInteger propertyVariableByte = decodevariableByte(datas,currentByteIndex);
            int propertyLength = propertyVariableByte.getValue();
            if(propertyLength == 0){
                return;
            }
            currentByteIndex = currentByteIndex + propertyVariableByte.getEncodedLength();

            if(currentByteIndex >= datas.length){
                return;
            }

            int propertyType = datas[currentByteIndex];

            while (currentByteIndex < datas.length){

                if(propertyType == 17){ //Session Expiry Interval
                    currentByteIndex++;
                    int sessionExpiry = (datas[currentByteIndex++] << 24)&0xff000000 | (datas[currentByteIndex++] << 16)&0xff0000 | (datas[currentByteIndex++] << 8)&0xff00 |  datas[currentByteIndex++]&0xff;
                    d("Session Expiry Interval: " + sessionExpiry + "秒");

                    if(currentByteIndex >= datas.length){
                        break;
                    }
                    propertyType = datas[currentByteIndex];
                }

                if(propertyType == 31){ //Reason String
                    currentByteIndex++;

                    int value = (datas[currentByteIndex++] << 8)&0xff00 | datas[currentByteIndex++]&0xff;

                    d("Reason String: " + value);
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

                    d("User Property: " + new String(keyB, StandardCharsets.UTF_8) + "=" + new String(valueB, StandardCharsets.UTF_8));

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

    public static VariableByteInteger decodevariableByte(byte [] datas, int currentByteIndex){
        byte digit;
        int value = 0;
        int multiplier = 1;
        int count = 0;

        do {
            digit = datas[currentByteIndex++];
            count++;
            value += ((digit & 0x7F) * multiplier);
            multiplier *= 128;
        } while ((digit & 0x80) != 0);


        return new VariableByteInteger(value, count);
    }

}
