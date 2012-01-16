package org.openforis.collect.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.collect.persistence.jooq.tables.User;
import org.openforis.collect.persistence.jooq.tables.UserRole;
import org.openforis.collect.persistence.jooq.tables.records.UserRecord;
import org.openforis.collect.persistence.jooq.tables.records.UserRoleRecord;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author M. Togna
 * 
 */
public class UserDAO extends CollectDAO implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Factory factory = getJooqFactory();
		UserRecord userRecord = factory.selectFrom(User.USER).where(User.USER.USERNAME.equal(username)).fetchOne();
		if (userRecord == null) {
			throw new UsernameNotFoundException(username);
		} else {
			Integer userId = userRecord.getId();

			Result<UserRoleRecord> result = factory.selectFrom(UserRole.USER_ROLE).where(UserRole.USER_ROLE.USER_ID.equal(userId)).fetch();
			Collection<GrantedAuthority> authorities = getAuthorities(result);
			boolean accountNonLocked = Boolean.TRUE;
			boolean credentialsNonExpired = Boolean.TRUE;
			boolean accountNonExpired = Boolean.TRUE;
			boolean enabled = userRecord.getValueAsString(User.USER.ENABLED).equals("Y") ? Boolean.TRUE : Boolean.FALSE;
			String password = userRecord.getPassword();
			org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(username, password, enabled, accountNonExpired, credentialsNonExpired,
					accountNonLocked, authorities);
			return user;
		}
	}

	public int getUserId(String username) {
		Factory jooqFactory = getJooqFactory();
		Record record = jooqFactory.select(User.USER.ID).from(User.USER).where(User.USER.USERNAME.equal(username)).fetchOne();
		Integer id = record.getValueAsInteger(User.USER.ID);
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
