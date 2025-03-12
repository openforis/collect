package org.openforis.collect.io.metadata.collectearth.balloon;


/**
 * 
 * @author S. Ricci
 *
 */
public class CETab extends CEFieldSet {

	private boolean main;
	private boolean multipleEntityForm;
	private CEAncillaryFields ancillaryDataHeader;
	
	
	public CEAncillaryFields getAncillaryDataHeader() {
		return ancillaryDataHeader;
	}

	public void setAncillaryDataHeader(CEAncillaryFields ancillaryDataHeader) {
		this.ancillaryDataHeader = ancillaryDataHeader;
	}

	public CETab(String name, String label) {
		super(name, HtmlUnicodeEscaperUtil.escapeHtmlUnicode( label ), null);
	}

	public boolean isMain() {
		return main;
	}

	public void setMain(boolean main) {
		this.main = main;
	}
	
	public boolean isMultipleEntityForm() {
		return multipleEntityForm;
	}
	
	public void setMultipleEntityForm(boolean multipleEntityForm) {
		this.multipleEntityForm = multipleEntityForm;
	}
}
