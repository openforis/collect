package org.openforis.collect.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class CollectUserDetailsService implements UserDetailsService {

	@Autowired
	private UserManager userManager;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userManager.loadEnabledUser(username);
		if (user == null) {
			throw new UsernameNotFoundException(username);
		} else {
			return createUserDetails(user);
		}
	}

	private UserDetails createUserDetails(User user) {
		String username = user.getUsername();
		boolean accountNonLocked = true;
		boolean credentialsNonExpired = true;
		boolean accountNonExpired = true;
		String password = user.getPassword();
		boolean enabled = true;
		Collection<GrantedAuthority> authorities = getAuthorities(user);
		
		org.springframework.security.core.userdetails.User userDetails = 
				new org.springframework.security.core.userdetails.User(username, password, enabled, 
						accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		return userDetails;
	}

	private Collection<GrantedAuthority> getAuthorities(User user) {
		List<String> roles = user.getRoleCodes();
		List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>(roles.size());
		for (String role : roles) {
			authList.add(new SimpleGrantedAuthority(role));
		}
		return authList;
	}

}
