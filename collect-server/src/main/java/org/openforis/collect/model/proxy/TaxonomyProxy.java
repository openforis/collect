package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.idm.model.species.Taxonomy;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonomyProxy implements Proxy {

	private Integer id;
	private String name;
	private String uri;
	
	public TaxonomyProxy() {
	}
	
	public TaxonomyProxy(Taxonomy taxonomy) {
		super();
		id = taxonomy.getId();
		name = taxonomy.getName();
		uri = taxonomy.getUri();
	}
	
	public static List<TaxonomyProxy> fromList(List<Taxonomy> list) {
		List<TaxonomyProxy> proxies = new ArrayList<TaxonomyProxy>();
		if (list != null) {
			for (Taxonomy item : list) {
				proxies.add(new TaxonomyProxy(item));
			}
		}
		return proxies;
	}
	
	public void copyTo(Taxonomy taxonomy) {
		taxonomy.setName(name);
		taxonomy.setUri(uri);
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
	
}
