package org.openforis.collect.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author S. Ricci
 */
@Controller
public class LoginController {
	
	@RequestMapping("/login")
	public String login() {
		return "login";
	}
}