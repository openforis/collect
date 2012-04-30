package org.openforis.collect.model;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordLock {

	private String id;
	private CollectRecord record;
	private User user;
	private long lastHeartBeatTime;
	private long timeoutMillis;
	
	public RecordLock(String id, CollectRecord record, User user, long timeoutMillis) {
		super();
		this.id = id;
		this.record = record;
		this.user = user;
		this.timeoutMillis = timeoutMillis;
		keepAlive();
	}

	public void keepAlive() {
		lastHeartBeatTime = System.currentTimeMillis();
	}
	
	public boolean isActive() {
		long now = System.currentTimeMillis();
		long diff = now - lastHeartBeatTime;
		return diff <= timeoutMillis;
	}
	
	public String getId() {
		return id;
	}

	public CollectRecord getRecord() {
		return record;
	}

	public User getUser() {
		return user;
	}

	public long getTimeoutMillis() {
		return timeoutMillis;
	}
	
}
