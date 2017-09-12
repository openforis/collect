package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openforis.commons.collection.Visitor;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 * @author S. Ricci
 * 
 */
public class ColumnProviderChain extends BasicColumnProvider {
	private List<ColumnProvider> providers;
	private String headingPrefix;
	private List<String> headings;
	protected EntityDefinition entityDefinition; //optional
	
	public ColumnProviderChain(CSVDataExportParameters config, List<ColumnProvider> providers) {
		this(config, null, providers);
	}

	public ColumnProviderChain(CSVDataExportParameters config, EntityDefinition entityDefinition, List<ColumnProvider> providers) {
		this(config, entityDefinition, null, providers);
	}

	public ColumnProviderChain(CSVDataExportParameters config, ColumnProvider... providers) {
		this(config, null, providers);
	}
	
	public ColumnProviderChain(CSVDataExportParameters config, EntityDefinition entityDefinition, ColumnProvider... providers) {
		this(config, entityDefinition, Arrays.asList(providers));
	}

	public ColumnProviderChain(CSVDataExportParameters config, EntityDefinition entityDefinition, String headingPrefix, List<ColumnProvider> providers) {
//		if ( providers == null || providers.isEmpty() ) {
//			throw new IllegalArgumentException("Providers may not be null or empty");
//		}
		super(config);
		this.entityDefinition = entityDefinition;
		this.providers = providers;
		this.headingPrefix = headingPrefix;
		this.headings = generateColumnHeadingsInternal();
		
		for (ColumnProvider p : providers) {
			if (p instanceof BasicColumnProvider) {
				((BasicColumnProvider) p).setParentProvider(this);
			}
		}
	}
	
	@Override
	protected String generateHeadingPrefix() {
		return "";
	}
	
	public List<String> getColumnHeadings() {
		return headings;
	}

	public List<ColumnProvider> getColumnProviders() {
		return providers;
	}
	
	protected String getHeadingPrefix() {
		if (headingPrefix == null) {
			headingPrefix = generateHeadingPrefix();
		}
		return headingPrefix;
	}
	
	private List<String> generateColumnHeadingsInternal() {
		ArrayList<String> h = new ArrayList<String>(); 
		for (ColumnProvider p : providers) {
			List<String> columnHeadings = p.getColumnHeadings();
			for (String heading : columnHeadings) {
				h.add(getHeadingPrefix() + heading);
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
	
	public void traverseProviders(Visitor<ColumnProvider> visitor) {
		Deque<ColumnProvider> stack = new LinkedList<ColumnProvider>();
		stack.addAll(providers);
		while (! stack.isEmpty()) {
			ColumnProvider p = stack.pop();
			visitor.visit(p);
			if (p instanceof ColumnProviderChain) {
				stack.addAll(((ColumnProviderChain) p).providers);
			}
		}
	}
	
	protected List<String> emptyValues() {
		return Collections.nCopies(headings.size(), "");
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(null)
				.append("Column provider chain")
				.append("providers: " + getColumnProviders())
				.build();
	}
}
