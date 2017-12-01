package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;

public interface UserManager extends ItemManager<User, Integer> {

	static final String ADMIN_USER_NAME = "admin";
	static final String ADMIN_DEFAULT_PASSWORD = "admin";
	static final String PASSWORD_PATTERN = "^[a-zA-Z]\\w{3,14}$"; //First character must be a letter, at least 4 characters, no more than 15

	User loadByUserName(String userName);

	User loadEnabledUser(String userName);

	User loadAdminUser();

	List<User> loadAllAvailableUsers(User availableTo);

	User insertUser(String name, String password, UserRole role, User createdByUser) throws UserPersistenceException;

	OperationResult changePassword(String username, String oldPassword, String newPassword) throws UserPersistenceException;

	Boolean isDefaultAdminPasswordSet();

}