package org.openforis.collect.metamodel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.util.IOUtils;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.ui.UIField;
import org.openforis.collect.metamodel.ui.UIForm;
import org.openforis.collect.metamodel.ui.UIFormComponent;
import org.openforis.collect.metamodel.ui.UIFormContentContainer;
import org.openforis.collect.metamodel.ui.UIFormSection;
import org.openforis.collect.metamodel.ui.UIFormSet;
import org.openforis.collect.metamodel.ui.UITable;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFormPrinter {
	
	public void print(CollectSurvey survey, OutputStream out) throws IOException {
		print(survey, survey.getDefaultLanguage(), out);
	}

	public void print(CollectSurvey survey, final String language, OutputStream out) throws IOException {
		SurveyFormRenderer renderer = new SurveyFormRenderer(survey, language, out);
		renderer.render();
	}

	private String getLabelOrName(NodeDefinition def, String language) {
		String label = def.getLabel(Type.INSTANCE, language);
		if (label == null) {
			return def.getName();
		} else {
			return label;
		}
	}

	private class SurveyFormRenderer extends UIObjectPaperFormRenderer {
		
		private CollectSurvey survey;
		private String language;
		private OutputStream out;
		protected XWPFDocument doc;
		
		public SurveyFormRenderer(CollectSurvey survey, String language, OutputStream out) {
			super(null);
			this.survey = survey;
			this.language = language;
			this.out = out;
		}
		
		@Override
		public void render() throws IOException {
			try {
				doc = new XWPFDocument();
				UIConfiguration uiConfiguration = survey.getUIConfiguration();
				List<UIFormSet> formSets = uiConfiguration.getFormSets();
				for (UIFormSet formSet : formSets) {
					FormSetPaperFormRenderer formSetPaperFormRenderer = new FormSetPaperFormRenderer(this, formSet);
					formSetPaperFormRenderer.render();
				}
				doc.write(out);
			} finally {
				IOUtils.closeQuietly(doc);
			}
		}

	}
	
	private abstract class UIObjectPaperFormRenderer {
		
		protected UIObjectPaperFormRenderer parentRenderer;
		
		public UIObjectPaperFormRenderer(UIObjectPaperFormRenderer parentRenderer) {
			super();
			this.parentRenderer = parentRenderer;
		}

		public void render() throws IOException {
		}
		
		protected SurveyFormRenderer getRootRenderer() {
			UIObjectPaperFormRenderer currentRenderer = this;
			while (currentRenderer.parentRenderer != null) {
				currentRenderer = currentRenderer.parentRenderer;
			}
			return (SurveyFormRenderer) currentRenderer;
		}
		
		protected void addPageBreak() {
			getRootRenderer().doc.createParagraph().createRun().addBreak(BreakType.PAGE);
		}
		
	}
	
	private class FieldPaperFormRenderer extends UIObjectPaperFormRenderer {
		
		private UIField field;

		FieldPaperFormRenderer(UIFormContentPaperFormRenderer parentRenderer, UIField field) {
			super(parentRenderer);
			this.field = field;
		}
		
		@Override
		public void render() throws IOException {
			super.render();
			XWPFTableRow row = ((UIFormContentPaperFormRenderer) parentRenderer).createRow();
			XWPFTableCell labelCell = row.getCell(0);
			AttributeDefinition def = field.getAttributeDefinition();
			labelCell.setText(getLabelOrName(def, getRootRenderer().survey.getDefaultLanguage()));
//			XWPFTableCell contentCell = row.getCell(1);
		}
		
	}
	
	private class TablePaperFormRenderer extends UIObjectPaperFormRenderer {
		
		private UITable table;

		TablePaperFormRenderer(UIFormContentPaperFormRenderer parentRenderer, UITable table) {
			super(parentRenderer);
			this.table = table;
		}
		
		@Override
		public void render() throws IOException {
			super.render();
			XWPFTableRow row = ((UIFormContentPaperFormRenderer) parentRenderer).createRow();
			XWPFTableCell labelCell = row.getCell(0);
			EntityDefinition entityDef = table.getEntityDefinition();
			labelCell.setText(getLabelOrName(entityDef, getRootRenderer().survey.getDefaultLanguage()));
//			XWPFTableCell contentCell = row.getCell(1);
		}
		
	}
	
	private class FormSetPaperFormRenderer extends UIFormContentPaperFormRenderer {
		
		private UIFormSet formSet;

		public FormSetPaperFormRenderer(UIObjectPaperFormRenderer parentRenderer, UIFormSet formSet) {
			super(parentRenderer, formSet);
			this.formSet = formSet;
		}
		
	}
	
	private class UIFormContentPaperFormRenderer extends UIObjectPaperFormRenderer {

		private UIFormContentContainer container;
		protected XWPFTable table;
		
		public UIFormContentPaperFormRenderer(UIObjectPaperFormRenderer parentRenderer, UIFormContentContainer container) {
			super(parentRenderer);
			this.container = container;
		}
		
		XWPFTableRow createRow() {
			return table.createRow();
		}

		@Override
		public void render() throws IOException {
			super.render();
			table = getRootRenderer().doc.createTable();
			List<UIFormComponent> children = container.getChildren();
			for (UIFormComponent component : children) {
				if (component instanceof UIField) {
					FieldPaperFormRenderer childRenderer = new FieldPaperFormRenderer(this, (UIField) component);
					childRenderer.render();
				} else if (component instanceof UIFormSection) {
					UIFormSectionPaperFormRenderer formSectionRenderer = new UIFormSectionPaperFormRenderer(this, (UIFormSection) component);
					formSectionRenderer.render();
				}
			}
			List<UIForm> forms = container.getForms();
			for (UIForm uiForm : forms) {
				addPageBreak();
				UIFormPaperFormRenderer formRenderer = new UIFormPaperFormRenderer(this, uiForm);
				formRenderer.render();;
			}
		}

	}
	
	private class UIFormPaperFormRenderer extends UIFormContentPaperFormRenderer {
		
		private UIForm form;

		public UIFormPaperFormRenderer(UIObjectPaperFormRenderer parentRenderer, UIForm form) {
			super(parentRenderer, form);
			this.form = form;
		}
		
		@Override
		public void render() throws IOException {
			XWPFParagraph header = getRootRenderer().doc.createParagraph();
			XWPFRun run = header.createRun();
			run.setText(form.getLabel(getRootRenderer().language));
			super.render();
		}
		
	}
	
	private class UIFormSectionPaperFormRenderer extends UIFormContentPaperFormRenderer {
		
		private UIFormSection formSection;

		public UIFormSectionPaperFormRenderer(UIObjectPaperFormRenderer parentRenderer, UIFormSection formSection) {
			super(parentRenderer, formSection);
			this.formSection = formSection;
		}
		
		@Override
		public void render() throws IOException {
			XWPFTable table = parentRenderer.getRootRenderer().doc.createTable();
			XWPFTableRow row = table.createRow();
			XWPFTableCell cell = row.getCell(0);
			XWPFParagraph header = cell.addParagraph();
			XWPFRun run = header.createRun();
			run.setText(getNullSafeLabel(formSection.getEntityDefinition()));
			super.render();
		}

		private String getNullSafeLabel(EntityDefinition entityDef) {
			String label = entityDef.getLabel(Type.INSTANCE, getRootRenderer().language);
			if (label == null) {
				return entityDef.getName();
			} else {
				return label;
			}
		}
		
	}
}
