package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.TaxonSearchParameters;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonTree {

	private TaxonomyDefinition taxonDefinition;
	private List<Node> roots;

	Map<String, Node> scientificNameToNode;
	Map<String, Node> codeToNode;
	Map<Integer, Node> taxonIdToNode;
	Map<Long, Node> systemIdToNode;
	Set<String> vernacularLanguageCodes;
	Long lastTaxonId;
	Long lastTaxonVernacularNameId;

	public TaxonTree(TaxonomyDefinition taxonDefinition) {
		super();
		this.taxonDefinition = taxonDefinition;
		roots = new ArrayList<Node>();
		scientificNameToNode = new HashMap<String, TaxonTree.Node>();
		codeToNode = new HashMap<String, TaxonTree.Node>();
		taxonIdToNode = new HashMap<Integer, TaxonTree.Node>();
		systemIdToNode = new HashMap<Long, TaxonTree.Node>();
	}
	
	public List<Node> getRoots() {
		return roots;
	}
	
	public Node addNode(Taxon parent, Taxon taxon) {
		Node parentNode = findNodeByTaxon(parent);
		Node newNode;
		if ( parentNode == null ) {
			newNode = new Node(this, taxon);
			roots.add(newNode);
		} else {
			newNode = new Node(parentNode, taxon);
			parentNode.addChild(newNode);
		}
		index(newNode);
		return newNode;
	}
	
	public void addVernacularNames(Node node, Collection<TaxonVernacularName> vernacularNames) {
		for (TaxonVernacularName vernacularName : vernacularNames) {
			addVernacularName(node, vernacularName);
		}
	}
		
	public void addVernacularName(Taxon taxon, TaxonVernacularName vernacularName) {
		Node node = findNodeByTaxon(taxon);
		addVernacularName(node, vernacularName);
	}

	protected void addVernacularName(Node node,
			TaxonVernacularName vernacularName) {
		node.addVernacularName(vernacularName);
		addVernacularLanguageCode(vernacularName.getLanguageCode());
	}
	
	protected void addVernacularLanguageCode(String languageCode) {
		if ( vernacularLanguageCodes == null ) {
			vernacularLanguageCodes = new HashSet<String>();
		}
		vernacularLanguageCodes.add(languageCode);
	}

	public Node findNodeByTaxon(final Taxon taxon) {
		if (taxon == null) return null;
		
		Long taxonSystemId = taxon.getSystemId();
		if (taxonSystemId != null) {
			Node node = systemIdToNode.get(taxonSystemId);
			if (node != null) return node;
		}
		Integer taxonId = taxon.getTaxonId();
		if (taxonId != null) {
			Node node = taxonIdToNode.get(taxonId);
			if (node != null) return node;
		}
		NodeFinderVisitor visitor = new AbstractNodeFinderVisitor() {
			@Override
			public void visit(Node node) {
				if ( node.getTaxon() == taxon ) {
					foundNode(node);
				}
			}
		};
		depthFirstVisit(visitor);
		return visitor.getFoundNode();
	}
	
	public Taxon findTaxonByScientificName(String scientificName) {
		Node result = findNodeByScientificName(scientificName);
		return result == null ? null : result.getTaxon();
	}

	protected Node findNodeByScientificName(String scientificName) {
		Node result = scientificNameToNode.get(scientificName);
		return result;
	}

	public Taxon findTaxonByCode(String code) {
		Node result = codeToNode.get(code);
		return result == null ? null : result.getTaxon();
	}
	
	public Taxon findTaxonByTaxonId(Integer id) {
		Node result = taxonIdToNode.get(id);
		return result == null ? null : result.getTaxon();
	}

	public List<Taxon> findTaxaByCodeStartingWith(final String code, final TaxonRank rank) {
		final List<Taxon> result = new ArrayList<Taxon>();
		breadthFirstVisit(new NodeVisitor() {
			public void visit(Node node) {
				Taxon taxon = node.getTaxon();
				if (StringUtils.startsWithIgnoreCase(taxon.getCode(), code) && taxon.getTaxonRank() == rank) {
					result.add(taxon);
				}
			}
		});
		return result;
	}
	
	public List<TaxonOccurrence> findTaxonOccurrencesByCodeStartingWith(String code, TaxonRank rank, 
			TaxonSearchParameters parameters) {
		List<Taxon> taxa = findTaxaByCodeStartingWith(code, rank);
		return taxaToOccurrences(taxa, parameters);
	}
	
	public List<Taxon> findTaxaByScientificNameStartingWith(final String scientificName, final TaxonRank rank) {
		final List<Taxon> result = new ArrayList<Taxon>();
		breadthFirstVisit(new NodeVisitor() {
			public void visit(Node node) {
				Taxon taxon = node.getTaxon();
				if (StringUtils.startsWithIgnoreCase(taxon.getScientificName(), scientificName) && taxon.getTaxonRank() == rank) {
					result.add(taxon);
				}
			}
		});
		return result;
	}
	
	public List<TaxonOccurrence> findTaxonOccurrencesByScientificNameStartingWith(String scientificName, TaxonRank rank, 
			TaxonSearchParameters parameters) {
		List<Taxon> taxa = findTaxaByScientificNameStartingWith(scientificName, rank);
		return taxaToOccurrences(taxa, parameters);
	}
	
	/**
	 * 
	 * Breadth-first visits nodes in the tree
	 * 
	 */
	public void breadthFirstVisit(NodeVisitor visitor) {
		Queue<Node> queue = new LinkedList<TaxonTree.Node>();
		queue.addAll(roots);
		while ( ! queue.isEmpty() ) {
			Node node = queue.poll();
			visitor.visit(node);
			if ( visitor instanceof NodeFinderVisitor && ((NodeFinderVisitor) visitor).isFound() ) {
				break;
			}
			List<Node> children = node.getChildren();
			if ( children != null && children.size() > 0 ) {
				queue.addAll(children);
			}
		}
	}

	/**
	 * 
	 * Depth-first visits nodes in the tree
	 * 
	 */
	public void depthFirstVisit(NodeVisitor visitor) {
		depthFirstVisit(visitor, roots.toArray(new Node[roots.size()]));
	}
	
	public void depthFirstVisit(NodeVisitor visitor, Node... startFromNodes) {
		LinkedList<Node> stack = new LinkedList<Node>();
		addInverseOrderedItems(stack, Arrays.asList(startFromNodes));
		while ( ! stack.isEmpty() ) {
			Node node = stack.pop();
			visitor.visit(node);
			if ( visitor instanceof NodeFinderVisitor && ((NodeFinderVisitor) visitor).isFound() ) {
				break;
			}
			if ( node.children != null ) {
				addInverseOrderedItems(stack, node.children);
			}
		}
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	protected void addInverseOrderedItems(List list, List items) {
		if ( items != null ) {
			ListIterator it = items.listIterator(items.size());
			while ( it.hasPrevious() ) {
				list.add(it.previous());
			}
		}
	}
	
	public void updateNodeInfo(Taxon taxon) {
		index(taxon);
	}
	
	protected void index(Taxon taxon) {
		Node node = findNodeByTaxon(taxon);
		index(node);
	}
	
	protected void index(Node node) {
		Taxon taxon = node.getTaxon();
		scientificNameToNode.put(taxon.getScientificName(), node);
		String code = taxon.getCode();
		if ( code != null ) {
			codeToNode.put(code, node);
		}
		Integer taxonId = taxon.getTaxonId();
		if ( taxonId != null ) {
			taxonIdToNode.put(taxonId, node);
		}
		Long systemId = taxon.getSystemId();
		if ( systemId != null ) {
			systemIdToNode.put(systemId, node);
		}
	}

	public Node getDuplicateScienfificNameNode(Taxon parent, String scientificName) {
		Node foundNode = findNodeByScientificName(scientificName);
		if ( foundNode != null && (foundNode.getTaxon().getCode() != null || foundNode.getTaxon().getTaxonId() != null) ) {
			Node foundParentNode = foundNode.getParent();
			Node parentNode = findNodeByTaxon(parent);
			if ( ObjectUtils.equals(foundParentNode, parentNode) ) {
				return foundNode;
			}
		}
		return null;
	}
	
	public Node getNodeByTaxonId(Integer taxonId) {
		Node foundNode = taxonIdToNode.get(taxonId);
		return foundNode;
	}
	
	public Node getNodeByCode(String code) {
		Node foundNode = codeToNode.get(code);
		return foundNode;
	}
	
	public Node getNodeBySystemId(Long id) {
		Node foundNode = systemIdToNode.get(id);
		return foundNode;
	}
	
	public List<TaxonSummary> toSummaries(final TaxonRank startFromRank) {
		return toSummaries(startFromRank, true);
	}
	
	public List<TaxonSummary> toSummaries(final TaxonRank startFromRank, final boolean includeGeneratedItems) {
		final List<TaxonSummary> result = new ArrayList<TaxonSummary>();
		depthFirstVisit(new NodeVisitor() {
			@Override
			public void visit(Node node) {
				Taxon taxon = node.getTaxon();
				boolean generated = isGeneratedItem(taxon);
				if ( taxon.getTaxonRank().compareTo(startFromRank) >= 0 && (includeGeneratedItems || ! generated)) {
					Node familyNode = node.getAncestor(TaxonRank.FAMILY);
					Taxon familyTaxon = familyNode == null ? null : familyNode.getTaxon();
					result.add(new TaxonSummary(taxonDefinition, 
							taxon, node.getVernacularNames(), familyTaxon));
				}
			}

		});
		return result;
	}
	
	public List<Taxon> getDescendants(final Taxon rootTaxon) {
		final List<Taxon> result = new ArrayList<Taxon>();
		Node startingNode = getNodeBySystemId(rootTaxon.getSystemId());
		depthFirstVisit(new NodeVisitor() {
			public void visit(Node node) {
				if (! node.getTaxon().equals(rootTaxon)) {
					result.add(node.getTaxon());
				}
			}
		}, startingNode);
		return result;
	}
	
	public List<TaxonOccurrence> getDescendantOccurrences(Taxon rootTaxon, TaxonSearchParameters parameters) {
		List<Taxon> descendants = getDescendants(rootTaxon);
		return taxaToOccurrences(descendants, parameters);
	}

	private boolean isGeneratedItem(Taxon taxon) {
		return taxon.getTaxonId() == null && StringUtils.isBlank(taxon.getCode());
	}

	private List<TaxonOccurrence> taxaToOccurrences(List<Taxon> list, TaxonSearchParameters parameters) {
		List<Node> nodes = taxaToNodes(list);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>(nodes.size());
		for (Node node : nodes) {
			result.add(createOccurrence(node, parameters));
		}
		return result;
	}

	private List<Node> taxaToNodes(List<Taxon> list) {
		List<Node> result = new ArrayList<Node>(list.size());
		for (Taxon taxon : list) {
			Node node = getNodeBySystemId(taxon.getSystemId());
			result.add(node);
		}
		return result;
	}
	
	private TaxonOccurrence createOccurrence(Node node, TaxonSearchParameters parameters) {
		TaxonOccurrence occurrence = new TaxonOccurrence(node.getTaxon());
		if (parameters.isIncludeUniqueVernacularName()) {
			includeUniqueVernacularNameIfAny(node, occurrence);
		}
		if (parameters.isIncludeAncestorTaxons()) {
			includeAncestorTaxons(node, occurrence);
		}
		return occurrence;
	}
	
	private void includeAncestorTaxons(Node node, TaxonOccurrence occurrence) {
		Node parent = node.getParent();
		while (parent != null) {
			occurrence.addAncestorTaxon(new TaxonOccurrence(parent.getTaxon()));
			parent = parent.getParent();
		}
	}

	private void includeUniqueVernacularNameIfAny(Node node, TaxonOccurrence o) {
		List<TaxonVernacularName> vernacularNames = node.getVernacularNames();
		if (vernacularNames.size() == 1) {
			TaxonVernacularName vernacularName = vernacularNames.get(0);
			o.setVernacularName(vernacularName.getVernacularName());
			o.setLanguageCode(vernacularName.getLanguageCode());
			o.setLanguageVariety(vernacularName.getLanguageVariety());
		}
	}
	
	
	public Set<String> getVernacularLanguageCodes() {
		return CollectionUtils.unmodifiableSet(vernacularLanguageCodes);
	}
	
	public void assignSystemIds(long startFromTaxonId, long startFromVernacularNameId) {
		lastTaxonId = startFromTaxonId;
		lastTaxonVernacularNameId = startFromVernacularNameId;
		depthFirstVisit(new NodeVisitor() {
			public void visit(Node node) {
				Taxon taxon = node.getTaxon();
				taxon.setSystemId(lastTaxonId++);
				Node parent = node.getParent();
				if ( parent != null ) {
					Taxon parentTaxon = parent.getTaxon();
					taxon.setParentId(parentTaxon.getSystemId());
				}
				List<TaxonVernacularName> vernacularNames = node.getVernacularNames();
				for (TaxonVernacularName vernacularName : vernacularNames) {
					vernacularName.setId(lastTaxonVernacularNameId++);
					vernacularName.setTaxonSystemId(taxon.getSystemId());
				}
			}
		});
	}
	
	public static class Node {
		
		TaxonTree tree;
		Node parent;
		Taxon taxon;
		List<Node> children;
		List<TaxonVernacularName> vernacularNames;
		Map<String, Object> metadata;
		
		public Node(Node parent, Taxon taxon) {
			this(parent.getTree(), taxon);
			this.parent = parent;
		}

		public Node(TaxonTree tree, Taxon taxon) {
			this.tree = tree;
			this.taxon = taxon;
		}
		
		public Node getRoot() {
			Node currentNode = this;
			while ( currentNode.parent != null ) {
				currentNode = currentNode.parent;
			}
			return currentNode;
		}
		
		public Node getAncestor(TaxonRank rank) {
			Node currentNode = this;
			while ( currentNode.parent != null ) {
				currentNode = currentNode.parent;
				Taxon taxon = currentNode.getTaxon();
				if ( taxon.getTaxonRank() == rank ) {
					return currentNode;
				}
			}
			return null;
		}
		
		public void addChild(Taxon taxon) {
			Node node = new Node(this, taxon);
			addChild(node);
		}

		protected void addChild(Node node) {
			if ( children == null ) {
				children = new ArrayList<Node>();
			}
			children.add(node);
		}
		
		protected void addVernacularName(TaxonVernacularName vernacularName) {
			if ( vernacularNames  == null ) {
				vernacularNames = new ArrayList<TaxonVernacularName>();
			}
			vernacularNames.add(vernacularName);
		}
		
		public void addMetadata(String key, Object value) {
			if ( metadata == null ) {
				metadata = new HashMap<String, Object>();
			}
			metadata.put(key, value);
		}
		
		public Object getMetadata(String key) {
			if ( metadata == null ) {
				return null;
			} else {
				return metadata.get(key);
			}
		}
		
		public TaxonTree getTree() {
			return tree;
		}
		
		public Taxon getTaxon() {
			return taxon;
		}
		
		public Node getParent() {
			return parent;
		}
		
		public List<Node> getChildren() {
			return children;
		}
		
		public List<TaxonVernacularName> getVernacularNames() {
			return CollectionUtils.unmodifiableList(vernacularNames);
		}
		
		public TaxonVernacularName getVernacularName(String langCode) {
			for (TaxonVernacularName vernacularName: vernacularNames) {
				if (langCode.equalsIgnoreCase(vernacularName.getLanguageCode())) {
					return vernacularName;
				}
			}
			return null;
		}
		
		@Override
		public String toString() {
			return taxon == null ? "---EMPTY---" : taxon.toString();
		}
		
	}

	public static interface NodeVisitor {
		void visit(Node node);
	}
	
	public static interface NodeFinderVisitor extends NodeVisitor {
		boolean isFound();
		Node getFoundNode();
		void foundNode(Node foundNode);
	}
	
	public abstract class AbstractNodeFinderVisitor implements NodeFinderVisitor {

		protected Node foundNode;
		
		@Override
		public boolean isFound() {
			return foundNode != null;
		}
		
		@Override
		public Node getFoundNode() {
			return foundNode;
		}
		
		@Override
		public void foundNode(Node foundNode) {
			this.foundNode = foundNode;
		}
		
	}

}
