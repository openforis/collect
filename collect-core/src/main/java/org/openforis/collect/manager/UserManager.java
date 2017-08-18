package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;

public interface UserManager {

	static final String ADMIN_USER_NAME = "admin";
	static final String ADMIN_DEFAULT_PASSWORD = "admin";
	static final String PASSWORD_PATTERN = "^\\w{5,}$"; // alphanumeric, at least 5 letters

	User loadById(int userId);

	User loadByUserName(String userName);

	User loadEnabledUser(String userName);

	User loadAdminUser();

	List<User> loadAll();
	
	List<User> loadAllAvailableUsers(User availableTo);

	void save(User user) throws UserPersistenceException;
	
	User insertUser(String name, String password, UserRole role) throws UserPersistenceException;

	OperationResult changePassword(String username, String oldPassword, String newPassword) throws UserPersistenceException;

	Boolean isDefaultAdminPasswordSet();

	void delete(int id) throws CannotDeleteUserException;

}