package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.List;

import org.openforis.collect.manager.InstitutionManager;
import org.openforis.collect.model.Institution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/institution/")
public class InstitutionController {

	@Autowired
	private InstitutionManager institutionManager;

	@RequestMapping(value = "summaries.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody List<Institution> loadPublicInstitutions() {
		return institutionManager.findPublicInstitutions();
	}
	
	@RequestMapping(value = "add.json", method=POST, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody Institution insertInstitution(@RequestBody Institution institution) {
		return institutionManager.save(institution);
	}
	
	@RequestMapping(value = "/{id}/delete.json", method=DELETE, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody void deleteInstitution(@PathVariable Long id) {
		institutionManager.delete(id);
	}
	
}
