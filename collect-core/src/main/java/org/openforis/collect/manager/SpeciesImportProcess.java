package org.openforis.collect.manager;

import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.gbif.ecat.parser.UnparsableException;
import org.gbif.ecat.voc.Rank;
import org.openforis.collect.manager.SpeciesImportProcess.TaxonTree.Node;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxonomy;

/**
 * 
 * @author S. Ricci
 * 
 */
public class SpeciesImportProcess implements Callable<Void> {

	private static Log LOG = LogFactory.getLog(SpeciesImportProcess.class);

	private static final TaxonRank[] TAXON_RANKS = new TaxonRank[] {FAMILY, GENUS, SPECIES, SUBSPECIES};

	private static final String CSV = "csv";
	private static final String ZIP = "zip";
	private static final String VERNACULAR_NAMES_SEPARATOR = ",";

	public enum Column {
		ID(0, "no"), CODE(1, "code"), FAMILY(2, "family"), SCIENTIFIC_NAME(3, "scientific_name");
		
		private int index;
		private String name;
		
		private Column(int index, String name) {
			this.index = index;
			this.name = name;
		}
		
		public int getIndex() {
			return index;
		}
		
		public String getName() {
			return name;
		}
	}
	
	enum Step {
		STOPPED, PREPARING, RUNNING, COMPLETE, CANCELLED, ERROR
	}
	
	private SpeciesManager speciesManager;
	private String taxonomyName;
	private File file;
	private String errorMessage;
	private Step step;
	
	private TaxonTree taxonTree;
	private List<Integer> processedLineNumbers;
	
	public SpeciesImportProcess(SpeciesManager speciesManager, String taxonomyName, File file) {
		super();
		this.speciesManager = speciesManager;
		this.taxonomyName = taxonomyName;
		this.file = file;
		step = Step.STOPPED;
	}

	public void cancel() {
		step = Step.CANCELLED;
	}

	public boolean isRunning() {
		return step == Step.RUNNING;
	}

	public boolean isComplete() {
		return step == Step.COMPLETE;
	}

	public Step getStep() {
		return step;
	}

	@Override
	public Void call() throws Exception {
		step = Step.RUNNING;
		prepare();
		processFile();
		if ( step == Step.RUNNING ) {
			step = Step.COMPLETE;
		}
		return null;
	}

	protected void prepare() {
		processedLineNumbers = new ArrayList<Integer>();
		initCache();
	}

	protected void initCache() {
		taxonTree = new TaxonTree();
	}
	
	protected void processFile() throws IOException {
		String fileName = file.getName();
		String extension = FilenameUtils.getExtension(fileName);
		if ( CSV.equalsIgnoreCase(extension) ) {
			processCSV(file);
		} else if (ZIP.equals(extension) ) {
			processPackagedFile();
		} else {
			errorMessage = "File type not supported" + extension;
			LOG.error(errorMessage);
		}
	}

