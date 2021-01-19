package kr.co.the_e.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MessageQueue implements Closeable {
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();

	private static final MemoryPersistence persistence = new MemoryPersistence();
	private static final String publisherId = UUID.randomUUID().toString();

	private final IMqttClient mqttClient;

	public MessageQueue(final String host, final String username, final char[] password) throws MqttException {

		mqttClient = new MqttClient(host, publisherId, persistence);

		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setUserName(username);
		connOpts.setPassword(password);
		connOpts.setCleanSession(true);
		connOpts.setAutomaticReconnect(true);
		connOpts.setConnectionTimeout(10);

		System.out.println("Connecting to broker: " + host);
		try {
			mqttClient.connect(connOpts);
			System.out.println("Connected");
		} catch (MqttException e) {
			e.printStackTrace();
		}

	}

	public void subscribe(final String topic, final int qos, final IMqttMessageListener listener) {
		executor.execute(() -> {
			try {
				mqttClient.subscribe(topic, qos, listener);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		});

	}

	public void subscribe(final String topic, final IMqttMessageListener listener) {
		subscribe(topic, 2, listener);
	}

	public void publish(final String topic, final String msg) {
		publish(topic, msg, 2);
	}

	public void publish(final String topic, final String msg, final int qos) {
		System.out.println("Publishing message: " + msg);
		MqttMessage message = new MqttMessage(msg.getBytes());
		message.setQos(qos);
		executor.execute(() -> {
			try {
				mqttClient.publish(topic, message);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		});
		System.out.println("Message published");
	}

	@Override
	public void close() throws IOException {
		try {
			mqttClient.disconnect();
		} catch (MqttException e) {
			throw new IOException(e);
		}
	}
}
