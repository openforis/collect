package org.openforis.collect.metamodel;

import org.openforis.collect.Proxy;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDefinitionSummary implements Proxy {
	
	private int id;
	private String name;
	private String label;
	
	public NodeDefinitionSummary(int id) {
		super();
		this.id = id;
	}

	public NodeDefinitionSummary(int id, String name) {
		this(id);
		this.name = name;
	}
	
	public NodeDefinitionSummary(int id, String name, String label) {
		this(id, name);
		this.label = label;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public int getId() {
		return id;
	}

}
