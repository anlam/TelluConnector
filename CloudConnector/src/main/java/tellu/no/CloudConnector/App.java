package tellu.no.CloudConnector;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Hello world!
 *
 */
public class App implements IMqttMessageListener, IMqttActionListener, MqttCallback
{
	public MqttAsyncClient client ;
	public MqttConnectOptions opts;
	public String brokerURL;
    public static void main( String[] args ) throws MqttException
    {
        System.out.println( "Hello World!" );
        
        App app = new App();
        
        
    }
    
    public App() throws MqttException
    {
    	brokerURL = "wss://mqtt.stagegw.tellucloud.com";
        opts = new MqttConnectOptions();
        opts.setUserName("P4GW1004");
        opts.setPassword("QNZ7vDDuNukJPtGWeqBQELtVLj9Q3Mdu".toCharArray());
        opts.setCleanSession(true);
        opts.setAutomaticReconnect(true);
        opts.setConnectionTimeout(30);
        
    	
    	client = new MqttAsyncClient(this.brokerURL, "testclient_Productive40" + System.currentTimeMillis());
    	client.setCallback(this);
    	IMqttToken token = client.connect(opts);
    	token.waitForCompletion();
    	
    	client.subscribe("tellu/P4GW1004/#",0);
    	
    	
    }

	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		
		System.out.println(arg0);
		System.out.println(new String(arg1.getPayload()));
		
		
		
	  
		
		
	}

	public void onFailure(IMqttToken arg0, Throwable arg1) {
		// TODO Auto-generated method stub
		System.out.println("onFailure");
	}

	public void onSuccess(IMqttToken arg0) {
		// TODO Auto-generated method stub
		System.out.println("onSuccess");
		
	}

	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionLost");
		
	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		System.out.println("deliveryComplete");
		
		
	}
}
