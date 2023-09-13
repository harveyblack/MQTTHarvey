package com.harvey.mqtt;

import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.logging.HarveyDebug;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.nio.charset.StandardCharsets;


/**
 * 关于 Last Will and Testament(LWT) 协议的详细研究
 * LWT 翻译为中文为：遗嘱
 *
 * 很明显，这个词用在MQTT技术中，初次看字面意思，感觉非常的奇怪
 *
 * 先说一下定义吧 : An Application Message which is published by the Server after the Network Connection is closed in cases
 * where the Network Connection is not closed normally.
 *
 * 定义来源： https://docs.oasis-open.org/mqtt/mqtt/v5.0/mqtt-v5.0.pdf
 *
 * 实验步骤：
 * 1. 启动工程
 * 2. 启动MQTTX客户端，订阅"harvey"主题
 * 3. 通过InteIIJ IDEA菜单中的红色停止按钮来终止工程运行
 * 4. 终止之后，在MQTTX客户端中可以收到"Offline"字样的内容
 *
 */
public class ExampleLWTDetail {

    public static void main(String[] args) {

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setCleanStart(true);
            connOpts.setKeepAliveInterval(0);
            connOpts.setSessionExpiryInterval(60L);

            MqttMessage msg = new MqttMessage();
            msg.setQos(2);
            msg.setRetained(true);
            msg.setPayload("Offline".getBytes(StandardCharsets.UTF_8));
            connOpts.setWill(MQTTConfigue.topic, msg);
            MqttAsyncClient sampleClient = new MqttAsyncClient(MQTTConfigue.broker, MQTTConfigue.clientId, persistence);
            IMqttToken token = sampleClient.connect(connOpts);
            token.waitForCompletion();

            subscription(sampleClient);

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
                HarveyDebug.d(new String(message.getPayload(), StandardCharsets.UTF_8));
            }
        }, mqttProperties);
    }

}
