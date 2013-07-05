package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonTree {

	private List<Node> roots;

	Map<String, Node> scientificNameToNode;
	Map<String, Node> codeToNode;
	Map<Integer, Node> taxonIdToNode;
	Set<String> vernacularLanguageCodes;
	Integer lastTaxonId;
	Integer lastTaxonVernacularNameId;

	public TaxonTree() {
		super();
		roots = new ArrayList<Node>();
		scientificNameToNode = new HashMap<String, TaxonTree.Node>();
		codeToNode = new HashMap<String, TaxonTree.Node>();
		taxonIdToNode = new HashMap<Integer, TaxonTree.Node>();
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
		Stack<Node> stack = new Stack<Node>();
		addInverseOrderedItems(stack, roots);
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
	
	public List<TaxonSummary> toSummaries(final TaxonRank startFromRank) {
		return toSummaries(startFromRank, true);
	}
	
	public List<TaxonSummary> toSummaries(final TaxonRank startFromRank, final boolean includeGeneratedItems) {
		final List<TaxonSummary> result = new ArrayList<TaxonSummary>();
		depthFirstVisit(new NodeVisitor() {
			@Override
			public void visit(Node node) {
				Taxon taxon = node.getTaxon();
				boolean generated = taxon.getTaxonId() == null && StringUtils.isBlank(taxon.getCode());
				if ( taxon.getTaxonRank().compareTo(startFromRank) >= 0 && (includeGeneratedItems || ! generated)) {
					TaxonSummary summary = createSummary(node);
					result.add(summary);
				}
			}
		});
		return result;
	}

	protected TaxonSummary createSummary(Node node) {
		Taxon taxon = node.getTaxon();
		TaxonSummary summary = new TaxonSummary();
		summary.setTaxonSystemId(taxon.getSystemId());
		summary.setTaxonId(taxon.getTaxonId());
		summary.setCode(taxon.getCode());
		summary.setRank(taxon.getTaxonRank());
		summary.setScientificName(taxon.getScientificName());
		Node familyAncestor = node.getAncestor(TaxonRank.FAMILY);
		if ( familyAncestor != null ) {
			Taxon familyTaxon = familyAncestor.getTaxon();
			summary.setFamilyName(familyTaxon.getScientificName());
		}
		for (TaxonVernacularName taxonVernacularName : node.getVernacularNames()) {
			//if lang code is blank, vernacular name will be considered as synonym
			String languageCode = StringUtils.trimToEmpty(taxonVernacularName.getLanguageCode());
			summary.addVernacularName(languageCode, taxonVernacularName.getVernacularName());
		}
		return summary;
	}
	
	public Set<String> getVernacularLanguageCodes() {
		return CollectionUtils.unmodifiableSet(vernacularLanguageCodes);
	}
	
	public void assignSystemIds(int startFromTaxonId, int startFromVernacularNameId) {
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
