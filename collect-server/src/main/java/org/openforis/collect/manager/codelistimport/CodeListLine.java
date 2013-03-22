package org.openforis.collect.manager.codelistimport;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.manager.referencedataimport.Line;


/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListLine extends Line {

	private List<CodeLabelItem> codeLabelItems;
	
	public CodeListLine() {
		codeLabelItems = new ArrayList<CodeListLine.CodeLabelItem>();
	}
	
	public void addCodeLabelItem(String code, String label) {
		CodeLabelItem item = new CodeLabelItem(code, label);
		codeLabelItems.add(item);
	}
	
	public CodeLabelItem getItemAtLevel(int levelIndex) {
		return codeLabelItems.get(levelIndex);
	}
	
	public List<CodeLabelItem> getCodeLabelItems() {
		return codeLabelItems;
	}

	public void setCodeLabelItems(List<CodeLabelItem> codeLabelItems) {
		this.codeLabelItems = codeLabelItems;
	}
	
	public static class CodeLabelItem {
		
		private String code;
		private String label;
		
		public CodeLabelItem(String code, String label) {
			super();
			this.code = code;
			this.label = label;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}

}