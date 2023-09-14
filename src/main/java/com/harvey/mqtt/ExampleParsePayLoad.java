package com.harvey.mqtt;

import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.logging.HarveyDebug;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * 本节实践目标：解析payload数据
 *
 * 包含PAYLOAD的消息有三个：CONNECT， PUBLISH， SUBSCRIBE
 *
 *
 */
public class ExampleParsePayLoad {

    public static void main(String[] args) {

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setCleanStart(true);
            connOpts.setKeepAliveInterval(0);
            connOpts.setSessionExpiryInterval(60L);
            connOpts.setUserName("adminName");
            connOpts.setPassword("adminPassword".getBytes(StandardCharsets.UTF_8));

            MqttMessage msg = new MqttMessage();
            msg.setQos(2);
            msg.setRetained(false);
            msg.setPayload("Offline".getBytes(StandardCharsets.UTF_8));
            connOpts.setWill(MQTTConfigue.topic, msg);

            MqttProperties mqttProperties = new MqttProperties();
            mqttProperties.setWillDelayInterval(30L);
            mqttProperties.setContentType("hello");
            mqttProperties.setResponseTopic("word");
            mqttProperties.setCorrelationData("hehe".getBytes(StandardCharsets.UTF_8));
            mqttProperties.setMessageExpiryInterval(60L);
            mqttProperties.setPayloadFormat(true);
            List<UserProperty> userPropertyList = new ArrayList<>();
            userPropertyList.add(new UserProperty("one", "Vone"));
            userPropertyList.add(new UserProperty("two", "Vtwo"));
            mqttProperties.setUserProperties(userPropertyList);
            connOpts.setWillMessageProperties(mqttProperties);
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
        MqttSubscription subscription2 = new MqttSubscription("TEST_TOPIC");
        MqttProperties mqttProperties = new MqttProperties();
        mqttProperties.setPayloadFormat(true);
        sampleClient.subscribe(new MqttSubscription[]{subscription, subscription2}, null, new MqttActionListener() {
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

}
