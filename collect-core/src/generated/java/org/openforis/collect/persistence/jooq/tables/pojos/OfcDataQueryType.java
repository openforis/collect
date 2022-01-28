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
public class OfcDataQueryType implements Serializable {

	private static final long serialVersionUID = -924582643;

	private Integer   id;
	private String    uuid;
	private Integer   surveyId;
	private String    code;
	private String    label;
	private String    description;
	private Timestamp creationDate;
	private Timestamp modifiedDate;

	public OfcDataQueryType() {}

	public OfcDataQueryType(OfcDataQueryType value) {
		this.id = value.id;
		this.uuid = value.uuid;
		this.surveyId = value.surveyId;
		this.code = value.code;
		this.label = value.label;
		this.description = value.description;
		this.creationDate = value.creationDate;
		this.modifiedDate = value.modifiedDate;
	}

	public OfcDataQueryType(
		Integer   id,
		String    uuid,
		Integer   surveyId,
		String    code,
		String    label,
		String    description,
		Timestamp creationDate,
		Timestamp modifiedDate
	) {
		this.id = id;
		this.uuid = uuid;
		this.surveyId = surveyId;
		this.code = code;
		this.label = label;
		this.description = description;
		this.creationDate = creationDate;
		this.modifiedDate = modifiedDate;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Integer getSurveyId() {
		return this.surveyId;
	}

	public void setSurveyId(Integer surveyId) {
		this.surveyId = surveyId;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	public Timestamp getModifiedDate() {
		return this.modifiedDate;
	}

	public void setModifiedDate(Timestamp modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
}
