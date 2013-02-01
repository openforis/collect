package org.openforis.collect.manager.speciesImport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.openforis.collect.manager.speciesImport.TaxonCSVReader.Column;
import org.openforis.collect.manager.speciesImport.TaxonParsingError.Type;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;

/**
 * 
 * @author S. Ricci
 *
 */
class TaxonTree {

	private List<Node> roots;

	Map<String, Node> scientificNameToNode;
	Map<String, Node> codeToNode;
	Map<Integer, Node> taxonIdToNode;

	
	TaxonTree() {
		super();
		roots = new ArrayList<Node>();
		scientificNameToNode = new HashMap<String, TaxonTree.Node>();
		codeToNode = new HashMap<String, TaxonTree.Node>();
		taxonIdToNode = new HashMap<Integer, TaxonTree.Node>();
	}
	
	public List<Node> getRoots() {
		return roots;
	}
	
	public void addNode(Taxon parent, Taxon taxon) {
		Node parentNode = findNodeByTaxon(parent);
		Node newNode;
		if ( parentNode == null ) {
			newNode = new Node(taxon);
			roots.add(newNode);
		} else {
			newNode = new Node(parentNode, taxon);
			parentNode.addChild(newNode);
		}
		index(newNode);
	}
	
	public void addVernacularName(Taxon taxon, TaxonVernacularName vernacularName) {
		Node node = findNodeByTaxon(taxon);
		node.addVernacularName(vernacularName);
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
		bfs(visitor);
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

	public void dfs(NodeVisitor visitor) {
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

	public void bfs(NodeVisitor visitor) {
		Stack<Node> stack = new Stack<Node>();
		stack.addAll(roots);
		while ( ! stack.isEmpty() ) {
			Node node = stack.pop();
			visitor.visit(node);
			if ( visitor instanceof NodeFinderVisitor && ((NodeFinderVisitor) visitor).isFound() ) {
				break;
			}
			if ( node.children != null ) {
				stack.addAll(node.children);
			}
		}
	}
	
	public void index(Taxon taxon) {
		Node node = findNodeByTaxon(taxon);
		index(node);
	}
	
	public void index(Node node) {
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

	public void checkDuplicateScienfificName(Taxon parent, String scientificName, long row) throws TaxonParsingException {
		Node foundNode = findNodeByScientificName(scientificName);
		if ( foundNode != null ) {
			Node foundParentNode = foundNode.getParent();
			Node parentNode = findNodeByTaxon(parent);
			if ( foundParentNode == parentNode ) {
				TaxonParsingError error = new TaxonParsingError(Type.DUPLICATE_VALUE, row, Column.SCIENTIFIC_NAME);
				throw new TaxonParsingException(error);
			}
		}
	}
	
	public void checkDuplicates(Integer taxonId, String code, long row) throws TaxonParsingException {
		if ( taxonId != null ) {
			checkDuplicateTaxonId(taxonId, row);
		}
		checkDuplicateCode(code, row);
	}

	protected void checkDuplicateTaxonId(Integer taxonId, long row) throws TaxonParsingException {
		Taxon oldTaxon = findTaxonByTaxonId(taxonId);
		if ( oldTaxon != null ) {
			TaxonParsingError error = new TaxonParsingError(Type.DUPLICATE_VALUE, row, Column.NO);
			throw new TaxonParsingException(error);
		}
	}
	
	private void checkDuplicateCode(String code, long row) throws TaxonParsingException {
		Taxon oldTaxon = findTaxonByCode(code);
		if ( oldTaxon != null ) {
			TaxonParsingError error = new TaxonParsingError(Type.DUPLICATE_VALUE, row, Column.CODE);
			throw new TaxonParsingException(error);
		}
	}
	
	static class Node {
		
		Node parent;
		Taxon taxon;
		List<Node> children;
		List<TaxonVernacularName> vernacularNames;
		
		public Node(Node parent, Taxon taxon) {
			super();
			this.parent = parent;
			this.taxon = taxon;
		}

		public Node(Taxon taxon) {
			this(null, taxon);
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
			return vernacularNames;
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
		
		public void addVernacularName(TaxonVernacularName vernacularName) {
			if ( vernacularNames  == null ) {
				vernacularNames = new ArrayList<TaxonVernacularName>();
			}
			vernacularNames.add(vernacularName);
		}
		
	}

	static interface NodeVisitor {
		void visit(Node node);
	}
	
	static interface NodeFinderVisitor extends NodeVisitor {
		boolean isFound();
		Node getFoundNode();
		void foundNode(Node foundNode);
	}
	
	abstract class AbstractNodeFinderVisitor implements NodeFinderVisitor {

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
