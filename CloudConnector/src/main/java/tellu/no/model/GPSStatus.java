package tellu.no.model;

public class GPSStatus {

	private long timestamp;
	private int status ;
	private int satellites_visible;
	private int satellites_used ;
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getSatellites_visible() {
		return satellites_visible;
	}
	public void setSatellites_visible(int satellites_visible) {
		this.satellites_visible = satellites_visible;
	}
	public int getSatellites_used() {
		return satellites_used;
	}
	public void setSatellites_used(int satellites_used) {
		this.satellites_used = satellites_used;
	}
	
	
}
