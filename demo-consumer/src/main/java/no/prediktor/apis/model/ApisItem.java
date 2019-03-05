package no.prediktor.apis.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class ApisItem {
	private String name;
	
	@SerializedName(value = "vQT", alternate = {"VQT", "vqt"})
    private ApisItemValue VQT;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ApisItemValue getVQT() {
		return VQT;
	}

	public void setVQT(ApisItemValue vQT) {
		VQT = vQT;
	}

	@Override
	public String toString() {
		return "ApisItem [name = " + name + ", VQT = " + VQT + "]";
	}

	public ApisItem(String name, ApisItemValue vQT) {

		this.name = name;
		VQT = vQT;
	}
	
	public ApisItem(String name, String value, short quality, Date timestamp) {

		this.name = name;
		VQT = new ApisItemValue(value, quality, timestamp);
	}
	
	public ApisItem()
	{
		
	}

}
