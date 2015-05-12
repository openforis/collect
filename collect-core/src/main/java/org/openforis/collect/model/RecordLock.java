package org.openforis.collect.model;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordLock {

	private String sessionId;
	private int recordId;
	private User user;
	private long lastHeartBeatTime;
	private long timeoutMillis;
	
	public RecordLock(String sessionId, int recordId, User user, long timeoutMillis) {
		super();
		this.sessionId = sessionId;
		this.recordId = recordId;
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
	
	public String getSessionId() {
		return sessionId;
	}

	public User getUser() {
		return user;
	}

	public long getTimeoutMillis() {
		return timeoutMillis;
	}

	public int getRecordId() {
		return recordId;
	}
	
}
