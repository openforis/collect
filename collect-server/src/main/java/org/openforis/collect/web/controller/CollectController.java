/**
 * 
 */
package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import javax.servlet.http.HttpServletRequest;

import org.openforis.collect.CollectCompleteInfo;
import org.openforis.collect.CollectInfo;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.remoting.service.CollectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping("api/")
public class CollectController extends BasicController {

	@Autowired
	private UserManager userManager;
	@Autowired
	private CollectInfoService infoService;
	
	@RequestMapping(value = "defaultpasswordactive", method=GET)
	public @ResponseBody Boolean isDefaultUserActive() {
		return userManager.isDefaultAdminPasswordSet(); 
	}
	
	@RequestMapping(value = "info", method=GET)
	public @ResponseBody CollectInfo info() {
		return infoService.getInfo();
	}
	
	@RequestMapping(value = "completeinfo", method=GET)
	public @ResponseBody CollectCompleteInfo completeInfo(HttpServletRequest request) {
		return infoService.getCompleteInfo(request);
	}
}
