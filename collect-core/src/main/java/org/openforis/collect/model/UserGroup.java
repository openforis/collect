package org.openforis.collect.model;

import java.util.Date;
import java.util.HashSet;
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
	
	public enum UserGroupJoinRequestStatus {
		ACCEPTED('A'), REJECTED('R'), PENDING('P');
		
		private char code;
		
		UserGroupJoinRequestStatus(char code) {
			this.code = code;
		}
		
		public static UserGroupJoinRequestStatus fromCode(String code) {
			return fromCode(code.charAt(0));
		}
		
		public static UserGroupJoinRequestStatus fromCode(char code) {
			for (UserGroupJoinRequestStatus value : values()) {
				if (value.code == code) {
					return value;
				}
			}
			throw new IllegalArgumentException("Invalid UserGrupJoinRequestStatus code: " + code);
		}
		
		public char getCode() {
			return code;
		}
	}
	
	public enum UserGroupRole {
		OWNER('O'), 
		ADMINISTRATOR('A'), 
		DATA_ANALYZER('D'), 
		OPERATOR('U'), 
		VIEWER('V');
		
		private char code;
		
		UserGroupRole(char code) {
			this.code = code;
		}
		
		public static UserGroupRole fromCode(String code) {
			return fromCode(code.charAt(0));
		}
		
		public static UserGroupRole fromCode(char code) {
			for (UserGroupRole value : values()) {
				if (value.code == code) {
					return value;
				}
			}
			throw new IllegalArgumentException("Invalid UserGroupRole code: " + code);
		}
		
		public char getCode() {
			return code;
		}
	}
	
	private User createdByUser;

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
		String code = getVisibilityCode();
		return StringUtils.isBlank(code) ? null : Visibility.fromCode(code.charAt(0));
	}

	public void setVisibility(Visibility visibility) {
		setVisibilityCode(visibility == null ? null : String.valueOf(visibility.getCode()));
	}
	
	private Set<UserInGroup> users = new HashSet<UserInGroup>();
	
	public Set<UserInGroup> getUsers() {
		return users;
	}
	
	public void setUsers(Set<UserInGroup> users) {
		this.users = users;
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

	public static class UserInGroup {
		
		private UserGroup group;
		private User user;
		private UserGroupRole role;
		private UserGroupJoinRequestStatus joinStatus;
		private Date memberSince;
		private Date requestDate;
		
		public UserInGroup() {
		}

		public UserGroup getGroup() {
			return group;
		}
		
		public void setGroup(UserGroup group) {
			this.group = group;
		}
		
		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

		public UserGroupRole getRole() {
			return role;
		}

		public void setRole(UserGroupRole role) {
			this.role = role;
		}
		
		public UserGroupJoinRequestStatus getJoinStatus() {
			return joinStatus;
		}
		
		public void setJoinStatus(UserGroupJoinRequestStatus joinStatus) {
			this.joinStatus = joinStatus;
		}
		
		public Date getMemberSince() {
			return memberSince;
		}
		
		public void setMemberSince(Date memberSince) {
			this.memberSince = memberSince;
		}
		
		public Date getRequestDate() {
			return requestDate;
		}
		
		public void setRequestDate(Date date) {
			this.requestDate = date;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((user == null) ? 0 : user.hashCode());
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
			UserInGroup other = (UserInGroup) obj;
			if (user == null) {
				if (other.user != null)
					return false;
			} else if (!user.equals(other.user))
				return false;
			return true;
		}

	}
}