	protected void processPackagedFile() throws IOException {
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
			InputStream is = zipFile.getInputStream(zipEntry);
			String unzippedPath = file.getAbsolutePath() + "_unzipped.csv";
			FileOutputStream os = new FileOutputStream(unzippedPath);
			IOUtils.copy(is, os);
			processCSV(new File(unzippedPath));
		}
		zipFile.close();
	}

	protected void processCSV(File file) {
		for (TaxonRank rank : TAXON_RANKS) {
			if ( step == Step.RUNNING ) {
				processCSV(file, rank);
			}
		}
		if ( step == Step.RUNNING ) {
			persistTaxa();
		}
	}

	protected void processCSV(File file, TaxonRank rank) {
		FileInputStream is = null;
		InputStreamReader reader = null;
		try {
			is = new FileInputStream(file);
			reader = new InputStreamReader(is);
			CsvReader csvReader = new CsvReader(reader);
			validateHeaders(csvReader);
			CsvLine line = csvReader.readNextLine();
			int count = 2;
			while ( line != null ) {
				if ( ! processedLineNumbers.contains(count) ) {
					boolean processed = processLine(rank, line);
					if (processed) {
						processedLineNumbers.add(count);
					}
				}
				line = csvReader.readNextLine();
				count ++;
			}
		} catch (Exception e) {
			step = Step.ERROR;
			LOG.error("Error importing species CSV file", e);
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch (IOException e) {}
			}
			if ( is != null ) {
				try {
					is.close();
				} catch (IOException e) {}
			}
		}
	}

	protected void validateHeaders(CsvReader csvReader) throws IOException {
		csvReader.readHeaders();
		List<String> colNames = csvReader.getColumnNames();
		Column[] columns = Column.values();
		int fixedColsSize = columns.length;
		if ( colNames == null || colNames.size() < fixedColsSize ) {
			throw new RuntimeException("Expected at least " + fixedColsSize + " columns");
		}
		for (int i = 0; i < fixedColsSize; i++) {
			String colName = colNames.get(i);
			String expectedColName = columns[i].getName();
			if ( ! expectedColName.equals(colName) ) {
				throw new RuntimeException("Invalid column name: " + colName + " - '"+ expectedColName +"' expected");
			}
		}
		for (int i = fixedColsSize; i < colNames.size(); i ++ ) {
			String colName = colNames.get(i);
			if ( ! Languages.exists(Languages.Standard.ISO_639_3, colName) ) {
				throw new RuntimeException("Invalid column name: " + colName + " - valid lanugage code (ISO-639-3) expected");
			}
		}
	}
	
	protected boolean processLine(TaxonRank rank, CsvLine line) {
		String familyName = normalize(line.getValue(Column.FAMILY.getIndex(), String.class));
		String rawScientificName = normalize(line.getValue(Column.SCIENTIFIC_NAME.getIndex(), String.class));
		boolean leaf = isLeaf(rank, line );
		switch (rank) {
		case FAMILY:
			createTaxonFamily(null, familyName, line);
			return leaf;
		case GENUS:
			createTaxonGenus(familyName, rawScientificName, line);
			return leaf;
		default:
			Taxon parent = findParentTaxon(line);
			if ( parent == null ) {
				return false;
			} else {
				createTaxon(line, rank, parent);
				return true;
			}
		}
	}

	protected void processVernacularNames(Taxon taxon, CsvLine line) {
		List<String> columnNames = line.getColumnNames();
		Column[] fixedColumns = Column.values();
		for ( int i = fixedColumns.length; i < columnNames.size(); i++ ) {
			String langCode = columnNames.get(i);
			String colValue = line.getValue(i, String.class);
			if ( colValue != null ) {
				List<String> vernacularNames = extractVernacularNames(colValue);
				for (String vernacularName : vernacularNames) {
					TaxonVernacularName taxonVN = new TaxonVernacularName();
					taxonVN.setLanguageCode(langCode);
					taxonVN.setVernacularName(normalize(vernacularName));
					taxonTree.addVernacularName(taxon, taxonVN);
				}
			}
		}
	}

	protected void persistTaxa() {
		Taxonomy taxonomy = new Taxonomy();
		taxonomy.setName(taxonomyName);
		speciesManager.save(taxonomy);
		Integer taxonomyId = taxonomy.getId();
		List<Node> roots = taxonTree.getRoots();
		Stack<Node> stack = new Stack<Node>();
		stack.addAll(roots);
		while ( ! stack.isEmpty() ) {
			Node node = stack.pop();
			persistTaxonTreeNode(taxonomyId, node);
			List<Node> children = node.getChildren();
			if ( children != null && ! children.isEmpty() ) {
				stack.addAll(children);
			}
		}
	}

	protected void persistTaxonTreeNode(Integer taxonomyId, Node node) {
		Taxon taxon = node.getTaxon();
		taxon.setTaxonomyId(taxonomyId);
		Node parent = node.getParent();
		if ( parent != null ) {
			Taxon parentTaxon = parent.getTaxon();
			taxon.setParentId(parentTaxon.getSystemId());
		}
		speciesManager.save(taxon);
		
		List<TaxonVernacularName> vernacularNames = node.getVernacularNames();
		if ( vernacularNames != null ) {
			for (TaxonVernacularName vernacularName : vernacularNames) {
				vernacularName.setTaxonSystemId(taxon.getSystemId());
				speciesManager.save(vernacularName);
			}
		}
	}

	private List<String> extractVernacularNames(String value) {
		String normalized = value.replaceAll("/", VERNACULAR_NAMES_SEPARATOR);
		String[] split = StringUtils.split(normalized, VERNACULAR_NAMES_SEPARATOR);
		List<String> result = new ArrayList<String>();
		for (String splitPart : split) {
			String trimmed = splitPart.replaceAll("^\\s+|\\s+$|;+$|\\.+$", "");
			if ( trimmed.length() > 0 ) {
				result.add(trimmed);
			}
		}
		return result;
	}

	private String normalize(String vernacularName) {
		return StringUtils.normalizeSpace(vernacularName);
	}

	private Taxon findParentTaxon(CsvLine line) {
		String rawScientificName = parseScientificName(line);
		TaxonRank rank = parseRank(rawScientificName);
		TaxonRank parentRank = rank.getParent();
		String scientificName;
		switch ( parentRank ) {
		case FAMILY:
			scientificName = parseFamilyName(line);
			break;
		case GENUS:
			scientificName = parseGenus(rawScientificName);
			break;
		default:
			throw new RuntimeException("Unsupported rank");
		}
		Taxon result = taxonTree.findTaxon(parentRank, scientificName);
		return result;
	}
	
	protected Taxon createTaxonFamily(Taxon parent, String familyName, CsvLine line) {
		String normalizedScientificName = normalize(familyName);
		return createTaxon(line, FAMILY, parent, normalizedScientificName);
	}
	
	protected Taxon createTaxonGenus(String familyName, String scientificName, CsvLine line) {
		Taxon parent = taxonTree.findTaxon(FAMILY, familyName);
		String normalizedScientificName = parseGenus(scientificName);
		return createTaxon(line, GENUS, parent, normalizedScientificName);
	}

	protected Taxon createTaxon(CsvLine line, TaxonRank rank, Taxon parent) {
		String normalizedScientificName = parseCanonicalScientificName(line);
		return createTaxon(line, rank, parent, normalizedScientificName);
	}
	
	protected Taxon createTaxon(CsvLine line, TaxonRank rank, Taxon parent, String normalizedScientificName) {
		Taxon taxon = taxonTree.findTaxon(rank, normalizedScientificName);
		if ( taxon == null ) {
			taxon = new Taxon();
			taxon.setTaxonRank(rank);
			taxon.setScientificName(normalizedScientificName);
			taxonTree.addNode(parent, taxon);
		}
		if ( isLeaf(rank, line) ) {
			String code = parseCode(line);
			taxon.setCode(code);
			Integer taxonId = parseTaxonId(line);
			taxon.setTaxonId(taxonId);
			processVernacularNames(taxon, line);
		}
		return taxon;
	}

	protected Integer parseTaxonId(CsvLine line) {
		Integer value = line.getValue(Column.ID.getIndex(), Integer.class);
		return value;
	}
	
	protected String parseCode(CsvLine line) {
		return normalize(line.getValue(Column.CODE.getIndex(), String.class));
	}
	
	protected String parseFamilyName(CsvLine line) {
		return normalize(line.getValue(Column.FAMILY.getIndex(), String.class));
	}
	
	protected String parseScientificName(CsvLine line) {
		String value = normalize(line.getValue(Column.SCIENTIFIC_NAME.getIndex(), String.class));
		return value;
	}
	
	protected TaxonRank parseRank(String rawScientificName) {
		try {
			NameParser nameParser = new NameParser();
			ParsedName<Object> parsedName = nameParser.parse(rawScientificName);
			Rank rank = parsedName.getRank();
			TaxonRank taxonRank;
			switch ( rank ) {
			case FAMILY:
				taxonRank = FAMILY;
				break;
			case GENUS:
				taxonRank = GENUS;
				break;
			case SPECIES:
				taxonRank = SPECIES;
				break;
			case SUBSPECIES:
				taxonRank = SUBSPECIES;
				break;
			default:
				taxonRank = SPECIES;
			}
			return taxonRank;
		} catch (UnparsableException e) {
			LOG.error("Error extracting rank from: " + rawScientificName, e);
			throw new RuntimeException(e);
		}
	}

	protected String parseCanonicalScientificName(CsvLine line) {
		String rawName = parseScientificName(line);
		return parseCanonicalScientificName(rawName);
	}

	protected String parseCanonicalScientificName(String rawScientificName) {
		try {
			NameParser nameParser = new NameParser();
			ParsedName<Object> parsedName;
			parsedName = nameParser.parse(rawScientificName);
			String result = parsedName.canonicalName();
			return result;
		} catch (UnparsableException e) {
			LOG.error("Error extracting scientific name from: " + rawScientificName, e);
			throw new RuntimeException(e);
		}
	}
	
	protected String parseGenus(String scientificName) {
		try {
			NameParser nameParser = new NameParser();
			ParsedName<Object> parsedName;
			parsedName = nameParser.parse(scientificName);
			String genus = parsedName.getGenusOrAbove();
			return genus;
		} catch (UnparsableException e) {
			LOG.error("Error extracting genus from: " + scientificName, e);
			throw new RuntimeException(e);
		}
	}

	private boolean isLeaf(TaxonRank rank, CsvLine line) {
		String rawScientificName = parseScientificName(line);
		TaxonRank lineRank = parseRank(rawScientificName);
		return lineRank == rank;
	}
	
	class TaxonTree {
		
		List<Node> roots;
		
		TaxonTree() {
			super();
			roots = new ArrayList<Node>();
		}
		
		public Taxon findTaxon(TaxonRank taxonRank, String scientificName) {
			List<Node> rankNodes = new ArrayList<Node>(roots);
			List<Node> nextRankNodes = new ArrayList<Node>();
			for (int rankIndex = 0; rankIndex < TAXON_RANKS.length; rankIndex++) {
				TaxonRank currentRank = TAXON_RANKS[rankIndex];
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
		
		class Node {
			
			Node parent;
			Taxon taxon;
			List<Node> children;
			List<TaxonVernacularName> vernacularNames;
			
			private Node(Node parent, Taxon taxon) {
				super();
				this.parent = parent;
				this.taxon = taxon;
			}

			private Node(Taxon taxon) {
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
			
			void addChild(Taxon taxon) {
				Node node = new Node(this, taxon);
				if ( children == null ) {
					children = new ArrayList<Node>();
				}
				children.add(node);
			}
			
			void addVernacularName(TaxonVernacularName vernacularName) {
				if ( vernacularNames  == null ) {
					vernacularNames = new ArrayList<TaxonVernacularName>();
				}
				vernacularNames.add(vernacularName);
			}
			
		}

		public Node findNode(Taxon taxon) {
			Stack<Node> stack = new Stack<Node>();
			stack.addAll(roots);
			while ( ! stack.isEmpty() ) {
				Node node = stack.pop();
				if ( node.taxon == taxon ) {
					return node;
				}
				if ( node.children != null ) {
					stack.addAll(node.children);
				}
			}
			return null;
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
		
	}

}
