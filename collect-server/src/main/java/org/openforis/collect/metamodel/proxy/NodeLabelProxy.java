/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.NodeLabel;

/**
 * @author M. Togna
 * 
 */
public class NodeLabelProxy implements Proxy {

	private transient NodeLabel nodeLabel;

	public enum Type {
		HEADING, INSTANCE, NUMBER;
	}

	static List<NodeLabelProxy> fromList(List<NodeLabel> labels) {
		List<NodeLabelProxy> proxies = new ArrayList<NodeLabelProxy>();
		if (labels != null) {
			for (NodeLabel l : labels) {
				proxies.add(new NodeLabelProxy(l));
			}
		}
		return proxies;
	}

	public NodeLabelProxy(NodeLabel nodeLabel) {
		super();
		this.nodeLabel = nodeLabel;
	}

	@ExternalizedProperty
	public Type getType() {
		return nodeLabel.getType() != null ? Type.valueOf(nodeLabel.getType().toString()) : null;
	}

	@ExternalizedProperty
	public String getLanguage() {
		return nodeLabel.getLanguage();
	}

	@ExternalizedProperty
	public String getText() {
		return nodeLabel.getText();
	}

}
