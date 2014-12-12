package org.openforis.collect.web.controller.datacleansing;

import org.openforis.collect.web.datacleansing.form.DataErrorQueryForm;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/datacleansing/dataerrorquery")
public class DataErrorQueryController {
	
	@RequestMapping(value = "/save.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response save(@Validated DataErrorQueryForm form, BindingResult result) {
	}

}
