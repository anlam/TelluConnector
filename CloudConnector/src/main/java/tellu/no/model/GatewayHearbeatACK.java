package tellu.no.model;

public class GatewayHearbeatACK {

	private long timestamp;
	private int incseq;
	
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getIncseq() {
		return incseq;
	}
	public void setIncseq(int incseq) {
		this.incseq = incseq;
	}
}
