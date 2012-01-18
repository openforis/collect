/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * @author M. Togna
 * 
 */
public class NodeDefinitionProxy implements Proxy {

	private transient NodeDefinition nodeDefinition;

	public NodeDefinitionProxy(NodeDefinition nodeDefinition) {
		super();
		this.nodeDefinition = nodeDefinition;
	}

	static List<NodeDefinitionProxy> fromList(List<NodeDefinition> list) {
		List<NodeDefinitionProxy> proxies = new ArrayList<NodeDefinitionProxy>();
		if (list != null) {
			for (NodeDefinition n : list) {
				NodeDefinitionProxy p = null;
				if (n instanceof AttributeDefinition) {
					if(n instanceof BooleanAttributeDefinition) {
						p = new BooleanAttributeDefinitionProxy((BooleanAttributeDefinition) n);
					} else if(n instanceof CodeAttributeDefinition) {
						p = new CodeAttributeDefinitionProxy((CodeAttributeDefinition) n);
					} else if(n instanceof CoordinateAttributeDefinition) {
						p = new CoordinateAttributeDefinitionProxy((CoordinateAttributeDefinition) n);
					} else if(n instanceof DateAttributeDefinition) {
						p = new DateAttributeDefinitionProxy((DateAttributeDefinition) n);
					} else if(n instanceof FileAttributeDefinition) {
						p = new FileAttributeDefinitionProxy((FileAttributeDefinition) n);
					} else if(n instanceof NumericAttributeDefinition) {
						p = new NumericAttributeDefinitionProxy((NumericAttributeDefinition) n);
					} else if(n instanceof TaxonAttributeDefinition) {
						p = new TaxonAttributeDefinitionProxy((TaxonAttributeDefinition) n);
					} else if(n instanceof TextAttributeDefinition) {
						p = new TextAttributeDefinitionProxy((TextAttributeDefinition) n);
					} else if(n instanceof TimeAttributeDefinition) {
						p = new TimeAttributeDefinitionProxy((TimeAttributeDefinition) n);
					} else {
						throw new RuntimeException("AttributeDefinition not supported: " + p.getClass().getSimpleName());
					}
				} else if (n instanceof EntityDefinition) {
					p = new EntityDefinitionProxy((EntityDefinition) n);
				}
				proxies.add(p);
			}
		}
		return proxies;
	}

	public String getSinceVersionName() {
		return nodeDefinition.getSinceVersionName();
	}

	@ExternalizedProperty
	public ModelVersionProxy getSinceVersion() {
		return nodeDefinition.getSinceVersion() != null ? new ModelVersionProxy(nodeDefinition.getSinceVersion()) : null;
	}

	@ExternalizedProperty
	public ModelVersionProxy getDeprecatedVersion() {
		return nodeDefinition.getDeprecatedVersion() != null ? new ModelVersionProxy(nodeDefinition.getDeprecatedVersion()) : null;
	}

	public Set<QName> getAnnotationNames() {
		return nodeDefinition.getAnnotationNames();
	}

	@ExternalizedProperty
	public Integer getId() {
		return nodeDefinition.getId();
	}

	@ExternalizedProperty
	public String getName() {
		return nodeDefinition.getName();
	}

	@ExternalizedProperty
	public String getRelevantExpression() {
		return nodeDefinition.getRelevantExpression();
	}

	@ExternalizedProperty
	public String getRequiredExpression() {
		return nodeDefinition.getRequiredExpression();
	}

	@ExternalizedProperty
	public boolean isMultiple() {
		return nodeDefinition.isMultiple();
	}

	@ExternalizedProperty
	public Integer getMinCount() {
		return nodeDefinition.getMinCount();
	}

	@ExternalizedProperty
	public Integer getMaxCount() {
		return nodeDefinition.getMaxCount();
	}

	@ExternalizedProperty
	public List<NodeLabelProxy> getLabels() {
		return NodeLabelProxy.fromList(nodeDefinition.getLabels());
	}

	@ExternalizedProperty
	public List<PromptProxy> getPrompts() {
		return PromptProxy.fromList(nodeDefinition.getPrompts());
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getDescriptions() {
		return LanguageSpecificTextProxy.fromList(nodeDefinition.getDescriptions());
	}

	@ExternalizedProperty
	public String getPath() {
		return nodeDefinition.getPath();
	}

	// TODO do we need the parent definition in flex-ui?
	// public EntityDefinition getParentDefinition() {
	// return nodeDefinition.getParentDefinition();
	// }

}
