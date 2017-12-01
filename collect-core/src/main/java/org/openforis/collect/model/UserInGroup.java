package org.openforis.collect.model;

import java.util.Date;

public class UserInGroup {
	
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
		SUPERVISOR('S'),
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
	
	private Integer groupId;
	private Integer userId;
	private UserGroupRole role;
	private UserGroupJoinRequestStatus joinStatus;
	private Date memberSince;
	private Date requestDate;
	
	public UserInGroup() {
	}

	public Integer getGroupId() {
		return groupId;
	}
	
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
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
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
}