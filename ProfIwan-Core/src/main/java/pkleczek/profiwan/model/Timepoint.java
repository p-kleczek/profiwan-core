package pkleczek.profiwan.model;

import org.joda.time.DateTime;

public class Timepoint  implements Comparable<Timepoint> {

	public static enum TimepointType {
		SESSION_STARTED,
		SESSION_FINISHED,
		REVISION_STARTED,
		REVISION_FINISHED,
		REVISION_INTERRUPTED
	}
	
	private long id;
	private DateTime createdAt = null;
	private TimepointType type;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public DateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(DateTime createdAt) {
		this.createdAt = createdAt;
	}
	public TimepointType getType() {
		return type;
	}
	public void setType(TimepointType type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return String.format("[%s] %s", createdAt, type.name());
	}
	@Override
	public int compareTo(Timepoint o) {
		return createdAt.compareTo(o.createdAt);
	}
	
	public static Timepoint create(TimepointType type) {
		Timepoint t = new Timepoint();
		t.setCreatedAt(DateTime.now());
		t.setType(type);
		return t;
	}
}
