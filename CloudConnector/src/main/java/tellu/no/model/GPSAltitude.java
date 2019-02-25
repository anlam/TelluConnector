package tellu.no.model;

public class GPSAltitude {
	
	private long timestamp;
	private double altitude   ;
	private double altitude_err  ;
	private double vspeed   ;
	private double vspeed_err    ;
	
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public double getAltitude() {
		return altitude;
	}
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	public double getAltitude_err() {
		return altitude_err;
	}
	public void setAltitude_err(double altitude_err) {
		this.altitude_err = altitude_err;
	}
	public double getVspeed() {
		return vspeed;
	}
	public void setVspeed(double vspeed) {
		this.vspeed = vspeed;
	}
	public double getVspeed_err() {
		return vspeed_err;
	}
	public void setVspeed_err(double vspeed_err) {
		this.vspeed_err = vspeed_err;
	}
}
