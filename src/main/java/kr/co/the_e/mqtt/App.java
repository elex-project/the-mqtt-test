package kr.co.the_e.mqtt;

import com.google.common.io.BaseEncoding;
import org.eclipse.paho.client.mqttv3.MqttException;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Properties;

public final class App extends JFrame {
	private static final Properties properties = new Properties();
	private static MessageQueue messageQueue;

	private final JSplitPane splitPane;
	private final JTextArea txtLeft, txtRight;

	static {
		final File settings = new File("setting.properties");
		try (FileInputStream is = new FileInputStream(settings)) {
			properties.load(is);

		} catch (IOException e) {
			try {
				if (settings.createNewFile()) {
					try (FileOutputStream os = new FileOutputStream(settings)) {
						properties.store(os, "Settings file");
					}
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}

		try {
			messageQueue = new MessageQueue(properties.getProperty("host"),
					properties.getProperty("username"),
					properties.getProperty("password").toCharArray());
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	App() throws MqttException {
		super();

		splitPane = new JSplitPane();
		txtLeft = new JTextArea();
		txtRight = new JTextArea();
		txtLeft.setAutoscrolls(true);
		txtRight.setAutoscrolls(true);
		DefaultCaret caretLeft = (DefaultCaret) txtLeft.getCaret();
		caretLeft.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		DefaultCaret caretRight = (DefaultCaret) txtLeft.getCaret();
		caretRight.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		setContentPane(new MainPanel());
		setTitle("MQTT Client");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);

		init();
	}

	private void init() {
		messageQueue.subscribe(properties.getProperty("left.topic"), (topic, message) -> {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					txtLeft.append(LocalDateTime.now().format(DateTimeFormatter
							.ofLocalizedTime(FormatStyle.MEDIUM)));
					txtLeft.append("\t");
					txtLeft.append(topic);
					txtLeft.append("\t");
					if (Boolean.parseBoolean(properties.getProperty("left.hex"))) {
						txtLeft.append(BaseEncoding.base16().encode(message.getPayload()));
					} else {
						txtLeft.append(new String(message.getPayload()));
					}
					txtLeft.append("\n");
					txtLeft.setCaretPosition(txtLeft.getDocument().getLength());
				}
			});
		});

		messageQueue.subscribe(properties.getProperty("right.topic"), (topic, message) -> {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					txtRight.append(LocalDateTime.now().format(DateTimeFormatter
							.ofLocalizedTime(FormatStyle.MEDIUM)));
					txtRight.append("\t");
					txtRight.append(topic);
					txtRight.append("\t");
					if (Boolean.parseBoolean(properties.getProperty("right.hex"))) {
						txtRight.append(BaseEncoding.base16().encode(message.getPayload()));
					} else {
						txtRight.append(new String(message.getPayload()));
					}
					txtRight.append("\n");
					txtRight.setCaretPosition(txtLeft.getDocument().getLength());
				}
			});
		});
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		splitPane.setDividerLocation(0.5f);
	}

	private class MainPanel extends JPanel {

		MainPanel() {
			super(new BorderLayout());

			splitPane.add(new JScrollPane(txtLeft), JSplitPane.LEFT);
			splitPane.add(new JScrollPane(txtRight), JSplitPane.RIGHT);

			add(splitPane, BorderLayout.CENTER);

		}
	}

	public static void main(String... args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					if (null != messageQueue) messageQueue.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					App app = new App();
					app.setVisible(true);
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
