/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    https://www.eclipse.org/legal/epl-2.0
 * and the Eclipse Distribution License is available at 
 *   https://www.eclipse.org/org/documents/edl-v10.php
 *
 * Contributors:
 *    Dave Locke - initial API and implementation and/or initial documentation
 */
package org.eclipse.paho.mqttv5.client.wire;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.paho.mqttv5.Tools;
import org.eclipse.paho.mqttv5.client.MqttClientException;
import org.eclipse.paho.mqttv5.client.internal.MqttState;
import org.eclipse.paho.mqttv5.client.logging.HarveyDebug;
import org.eclipse.paho.mqttv5.client.logging.Logger;
import org.eclipse.paho.mqttv5.client.logging.LoggerFactory;
import org.eclipse.paho.mqttv5.common.ExceptionHelper;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.packet.MqttWireMessage;


/**
 * An <code>MqttOutputStream</code> lets applications write instances of
 * <code>MqttWireMessage</code>. 
 */
public class MqttOutputStream extends OutputStream {
	private static final String CLASS_NAME = MqttOutputStream.class.getName();
	private Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);

	private MqttState clientState = null;
	private BufferedOutputStream out;
	
	public MqttOutputStream(MqttState clientState, OutputStream out, String clientId) {
		this.clientState = clientState;
		this.out = new BufferedOutputStream(out);
		log.setResourceName(clientId);
	}
	
	public void close() throws IOException {
		out.close();
	}
	
	public void flush() throws IOException {
		out.flush();
	}
	
	public void write(byte[] b) throws IOException {
		out.write(b);
		clientState.notifySentBytes(b.length);
	}
	
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		clientState.notifySentBytes(len);
	}
	
	public void write(int b) throws IOException {
		out.write(b);
	}

	/**
	 * Writes an <code>MqttWireMessage</code> to the stream.
	 * @param message The {@link MqttWireMessage} to send
	 * @throws IOException if an exception is thrown when writing to the output stream.
	 * @throws MqttException if an exception is thrown when getting the header or payload
	 */
	public void write(MqttWireMessage message) throws IOException, MqttException {
		final String methodName = "write";
		byte[] bytes = message.getHeader();
		byte[] pl = message.getPayload();
		if(this.clientState.getOutgoingMaximumPacketSize() != null && 
				bytes.length+pl.length > this.clientState.getOutgoingMaximumPacketSize() ) {
			// Outgoing packet is too large
			throw ExceptionHelper.createMqttException(MqttClientException.REASON_CODE_OUTGOING_PACKET_TOO_LARGE);
		}

		if(HarveyDebug.DEBUG_MODE == 1){
			try {
				HarveyDebug.parseHeader(bytes);
			} catch (Throwable t){
				HarveyDebug.e("MqttOutputStream:94 " + t.getMessage());
			}
		}

		//调试日志S HARVEY
		final String[] PACKET_NAMES = { "reserved", "CONNECT", "CONNACK", "PUBLISH", "PUBACK", "PUBREC",
				"PUBREL", "PUBCOMP", "SUBSCRIBE", "SUBACK", "UNSUBSCRIBE", "UNSUBACK", "PINGREQ", "PINGRESP", "DISCONNECT",
				"AUTH" };
		byte type = (byte) ((bytes[0] >>> 4) & 0x0F);
		HarveyDebug.d();
		HarveyDebug.d("Send 包类型：" + PACKET_NAMES[type]);

		StringBuffer sb = new StringBuffer();
		for(byte b : bytes){
			sb.append(Tools.toBinaryString(b&0xff)).append(" ");
		}
		if(sb.toString().length() != 0){
			HarveyDebug.d("Send Message Header(二进制内容) : " + sb.toString());
		}

		if(HarveyDebug.DEBUG_MODE == 0){
			HarveyDebug.d("Send Message Header(字符串内容) : " + new String(bytes, "UTF-8"));
		}
        //调试日志E

		out.write(bytes,0,bytes.length);
		clientState.notifySentBytes(bytes.length);
		
        int offset = 0;
        int chunckSize = 1024;
        while (offset < pl.length) {
        	int length = Math.min(chunckSize, pl.length - offset);
        	out.write(pl, offset, length);
        	offset += chunckSize;
        	clientState.notifySentBytes(length);
        }
		StringBuilder sb2 = new StringBuilder();
		for(byte b : pl){
			sb2.append(Tools.toBinaryString(b&0xff)).append(" ");
		}
		if(sb.toString().length() != 0){
			HarveyDebug.d("Send Message payload(二进制内容) : " + sb2.toString());
		}

		if(HarveyDebug.DEBUG_MODE == 0){
		  HarveyDebug.d("Send Message payload(字符串内容) : 内容长度=" + pl.length + "，内容=" + new String(pl, "UTF-8"));
		}

		// @TRACE 529= sent {0}
    	log.fine(CLASS_NAME, methodName, "529", new Object[]{message});
	}
}

