package org.openforis.idm.metamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.utils.Dates;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.lang.DeepComparable;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.model.NodePathPointer;
import org.openforis.idm.model.Record;


/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 * @author E. Suprapto Wibowo
 */
public class Survey implements Serializable, Annotatable, DeepComparable {

	private static final long serialVersionUID = 1L;
	
	public static final String INTERNAL_NAME_REGEX = "[a-z][a-z0-9_]*";
	public static final Pattern INTERNAL_NAME_PATTERN = Pattern.compile(INTERNAL_NAME_REGEX);
	
	private Integer id;
	private String name;
	private LanguageSpecificTextMap projectNames;
	private String uri;
	private boolean published;
	private String cycle;
	private LanguageSpecificTextMap descriptions;
	private LinkedHashMap<String, ApplicationOptions> applicationOptionsMap;
	private List<ModelVersion> modelVersions;
	private List<CodeList> codeLists;
	private List<Unit> units;
	private List<SpatialReferenceSystem> spatialReferenceSystems;
	private List<String> languages;
	private Schema schema;
	private Map<String, String> namespaces;
	private ReferenceDataSchema referenceDataSchema;
	private int lastId;
	private Map<QName,String> annotations;

	private Date creationDate;
	private Date modifiedDate;

	private transient SurveyContext surveyContext;
	private transient SurveyDependencies surveyDependencies;
	
	protected Survey(SurveyContext surveyContext) {
		this.surveyContext = surveyContext;
		this.schema = new Schema(this);
		this.referenceDataSchema = new ReferenceDataSchema();
		this.lastId = 1;
		this.creationDate = this.modifiedDate = new Date();
		this.languages = new ArrayList<String>();
	}
	
	/**
	 * Initializes internal variables.
	 * To be called after survey unmarshalling process is complete.
	 */
	public void init() {
		initSchemaDefinitions();
		initCoordinateOperations();
	}

	private void initSchemaDefinitions() {
		Schema schema = getSchema();
		if ( schema != null ) {
			for (EntityDefinition entityDefinition : schema.getRootEntityDefinitions()) {
				entityDefinition.traverse(new NodeDefinitionVisitor() {
					@Override
					public void visit(NodeDefinition definition) {
						definition.init();
					}
				});
			}
		}
	}
	
	private void initCoordinateOperations() {
		CoordinateOperations coordinateOperations = getContext().getCoordinateOperations();
		coordinateOperations.registerSRS(getSpatialReferenceSystems());
	}
	
	public <R extends Record> R createRecord() {
		ModelVersion latestVersion = getLatestVersion();
		String versionName = latestVersion == null ? null : latestVersion.getName();
		return createRecord(versionName, getSchema().getFirstRootEntityDefinition());
	}
	
	public <R extends Record> R createRecord(String versionName, String rootEntityName) {
		return createRecord(versionName, getSchema().getRootEntityDefinition(rootEntityName));
	}
	
	@SuppressWarnings("unchecked")
	public <R extends Record> R createRecord(String versionName, EntityDefinition rootEntityDefinition) {
		return (R) new Record(this, versionName, rootEntityDefinition);
	}

	public void setLastId(int lastId) {
		if ( lastId < this.lastId ) {
			throw new IllegalArgumentException("lastId cannot be decreased");
		}
		this.lastId = lastId;
	}
	
	@Override
	public String getAnnotation(QName qname) {
		return annotations == null ? null : annotations.get(qname);
	}

	@Override
	public void setAnnotation(QName qname, String value) {
		if ( annotations == null ) {
			annotations = new HashMap<QName, String>();
		}
		if (StringUtils.isNotBlank(value)) {
			annotations.put(qname, value);
		} else {
			annotations.remove(qname);
		}
	}
	
	@Override
	public Set<QName> getAnnotationNames() {
		if ( annotations == null ) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableSet(annotations.keySet());
		}
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<LanguageSpecificText> getProjectNames() {
		if ( this.projectNames == null ) {
			return Collections.emptyList();
		} else {
			return this.projectNames.values();
		}
	}

	public String getProjectName() {
		return getProjectName(getDefaultLanguage());
	}
	
	public String getProjectName(String language) {
		return getProjectName(language, false);
	}
	
	public String getProjectName(String language, boolean defaultToDefaultLanguage) {
		if (projectNames == null) {
			return null;
		}
		return projectNames.getText(language, getDefaultLanguage(), defaultToDefaultLanguage);
	}
	
