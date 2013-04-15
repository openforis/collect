/**
 * 
 */
package org.openforis.collect.manager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class UserManager {

	protected static final String PASSWORD_PATTERN = "^\\w{5,}$"; // alphanumeric, at least 5 letters

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private RecordDao recordDao;
	

	@Transactional
	public int getUserId(String username) {
		return userDao.getUserId(username);
	}

	@Transactional
	public User loadById(int userId) {
		return userDao.loadById(userId);
	}

	@Transactional
	public User loadByUserName(String userName, Boolean enabled) {
		return userDao.loadByUserName(userName, enabled);
	}

	public User loadByUserName(String userName) {
		return userDao.loadByUserName(userName, null);
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
			String encodedPassword = encodePassword(password);
			user.setPassword(encodedPassword);
		}
		if (userId == null) {
			userDao.insert(user);
		} else {
			userDao.update(user);
		}
	}

	protected String encodePassword(String password) throws UserPersistenceException {
		boolean matchesPattern = Pattern.matches(PASSWORD_PATTERN, password);
		if (matchesPattern) {
			MessageDigest messageDigest;
			try {
				messageDigest = MessageDigest.getInstance("MD5");
				byte[] bytes = password.getBytes();
				byte[] digest = messageDigest.digest(bytes);
				char[] resultChar = Hex.encodeHex(digest);
				return new String(resultChar);
			} catch (NoSuchAlgorithmException e) {
				throw new UserPersistenceException("Error encoding user password");
			}
		} else {
			throw new InvalidUserPasswordException();
		}
	}

	@Transactional
	public void insert(User user) {
		userDao.insert(user);
	}

	@Transactional
	public void delete(int id) throws CannotDeleteUserException {
		if ( recordDao.hasAssociatedRecords(id) ) {
			throw new CannotDeleteUserException();
		}
		userDao.delete(id);
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
