package tellu.no.CloudConnector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

import eu.arrowhead.client.common.Utility;
import tellu.no.model.ApisItem;
import tellu.no.model.GPSAltitude;
import tellu.no.model.GPSPosition;
import tellu.no.model.GPSStatus;
import tellu.no.model.GatewayHearbeatACK;
import tellu.no.model.GatewayHeartbeat;
import tellu.no.model.RuuviMeasurement;

public class MQTTClient implements IMqttMessageListener, IMqttActionListener, MqttCallback{

	
	private MqttAsyncClient client ;
	private MqttConnectOptions opts;
	private String brokerURL;
	private String topic;
	private String apisURL;
	
	public MQTTClient(String mqttAdd, String mqttUser, String mqttPass) {
		
		brokerURL = mqttAdd;
		
        opts = new MqttConnectOptions();
        opts.setUserName(mqttUser);
        opts.setPassword(mqttPass.toCharArray());
        opts.setCleanSession(true);
        opts.setAutomaticReconnect(true);
        opts.setConnectionTimeout(30);
        opts.setKeepAliveInterval(300);

    	
	}
	
	
	public void consumeAPISService(String url, String mqttTopic)
	{
		apisURL = url;
		topic = mqttTopic;
		
		connect(brokerURL, opts, topic);
		
		
	}
	
	private void connect(String mqttAdd, MqttConnectOptions opts, String mqtt_topic)
	{
		
		try 
		{
			
			/*
			 * if(client != null && client.isConnected()) { client.disconnectForcibly();
			 * client.close(); }
			 */
			 
				
			
			client = new MqttAsyncClient(mqttAdd, "testclient_Productive40");
			client.setCallback(this);
	    	IMqttToken token = client.connect(opts);
	    	token.waitForCompletion();
	    	client.subscribe(mqtt_topic,0);
	    	
		} catch (MqttException e) {
			System.out.println("Fail to connect: " + e.getMessage());
		}
    	
	}
	
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		
		System.out.println(arg0);
		//System.out.println(new String(arg1.getPayload()));
		
		String topic = arg0;
		String payload = new String(arg1.getPayload());
		
		String[] strs = topic.split("/");
		
		String updateURL = this.apisURL + "/items";
		if(strs.length == 3)
		{
			String gateway = strs[1];
			String sub_topic = strs[2];
			Gson gson = new Gson();
			List<ApisItem> items = new ArrayList<>();
			ApisItem it;
			
			
			if(sub_topic.equals("ruuvi_measurement"))
			{
				RuuviMeasurement pl = gson.fromJson(payload, RuuviMeasurement.class);
				
				Date timestamp = new Date(pl.getTimestamp());
				String dv = String.valueOf(pl.getDeviceID());
				
				it = new ApisItem(String.join(".", gateway, dv, "humidity"), String.valueOf(pl.getHumidity()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, dv, "temperature"), String.valueOf(pl.getTemperature()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, dv, "pressure"), String.valueOf(pl.getPressure()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, dv, "ax"), String.valueOf(pl.getAx()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, dv, "ay"), String.valueOf(pl.getAy()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, dv, "az"), String.valueOf(pl.getAz()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, dv, "battery"), String.valueOf(pl.getBattery()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, dv, "rssi"), String.valueOf(pl.getRssi()), (short) 192, timestamp);
				items.add(it);
				
			}
			else if (sub_topic.equals("gateway_heartbeat"))
			{
				GatewayHeartbeat pl = gson.fromJson(payload, GatewayHeartbeat.class);
				
				Date timestamp = new Date(pl.getTimestamp());
				
				it = new ApisItem(String.join(".", gateway, "heartbeat", "seq"), String.valueOf(pl.getSeq()), (short) 192, timestamp);
				items.add(it);
				
				
			}
			else if (sub_topic.equals("gateway_heartbeat_ack"))
			{
				GatewayHearbeatACK pl = gson.fromJson(payload, GatewayHearbeatACK.class);
				
				Date timestamp = new Date(pl.getTimestamp());
				
				it = new ApisItem(String.join(".", gateway, "heartbeat", "incseq"), String.valueOf(pl.getIncseq()), (short) 192, timestamp);
				items.add(it);
			}
			else if (sub_topic.equals("gps_status"))
			{
				GPSStatus pl = gson.fromJson(payload, GPSStatus.class);
				
				Date timestamp = new Date(pl.getTimestamp());
				
				it = new ApisItem(String.join(".", gateway, "gps", "status"), String.valueOf(pl.getStatus()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "satellites_visible"), String.valueOf(pl.getSatellites_visible()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "satellites_used"), String.valueOf(pl.getSatellites_used()), (short) 192, timestamp);
				items.add(it);
			}
			else if (sub_topic.equals("gps_position"))
			{
				GPSPosition pl = gson.fromJson(payload, GPSPosition.class);
				
				Date timestamp = new Date(pl.getTimestamp());
				
				it = new ApisItem(String.join(".", gateway, "gps", "gpstime"), String.valueOf(pl.getGpstime()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "latitude"), String.valueOf(pl.getLatitude()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "latitude_err"), String.valueOf(pl.getLatitude_err()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "longitude"), String.valueOf(pl.getLongitude()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "longitude_err"), String.valueOf(pl.getLongitude_err()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "speed"), String.valueOf(pl.getSpeed()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "speed_err"), String.valueOf(pl.getSpeed_err()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "track"), String.valueOf(pl.getTrack()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "track_err"), String.valueOf(pl.getTrack_err()), (short) 192, timestamp);
				items.add(it);
			}
			else if (sub_topic.equals("gps_altitude"))
			{
				GPSAltitude pl = gson.fromJson(payload, GPSAltitude.class);
				
				Date timestamp = new Date(pl.getTimestamp());
				
				it = new ApisItem(String.join(".", gateway, "gps", "altitude"), String.valueOf(pl.getAltitude()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "altitude_err"), String.valueOf(pl.getAltitude_err()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps",  "vspeed"), String.valueOf(pl.getVspeed()), (short) 192, timestamp);
				items.add(it);
				
				it = new ApisItem(String.join(".", gateway, "gps", "vspeed_err"), String.valueOf(pl.getVspeed_err()), (short) 192, timestamp);
				items.add(it);
			}
			else
			{
				System.err.println("Unknown topic: " + topic);
				System.err.println("Payload: " + payload);
			}
			
			if(!items.isEmpty())
			{
				String sValue = gson.toJson(items);
				Response getResponse = Utility.sendRequest(updateURL, "PUT", sValue);
				
				if(getResponse.getStatus() != 200)
				{
					System.err.println("Write Failed");
					System.err.println("Topic: " + topic);
					System.err.println("Payload: " + payload);
				}
			}
			
			
		}
		else
		{
			System.err.println("Unknown topic: " + topic);
			System.err.println("Payload: " + payload);
		}
	  
		
		//Response getResponse = Utility.sendRequest(providerUrl, "GET", null);
		
	}

	public void onFailure(IMqttToken arg0, Throwable arg1) {
		
		System.out.println("onFailure");
	}

	public void onSuccess(IMqttToken arg0) {
		
		System.out.println("onSuccess");
		
	}

	public void connectionLost(Throwable arg0) {
		
		System.out.println("connectionLost:" + arg0.getMessage());
		try {
			client.reconnect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Trying to reconnect");
	
	
		//connect(brokerURL, opts, topic);
		
		
	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		
		System.out.println("deliveryComplete");
		
		
	}

}
