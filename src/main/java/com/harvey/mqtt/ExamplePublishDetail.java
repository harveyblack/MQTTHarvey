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
 * 关于 PUBLISH 协议的详细研究
 *
 */
public class ExamplePublishDetail {

    public static void main(String[] args) {

        publish();

    }

    /**
     *
     * 发布主题内容
     *
     */
    private static void publish(){
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setCleanStart(true);
            connOpts.setKeepAliveInterval(0);
            connOpts.setSessionExpiryInterval(60L);
            connOpts.setMaximumPacketSize(1000L);
            connOpts.setTopicAliasMaximum(30);
            connOpts.setReceiveMaximum(1000);
            connOpts.setAutomaticReconnect(true);
            connOpts.setAutomaticReconnectDelay(5, 15);
            connOpts.setMaxReconnectDelay(100);
            connOpts.setRequestProblemInfo(true);
            connOpts.setSendReasonMessages(true);

            MqttMessage msg = new MqttMessage();
            msg.setQos(2);
            msg.setDuplicate(true);
            msg.setRetained(true);
            connOpts.setWill(MQTTConfigue.topic, msg);
            MqttAsyncClient sampleClient = new MqttAsyncClient(MQTTConfigue.broker, MQTTConfigue.clientId, persistence);
            IMqttToken token = sampleClient.connect(connOpts);
            token.waitForCompletion();

            MqttMessage message = new MqttMessage();
            message.setQos(2);
            message.setDuplicate(true);
            message.setRetained(true);
            message.setId(9);

            MqttProperties mqttProperties = new MqttProperties();
            mqttProperties.setCorrelationData("HW".getBytes(StandardCharsets.UTF_8));
            mqttProperties.setTopicAlias(9);
            mqttProperties.setSessionExpiryInterval(50L);
            mqttProperties.setContentType("TEST_PUBLISH");
            mqttProperties.setPayloadFormat(true);
            mqttProperties.setResponseTopic("quick");
            mqttProperties.setMessageExpiryInterval(60*60L);
            mqttProperties.setSubscriptionIdentifier(40);

            List<UserProperty> userPropertyList = new ArrayList<>();
            userPropertyList.add(new UserProperty("first", "firstValue"));
            userPropertyList.add(new UserProperty("second", "secondValue"));
            mqttProperties.setUserProperties(userPropertyList);
            message.setProperties(mqttProperties);

            message.setPayload("ShangHai".getBytes(StandardCharsets.UTF_8));

            sampleClient.publish(MQTTConfigue.topic, message);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            subscription(sampleClient);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sampleClient.unsubscribe(MQTTConfigue.topic);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            subscription(sampleClient);

            try {
                Thread.sleep(25000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sampleClient.disconnect();

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

}
