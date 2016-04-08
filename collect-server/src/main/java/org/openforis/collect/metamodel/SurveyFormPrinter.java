package org.openforis.collect.metamodel;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;

import org.apache.poi.util.IOUtils;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.metamodel.ui.UIColumn;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.ui.UIField;
import org.openforis.collect.metamodel.ui.UIForm;
import org.openforis.collect.metamodel.ui.UIFormComponent;
import org.openforis.collect.metamodel.ui.UIFormContentContainer;
import org.openforis.collect.metamodel.ui.UIFormSection;
import org.openforis.collect.metamodel.ui.UIFormSet;
import org.openforis.collect.metamodel.ui.UITable;
import org.openforis.collect.metamodel.ui.UITableHeadingComponent;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFormPrinter {
	
	private static final String NUMBERING_STYLE_FORMAT = 
			"<w:numbering xmlns:wpc=\"http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas\" "
					+ "xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" "
					+ "xmlns:o=\"urn:schemas-microsoft-com:office:office\" "
					+ "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
					+ "xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\" "
					+ "xmlns:v=\"urn:schemas-microsoft-com:vml\" "
					+ "xmlns:wp14=\"http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing\" "
					+ "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" "
					+ "xmlns:w10=\"urn:schemas-microsoft-com:office:word\" "
					+ "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
					+ "xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\" "
					+ "xmlns:w15=\"http://schemas.microsoft.com/office/word/2012/wordml\" "
					+ "xmlns:wpg=\"http://schemas.microsoft.com/office/word/2010/wordprocessingGroup\" "
					+ "xmlns:wpi=\"http://schemas.microsoft.com/office/word/2010/wordprocessingInk\" "
					+ "xmlns:wne=\"http://schemas.microsoft.com/office/word/2006/wordml\" "
					+ "xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\" "
					+ "mc:Ignorable=\"w14 w15 wp14\">\n"
				+ "<w:abstractNum>\n" 
//					+ "<w:nsid w:val=\"6871722E\"/>\n"
//					+ "<w:multiLevelType w:val=\"hybridMultilevel\"/>\n" 
//					+ "<w:tmpl w:val=\"8FE6E4C8\"/>\n"
					/*
					+ "<w:lvl w:ilvl=\"0\" w:tplc=\"0410000D\">\n" 
						+ "<w:start w:val=\"1\"/>\n"
						+ "<w:numFmt w:val=\"bullet\"/>\n" 
						+ "<w:lvlText w:val=\"&#183;\"/>\n"
						+ "<w:lvlJc w:val=\"left\"/>\n" 
						+ "<w:pPr>\n" 
							+ "<w:ind w:left=\"720\" w:hanging=\"360\"/>\n" 
						+ "</w:pPr>\n"
						+ "<w:rPr>\n" 
							+ "<w:rFonts w:ascii=\"Webdings\" w:hAnsi=\"Webdings\" w:hint=\"default\"/>\n" 
						+ "</w:rPr>\n"
					+ "</w:lvl>\n"
					*/ 
					+ "<w:lvl w:ilvl=\"0\">"
					+ "  <w:lvlText w:val=\"■\" />"
					+ "  <w:rPr>"
					+ "    <w:rFonts w:ascii=\"Symbol\" w:h-ansi=\"Symbol\" w:hint=\"default\" />"
					+ "  </w:rPr>"
					+ "  <w:pPr>"
					+ "    <w:ind w:left=\"720\" w:hanging=\"360\" />"
					+ "  </w:pPr>"
					+ "</w:lvl>"
				+ "</w:abstractNum>\n" 
//				+ "<w:num w:numId=\"1\">\n" 
//					+ "<w:abstractNumId w:val=\"0\"/>\n"
//				+ "</w:num>\n" 
			+ "</w:numbering>";
		
	private static final String NUMBERING = 
		"<?xml version='1.0' encoding='UTF-8' standalone='yes'?>" +
		"<w:numbering" +
		"     xmlns:w='http://schemas.openxmlformats.org/wordprocessingml/2006/main'>" +
		"     <w:abstractNum w:abstractNumId='1'>" +
		"       	<w:lvl w:ilvl='0'>" +
		"            <w:start w:val='1' />" +
		"            <w:numFmt w:val='bullet' />" +
		"            <w:lvlJc w:val='left' />" +
		"            <!-- IDENTIFICATION CODE SYMBOL SQUARE '&# 9632;' -->" +
		"            <w:lvlText w:val='\\\\U+20DE' />" +
		"            <w:pPr>" +
		"                <w:ind w:hanging='180'/>" +
		"            </w:pPr>" +
		"            <w:rPr>" +
		"                <w:rFonts w:ascii='Wingdings' w:cs='Wingdings' w:hAnsi='Wingdings'" +
		"                    w:hint='default' />" +
		"                <w:sz w:val='30' />" +
		"          </w:rPr>" +
		"        </w:lvl>" +
		"    </w:abstractNum>" +
		"</w:numbering>";
	
	private CodeListManager codeListManager;
	
	public SurveyFormPrinter(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	public void print(OutputStream out, CollectSurvey survey) throws IOException {
		print(out, survey, survey.getDefaultLanguage());
	}

	public void print(OutputStream out, CollectSurvey survey, String language) throws IOException {
		MSWordSurveyFormPrinter renderer = new MSWordSurveyFormPrinter(out, survey, language);
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

	private class MSWordSurveyFormPrinter extends UIObjectPaperFormRenderer {
		
		private OutputStream out;
		private CollectSurvey survey;
		private String language;
		protected XWPFDocument doc;
		
		public MSWordSurveyFormPrinter(OutputStream out, CollectSurvey survey, String language) {
			super(null);
			this.out = out;
			this.survey = survey;
			this.language = language;
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
	
	private static BigInteger addListStyle(XWPFDocument doc) {
        try {
            XWPFNumbering numbering = doc.createNumbering();
            // generate numbering style from XML
//            BigInteger id = generateNewNumberingId(doc);
            CTAbstractNum abstractNum = CTAbstractNum.Factory.parse(NUMBERING_STYLE_FORMAT);
            XWPFAbstractNum abs = new XWPFAbstractNum(abstractNum, numbering);
            // find available id in document
            BigInteger id = generateNewNumberingId(doc);
            // assign id
            abs.getAbstractNum().setAbstractNumId(id);
            // add to numbering, should get back same id
            id = numbering.addAbstractNum(abs);
            // add to num list, result is numid
            return doc.getNumbering().addNum(id);
        } catch (Exception e) {
            throw new RuntimeException("Error adding list style: " + e.getMessage(), e);
        }
    }
	
	private static BigInteger generateNewNumberingId(XWPFDocument doc) {
		 XWPFNumbering numbering = doc.createNumbering();
		// find available id in document
         BigInteger id = BigInteger.valueOf(1);
         boolean found = false;
         while (!found) {
             Object o = numbering.getAbstractNum(id);
             found = (o == null);
             if (!found) {
                 id = id.add(BigInteger.ONE);
             }
         }
         return id;
	}
	
	private abstract class UIObjectPaperFormRenderer {
		
		protected UIObjectPaperFormRenderer parentRenderer;
		
		public UIObjectPaperFormRenderer(UIObjectPaperFormRenderer parentRenderer) {
			super();
			this.parentRenderer = parentRenderer;
		}

		public void render() throws IOException {
		}
		
		protected MSWordSurveyFormPrinter getRootRenderer() {
			UIObjectPaperFormRenderer currentRenderer = this;
			while (currentRenderer.parentRenderer != null) {
				currentRenderer = currentRenderer.parentRenderer;
			}
			return (MSWordSurveyFormPrinter) currentRenderer;
		}
		
		protected void addPageBreak() {
			getDocument().createParagraph().createRun().addBreak(BreakType.PAGE);
		}
		
		protected XWPFDocument getDocument() {
			return getRootRenderer().doc;
		}
		
		protected XWPFTable insertNestedTable(XWPFTableCell cell) {
			XWPFParagraph paragraph = cell.getParagraphs().get(0);
			XmlCursor cursor = paragraph.getCTP().newCursor();
			XWPFTable table = cell.insertNewTbl(cursor);
			CTTbl ctTbl = table.getCTTbl();
			CTTblPr ctTblPr = ctTbl.addNewTblPr();
			CTTblWidth ctTblWidth = CTTblWidth.Factory.newInstance();
			ctTblWidth.setW(BigInteger.valueOf(5000));
			ctTblWidth.setType(STTblWidth.PCT);
			ctTblPr.setTblW(ctTblWidth);
			return table;
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
			XWPFTableCell contentCell = row.getCell(1);
			XWPFParagraph contentParagraph = contentCell.getParagraphs().get(0);
			XmlCursor cursor = contentParagraph.getCTP().newCursor();
			
			AttributeDefinition attrDef = field.getAttributeDefinition();
			if (attrDef instanceof BooleanAttributeDefinition) {
				
			} else if (attrDef instanceof TextAttributeDefinition
					|| attrDef instanceof NumberAttributeDefinition) {
				XWPFRun run = contentParagraph.createRun();
				run.setText("           ");
				contentParagraph.setBorderBottom(Borders.SINGLE);
				contentParagraph.setBorderLeft(Borders.SINGLE);
				contentParagraph.setBorderRight(Borders.SINGLE);
				contentParagraph.setBorderTop(Borders.SINGLE);
			} else if (attrDef instanceof DateAttributeDefinition) {
				XWPFTable table = insertNestedTable(contentCell);
				XWPFTableRow row1 = table.createRow();
				{
					XWPFTableCell cell = row1.createCell();
					cell.setText("  ");
				}
				{
					XWPFTableCell cell = row1.createCell();
					cell.setText("/");
				}
				{
					XWPFTableCell cell = row1.createCell();
					cell.setText("  ");
				}
				{
					XWPFTableCell cell = row1.createCell();
					cell.setText("/");
				}
				{
					XWPFTableCell cell = row1.createCell();
					cell.setText("    ");
				}
			} else if (attrDef instanceof CodeAttributeDefinition) {
				CodeList list = ((CodeAttributeDefinition) attrDef).getList();
				if (! (list.isExternal() || list.isHierarchical())) {
//					XWPFParagraph p = contentCell.getParagraphs().get(0);
					List<CodeListItem> items = codeListManager.loadItems(list, 1);
					for (int i = 0; i < items.size(); i++) {
						XWPFParagraph p = i == 0 ? contentCell.getParagraphs().get(0) : contentCell.addParagraph();
//						p.setNumID(addListStyle(getDocument()));
						CodeListItem item = items.get(i);
						XWPFRun run = p.createRun();
						run.setText("- " + item.getLabel(getRootRenderer().language));
					}
				} else {
					XWPFParagraph paragraph = contentCell.getParagraphs().get(0);
					XWPFRun run = paragraph.createRun();
					run.setText("     ");
					paragraph.setBorderBottom(Borders.SINGLE);
					paragraph.setBorderLeft(Borders.SINGLE);
					paragraph.setBorderRight(Borders.SINGLE);
				}
			}
		}
		
	}
	
	private class UITablePaperFormRenderer extends UIObjectPaperFormRenderer {
		
		private UITable table;

		UITablePaperFormRenderer(UIFormContentPaperFormRenderer parentRenderer, UITable table) {
			super(parentRenderer);
			this.table = table;
		}
		
		@Override
		public void render() throws IOException {
			super.render();
			XWPFTableRow contentRow = ((UIFormContentPaperFormRenderer) parentRenderer).createRow();
			XWPFTableCell labelCell = contentRow.getCell(0);
			EntityDefinition entityDef = table.getEntityDefinition();
			labelCell.setText(getLabelOrName(entityDef, getRootRenderer().survey.getDefaultLanguage()));
			
			XWPFTableCell contentCell = contentRow.getCell(1);
			XWPFTable nestedTable = insertNestedTable(contentCell);
			
			XWPFTableRow headerRow = nestedTable.createRow();
			List<UITableHeadingComponent> headingComponents = table.getHeadingComponents();
			for (UITableHeadingComponent headingComponent : headingComponents) {
				XWPFTableCell cell = headerRow.createCell();
				if (headingComponent instanceof UIColumn) {
					AttributeDefinition attrDef = ((UIColumn) headingComponent).getAttributeDefinition();
					cell.setText(getLabelOrName(attrDef, getRootRenderer().language));
				}
			}
			addRows(nestedTable, 10, table.getHeadingComponents().size());
		}

		private void addRows(XWPFTable table, int rows, int cellsCount) {
			for (int i=0; i < rows; i++) {
				table.createRow();
			}
		}
	}
	
	private class FormSetPaperFormRenderer extends UIFormContentPaperFormRenderer {
		
		private UIFormSet formSet;

		public FormSetPaperFormRenderer(UIObjectPaperFormRenderer parentRenderer, UIFormSet formSet) {
			super(parentRenderer, formSet);
			this.formSet = formSet;
		}
		
		@Override
		protected void renderFormComponents() throws IOException {
			//do nothing
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
			renderFormComponents();
			renderNestedForms();
		}

		private void renderNestedForms() throws IOException {
			List<UIForm> forms = container.getForms();
			for (UIForm uiForm : forms) {
				UIFormPaperFormRenderer formRenderer = new UIFormPaperFormRenderer(this, uiForm);
				formRenderer.render();
				addPageBreak();
			}
		}

		protected void renderFormComponents() throws IOException {
			initializeFormComponentsTable();
			List<UIFormComponent> children = container.getChildren();
			for (UIFormComponent component : children) {
				UIObjectPaperFormRenderer childRenderer = createRenderer(component);
				if (childRenderer != null) {
					childRenderer.render();
				}
			}
			addPageBreak();
		}

		private void initializeFormComponentsTable() {
			table = getRootRenderer().doc.createTable(1, 2);
			CTTbl ctTbl = table.getCTTbl();
			CTTblPr tblPr = ctTbl.getTblPr();
			tblPr.unsetTblBorders();
			CTTblWidth tblW = tblPr.getTblW();
			tblW.setW(BigInteger.valueOf(5000));
			tblW.setType(STTblWidth.PCT);
//			tblPr.setTblW(tblW);
//			ctTbl.setTblPr(tblPr);
		}

		private UIObjectPaperFormRenderer createRenderer(UIFormComponent component) {
			if (component instanceof UIField) {
				return new FieldPaperFormRenderer(this, (UIField) component);
			} else if (component instanceof UIFormSection) {
				return new UIFormSectionPaperFormRenderer(this, (UIFormSection) component);
			} else if (component instanceof UITable) {
				return new UITablePaperFormRenderer(this, (UITable) component);
			} else {
				return null;
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
			XWPFTable table = createContentTable();
			XWPFTableRow row = table.getRow(0);
			XWPFTableCell cell = row.getCell(0);
			XWPFParagraph header = cell.addParagraph();
			XWPFRun run = header.createRun();
			run.setText(getNullSafeLabel(formSection.getEntityDefinition()));
			super.render();
		}

		private XWPFTable createContentTable() {
			XWPFTable table = getDocument().createTable();
			CTTbl ctTbl = table.getCTTbl();
			CTTblPr tblPr = ctTbl.getTblPr();
			tblPr.unsetTblBorders();
			CTTblWidth tblW = tblPr.getTblW();
			tblW.setW(BigInteger.valueOf(5000));
			tblW.setType(STTblWidth.PCT);
			return table;
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

	private static class POIUtils {
		
		public static void addCells(XWPFTableRow row, int count) {
			for (int i=0; i < count; i++) {
				row.createCell();
			}
		}
	}

}
