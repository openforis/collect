/**
 * 
 */
package org.openforis.collect.web.controller;

import org.openforis.collect.CollectInfo;
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

	@RequestMapping(value = "/info.json", method = RequestMethod.GET)
	public @ResponseBody CollectInfo info() {
		return CollectInfo.getInstance();
	}
	
}
