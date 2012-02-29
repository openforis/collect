package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.tables.OfcUserAccount.USER_ACCOUNT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.UserRole;
import org.openforis.collect.persistence.jooq.tables.records.UserAccountRecord;
import org.openforis.collect.persistence.jooq.tables.records.UserRoleRecord;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author M. Togna
 * 
 */
public class UserDAO extends JooqDaoSupport implements UserDetailsService {

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Factory factory = getJooqFactory();
		UserAccountRecord userAccountRecord = factory.selectFrom(USER_ACCOUNT).where(USER_ACCOUNT.USERNAME.equal(username)).fetchOne();
		if (userAccountRecord == null) {
			throw new UsernameNotFoundException(username);
		} else {
			Integer userId = userAccountRecord.getId();

			Result<UserRoleRecord> result = factory.selectFrom(UserRole.USER_ROLE).where(UserRole.USER_ROLE.USER_ID.equal(userId)).fetch();
			Collection<GrantedAuthority> authorities = getAuthorities(result);
			boolean accountNonLocked = Boolean.TRUE;
			boolean credentialsNonExpired = Boolean.TRUE;
			boolean accountNonExpired = Boolean.TRUE;
			boolean enabled = userAccountRecord.getValueAsString(USER_ACCOUNT.ENABLED).equals("Y") ? Boolean.TRUE : Boolean.FALSE;
			String password = userAccountRecord.getPassword();
			org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(username, password, enabled, accountNonExpired, credentialsNonExpired,
					accountNonLocked, authorities);
			return user;
		}
	}

	@Transactional
	public int getUserId(String username) {
		Factory jooqFactory = getJooqFactory();
		Record record = jooqFactory.select(USER_ACCOUNT.ID).from(USER_ACCOUNT).where(USER_ACCOUNT.USERNAME.equal(username)).fetchOne();
		Integer id = record.getValueAsInteger(USER_ACCOUNT.ID);
		return id;
	}

	private Collection<GrantedAuthority> getAuthorities(Result<UserRoleRecord> result) {
		List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>(2);
		for (UserRoleRecord userRoleRecord : result) {
			String role = userRoleRecord.getValueAsString(UserRole.USER_ROLE.ROLE);
			authList.add(new SimpleGrantedAuthority(role));
		}
		return authList;
	}

}
