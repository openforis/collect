/**
 * 
 */
package org.openforis.collect.blazeds.service;

import java.util.Date;

/**
 * @author M. Togna
 * 
 */
public class RecordListItem {

	/**
	 * @uml.property name="createdBy" readOnly="true"
	 */
	private String createdBy;

	/**
	 * @uml.property name="creationDate" readOnly="true"
	 */
	private Date creationDate;

	/**
	 * @uml.property name="errorCount" readOnly="true"
	 */
	private int errorCount;

	/**
	 * @uml.property name="id" readOnly="true"
	 */
	private String id;

	/**
	 * @uml.property name="modifiedBy" readOnly="true"
	 */
	private String modifiedBy;

	/**
	 * @uml.property name="modifiedDate" readOnly="true"
	 */
	private Date modifiedDate;

	/**
	 * @uml.property name="warningCount" readOnly="true"
	 */
	private int warningCount;

	/**
	 * Getter of the property <tt>createdBy</tt>
	 * 
	 * @return Returns the createdBy.
	 * @uml.property name="createdBy"
	 */
	public String getCreatedBy() {
		return this.createdBy;
	}

	/**
	 * Getter of the property <tt>creationDate</tt>
	 * 
	 * @return Returns the creationDate.
	 * @uml.property name="creationDate"
	 */
	public Date getCreationDate() {
		return this.creationDate;
	}

	/**
	 * Getter of the property <tt>errorCount</tt>
	 * 
	 * @return Returns the errorCount.
	 * @uml.property name="errorCount"
	 */
	public int getErrorCount() {
		return this.errorCount;
	}

	/**
	 * Getter of the property <tt>id</tt>
	 * 
	 * @return Returns the id.
	 * @uml.property name="id"
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Getter of the property <tt>modifiedBy</tt>
	 * 
	 * @return Returns the modifiedBy.
	 * @uml.property name="modifiedBy"
	 */
	public String getModifiedBy() {
		return this.modifiedBy;
	}

	/**
	 * Getter of the property <tt>modifiedDate</tt>
	 * 
	 * @return Returns the modifiedDate.
	 * @uml.property name="modifiedDate"
	 */
	public Date getModifiedDate() {
		return this.modifiedDate;
	}

	/**
	 * Getter of the property <tt>warningCount</tt>
	 * 
	 * @return Returns the warningCount.
	 * @uml.property name="warningCount"
	 */
	public int getWarningCount() {
		return this.warningCount;
	}

}