	public void setProjectName(String language, String text) {
		if ( projectNames == null ) {
			projectNames = new LanguageSpecificTextMap();
		}
		projectNames.setText(language, text);
	}
	
	public void addProjectName(LanguageSpecificText projectName) {
		if ( projectNames == null ) {
			projectNames = new LanguageSpecificTextMap();
		}
		projectNames.add(projectName);
	}

	public void removeProjectName(String language) {
		projectNames.remove(language);
	}
	
	public String getCycle() {
		return this.cycle;
	}
	
	public void setCycle(String cycle) {
		this.cycle = cycle;
	}

	public List<LanguageSpecificText> getDescriptions() {
		if ( descriptions == null ) {
			return Collections.emptyList();
		} else {
			return descriptions.values();
		}
	}

	public String getDescription() {
		return getDescription(getDefaultLanguage());
	}
	
	public String getDescription(String language) {
		return descriptions == null ? null: descriptions.getText(language, getDefaultLanguage());
	}
	
	public String getFailSafeDescription(String language) {
		return descriptions == null ? null: descriptions.getFailSafeText(language, getDefaultLanguage());
	}
	
	public void setDescription(String language, String description) {
		if ( descriptions == null ) {
			descriptions = new LanguageSpecificTextMap();
		}
		descriptions.setText(language, description);
	}
	
	public void addDescription(LanguageSpecificText description) {
		if ( descriptions == null ) {
			descriptions = new LanguageSpecificTextMap();
		}
		descriptions.add(description);
	}

	public void removeDescription(String language) {
		descriptions.remove(language);
	}

	/**
	 * Returns the list of model versions sorted by date.
	 */
	public List<ModelVersion> getSortedVersions() {
		if (this.modelVersions == null) {
			return Collections.emptyList();
		}
		List<ModelVersion> versions = new ArrayList<ModelVersion>(this.modelVersions);
		Collections.sort(versions, new Comparator<ModelVersion>() {
			public int compare(ModelVersion v1, ModelVersion v2) {
				return Dates.compareDateOnly(v1.getDate(), v2.getDate());
			}
		});
		return CollectionUtils.unmodifiableList(versions);
	}
	
	public List<ModelVersion> getVersions() {
		return CollectionUtils.unmodifiableList(this.modelVersions);
	}
	
	public ModelVersion getLatestVersion() {
		List<ModelVersion> sortedVersions = getSortedVersions();
		if (sortedVersions.isEmpty()) {
			return null;
		} else {
			return sortedVersions.get(sortedVersions.size() - 1);
		}
	}
	
	public void addVersion(ModelVersion version) {
		if ( modelVersions == null ) {
			modelVersions = new ArrayList<ModelVersion>();
		}
		modelVersions.add(version);
	}
	
	public void removeVersion(ModelVersion version) {
		if ( modelVersions != null ) {
			ModelVersion oldVersion = getVersionById(version.getId());
			modelVersions.remove(oldVersion);
			removeVersioningReferences(oldVersion);
		}
	}
	
	protected void removeVersioningReferences(ModelVersion version) {
		schema.removeVersioning(version);
		removeCodeListsVersioning(version);
	}

	protected void removeCodeListsVersioning(ModelVersion version) {
		List<CodeList> codeLists = getCodeLists();
		for (CodeList codeList : codeLists) {
			codeList.removeVersioningRecursive(version);
		}
	}

	public void moveVersion(ModelVersion version, int index) {
		CollectionUtils.shiftItem(modelVersions, version, index);
	}
	
	public void updateVersion(ModelVersion version) {
		ModelVersion oldVersion = getVersionById(version.getId());
		int index = modelVersions.indexOf(oldVersion);
		modelVersions.set(index, version);
	}

	public ModelVersion getVersionById(int id) {
		for (ModelVersion v : modelVersions) {
			if (id == v.getId() ) {
				return v;
			}
		}
		return null;
	}

	public List<CodeList> getCodeLists() {
		return CollectionUtils.unmodifiableList(this.codeLists);
	}
	
	public void addCodeList(CodeList codeList) {
		// TODO check survey in other methods as well
		// TODO check that code list id is not already in survey; same in other add methods as well
		// TODO check that code list id is <= lastId; same other add methods as well
		if ( codeList.getSurvey() != this ) {
			throw new IllegalArgumentException("Code list belongs to another survey");
		}
		
		if ( codeLists == null ) {
			codeLists = new ArrayList<CodeList>();
		}
		codeLists.add(codeList);
	}
	
	public void removeCodeList(CodeList codeList) {
		if ( codeLists != null ) {
			CodeList oldCodeList = getCodeListById(codeList.getId());
			codeLists.remove(oldCodeList);
		}
	}
	
