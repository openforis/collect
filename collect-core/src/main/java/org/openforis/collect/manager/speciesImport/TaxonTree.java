package org.openforis.collect.manager.speciesImport;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;

/**
 * 
 * @author S. Ricci
 *
 */
class TaxonTree {

	private List<Node> roots;
	private TaxonRank[] ranks;
	
	TaxonTree(TaxonRank[] ranks) {
		super();
		this.ranks = ranks;
		roots = new ArrayList<Node>();
	}
	
	public Taxon findTaxon(TaxonRank taxonRank, String scientificName) {
		List<Node> rankNodes = new ArrayList<Node>(roots);
		List<Node> nextRankNodes = new ArrayList<Node>();
		for (int rankIndex = 0; rankIndex < ranks.length; rankIndex++) {
			TaxonRank currentRank = ranks[rankIndex];
			for (Node node : rankNodes) {
				if ( currentRank == taxonRank ) {
					Taxon taxon = node.getTaxon();
					if ( scientificName.equals(taxon.getScientificName()) ) {
						return taxon;
					}
				}
				List<Node> children = node.getChildren();
				if ( children != null && children.size() > 0 ) {
					nextRankNodes.addAll(children);
				}
			}
			rankNodes.clear();
			rankNodes = nextRankNodes;
			nextRankNodes = new ArrayList<Node>();
		}
		return null;
	}

	public List<Node> getRoots() {
		return roots;
	}
	
	public void addNode(Taxon parent, Taxon taxon) {
		Node parentNode = findNode(parent);
		if ( parentNode == null ) {
			roots.add(new Node(taxon));
		} else {
			parentNode.addChild(taxon);
		}
	}
	
	public void addVernacularName(Taxon taxon, TaxonVernacularName vernacularName) {
		Node node = findNode(taxon);
		node.addVernacularName(vernacularName);
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

	public Node findNode(final Taxon taxon) {
		TaxonFinder visitor = new TaxonFinder(taxon);
		bfs(visitor);
		return visitor.getFoundNode();
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
	
	static interface NodeVisitor {
		void visit(Node node);
	}
	
	static interface NodeFinderVisitor extends NodeVisitor {
		boolean isFound();
		Node getFoundNode();
	}
	
	class TaxonFinder implements NodeFinderVisitor {
		private Taxon searchItem;
		private Node foundNode;
		
		public TaxonFinder(Taxon searchItem) {
			super();
			this.searchItem = searchItem;
			this.foundNode = null;
		}

		@Override
		public void visit(Node node) {
			if ( node.taxon == searchItem) {
				this.foundNode = node;
			}
		}
		
		@Override
		public boolean isFound() {
			return foundNode != null;
		}
		
		@Override
		public Node getFoundNode() {
			return foundNode;
		}
	}
	
	

	
}
