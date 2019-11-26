package org.openforis.collect.web.controller;

import org.openforis.collect.manager.ImageryManager;
import org.openforis.collect.model.Imagery;
import org.openforis.collect.web.controller.ImageryController.ImageryForm;
import org.openforis.collect.web.validator.ImageryValidator;
import org.openforis.commons.web.PersistedObjectForm;
import org.openforis.commons.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/imagery")
public class ImageryController extends AbstractPersistedObjectEditFormController<Integer, Imagery, ImageryForm, ImageryManager> {
	
	@Autowired
	private ImageryValidator validator;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}
	
	@Override
	protected Imagery createItemInstance() {
		return new Imagery();
	}

	@Override
	protected ImageryForm createFormInstance(Imagery item) {
		return new ImageryForm(item);
	}
	
	@Override
	public Response insert(@RequestBody ImageryForm form, BindingResult result) {
		return super.insert(form, result);
	}
	
	@Override
	public Response update(@RequestBody ImageryForm form, BindingResult result) {
		return super.update(form, result);
	}

	public static class ImageryForm extends PersistedObjectForm<Integer, Imagery> {

		private String title;
		private String attribution;
		private String extent;
		private String sourceConfig;

		public ImageryForm() {}
		
		public ImageryForm(Imagery imagery) {
			super(imagery);
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getAttribution() {
			return attribution;
		}

		public void setAttribution(String attribution) {
			this.attribution = attribution;
		}

		public String getExtent() {
			return extent;
		}

		public void setExtent(String extent) {
			this.extent = extent;
		}

		public String getSourceConfig() {
			return sourceConfig;
		}

		public void setSourceConfig(String sourceConfig) {
			this.sourceConfig = sourceConfig;
		}
	}

}

