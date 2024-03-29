/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables.pojos;


import java.io.Serializable;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcUserRole implements Serializable {

	private static final long serialVersionUID = 229717087;

	private Integer id;
	private Integer userId;
	private String  role;

	public OfcUserRole() {}

	public OfcUserRole(OfcUserRole value) {
		this.id = value.id;
		this.userId = value.userId;
		this.role = value.role;
	}

	public OfcUserRole(
		Integer id,
		Integer userId,
		String  role
	) {
		this.id = id;
		this.userId = userId;
		this.role = role;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getRole() {
		return this.role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
