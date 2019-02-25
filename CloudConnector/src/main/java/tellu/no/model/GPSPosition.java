package tellu.no.model;

public class GPSPosition {

	private long timestamp;
	private long gpstime ;
	private double latitude  ;
	private double latitude_err ;
	private double longitude  ;
	private double longitude_err   ;
	private double speed  ;
	private double speed_err   ;
	private double track   ;
	private double track_err    ;
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public long getGpstime() {
		return gpstime;
	}
	public void setGpstime(long gpstime) {
		this.gpstime = gpstime;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLatitude_err() {
		return latitude_err;
	}
	public void setLatitude_err(double latitude_err) {
		this.latitude_err = latitude_err;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLongitude_err() {
		return longitude_err;
	}
	public void setLongitude_err(double longitude_err) {
		this.longitude_err = longitude_err;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getSpeed_err() {
		return speed_err;
	}
	public void setSpeed_err(double speed_err) {
		this.speed_err = speed_err;
	}
	public double getTrack() {
		return track;
	}
	public void setTrack(double track) {
		this.track = track;
	}
	public double getTrack_err() {
		return track_err;
	}
	public void setTrack_err(double track_err) {
		this.track_err = track_err;
	}
	
}
