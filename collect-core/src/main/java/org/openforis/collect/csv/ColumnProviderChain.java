package org.openforis.collect.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class ColumnProviderChain implements ColumnProvider {
	private List<ColumnProvider> providers;
	private List<String> headings;

	public ColumnProviderChain(String headingPrefix, List<ColumnProvider> providers) {
		if ( providers == null || providers.isEmpty() ) {
			throw new IllegalArgumentException("Providers may not be null or empty");
		}
		this.providers = providers;
		this.headings = getColumnHeadingsInternal(headingPrefix);
	}

	public ColumnProviderChain(String headingPrefix, ColumnProvider... providers) {
		this(headingPrefix, Arrays.asList(providers));
	}

	public ColumnProviderChain(List<ColumnProvider> providers) {
		this("", providers);
	}

	public ColumnProviderChain(ColumnProvider... providers) {
		this(Arrays.asList(providers));
	}

	public List<String> getColumnHeadings() {
		return headings;
	}

	public List<ColumnProvider> getColumnProviders() {
		return providers;
	}
	
	private List<String> getColumnHeadingsInternal(String headingPrefix) {
		ArrayList<String> h = new ArrayList<String>(); 
		for (ColumnProvider p : providers) {
			List<String> columnHeadings = p.getColumnHeadings();
			for (String heading : columnHeadings) {
				h.add(headingPrefix+heading);
			}
		}
		return Collections.unmodifiableList(h);
	}

	public List<String> extractValues(Node<?> axis) {
		ArrayList<String> v = new ArrayList<String>();
		for (ColumnProvider p : providers) {
			v.addAll(p.extractValues(axis));
		}
		return v;
	}
	
	protected List<String> emptyValues() {
		ArrayList<String> v = new ArrayList<String>();
		for (int i = 0; i < getColumnHeadings().size(); i++) {
			v.add("");
		}
		return v;
	}
}
