package org.openforis.collect.designer.form;

import org.openforis.collect.model.CollectTaxonomy;

public class TaxonomyFormObject extends FormObject<CollectTaxonomy> {

	private String name;

	@Override
	protected void reset() {
		this.name = null;
	}
	
	@Override
	public void loadFrom(CollectTaxonomy source, String language) {
		super.loadFrom(source, language);
		this.name = source.getName();
	}
	
	@Override
	public void saveTo(CollectTaxonomy dest, String language) {
		super.saveTo(dest, language);
		dest.setName(this.name);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	

}
