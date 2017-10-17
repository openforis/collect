package org.openforis.collect.web.controller;

import org.openforis.collect.manager.ImageryManager;
import org.openforis.collect.model.Imagery;
import org.openforis.collect.web.controller.ImageryController.ImageryForm;
import org.openforis.commons.web.PersistedObjectForm;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/imagery")
public class ImageryController extends AbstractPersistedObjectEditFormController<Imagery, ImageryForm, ImageryManager> {
	
	@Override
	protected Imagery createItemInstance() {
		return new Imagery();
	}

	@Override
	protected ImageryForm createFormInstance(Imagery item) {
		return new ImageryForm();
	}

	public static class ImageryForm extends PersistedObjectForm<Imagery> {

		private String  title;
		private String  attribution;
		private String  extent;
		private String  sourceConfig;

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

