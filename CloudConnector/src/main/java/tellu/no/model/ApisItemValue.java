package tellu.no.model;

import java.util.Date;

public class ApisItemValue {

	 private String value;
     private short quality;
     private Date timestamp;
     
     public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public short getQuality() {
		return quality;
	}

	public void setQuality(short quality) {
		this.quality = quality;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
 	public String toString() {
 		return "ApisItemValue [value = " + value + ", quality = " + quality + ", timestamp = " + timestamp + "]";
 	}

	public ApisItemValue(String value, short quality, Date timestamp) {
		
		this.value = value;
		this.quality = quality;
		this.timestamp = timestamp;
	}
	
	public ApisItemValue()
	{
		
	}
	
}