	public void moveCodeList(CodeList codeList, int index) {
		CollectionUtils.shiftItem(codeLists, codeList, index);
	}
	
	public void updateCodeList(CodeList codeList) {
		CodeList oldCodeList = getCodeListById(codeList.getId());
		int index = codeLists.indexOf(oldCodeList);
		codeLists.set(index, codeList);
	}

	public CodeList getCodeListById(int id) {
		if ( codeLists != null) {
			for (CodeList c : codeLists) {
				if ( id == c.getId() ) {
					return c;
				}
			}
		}
		return null;
	}

	public List<Unit> getUnits() {
		return CollectionUtils.unmodifiableList(this.units);
	}
	
	public void addUnit(Unit unit) {
		if ( units == null ) {
			units = new ArrayList<Unit>();
		}
		units.add(unit);
	}
	
	public void removeUnit(Unit unit) {
		if ( units != null ) {
			Unit oldUnit = getUnitById(unit.getId());
			units.remove(oldUnit);
			removeReferences(unit);
		}
	}
	
	protected void removeReferences(Unit unit) {
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		Deque<NodeDefinition> stack = new LinkedList<NodeDefinition>();
		stack.addAll(rootEntities);
		while ( ! stack.isEmpty() ) {
			NodeDefinition defn = stack.pop();
			if ( defn instanceof EntityDefinition ) {
				stack.addAll(((EntityDefinition) defn).getChildDefinitions());
			} else if ( defn instanceof NumericAttributeDefinition ) {
				NumericAttributeDefinition numericAttrDefn = (NumericAttributeDefinition) defn;
				numericAttrDefn.removePrecisionDefinitions(unit);
			}
		}
	}

	public void moveUnit(Unit unit, int index) {
		CollectionUtils.shiftItem(units, unit, index);
	}
	
	public void updateUnit(Unit unit) {
		Unit oldUnit = getUnitById(unit.getId());
		int index = units.indexOf(oldUnit);
		units.set(index, unit);
	}

	protected Unit getUnitById(int id) {
		for (Unit i : units) {
			if ( id == i.getId() ) {
				return i;
			}
		}
		return null;
	}

	public List<SpatialReferenceSystem> getSpatialReferenceSystems() {
		return CollectionUtils.unmodifiableList(this.spatialReferenceSystems);
	}
	
	public SpatialReferenceSystem getSpatialReferenceSystem(String id) {
		if ( spatialReferenceSystems != null ) {
			for (SpatialReferenceSystem s : spatialReferenceSystems) {
				if ( id.equals(s.getId()) ) {
					return s;
				}
			}
		}
		return null;
	}
	
	public void addSpatialReferenceSystem(SpatialReferenceSystem srs) {
		if ( spatialReferenceSystems == null ) {
			spatialReferenceSystems = new ArrayList<SpatialReferenceSystem>();
		}
		spatialReferenceSystems.add(srs);
	}
	
	public void removeSpatialReferenceSystem(SpatialReferenceSystem srs) {
		spatialReferenceSystems.remove(srs);
	}
	
	public void moveSpatialReferenceSystem(SpatialReferenceSystem srs, int index) {
		CollectionUtils.shiftItem(spatialReferenceSystems, srs, index);
	}
	
	public void updateSpatialReferenceSystem(SpatialReferenceSystem srs) {
		SpatialReferenceSystem oldSpatialReferenceSystem = getSpatialReferenceSystemById(srs.getId());
		int index = spatialReferenceSystems.indexOf(oldSpatialReferenceSystem);
		spatialReferenceSystems.set(index, srs);
	}

	protected SpatialReferenceSystem getSpatialReferenceSystemById(String id) {
		for (SpatialReferenceSystem i : spatialReferenceSystems) {
			if ( id.equals(i.getId()) ) {
				return i;
			}
		}
		return null;
	}

	public Schema getSchema() {
		return this.schema;
	}
	
	public ModelVersion getVersion(String name) {
		if ( modelVersions != null && name != null ) {
			for (ModelVersion v : modelVersions) {
				if ( name.equals(v.getName()) ) {
					return v;
				}
			}
		}
		return null;
	}

	public CodeList getCodeList(String name) {
		if ( codeLists != null && name != null ) {
			for (CodeList codeList : codeLists) {
				if (name.equals(codeList.getName())) {
					return codeList;
				}
			}
		}
		return null;
	}

