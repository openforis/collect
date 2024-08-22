/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.EntityDefinition.TraversalType;
import org.openforis.idm.path.InvalidPathException;
import org.openforis.idm.path.Path;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class Schema extends SurveyObject {

	private static final long serialVersionUID = 1L;

	private List<EntityDefinition> rootEntityDefinitions;
	private Map<Integer, NodeDefinition> definitionsById;
	
	public Schema(Survey survey) {
		super(survey);
		this.definitionsById = new HashMap<Integer, NodeDefinition>();
	}
	
	public NodeDefinition getDefinitionByPath(String absolutePath) throws InvalidPathException {
		Path path = Path.parse(absolutePath);
		return path.evaluate(this);
	}
	
	@SuppressWarnings("unchecked")
	public <N extends NodeDefinition> N getDefinitionById(int id) {
		return (N) definitionsById.get(id);
	}
	
	public boolean containsDefinitionWithId(int id) {
		return definitionsById.containsKey(id);
	}

	public List<EntityDefinition> getRootEntityDefinitions() {
		return CollectionUtils.unmodifiableList(rootEntityDefinitions);
	}
	
	public EntityDefinition getFirstRootEntityDefinition() {
		return rootEntityDefinitions == null || rootEntityDefinitions.isEmpty() ? null : rootEntityDefinitions.get(0);
	}

	public void addRootEntityDefinition(EntityDefinition defn) {
		if ( defn.isDetached() ) {
			throw new IllegalArgumentException("Detached definitions cannot be added");
		}
		
		if ( defn.getSchema() != this ) {
			throw new IllegalArgumentException("Definition does not belong to this schema");
		}
		
		if ( defn.getParentDefinition() != null ) {
			throw new IllegalArgumentException("Parent of root definition must be null");
		}
		
		int id = defn.getId();
		if ( id < 1 || id > getLastId() ) {
			throw new IllegalArgumentException("Invalid definition id " + id);
		}
		
		if ( rootEntityDefinitions == null) {
			rootEntityDefinitions = new ArrayList<EntityDefinition>();
		}

		rootEntityDefinitions.add(defn);
		index(defn);
	}
	
	public void removeRootEntityDefinition(String name) {
		EntityDefinition defn = getRootEntityDefinition(name);
		removeRootEntityDefinition(defn);
	}

	protected void removeRootEntityDefinition(EntityDefinition defn) {
		rootEntityDefinitions.remove(defn);
		detach(defn);
	}
	
	public EntityDefinition getRootEntityDefinition(String name) {
		if ( rootEntityDefinitions != null ) {
			for (EntityDefinition defn : rootEntityDefinitions) {
				if ( defn.getName().equals(name) ) {
					return defn;
				}
			}
		}
		return null;
	}
	
	public EntityDefinition getRootEntityDefinition(int id) {
		if ( rootEntityDefinitions != null ) {
			for (EntityDefinition node : rootEntityDefinitions) {
				if ( node.getId() == id ) {
					return node;
				}
			}
		}
		throw new IllegalArgumentException("Root entity definition with id " + id + " not found");
	}

	public int getRootEntityIndex(EntityDefinition rootEntity) {
		if ( rootEntityDefinitions != null ) {
			int result = rootEntityDefinitions.indexOf(rootEntity);
			if ( result < 0 ) {
				throw new IllegalArgumentException("Root entity not found:" + rootEntity.getName());
			}
			return result;
		} else {
			throw new IllegalArgumentException("Schema has no root entities");
		}
	}
	
	public void moveRootEntityDefinition(EntityDefinition rootEntity, int newIndex) {
		CollectionUtils.shiftItem(rootEntityDefinitions, rootEntity, newIndex);
	}
	
	protected void removeVersioning(final ModelVersion version) {
		List<EntityDefinition> rootDefns = getRootEntityDefinitions();
		for (EntityDefinition entityDefinition : rootDefns) {
			entityDefinition.removeVersioning(version);
			entityDefinition.traverse(new NodeDefinitionVisitor() {
				@Override
				public void visit(NodeDefinition defn) {
					defn.removeVersioning(version);
				}
			});
		}
	}
	
	public List<TaxonAttributeDefinition> getTaxonAttributeDefinitions(String taxonomyName) {
		List<TaxonAttributeDefinition> result = new ArrayList<TaxonAttributeDefinition>();
		List<EntityDefinition> rootDefns = getRootEntityDefinitions();
		Deque<NodeDefinition> stack = new LinkedList<NodeDefinition>();
		stack.addAll(rootDefns);
		while ( ! stack.isEmpty() ) {
			NodeDefinition node = stack.pop();
			if ( node instanceof TaxonAttributeDefinition ) {
				TaxonAttributeDefinition taxonAttr = (TaxonAttributeDefinition) node;
				if ( taxonAttr.getTaxonomy().equals(taxonomyName) ) {
					result.add(taxonAttr);
				}
			} else if ( node instanceof EntityDefinition ) {
				stack.addAll(((EntityDefinition) node).getChildDefinitions());
			}
		}
		return result;
	}
	
	/**
	 * Returns first level entity definitions that have the attribute countInSummaryList set to true
	 * 
	 * @param rootEntityDefinition
	 * @return 
	 */
	public List<EntityDefinition> getCountableEntitiesInRecordList(EntityDefinition rootEntityDefinition) {
		return getAnnotatedAttributeDefinitions(rootEntityDefinition, Annotation.COUNT_IN_SUMMARY_LIST, false);
	}
	
	public List<AttributeDefinition> getQualifierAttributeDefinitions(EntityDefinition rootEntityDefinition) {
		return getAnnotatedAttributeDefinitions(rootEntityDefinition, Annotation.QUALIFIER, true);
	}
	
	public List<AttributeDefinition> getSummaryAttributeDefinitions(EntityDefinition entityDefinition) {
		return getAnnotatedAttributeDefinitions(entityDefinition, Annotation.SHOW_IN_SUMMARY_LIST, true);
	}
	
	private <N extends NodeDefinition> List<N> getAnnotatedAttributeDefinitions(EntityDefinition rootEntityDefinition, 
			final Annotation annotation, boolean onlyFirstLevel) {
		return rootEntityDefinition.findDescendantDefinitions(new NodeDefinitionVerifier() {
			public boolean verify(NodeDefinition def) {
				String annotationVal = def.getAnnotation(annotation.getQName());
				return Boolean.parseBoolean(annotationVal);
			}
		}, false, onlyFirstLevel);
	}
	
	public List<AttributeDefinition> getRootEntitySummaryAttributeDefinitions() {
		return getSummaryAttributeDefinitions(getFirstRootEntityDefinition());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rootEntityDefinitions == null) ? 0 : rootEntityDefinitions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Schema other = (Schema) obj;
		if (rootEntityDefinitions == null) {
			if (other.rootEntityDefinitions != null)
				return false;
		} else if (!rootEntityDefinitions.equals(other.rootEntityDefinitions))
			return false;
		return true;
	}

	private int nextId() {
		return getSurvey().nextId();
	}

	private int getLastId() {
		return getSurvey().getLastId();
	}
	
	private <T extends NodeDefinition> T index(T defn) {
		int id = defn.getId();
		definitionsById.put(id, defn);
		return defn;
	}

	public EntityDefinition createEntityDefinition(int id) {
		return index(new EntityDefinition(getSurvey(), id));
	}

	public EntityDefinition createEntityDefinition() {
		return index(createEntityDefinition(nextId()));
	}

	public CodeAttributeDefinition createCodeAttributeDefinition(int id) {
		return index(new CodeAttributeDefinition(getSurvey(), id));
	}

	public CodeAttributeDefinition createCodeAttributeDefinition() {
		return index(createCodeAttributeDefinition(nextId()));
	}

	public TextAttributeDefinition createTextAttributeDefinition(int id) {
		return index(new TextAttributeDefinition(getSurvey(), id));
	}

	public TextAttributeDefinition createTextAttributeDefinition() {
		return index(createTextAttributeDefinition(nextId()));
	}

	public FileAttributeDefinition createFileAttributeDefinition(int id) {
		return index(new FileAttributeDefinition(getSurvey(), id));
	}

	public FileAttributeDefinition createFileAttributeDefinition() {
		return index(createFileAttributeDefinition(nextId()));
	}

	public NumberAttributeDefinition createNumberAttributeDefinition(int id) {
		return index(new NumberAttributeDefinition(getSurvey(), id));
	}

	public NumberAttributeDefinition createNumberAttributeDefinition() {
		return index(createNumberAttributeDefinition(nextId()));
	}

	public RangeAttributeDefinition createRangeAttributeDefinition(int id) {
		return index(new RangeAttributeDefinition(getSurvey(), id));
	}

	public RangeAttributeDefinition createRangeAttributeDefinition() {
		return index(createRangeAttributeDefinition(nextId()));
	}

	public TimeAttributeDefinition createTimeAttributeDefinition(int id) {
		return index(new TimeAttributeDefinition(getSurvey(), id));
	}

	public TimeAttributeDefinition createTimeAttributeDefinition() {
		return index(createTimeAttributeDefinition(nextId()));
	}

	public DateAttributeDefinition createDateAttributeDefinition(int id) {
		return index(new DateAttributeDefinition(getSurvey(), id));
	}

	public DateAttributeDefinition createDateAttributeDefinition() {
		return index(createDateAttributeDefinition(nextId()));
	}
	

	public TaxonAttributeDefinition createTaxonAttributeDefinition(int id) {
		return index(new TaxonAttributeDefinition(getSurvey(), id));
	}

	public TaxonAttributeDefinition createTaxonAttributeDefinition() {
		return index(createTaxonAttributeDefinition(nextId()));
	}


	public BooleanAttributeDefinition createBooleanAttributeDefinition(int id) {
		return index(new BooleanAttributeDefinition(getSurvey(), id));
	}

	public BooleanAttributeDefinition createBooleanAttributeDefinition() {
		return index(createBooleanAttributeDefinition(nextId()));
	}

	public CoordinateAttributeDefinition createCoordinateAttributeDefinition(int id) {
		return index(new CoordinateAttributeDefinition(getSurvey(), id));
	}

	public CoordinateAttributeDefinition createCoordinateAttributeDefinition() {
		return index(createCoordinateAttributeDefinition(nextId()));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends NodeDefinition> T cloneDefinition(T def, String... ignoreChildDefinitions) {
		if (def instanceof BooleanAttributeDefinition) {
			return index((T) new BooleanAttributeDefinition(getSurvey(), (BooleanAttributeDefinition) def, nextId()));
		} else if (def instanceof CodeAttributeDefinition) {
			return index((T) new CodeAttributeDefinition(getSurvey(), (CodeAttributeDefinition) def, nextId()));
		} else if (def instanceof CoordinateAttributeDefinition) {
			return index((T) new CoordinateAttributeDefinition(getSurvey(), (CoordinateAttributeDefinition) def, nextId()));
		} else if (def instanceof DateAttributeDefinition) {
			return index((T) new DateAttributeDefinition(getSurvey(), (DateAttributeDefinition) def, nextId()));
		} else if (def instanceof FileAttributeDefinition) {
			return index((T) new FileAttributeDefinition(getSurvey(), (FileAttributeDefinition) def, nextId()));
		} else if (def instanceof NumberAttributeDefinition) {
			return index((T) new NumberAttributeDefinition(getSurvey(), (NumberAttributeDefinition) def, nextId()));
		} else if (def instanceof RangeAttributeDefinition) {
			return index((T) new RangeAttributeDefinition(getSurvey(), (RangeAttributeDefinition) def, nextId()));
		} else if (def instanceof TaxonAttributeDefinition) {
			return index((T) new TaxonAttributeDefinition(getSurvey(), (TaxonAttributeDefinition) def, nextId()));
		} else if (def instanceof TextAttributeDefinition) {
			return index((T) new TextAttributeDefinition(getSurvey(), (TextAttributeDefinition) def, nextId()));
		} else if (def instanceof TimeAttributeDefinition) {
			return index((T) new TimeAttributeDefinition(getSurvey(), (TimeAttributeDefinition) def, nextId()));
		} else if (def instanceof EntityDefinition) {
			return index((T) new EntityDefinition(getSurvey(), (EntityDefinition) def, nextId(), ignoreChildDefinitions));
		} else {
			throw new UnsupportedOperationException("Clone not supported for node type: " + def.getClass().getName());
		}
	}
	
	public void detach(NodeDefinition defn) {
		int id = defn.getId();
		definitionsById.remove(id);	
	}
	
	public void traverse(NodeDefinitionVisitor visitor) {
		traverse(visitor, TraversalType.DFS);
	}

	public void traverse(NodeDefinitionVisitor visitor, TraversalType traversalType) {
		if (rootEntityDefinitions != null) {
			for (EntityDefinition root : rootEntityDefinitions) {
				root.traverse(visitor, traversalType);
			}
		}
	}
	
	public <N extends NodeDefinition> N findNodeDefinition(NodeDefinitionVerifier verifier) {
		@SuppressWarnings("unchecked")
		List<N> nodeDefns = (List<N>) findNodeDefinitions(verifier, true);
		return nodeDefns.isEmpty() ? null: nodeDefns.get(0);
	}
	
	public List<? extends NodeDefinition> findNodeDefinitions(NodeDefinitionVerifier verifier) {
		return findNodeDefinitions(verifier, false);
	}
	
	public List<? extends NodeDefinition> findNodeDefinitions(NodeDefinitionVerifier verifier, boolean stopAfterFirstFound) {
		List<NodeDefinition> foundNodeDefns = new ArrayList<NodeDefinition>();
		for (EntityDefinition rootEntityDef : rootEntityDefinitions) {
			if (verifier.verify(rootEntityDef)) {
				foundNodeDefns.add(rootEntityDef);
				if (stopAfterFirstFound) {
					break;
				}
			}
			foundNodeDefns.addAll(rootEntityDef.findDescendantDefinitions(verifier, stopAfterFirstFound));
		}
		return foundNodeDefns;
	}

	public void changeParentEntity(NodeDefinition node, EntityDefinition newParent) {
		EntityDefinition oldParent = node.getParentEntityDefinition();
		if (oldParent != null) {
			oldParent.removeChildDefinition(node, false);
		}
		newParent.addChildDefinition(node);
	}

}
