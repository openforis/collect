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
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * @author M. Togna
 * 
 */
public class NodeDefinitionProxy implements Proxy {

	private transient NodeDefinition nodeDefinition;
	protected EntityDefinitionProxy parent;
	
	public NodeDefinitionProxy(EntityDefinitionProxy parent, NodeDefinition nodeDefinition) {
		super();
		this.parent = parent;
		this.nodeDefinition = nodeDefinition;
	}

	static List<NodeDefinitionProxy> fromList(EntityDefinitionProxy parent, List<? extends NodeDefinition> list) {
		List<NodeDefinitionProxy> proxies = new ArrayList<NodeDefinitionProxy>();
		if (list != null) {
			for (NodeDefinition n : list) {
				NodeDefinitionProxy p = null;
				if (n instanceof AttributeDefinition) {
					if (n instanceof BooleanAttributeDefinition) {
						p = new BooleanAttributeDefinitionProxy(parent, (BooleanAttributeDefinition) n);
					} else if (n instanceof CodeAttributeDefinition) {
						p = new CodeAttributeDefinitionProxy(parent, (CodeAttributeDefinition) n);
					} else if (n instanceof CoordinateAttributeDefinition) {
						p = new CoordinateAttributeDefinitionProxy(parent, (CoordinateAttributeDefinition) n);
					} else if (n instanceof DateAttributeDefinition) {
						p = new DateAttributeDefinitionProxy(parent, (DateAttributeDefinition) n);
					} else if (n instanceof FileAttributeDefinition) {
						p = new FileAttributeDefinitionProxy(parent, (FileAttributeDefinition) n);
					} else if (n instanceof NumberAttributeDefinition) {
						p = new NumberAttributeDefinitionProxy(parent, (NumberAttributeDefinition) n);
					} else if (n instanceof RangeAttributeDefinition) {
						p = new RangeAttributeDefinitionProxy(parent, (RangeAttributeDefinition) n);
					} else if (n instanceof TaxonAttributeDefinition) {
						p = new TaxonAttributeDefinitionProxy(parent, (TaxonAttributeDefinition) n);
					} else if (n instanceof TextAttributeDefinition) {
						p = new TextAttributeDefinitionProxy(parent, (TextAttributeDefinition) n);
					} else if (n instanceof TimeAttributeDefinition) {
						p = new TimeAttributeDefinitionProxy(parent, (TimeAttributeDefinition) n);
					} else {
						throw new RuntimeException("AttributeDefinition not supported: " + n.getClass().getSimpleName());
					}
				} else if (n instanceof EntityDefinition) {
					p = new EntityDefinitionProxy(parent, (EntityDefinition) n);
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

	@ExternalizedProperty
	public String getUiTabName() {
		String namespaceURI = "http://www.openforis.org/collect/3.0/ui";
		QName qname = new QName(namespaceURI, "tab");
		String string = nodeDefinition.getAnnotation(qname);
		return string;
	}

	public EntityDefinitionProxy getParent() {
		return parent;
	}

}
