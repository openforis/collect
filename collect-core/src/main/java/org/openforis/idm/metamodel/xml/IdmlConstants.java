package org.openforis.idm.metamodel.xml;


/**
 * 
 * @author G. Miceli
 *
 */
public interface IdmlConstants {
	static String IDML3_NAMESPACE_URI = "http://www.openforis.org/idml/3.0";

	// Standard Xml 
	static String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
	static String XML_NAMESPACE_PREFIX = "xml";
	static String XML_LANG_ATTRIBUTE = "lang";

	// Survey
	static String SURVEY = "survey";
	static String LAST_ID = "lastId";
	static String PUBLISHED = "published";
	static String LANGUAGE = "language";
	static String URI = "uri";
	static String CYCLE = "cycle";
	static String PROJECT = "project";

	// Application Options
	static String APPLICATION_OPTIONS = "applicationOptions";
	static String OPTIONS = "options";

	// Versioning
	static String VERSIONING = "versioning";
	static String VERSION = "version";
	
	// Code lists
	static String CODE_LISTS = "codeLists";
	static String LIST = "list";
	static String HIERARCHY = "hierarchy";
	static String LEVEL = "level";
	static String QUALIFIABLE = "qualifiable";
	static String SCOPE = "scope";
	static String CODING_SCHEME = "codingScheme";
	static String ITEMS = "items";
	static String ITEM = "item";

	// SRSs
	static String SPATIAL_REFERENCE_SYSTEMS = "spatialReferenceSystems";
	static String SRID = "srid";
	static String SPATIAL_REFERENCE_SYSTEM = "spatialReferenceSystem";
	static String WKT = "wkt";

	// Reference data schema
	static String REFERENCE_DATA_SCHEMA = "referenceDataSchema";
	static String SAMPLING_POINT = "samplingPoint";
	static String ATTRIBUTE = "attribute";
	static String TAXONOMY_NAME = "taxonomy_name";
	
	// Schema
	static String SCHEMA = "schema";
	static String ENTITY = "entity";

	// All nodes
	static String NAME = "name";
	static String MAX_COUNT = "maxCount";
	static String MULTIPLE = "multiple";
	static String MIN_COUNT = "minCount";
	static String RELEVANT = "relevant";
	static String REQUIRED_IF = "requiredIf";
	static String REQUIRED = "required";
	static String SINCE = "since";
	static String DEPRECATED = "deprecated";
	
	// Entities
	static String VIRTUAL = "virtual";
	static String GENERATOR_EXPRESSION = "generatorExpression";
	
	// Attributes
	static String FIELD = "field";
	static String FIELD_LABEL = "fieldLabel";
	static String TEXT = "text";
	static String BOOLEAN = "boolean";
	static String CALCULATED = "calculated";
	static String COORDINATE = "coordinate";
	static String FILE = "file";
	static String NUMBER = "number";
	static String DATE = "date";
	static String TIME = "time";
	static String RANGE = "range";
	
	// Attribute options
	static String DEFAULT = "default";
	static String DECIMAL_DIGITS = "decimalDigits";
	static String PRECISION = "precision";
	static String AFFIRMATIVE_ONLY = "affirmativeOnly";
	static String LOOKUP = "lookup";
	static String STRICT = "strict";
	static String ALLOW_VALUES_SORTING = "allow_values_sorting";
	static String PARENT = "parent";
	static String KEY = "key";
	static String UNITS = "units";
	static String CONVERSION_FACTOR = "conversionFactor";
	static String DIMENSION = "dimension";
	static String UNIT = "unit";
	static String ABBREVIATION = "abbreviation";
	static String HIGHEST_RANK = "highestRank";
	static String TAXONOMY = "taxonomy";
	static String TAXON = "taxon";
	static String QUALIFIERS = "qualifiers";
	static String EXTENSIONS = "extensions";
	static String MAX_SIZE = "maxSize";
	static String REFERENCED_ATTRIBUTE = "referencedAttribute";
	
	// Checks
	static String FLAG = "flag";
	static String MESSAGE = "message";
	static String COMPARE = "compare";
	static String GTE = "gte";
	static String GT = "gt";
	static String LTE = "lte";
	static String LT = "lt";
	static String EQ = "eq";
	static String DISTANCE = "distance";
	static String FROM = "from";
	static String TO = "to";
	static String MAX = "max";
	static String MIN = "min";
	static String CHECK = "check";
	static String UNIQUE = "unique";
	static String PATTERN = "pattern";
	static String REGEX = "regex";

	// Shared
	static String ID = "id";
	static String LABEL = "label";
	static String DESCRIPTION = "description";
	static String PROMPT = "prompt";
	static String VALUE = "value";
	static String EXPR = "expr";
	static String IF = "if";
	static String CODE = "code";
	static String TYPE = "type";
	static String CREATED = "created";
	static String MODIFIED = "modified";

}
