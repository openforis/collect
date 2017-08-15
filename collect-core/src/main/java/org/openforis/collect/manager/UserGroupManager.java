package org.openforis.collect.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserGroup.UserGrupJoinRequestStatus;
import org.openforis.collect.model.UserGroup.Visibility;
import org.openforis.collect.persistence.UserGroupDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class UserGroupManager {

	private static final String DEFAULT_PUBLIC_USER_GROUP_NAME = "default_public_group";
	private static final String DEFAULT_PRIVATE_USER_GROUP_SUFFIX = "_default_private_group";
	private static final String DEFAULT_PRIVATE_USER_GROUP_LABEL_SUFFIX = " Default Private Group";
	
	private static UserGroup DEFAULT_PUBLIC_USER_GROUP = null;
	
	@Autowired
	private UserGroupDao dao;

	@Transactional(propagation=Propagation.REQUIRED)
	public UserGroup createDefaultPrivateUserGroup(User user) {
		UserGroup userGroup = new UserGroup();
		userGroup.setName(getDefaultPrivateUserGroupName(user));
		userGroup.setLabel(user.getName() + DEFAULT_PRIVATE_USER_GROUP_LABEL_SUFFIX);
		userGroup.setVisibility(Visibility.PRIVATE);
		dao.insert(userGroup);
		insertRelation(user, userGroup, UserGrupJoinRequestStatus.ACCEPTED, new Date());
		return userGroup;
	}

	public String getDefaultPrivateUserGroupName(User user) {
		return user.getName() + DEFAULT_PRIVATE_USER_GROUP_SUFFIX;
	}
	
	public String getDefaultPublicUserGroupName() {
		return DEFAULT_PUBLIC_USER_GROUP_NAME;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void save(UserGroup userGroup) {
		dao.save(userGroup);
	}
	
	public List<UserGroup> loadAll() {
		return dao.loadAll();
	}

	public UserGroup loadByName(String userGroupName) {
		return dao.loadByName(userGroupName);
	}

	public UserGroup loadById(int id) {
		return dao.loadById(id);
	}

	public UserGroup getDefaultPublicUserGroup() {
		if (DEFAULT_PUBLIC_USER_GROUP == null) {
			DEFAULT_PUBLIC_USER_GROUP = dao.loadByName(DEFAULT_PUBLIC_USER_GROUP_NAME);
		}
		return DEFAULT_PUBLIC_USER_GROUP;
	}
	
	public Collection<User> findUsersByGroup(UserGroup userGroup) {
		return dao.findUsersByGroup(userGroup);
	}
	
	public List<UserGroup> findByUser(User user) {
		List<UserRole> userRoles = user.getRoles();
		if (userRoles.contains(UserRole.ADMIN)) {
			return dao.loadAll();
		} else {
			return dao.findGroupByUser(user);
		}
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void requestJoin(User user, UserGroup userGroup) {
		insertRelation(user, userGroup, UserGrupJoinRequestStatus.PENDING, null);
	}

	private void insertRelation(User user, UserGroup userGroup, UserGrupJoinRequestStatus joinStatus, Date memberSince) {
		dao.insertRelation(user, userGroup, joinStatus, memberSince);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void acceptJoinRequest(User user, UserGroup userGroup) {
		dao.acceptJoinRequest(user, userGroup);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void delete(int id) {
		dao.deleteById(id);
	}

}
