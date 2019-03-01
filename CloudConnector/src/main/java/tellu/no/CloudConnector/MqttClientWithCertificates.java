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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

public class MqttClientWithCertificates implements MqttCallback, IMqttActionListener {

	MqttAsyncClient client;
	String brokerUrl;
	MemoryPersistence persistence;
	MqttConnectOptions opts = new MqttConnectOptions();

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
		for (int i=0; i<6; i++) {
			client.subscribe("tellu/P4GW100"+i+"/#", 0);
		}
		
	}

	@Override
	public void onSuccess(IMqttToken imt) {

	}

	@Override
	public void onFailure(IMqttToken imt, Throwable thrwbl) {
		Logger.getLogger(MqttClientWithCertificates.class.getName()).log(Level.SEVERE, "MQTT Failure.", thrwbl);
	}

	protected void cleanupAndDie() {
		try {
			client.disconnect();
		} catch (MqttException ex) {
			Logger.getLogger(MqttClientWithCertificates.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.exit(0);
	}

	@Override
	public void connectionLost(Throwable arg0) {
		cleanupAndDie();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {

	}


	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		System.out.println(topic);
		System.out.println(new String(msg.getPayload()));
	}

	
	private static SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile,
			final String password) throws Exception {

		Security.addProvider(new BouncyCastleProvider());

		// load CA certificate
		X509Certificate caCert = null;

		BufferedInputStream bis = new BufferedInputStream(
				MqttClientWithCertificates.class.getClassLoader().getResourceAsStream(caCrtFile));
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		while (bis.available() > 0) {
			caCert = (X509Certificate) cf.generateCertificate(bis);
			// System.out.println(caCert.toString());
		}

		// load client certificate
		bis = new BufferedInputStream(MqttClientWithCertificates.class.getClassLoader().getResourceAsStream(crtFile));
		X509Certificate cert = null;
		while (bis.available() > 0) {
			cert = (X509Certificate) cf.generateCertificate(bis);
			// System.out.println(caCert.toString());
		}

		// load client private key
		PEMParser pemParser = new PEMParser(
				new InputStreamReader(MqttClientWithCertificates.class.getClassLoader().getResourceAsStream(keyFile)));
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

		new MqttClientWithCertificates().initialize();

	}

}
