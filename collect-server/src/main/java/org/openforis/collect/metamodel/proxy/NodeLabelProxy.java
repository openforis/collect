/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.NodeLabel;

/**
 * @author M. Togna
 * 
 */
public class NodeLabelProxy extends TypedLanguageSpecificTextProxy<NodeLabel.Type, NodeLabelProxy.Type> {

	public enum Type {
		HEADING, INSTANCE, NUMBER;
	}

	static List<NodeLabelProxy> fromNodeLabelList(List<NodeLabel> labels) {
		List<NodeLabelProxy> proxies = new ArrayList<NodeLabelProxy>();
		if (labels != null) {
			for (NodeLabel l : labels) {
				proxies.add(new NodeLabelProxy(l));
			}
		}
		return proxies;
	}

	public NodeLabelProxy(NodeLabel nodeLabel) {
		super(nodeLabel);
	}

	@ExternalizedProperty
	public Type getType() {
		return Type.valueOf(typedLanguageSpecificText.getType().toString());
	}

}
