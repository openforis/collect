package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Tables.OFC_USER;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER_ROLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
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
		Record userAccountRecord = factory.selectFrom(OFC_USER).where(OFC_USER.USERNAME.equal(username)).fetchOne();
		if (userAccountRecord == null) {
			throw new UsernameNotFoundException(username);
		} else {
			Integer userId = userAccountRecord.getValueAsInteger(OFC_USER.ID);

			Result<?> result = factory.selectFrom(OFC_USER_ROLE).where(OFC_USER_ROLE.USER_ID.equal(userId)).fetch();
			Collection<GrantedAuthority> authorities = getAuthorities(result);
			boolean accountNonLocked = Boolean.TRUE;
			boolean credentialsNonExpired = Boolean.TRUE;
			boolean accountNonExpired = Boolean.TRUE;
			boolean enabled = "Y".equals(userAccountRecord.getValueAsString(OFC_USER.ENABLED)) ? Boolean.TRUE : Boolean.FALSE;
			String password = userAccountRecord.getValueAsString(OFC_USER.PASSWORD);
			org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(username, password, enabled, accountNonExpired, credentialsNonExpired,
					accountNonLocked, authorities);
			return user;
		}
	}

	@Transactional
	public int getUserId(String username) {
		Factory jooqFactory = getJooqFactory();
		Record record = jooqFactory.select(OFC_USER.ID).from(OFC_USER).where(OFC_USER.USERNAME.equal(username)).fetchOne();
		Integer id = record.getValueAsInteger(OFC_USER.ID);
		return id;
	}

	private Collection<GrantedAuthority> getAuthorities(Result<?> result) {
		List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>(2);
		for (Record userRoleRecord : result) {
			String role = userRoleRecord.getValueAsString(OFC_USER_ROLE.ROLE);
			authList.add(new SimpleGrantedAuthority(role));
		}
		return authList;
	}

}
