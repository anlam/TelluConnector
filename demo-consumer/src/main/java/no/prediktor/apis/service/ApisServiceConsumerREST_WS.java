package no.prediktor.apis.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;

import eu.arrowhead.client.common.Utility;
import no.prediktor.apis.model.ApisItem;
import no.prediktor.apis.model.ApisItemValue;


public class ApisServiceConsumerREST_WS implements ApisService {

	
	protected String apisURL;

	public ApisServiceConsumerREST_WS(String endpoint) {

		this.apisURL = endpoint;
	}


	public List<ApisItem> getAllItems() {
		List<ApisItem> result = null;

		String updateURL = this.apisURL + "/items";
		Gson gson = new Gson();
		Response getResponse = Utility.sendRequest(updateURL, "GET", null);
		
		if(getResponse.getStatus() == 200)
		{
			
			ApisItem[] ret = gson.fromJson( getResponse.readEntity(String.class), ApisItem[].class);
			result = Arrays.asList(ret);
		}
		return result;
	}

	public List<String> getAllItemsName() {
		List<String> result = null;

		
		String updateURL = this.apisURL + "/items/ids";
		Gson gson = new Gson();
		Response getResponse = Utility.sendRequest(updateURL, "GET", null);
		
		if(getResponse.getStatus() == 200)
		{
			String[] ret = gson.fromJson( getResponse.readEntity(String.class), String[].class);
			result = Arrays.asList(ret);
		}
		return result;
	}

	public ApisItemValue getItemByName(String name) {
		
		ApisItemValue result = null;
		
		String updateURL = this.apisURL + "/items/" + name;
		Gson gson = new Gson();
		Response getResponse = Utility.sendRequest(updateURL, "GET", null);
		
		if(getResponse.getStatus() == 200)
		{
			result = gson.fromJson(getResponse.readEntity(String.class), ApisItemValue.class);
		}
		return result;
		
	}

	public boolean setItemValue(String name, ApisItemValue value) {
		
		
		String updateURL = this.apisURL + "/items/" + name;
		Gson gson = new Gson();
		String sValue = gson.toJson(value);
		Response getResponse = Utility.sendRequest(updateURL, "PUT", sValue);
		
		if(getResponse.getStatus() != 200)
		{
			return false;
		}
		return true;
	}

	public boolean setItemsValue(List<ApisItem> items) {
		String updateURL = this.apisURL + "/items";
		Gson gson = new Gson();
		String sValue = gson.toJson(items);
		Response getResponse = Utility.sendRequest(updateURL, "PUT", sValue);
		
		if(getResponse.getStatus() != 200)
		{
			return false;
		}
		return true;
	}

	
	public static void main(String[] args) {

		String endpoint = "http://localhost:6666/pull";
		
		ApisServiceConsumerREST_WS consumer = new ApisServiceConsumerREST_WS(endpoint);

		List<ApisItem> ret = consumer.getAllItems();
		System.out.println(ret.toString());
		
		List<String> ls = consumer.getAllItemsName();
		System.out.println(ls);
		
		ApisItemValue aiv = new ApisItemValue();
		List<ApisItem> list = new ArrayList();
		aiv.setValue("This Is A New Value of TestItem");
		aiv.setTimestamp(new Date());
		aiv.setQuality((short) 192);
		list.add(new ApisItem("TestItem", aiv) );
		list.add(new ApisItem("IntItem", "This is new value of IntItem", (short) 192, new Date()));
		list.add(new ApisItem("StringItem", "This is new value of StringItem", (short) 192, new Date()));
		boolean br = consumer.setItemsValue(list);
		System.out.println(br);
		
		
		aiv = consumer.getItemByName("TestItem");
		System.out.println(aiv);
		
	}
	

}
