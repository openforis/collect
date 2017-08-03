package org.openforis.collect.datacleansing.controller;

import java.util.List;

import org.openforis.collect.datacleansing.form.DataCleansingStepValueForm;
import org.openforis.collect.datacleansing.form.validation.DataCleansingStepValueValidator;
import org.openforis.commons.web.AbstractFormUpdateValidationResponse;
import org.openforis.commons.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/datacleansing/datacleansingstepvalues")
public class DataCleansingStepValueController {
	
	@Autowired
	private DataCleansingStepValueValidator dataCleansingStepValueValidator;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(dataCleansingStepValueValidator);
	}
	
	@RequestMapping(value="validate.json", method = RequestMethod.POST)
	public @ResponseBody
	Response validate(@Validated DataCleansingStepValueForm form, BindingResult result) {
		List<ObjectError> errors = result.getAllErrors();
		return new SimpleFormUpdateResponse(errors);
	}
	
	private static class SimpleFormUpdateResponse extends AbstractFormUpdateValidationResponse<DataCleansingStepValueForm> {

		public SimpleFormUpdateResponse(DataCleansingStepValueForm form) {
			super(form);
		}

		public SimpleFormUpdateResponse(List<ObjectError> errors) {
			super(errors);
		}
		
	}
}
