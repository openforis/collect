/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables.pojos;


import java.io.Serializable;
import java.sql.Timestamp;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcMessage implements Serializable {

	private static final long serialVersionUID = -167340586;

	private String    id;
	private Integer   sequenceNo;
	private Timestamp publicationTime;
	private String    queueId;
	private String    messageString;
	private byte[]    messageBytes;

	public OfcMessage() {}

	public OfcMessage(OfcMessage value) {
		this.id = value.id;
		this.sequenceNo = value.sequenceNo;
		this.publicationTime = value.publicationTime;
		this.queueId = value.queueId;
		this.messageString = value.messageString;
		this.messageBytes = value.messageBytes;
	}

	public OfcMessage(
		String    id,
		Integer   sequenceNo,
		Timestamp publicationTime,
		String    queueId,
		String    messageString,
		byte[]    messageBytes
	) {
		this.id = id;
		this.sequenceNo = sequenceNo;
		this.publicationTime = publicationTime;
		this.queueId = queueId;
		this.messageString = messageString;
		this.messageBytes = messageBytes;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getSequenceNo() {
		return this.sequenceNo;
	}

	public void setSequenceNo(Integer sequenceNo) {
		this.sequenceNo = sequenceNo;
	}

	public Timestamp getPublicationTime() {
		return this.publicationTime;
	}

	public void setPublicationTime(Timestamp publicationTime) {
		this.publicationTime = publicationTime;
	}

	public String getQueueId() {
		return this.queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public String getMessageString() {
		return this.messageString;
	}

	public void setMessageString(String messageString) {
		this.messageString = messageString;
	}

	public byte[] getMessageBytes() {
		return this.messageBytes;
	}

	public void setMessageBytes(byte[] messageBytes) {
		this.messageBytes = messageBytes;
	}
}
