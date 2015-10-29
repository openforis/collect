/**
 * 
 */
package org.openforis.collect.web.controller;

import org.openforis.collect.Collect;
import org.openforis.collect.CollectInfo;
import org.openforis.collect.manager.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author S. Ricci
 *
 */
@Controller
public class CollectController extends BasicController {

	@Autowired
	private UserManager userManager;
	
	@RequestMapping(value = "/default-password-active.json", method = RequestMethod.GET)
	public @ResponseBody Boolean isDefaultUserActive() {
		return userManager.isDefaultAdminPasswordSet(); 
	}
	
	
	@RequestMapping(value = "/info.json", method = RequestMethod.GET)
	public @ResponseBody CollectInfo info() {
		return new CollectInfo(Collect.VERSION);
	}
}
