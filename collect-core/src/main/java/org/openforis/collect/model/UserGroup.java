package org.openforis.collect.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.persistence.jooq.tables.pojos.OfcUsergroup;
import org.openforis.idm.metamodel.PersistedObject;

public class UserGroup extends OfcUsergroup implements PersistedObject {

	private static final long serialVersionUID = 1L;

	public enum Visibility {
		PUBLIC('P'), PRIVATE('N');
		
		private char code;
		
		Visibility(char code) {
			this.code = code;
		}
		
		public static Visibility fromCode(char code) {
			for (Visibility value : values()) {
				if (value.code == code) {
					return value;
				}
			}
			throw new IllegalArgumentException("Invalid User Group Visibility code: " + code);
		}
		
		public char getCode() {
			return code;
		}
	}
	
	private User createdByUser;
	private Visibility visibility;
	private Set<UserInGroup> users = new HashSet<UserInGroup>();
	private Set<Integer> childrenGroupIds = new HashSet<Integer>();
	
	@SuppressWarnings("serial")
	public Map<String, String> getQualifiersByName() {
		if (StringUtils.isBlank(getQualifierName())) {
			return Collections.emptyMap();
		} else {
			return new HashMap<String, String>(){{
				put(getQualifierName(), getQualifierValue());
			}};
		}
	}
	
	public User getCreatedByUser() {
		return createdByUser;
	}

	public void setCreatedByUser(User createdByUser) {
		this.createdByUser = createdByUser;
		if (createdByUser != null) {
			this.setCreatedBy(createdByUser.getId());
		}
	}

	public Visibility getVisibility() {
		return visibility;
	}
	
	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
		setInternalVisibilityCode(visibility == null ? null : String.valueOf(visibility.getCode()));
	}
	
	@Override
	public void setVisibilityCode(java.lang.String visibilityCode) {
		setInternalVisibilityCode(visibilityCode);
		this.visibility = StringUtils.isBlank(visibilityCode) ? null : Visibility.fromCode(visibilityCode.charAt(0));
	}
	
	protected void setInternalVisibilityCode(String visibilityCode) {
		super.setVisibilityCode(visibilityCode);
	}

	public String getQualifierName() {
		return getQualifier1Name();
	}
	
	public void setQualifierName(String qualifierName) {
		setQualifier1Name(qualifierName);
	}
	
	public String getQualifierValue() {
		return getQualifier1Value();
	}
	
	public void setQualifierValue(String qualifierValue) {
		setQualifier1Value(qualifierValue);
	}
	
	public Set<UserInGroup> getUsers() {
		return users;
	}
	
	public void setUsers(Set<UserInGroup> users) {
		this.users = users;
	}
	
	public Set<Integer> getChildrenGroupIds() {
		return childrenGroupIds;
	}
	
	public void setChildrenGroupIds(Set<Integer> childrenGroupIds) {
		this.childrenGroupIds = childrenGroupIds;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserGroup other = (UserGroup) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
}