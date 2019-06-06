package tellu.no.CloudConnector;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.core.Response;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;

import eu.arrowhead.client.common.Utility;
import tellu.no.model.ApisItem;
import tellu.no.model.GPSAltitude;
import tellu.no.model.GPSPosition;
import tellu.no.model.GPSStatus;
import tellu.no.model.GatewayHearbeatACK;
import tellu.no.model.GatewayHeartbeat;
import tellu.no.model.RuuviMeasurement;

public class NewMQTTClient implements MqttCallback, IMqttActionListener {

	MqttAsyncClient client;
	String brokerUrl;
	MemoryPersistence persistence;
	MqttConnectOptions opts = new MqttConnectOptions();

	private String apisURL;

	public void initialize() throws Exception {

		brokerUrl = "ssl://mqtt.stagegw.tellucloud.com:8883";
		persistence = new MemoryPersistence();

		opts.setCleanSession(true);
		opts.setAutomaticReconnect(true);
		opts.setConnectionTimeout(30);

		String caFilePath = "tellucloud_ca_cacert.pem";
		String clientCrtFilePath = "P4Arrowhead_cert.pem";
		String clientKeyFilePath = "P4Arrowhead_key.pem";

		SSLSocketFactory socketFactory = getSocketFactory(caFilePath, clientCrtFilePath, clientKeyFilePath, "");
		opts.setSocketFactory(socketFactory);

		client = new MqttAsyncClient(brokerUrl, "P4Arrowhead_Client" + System.currentTimeMillis(), persistence);
		client.setCallback(this);

		IMqttToken token = client.connect(opts, null, this);
		token.waitForCompletion();

		// Subscribe to all the gateways
		//for (int i = 0; i < 6; i++) {
		//	client.subscribe("tellu/P4GW100" + i + "/#", 0);
		//}
		
		client.subscribe("tellu/P4GW1004" +  "/#", 0);
		client.subscribe("tellu/P4GW1003"  + "/#", 0);

	}

	@Override
	public void onSuccess(IMqttToken imt) {

	}

	@Override
	public void onFailure(IMqttToken imt, Throwable thrwbl) {
		Logger.getLogger(NewMQTTClient.class.getName()).log(Level.SEVERE, "MQTT Failure.", thrwbl);
	}

