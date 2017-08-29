package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.List;

import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.model.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/usergroup")
public class UserGroupController {

	@Autowired
	private UserGroupManager userGroupManager;

	@RequestMapping(method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody List<UserGroup> loadPublicInstitutions() {
		return userGroupManager.findPublicUserGroups();
	}
	
	@RequestMapping(method=POST, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody UserGroup insertInstitution(@RequestBody UserGroup institution) {
		return userGroupManager.save(institution);
	}
	
	@RequestMapping(value = "/{id}", method=DELETE, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody void deleteInstitution(@PathVariable Long id) {
		userGroupManager.delete(id);
	}
	
}
