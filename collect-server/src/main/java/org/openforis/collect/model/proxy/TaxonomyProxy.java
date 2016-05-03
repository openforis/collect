package org.openforis.collect.model.proxy;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.CollectTaxonomy;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonomyProxy implements Proxy {

	private Integer id;
	private String name;
	private String uri;
	private Integer surveyId;
	
	public TaxonomyProxy() {
	}
	
	public TaxonomyProxy(CollectTaxonomy taxonomy) {
		super();
		id = taxonomy.getId();
		name = taxonomy.getName();
		uri = taxonomy.getUri();
		surveyId = taxonomy.getSurveyId();
	}
	
	public void copyPropertiesForUpdate(CollectTaxonomy taxonomy) {
		taxonomy.setName(name);
		taxonomy.setUri(uri);
		taxonomy.setSurveyId(surveyId);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Integer getSurveyId() {
		return surveyId;
	}

	public void setSurveyId(Integer surveyId) {
		this.surveyId = surveyId;
	}

}
