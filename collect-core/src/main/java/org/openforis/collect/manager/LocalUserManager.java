/**
 * 
 */
package org.openforis.collect.manager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.User;
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
public class LocalUserManager implements UserManager {

	@Autowired
	private UserDao userDao;
	@Autowired
	private RecordDao recordDao;
	
	//cache
	private Map<Integer, User> userById = new TreeMap<Integer, User>();
	private Map<String, User> userByName = new TreeMap<String, User>();
	
	public User loadById(int userId) {
		User user = userById.get(userId);
		if (user == null) {
			user = userDao.loadById(userId);
			updateCache(user);
		}
		return user;
	}

	public User loadByUserName(String userName) {
		return loadByUserName(userName, null);
	}
	
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
	
	public User loadAdminUser() {
		return loadByUserName(ADMIN_USER_NAME);
	}
	
	public List<User> loadAll() {
		return userDao.loadAll();
	}

	@Transactional
	public void save(User user) throws UserPersistenceException {
		Integer userId = user.getId();
		String password = user.getPassword();
		if (StringUtils.isBlank(password)) {
			if (userId != null) {
				// preserve old password
				User oldUser = userDao.loadById(userId);
				user.setPassword(oldUser.getPassword());
			}
		} else {
			String encodedPassword = checkAndEncodePassword(password);
			user.setPassword(encodedPassword);
		}
		if (userId == null) {
			userDao.insert(user);
		} else {
			userDao.update(user);
		}
		updateCache(user);
	}
	
	@Transactional
	public OperationResult changePassword(String username, String oldPassword, String newPassword) throws UserPersistenceException {
		User user = userDao.loadByUserName(username, true);
		String encodedOldPassword = encodePassword(oldPassword);
		if ( user.getPassword().equals(encodedOldPassword) ) {
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
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			byte[] bytes = password.getBytes();
			byte[] digest = messageDigest.digest(bytes);
			char[] resultChar = Hex.encodeHex(digest);
			return new String(resultChar);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Error encoding user password", e);
		}
	}
	
	public Boolean isDefaultAdminPasswordSet() {
		User adminUser = loadAdminUser();
		String encodedDefaultPassword = encodePassword(ADMIN_DEFAULT_PASSWORD);
		return encodedDefaultPassword.equals(adminUser.getPassword());
	}

	@Transactional
	public void delete(int id) throws CannotDeleteUserException {
		if ( recordDao.hasAssociatedRecords(id) ) {
			throw new CannotDeleteUserException();
		}
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
	@Transactional
	public User insertUser(String name, String password, UserRole role) throws UserPersistenceException {
		User user = new User(name);
		user.setPassword(password);
		user.addRole(role);
		save(user);
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
