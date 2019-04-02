/**
 * 
 */
package org.openforis.collect.manager;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 */
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class LocalUserManager extends AbstractPersistedObjectManager<User, Integer, UserDao> implements UserManager {

	@Autowired
	private UserDao userDao;
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private UserGroupManager groupManager;
	
	//cache
	private Map<Integer, User> userById = new TreeMap<Integer, User>();
	private Map<String, User> userByName = new TreeMap<String, User>();
	
	@Override
	public User loadById(Integer id) {
		User user = userById.get(id);
		if (user == null) {
			user = userDao.loadById(id);
			updateCache(user);
		}
		return user;
	}
	
	@Override
	public User loadByUserName(String userName) {
		return loadByUserName(userName, null);
	}
	
	@Override
	public User loadEnabledUser(String userName) {
		return loadByUserName(userName, true);
	}
	
	private User loadByUserName(String userName, Boolean enabled) {
		User user = userByName.get(userName);
		if (user == null) {
			user = userDao.loadByUserName(userName, null);
			updateCache(user);
		}
		if (user != null && (enabled == null || enabled.equals(user.getEnabled()))) {
			return user;
		} else {
			return null;
		}
	}
	
	@Override
	public User loadAdminUser() {
		return loadByUserName(ADMIN_USER_NAME);
	}
	
	@Override
	public List<User> loadAll() {
		return userDao.loadAll();
	}
	
	@Override
	public List<User> loadAllAvailableUsers(User availableTo) {
		if (availableTo.getRoles().contains(UserRole.ADMIN)) {
			return loadAll();
		} else {
			Set<User> users = new TreeSet<User>();
			List<UserGroup> userGroups = groupManager.findByUser(availableTo);
			for (UserGroup userGroup : userGroups) {
				List<UserInGroup> groupUsers = groupManager.findUsersInGroup(userGroup.getId());
				for (UserInGroup userInGroup : groupUsers) {
					Integer userId = userInGroup.getUserId();
					User user = loadById(userId);
					users.add(user);
				}
			}
			//sorted by username by default (see User.compareTo)
			return new ArrayList<User>(users);
		}
	}

	@Override
	@Transactional(readOnly=false, propagation=REQUIRED)
	public User save(User user, User createdByUser) {
		Integer userId = user.getId();
		String rawPassword = user.getRawPassword();
		if (StringUtils.isBlank(rawPassword)) {
			if (userId == null) {
				throw new IllegalArgumentException("Blank password specified for a new user");
			} else {
				// preserve old password
				User oldUser = userDao.loadById(userId);
				user.setPassword(oldUser.getPassword());
			}
		} else {
			String encodedPassword = checkAndEncodePassword(rawPassword);
			user.setPassword(encodedPassword);
		}
		if (userId == null) {
			userDao.insert(user);
			groupManager.createDefaultPrivateUserGroup(user, createdByUser);
			groupManager.joinToDefaultPublicGroup(user, UserGroupRole.ADMINISTRATOR);
		} else {
			userDao.update(user);
		}
		user.setRawPassword(null); //do not store raw password in cache
		updateCache(user);
		return user;
	}
	
	@Override
	public boolean verifyPassword(String username, String password) {
		User user = userDao.loadByUserName(username, true);
		String encodedPassword = encodePassword(password);
		return user.getPassword().equals(encodedPassword);
	}
	
	@Override
	@Transactional(readOnly=false, propagation=REQUIRED)
	public OperationResult changePassword(String username, String oldPassword, String newPassword) throws UserPersistenceException {
		if (verifyPassword(username, oldPassword)) {
			User user = userDao.loadByUserName(username, true);
			String encodedNewPassword = checkAndEncodePassword(newPassword);
			user.setPassword(encodedNewPassword);
			userDao.update(user);
			updateCache(user);
			return new OperationResult();
		} else {
			return new OperationResult(false, "WRONG_PASSWORD", "Wrong password specified");
		}
	}

	private void updateCache(User user) {
		if (user != null) {
			userById.put(user.getId(), user);
			userByName.put(user.getUsername(), user);
		}
	}

	protected String checkAndEncodePassword(String password) throws UserPersistenceException {
		boolean matchesPattern = Pattern.matches(PASSWORD_PATTERN, password);
		if (matchesPattern) {
			return encodePassword(password);
		} else {
			throw new InvalidUserPasswordException();
		}
	}
	
	protected String encodePassword(String password) {
		return MD5PasswordEncoder.encode(password);
	}
	
	public Boolean isDefaultAdminPasswordSet() {
		User adminUser = loadAdminUser();
		String encodedDefaultPassword = encodePassword(ADMIN_DEFAULT_PASSWORD);
		return encodedDefaultPassword.equals(adminUser.getPassword());
	}

	@Override
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void deleteById(Integer id) throws CannotDeleteUserException {
		if ( recordDao.hasAssociatedRecords(id) ) {
			throw new CannotDeleteUserException();
		}
		User user = userDao.loadById(id);
		groupManager.deleteAllUserRelations(user);
		userDao.delete(id);
		
		User cachedUser = userById.get(id);
		if (cachedUser != null) {
			userById.remove(id);
			userByName.remove(cachedUser.getUsername());
		}
	}
	
	/**
	 * Inserts a new user with name, password and role as specified.
	 * @return 
	 * 
	 * @throws UserPersistenceException 
	 */
	@Override
	@Transactional(readOnly=false, propagation=REQUIRED)
	public User insertUser(String name, String password, UserRole role, User createdByUser) throws UserPersistenceException {
		User user = new User(name);
		user.setRawPassword(password);
		user.addRole(role);
		save(user, createdByUser);
		return user;
	}
	
	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public RecordDao getRecordDao() {
		return recordDao;
	}

	public void setRecordDao(RecordDao recordDao) {
		this.recordDao = recordDao;
	}

}