	protected void cleanupAndDie() {
		try {
			client.disconnect();
		} catch (MqttException ex) {
			Logger.getLogger(NewMQTTClient.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.exit(0);
	}
	
	
	protected void cleanupAndReconnect() {
		try {
			client.disconnect();
		} catch (MqttException ex) {
			Logger.getLogger(NewMQTTClient.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		try {
			initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
			
		}
		
	}

	@Override
	public void connectionLost(Throwable arg0) {
		cleanupAndReconnect();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {

	}

	/*
	 * @Override public void messageArrived(String topic, MqttMessage msg) throws
	 * Exception { System.out.println(topic); System.out.println(new
	 * String(msg.getPayload())); }
	 */

	private static SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile,
			final String password) throws Exception {

		Security.addProvider(new BouncyCastleProvider());

		// load CA certificate
		X509Certificate caCert = null;

		BufferedInputStream bis = new BufferedInputStream(
				NewMQTTClient.class.getClassLoader().getResourceAsStream(caCrtFile));
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		while (bis.available() > 0) {
			caCert = (X509Certificate) cf.generateCertificate(bis);
			// System.out.println(caCert.toString());
		}

		// load client certificate
		bis = new BufferedInputStream(NewMQTTClient.class.getClassLoader().getResourceAsStream(crtFile));
		X509Certificate cert = null;
		while (bis.available() > 0) {
			cert = (X509Certificate) cf.generateCertificate(bis);
			// System.out.println(caCert.toString());
		}

		// load client private key
		PEMParser pemParser = new PEMParser(
				new InputStreamReader(NewMQTTClient.class.getClassLoader().getResourceAsStream(keyFile)));
		Object object = pemParser.readObject();
		PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
		KeyPair key;
		if (object instanceof PEMEncryptedKeyPair) {
			System.out.println("Encrypted key - we will use provided password");
			key = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
		} else {
			System.out.println("Unencrypted key - no password needed");
			key = converter.getKeyPair((PEMKeyPair) object);
		}
		pemParser.close();

		// CA certificate is used to authenticate server
		KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", caCert);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init(caKs);

		// client key and certificates are sent to server so it can authenticate
		// us
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("certificate", cert);
		ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
				new java.security.cert.Certificate[] { cert });
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, password.toCharArray());

		// finally, create SSL socket factory
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		return context.getSocketFactory();
	}

	public static void main(String[] args) throws Exception {

		new NewMQTTClient().initialize();

	}

	public void consumeAPISService(String url) {
		apisURL = url;

		try {
			initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {

		System.out.println(arg0);
		// System.out.println(new String(arg1.getPayload()));

		String topic = arg0;
		String payload = new String(arg1.getPayload());

		String[] strs = topic.split("/");
		String item_prefix = topic.replaceAll("/", ".");
		
		if(item_prefix.startsWith("tellu"))
			item_prefix = item_prefix.substring(6);

		String updateURL = this.apisURL + "/items";

		String sub_topic = strs[strs.length - 1];
		Gson gson = new Gson();
		List<ApisItem> items = new ArrayList<>();
		ApisItem it;

		if (sub_topic.equals("ruuvi_measurement")) {
			RuuviMeasurement pl = gson.fromJson(payload, RuuviMeasurement.class);

			Date timestamp = new Date(pl.getTimestamp()*1000);
			String dv = String.valueOf(pl.getDeviceID());

			it = new ApisItem(String.join(".", item_prefix, dv, "humidity"), String.valueOf(pl.getHumidity()),
					(short) 192, timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, dv, "temperature"), String.valueOf(pl.getTemperature()),
					(short) 192, timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, dv, "pressure"), String.valueOf(pl.getPressure()),
					(short) 192, timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, dv, "ax"), String.valueOf(pl.getAx()), (short) 192,
					timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, dv, "ay"), String.valueOf(pl.getAy()), (short) 192,
					timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, dv, "az"), String.valueOf(pl.getAz()), (short) 192,
					timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, dv, "battery"), String.valueOf(pl.getBattery()),
					(short) 192, timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, dv, "rssi"), String.valueOf(pl.getRssi()), (short) 192,
					timestamp);
			items.add(it);

		} else if (sub_topic.equals("gateway_heartbeat")) {
			GatewayHeartbeat pl = gson.fromJson(payload, GatewayHeartbeat.class);

			Date timestamp = new Date(pl.getTimestamp()*1000);

			it = new ApisItem(String.join(".", item_prefix, "seq"), String.valueOf(pl.getSeq()), (short) 192,
					timestamp);
			items.add(it);

		} else if (sub_topic.equals("gateway_heartbeat_ack")) {
			GatewayHearbeatACK pl = gson.fromJson(payload, GatewayHearbeatACK.class);

			Date timestamp = new Date(pl.getTimestamp()*1000);

			it = new ApisItem(String.join(".", item_prefix, "incseq"), String.valueOf(pl.getIncseq()), (short) 192,
					timestamp);
			items.add(it);
		} else if (sub_topic.equals("gps_status")) {
			GPSStatus pl = gson.fromJson(payload, GPSStatus.class);

			Date timestamp = new Date(pl.getTimestamp()*1000);

			it = new ApisItem(String.join(".", item_prefix, "status"), String.valueOf(pl.getStatus()), (short) 192,
					timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "satellites_visible"),
					String.valueOf(pl.getSatellites_visible()), (short) 192, timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "satellites_used"), String.valueOf(pl.getSatellites_used()),
					(short) 192, timestamp);
			items.add(it);
		} else if (sub_topic.equals("gps_position")) {
			GPSPosition pl = gson.fromJson(payload, GPSPosition.class);

			Date timestamp = new Date(pl.getTimestamp()*1000);

			it = new ApisItem(String.join(".", item_prefix, "gpstime"), String.valueOf(pl.getGpstime()), (short) 192,
					timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "latitude"), String.valueOf(pl.getLatitude()), (short) 192,
					timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "latitude_err"), String.valueOf(pl.getLatitude_err()),
					(short) 192, timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "longitude"), String.valueOf(pl.getLongitude()),
					(short) 192, timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "longitude_err"), String.valueOf(pl.getLongitude_err()),
					(short) 192, timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "speed"), String.valueOf(pl.getSpeed()), (short) 192,
					timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "speed_err"), String.valueOf(pl.getSpeed_err()),
					(short) 192, timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "track"), String.valueOf(pl.getTrack()), (short) 192,
					timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "track_err"), String.valueOf(pl.getTrack_err()),
					(short) 192, timestamp);
			items.add(it);
		} else if (sub_topic.equals("gps_altitude")) {
			GPSAltitude pl = gson.fromJson(payload, GPSAltitude.class);

			Date timestamp = new Date(pl.getTimestamp()*1000);

			it = new ApisItem(String.join(".", item_prefix, "altitude"), String.valueOf(pl.getAltitude()), (short) 192,
					timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "altitude_err"), String.valueOf(pl.getAltitude_err()),
					(short) 192, timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "vspeed"), String.valueOf(pl.getVspeed()), (short) 192,
					timestamp);
			items.add(it);

			it = new ApisItem(String.join(".", item_prefix, "vspeed_err"), String.valueOf(pl.getVspeed_err()),
					(short) 192, timestamp);
			items.add(it);
		} else {
			System.err.println("Unknown topic: " + topic);
			System.err.println("Payload: " + payload);
		}

		if (!items.isEmpty()) {
			String sValue = gson.toJson(items);
			Response getResponse = Utility.sendRequest(updateURL, "PUT", sValue);

			if (getResponse.getStatus() != 200) {
				System.err.println("Write Failed");
				System.err.println("Topic: " + topic);
				System.err.println("Payload: " + payload);
			}
		}

	}

	// Response getResponse = Utility.sendRequest(providerUrl, "GET", null);

}
