package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

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
	private Integer surveyWorkId;
	
	public TaxonomyProxy() {
	}
	
	public TaxonomyProxy(CollectTaxonomy taxonomy) {
		super();
		id = taxonomy.getId();
		name = taxonomy.getName();
		uri = taxonomy.getUri();
		surveyId = taxonomy.getSurveyId();
		surveyWorkId = taxonomy.getSurveyWorkId();
	}
	
	public static List<TaxonomyProxy> fromList(List<CollectTaxonomy> list) {
		List<TaxonomyProxy> proxies = new ArrayList<TaxonomyProxy>();
		if (list != null) {
			for (CollectTaxonomy item : list) {
				proxies.add(new TaxonomyProxy(item));
			}
		}
		return proxies;
	}
	
	public void copyPropertiesForUpdate(CollectTaxonomy taxonomy) {
		taxonomy.setName(name);
		taxonomy.setUri(uri);
		taxonomy.setSurveyId(surveyId);
		taxonomy.setSurveyWorkId(surveyWorkId);
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

	public Integer getSurveyWorkId() {
		return surveyWorkId;
	}

	public void setSurveyWorkId(Integer surveyWorkId) {
		this.surveyWorkId = surveyWorkId;
	}
	
}
