package com.harvey.mqtt;

import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.logging.HarveyDebug;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.nio.charset.StandardCharsets;

public class ExampleConnect {

    public static void main(String[] args) {
        connect();
        connectAndDisconnect();
        connectAndPing();
        subscribeAndUnsubscribe();
        subscribeAndDisconnectAndReConnect();
    }

    /**
     * 连接MQTT服务器
     */
    private static void connect(){
        connect("");
    }

    private static void connect(String id){
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setKeepAliveInterval(0);
            MqttAsyncClient sampleClient = new MqttAsyncClient(MQTTConfigue.broker, MQTTConfigue.clientId+id, persistence);
            IMqttToken token = sampleClient.connect(connOpts);
            token.waitForCompletion();

        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }

    /**
     * 连接 -> 断开
     */
    private static void connectAndDisconnect(){
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setKeepAliveInterval(0);
            MqttAsyncClient sampleClient = new MqttAsyncClient(MQTTConfigue.broker, MQTTConfigue.clientId, persistence);
            IMqttToken token = sampleClient.connect(connOpts);
            token.waitForCompletion();

            sampleClient.disconnect();
            token.waitForCompletion();

        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }

    /**
     * 连接 -> ping
     */
    private static void connectAndPing(){
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setKeepAliveInterval(10);
            MqttAsyncClient sampleClient = new MqttAsyncClient(MQTTConfigue.broker, MQTTConfigue.clientId, persistence);
            IMqttToken token = sampleClient.connect(connOpts);
            token.waitForCompletion();

        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }

    /**
     *
     * 订阅主题 & 解除订阅
     *
     */
    private static void subscribeAndUnsubscribe(){
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setKeepAliveInterval(0);
            MqttAsyncClient sampleClient = new MqttAsyncClient(MQTTConfigue.broker, MQTTConfigue.clientId, persistence);
            IMqttToken token = sampleClient.connect(connOpts);
            token.waitForCompletion();

            MqttSubscription subscription = new MqttSubscription(MQTTConfigue.topic);
            MqttProperties mqttProperties = new MqttProperties();
            mqttProperties.setPayloadFormat(true);
            sampleClient.subscribe(new MqttSubscription[]{subscription}, null, new MqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    try {
                        if(asyncActionToken.getTopics()[0].equals(MQTTConfigue.topic))
                          sampleClient.unsubscribe(MQTTConfigue.topic);

                        for (String topic : asyncActionToken.getTopics()){
                            if(topic.equals(MQTTConfigue.topic)){
                                sampleClient.unsubscribe(MQTTConfigue.topic);
                            } else if(topic.equals(MQTTConfigue.topic+"C")){
                                sampleClient.unsubscribe(MQTTConfigue.topic+"C");
                            }
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            }, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    HarveyDebug.d(new String(message.getPayload(), StandardCharsets.UTF_8));
                }
            }, mqttProperties);

        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }

    private static void subscription(MqttAsyncClient sampleClient) throws MqttException {

        MqttSubscription subscription = new MqttSubscription(MQTTConfigue.topic);
        MqttProperties mqttProperties = new MqttProperties();
        mqttProperties.setPayloadFormat(true);
        sampleClient.subscribe(new MqttSubscription[]{subscription}, null, new MqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

            }
        }, new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }
        }, mqttProperties);
    }

    /**
     *
     * 订阅主题 & 客户端断开 & 重新连接
     *
     */
    private static void subscribeAndDisconnectAndReConnect(){
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setCleanStart(false);
            connOpts.setKeepAliveInterval(0);
            connOpts.setSessionExpiryInterval(60L);
            MqttAsyncClient sampleClient = new MqttAsyncClient(MQTTConfigue.broker, MQTTConfigue.clientId, persistence);
            IMqttToken token = sampleClient.connect(connOpts);
            token.waitForCompletion();

            subscription(sampleClient);

            IMqttToken iMqttToken = sampleClient.disconnect();
            iMqttToken.waitForCompletion();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                sampleClient.reconnect();
            } catch (MqttException e) {
                HarveyDebug.d(e.getMessage());
            }

        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }

}