	public Unit getUnit(int id) {
		if ( units != null) {
			for (Unit unit : units) {
				if (unit.getId() == id) {
					return unit;
				}
			}
		}
		return null;
	}
	
	public Unit getUnit(String name) {
		if ( units != null && name != null ) {
			for (Unit unit : units) {
				if (name.equals(unit.getName())) {
					return unit;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <A extends ApplicationOptions> A getApplicationOptions(String type) {
		if ( applicationOptionsMap == null ) {
			return null;
		} else {
			return (A) applicationOptionsMap.get(type);
		}
	}

	public List<String> getApplicationOptionTypes() {
		if ( applicationOptionsMap == null ) {
			return Collections.emptyList();
		} else {
			List<String> types = new ArrayList<String>(applicationOptionsMap.keySet());
			return Collections.unmodifiableList(types);
		}		
	}
	
	public List<ApplicationOptions> getApplicationOptions() {
		if ( applicationOptionsMap == null ) {
			return Collections.emptyList();
		} else {
			List<ApplicationOptions> values = new ArrayList<ApplicationOptions>(applicationOptionsMap.values());
			return Collections.unmodifiableList(values);
		}
	}
	
	public void addApplicationOptions(ApplicationOptions options) {
		if ( applicationOptionsMap == null ) {
			this.applicationOptionsMap = new LinkedHashMap<String, ApplicationOptions>();
		}
		applicationOptionsMap.put(options.getType(), options);
	}
	
	public void removeApplicationOptions(String type) {
		if ( applicationOptionsMap != null ) {
			applicationOptionsMap.remove(type);
		}
	}
	
	public List<String> getLanguages() {
		return CollectionUtils.unmodifiableList(languages);
	}
	
	public String getDefaultLanguage() {
		if (languages.isEmpty()) {
			return null;
		} else {
			return languages.get(0);
		}
	}
	
	public boolean isDefaultLanguage(String langCode) {
		String defaultLanguage = getDefaultLanguage();
		return defaultLanguage.equals(langCode);
	}
	
	public void addLanguage(String lang) {
		if ( ! getLanguages().contains(lang) ) {
			languages.add(lang);
		}
		if ( languages.size() == 1 ) {
			/*
			 * Workaround: assign the default language to void language texts that can have been inserted 
			 * before a default language has been specified for the survey.
			 * TODO add languages to survey constructor
			 */
			String defaultLanguage = getDefaultLanguage();
			LanguageSpecificTextMap.assignDefaultToVoidLanguageText(descriptions, defaultLanguage);
			LanguageSpecificTextMap.assignDefaultToVoidLanguageText(projectNames, defaultLanguage);
		}
	}
	
	public void setDefaultLanguage(String lang) {
		if ( ! getLanguages().contains(lang) ) {
			addLanguage(lang);
		}
		moveLanguage(lang, 0);
	}
	
	public void removeLanguage(String lang) {
		if (languages.isEmpty()) {
			return;
		}
		languages.remove(lang);
	}
	
	public void moveLanguage(String language, int index) {
		CollectionUtils.shiftItem(languages, language, index);
	}
	
	@SuppressWarnings("unchecked")
	public <C extends SurveyContext> C getContext() {
		return (C) surveyContext;
	}
	
	public void setSurveyContext(SurveyContext surveyContext) {
		this.surveyContext = surveyContext;
	}
	
	public ReferenceDataSchema getReferenceDataSchema() {
		return referenceDataSchema;
	}
	
	public void setReferenceDataSchema(ReferenceDataSchema referenceDataSchema) {
		this.referenceDataSchema = referenceDataSchema;
	}
	
	public Set<NodePathPointer> getValidationDependencies(NodeDefinition definition) {
		return getSurveyDependencies().getValidationDependencies(definition);
	}
	
	public Set<NodePathPointer> getValidationSources(NodeDefinition definition) {
		return getSurveyDependencies().getValidationSources(definition);
	}
	
	// TODO move to ??
	public Set<NodePathPointer> getRelevanceDependencies(NodeDefinition definition) {
		return getSurveyDependencies().getRelevanceDependencies(definition);
	}
	
	public Set<NodePathPointer> getRelevanceSources(NodeDefinition definition) {
		return getSurveyDependencies().getRelevanceSources(definition);
	}
	
	public Set<NodeDefinition> getRelevanceSourceNodeDefinitions(NodeDefinition definition) {
		return getSurveyDependencies().getRelevanceSourceNodeDefinitions(definition);
	}

	public Set<NodePathPointer> getMinCountDependencies(NodeDefinition definition) {
		return getSurveyDependencies().getMinCountDependencies(definition);
	}
	
	public Set<NodePathPointer> getMinCountSources(NodeDefinition definition) {
		return getSurveyDependencies().getMinCountSources(definition);
	}
	
	public Set<NodePathPointer> getMaxCountDependencies(NodeDefinition definition) {
		return getSurveyDependencies().getMaxCountDependencies(definition);
	}
	
	public Set<NodePathPointer> getMaxCountSources(NodeDefinition definition) {
		return getSurveyDependencies().getMaxCountSources(definition);
	}
	
	public Set<NodePathPointer> getCalculatedValueDependencies(NodeDefinition definition) {
		return getSurveyDependencies().getCalculatedValueDependencies(definition);
	}
	
	public Set<NodePathPointer> getCalculatedValueSources(NodeDefinition definition) {
		return getSurveyDependencies().getCalculatedValueSources(definition);
	}
	
	public Set<NodePathPointer> getRelatedCodeDependencies(CodeAttributeDefinition definition) {
		return getSurveyDependencies().getRelatedCodeDependencies(definition);
	}
	
	public Set<NodePathPointer> getRelatedCodeSources(CodeAttributeDefinition definition) {
		return getSurveyDependencies().getRelatedCodeSources(definition);
	}
	
	
	public void refreshSurveyDependencies() {
		surveyDependencies = new SurveyDependencies(this);
		initCoordinateOperations();
	}

	private SurveyDependencies getSurveyDependencies() {
		if(surveyDependencies == null){
			surveyDependencies = new SurveyDependencies(this);
		}
		return surveyDependencies;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Survey other = (Survey) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Survey other = (Survey) obj;
		if (codeLists == null) {
			if (other.codeLists != null)
				return false;
		} else if (! CollectionUtils.deepEquals(codeLists, other.codeLists))
			return false;
		if (applicationOptionsMap == null) {
			if (other.applicationOptionsMap != null)
				return false;
		} else if (!applicationOptionsMap.equals(other.applicationOptionsMap))
			return false;
		if (cycle == null) {
			if (other.cycle != null)
				return false;
		} else if (!cycle.equals(other.cycle))
			return false;
		if (descriptions == null) {
			if (other.descriptions != null)
				return false;
		} else if (!descriptions.equals(other.descriptions))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (modelVersions == null) {
			if (other.modelVersions != null)
				return false;
		} else if (! CollectionUtils.deepEquals(modelVersions, other.modelVersions))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (projectNames == null) {
			if (other.projectNames != null)
				return false;
		} else if (!projectNames.equals(other.projectNames))
			return false;
		if (referenceDataSchema == null) {
			if (other.referenceDataSchema != null)
				return false;
		} else if (!referenceDataSchema.equals(other.referenceDataSchema))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.deepEquals(other.schema))
			return false;
		if (spatialReferenceSystems == null) {
			if (other.spatialReferenceSystems != null)
				return false;
		} else if (! CollectionUtils.deepEquals(spatialReferenceSystems, other.spatialReferenceSystems))
			return false;
		if (units == null) {
			if (other.units != null)
				return false;
		} else if (! CollectionUtils.deepEquals(units,  other.units))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	synchronized 
	public int nextId() {
		return ++lastId;
	}
	
	synchronized 
	public int getLastId() {
		return lastId;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Date getModifiedDate() {
		return modifiedDate;
	}
	
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	public ModelVersion createModelVersion(int id) {
		return new ModelVersion(this, id);
	}

	public ModelVersion createModelVersion() {
		return new ModelVersion(this, nextId());
	}
	
	public CodeList createCodeList(int id) {
		return new CodeList(this, id);
	}

	public CodeList createCodeList() {
		return new CodeList(this, nextId());
	}
	
	public Unit createUnit(int id) {
		return new Unit(this, id);
	}

	public Unit createUnit() {
		return new Unit(this, nextId());
	}
	
	public void addCustomNamespace(String uri, String prefix) {
		if ( namespaces == null ) {
			namespaces = new LinkedHashMap<String, String>();
		}
		namespaces.put(uri, prefix);
	}
	
	public void removeCustomNamespace(String uri) {
		if ( namespaces != null ) {
			namespaces.remove(uri);
		}
	}
	
	public List<String> getCustomNamespaces() {
		if ( namespaces == null ) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(new ArrayList<String>(namespaces.keySet()));
		}
	}
	public String getCustomNamespacePrefix(String uri) {
		if ( namespaces == null ) {
			return null;
		} else {
			return namespaces.get(uri);
		}
	}

}