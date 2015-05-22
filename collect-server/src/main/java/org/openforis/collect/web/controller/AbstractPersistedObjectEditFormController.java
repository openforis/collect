package org.openforis.collect.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.manager.AbstractPersistedObjectManager;
import org.openforis.commons.web.AbstractFormUpdateResponse;
import org.openforis.commons.web.PersistedObjectForm;
import org.openforis.commons.web.Response;
import org.openforis.idm.metamodel.PersistedObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractPersistedObjectEditFormController<T extends PersistedObject, 
											F extends PersistedObjectForm<T>, 
											M extends AbstractPersistedObjectManager<T, ?>> {
	
	protected M itemManager;
	
	protected abstract T createItemInstance();
	protected abstract F createFormInstance(T item);
	
	@RequestMapping(value="list.json", method = RequestMethod.GET)
	public @ResponseBody
	List<F> loadAll() {
		List<T> items = itemManager.loadAll();
		List<F> forms = new ArrayList<F>(items.size());
		for (T item : items) {
			forms.add(createFormInstance(item));
		}
		return forms;
	}
	
	@RequestMapping(value = "/{id}.json", method = RequestMethod.GET)
	public @ResponseBody
	F load(@PathVariable int id) {
		T item = itemManager.loadById(id);
		F form = createFormInstance(item);
		return form;
	}
	
	@RequestMapping(value="save.json", method = RequestMethod.POST)
	public @ResponseBody
	Response save(@Validated F form, BindingResult result) {
		List<ObjectError> errors = result.getAllErrors();
		SimpleFormUpdateResponse response;
		if (errors.isEmpty()) {
			T item;
			if (form.getId() == null) {
				item = createItemInstance();
			} else {
				item = itemManager.loadById(form.getId());
			}
			form.copyTo(item);
			itemManager.save(item);
			F responseForm = createFormInstance(item);
			response = new SimpleFormUpdateResponse(responseForm);
		} else {
			response = new SimpleFormUpdateResponse(errors);
		}
		return response;
	}

	@RequestMapping(value="validate.json", method = RequestMethod.POST)
	public @ResponseBody
	Response validate(@Validated F form, BindingResult result) {
		List<ObjectError> errors = result.getAllErrors();
		Response response = new SimpleFormUpdateResponse(errors);
		return response;
	}
	
	@RequestMapping(value = "{id}.json", method = RequestMethod.DELETE)
	public @ResponseBody
	Response delete(@PathVariable int id) {
		itemManager.delete(id);
		return new Response();
	}
	
	public void setItemManager(M itemManager) {
		this.itemManager = itemManager;
	}

	public class SimpleFormUpdateResponse extends AbstractFormUpdateResponse<F> {

		public SimpleFormUpdateResponse(F form) {
			super(form);
		}

		public SimpleFormUpdateResponse(List<ObjectError> errors) {
			super(errors);
		}
		
	}

}
