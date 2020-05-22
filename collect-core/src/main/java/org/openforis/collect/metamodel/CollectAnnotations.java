package org.openforis.collect.metamodel;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.UI_NAMESPACE_URI;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.ui.UIOptions.CodeAttributeLayoutType;
import org.openforis.collect.metamodel.ui.UIOptions.Orientation;
import org.openforis.collect.metamodel.ui.UIOptionsConstants;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.Annotatable;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;

/**
 *
 * @author S. Ricci
 *
 */
public class CollectAnnotations {

	public static final String COLLECT_3_NAMESPACE_PREFIX = "http://www.openforis.org/collect/3.0/";
	public static final String COLLECT_NAMESPACE_URI_SUFFIX = "collect";
	public static final String COLLECT_MOBILE_NAMESPACE_URI_SUFFIX = "mobile";
	public static final String COLLECT_EARTH_NAMESPACE_URI_SUFFIX = "collectearth";
	public static final String COLLECT_NAMESPACE_URI = COLLECT_3_NAMESPACE_PREFIX + COLLECT_NAMESPACE_URI_SUFFIX;
	public static final String COLLECT_MOBILE_NAMESPACE_URI = COLLECT_3_NAMESPACE_PREFIX + COLLECT_MOBILE_NAMESPACE_URI_SUFFIX;
	public static final String COLLECT_EARTH_NAMESPACE_URI = COLLECT_3_NAMESPACE_PREFIX + COLLECT_EARTH_NAMESPACE_URI_SUFFIX;

	public enum FileType {
		IMAGE, AUDIO, VIDEO, DOCUMENT
	}

	public enum TextInput {
		KEYBOARD, BARCODE
	}

	public enum Annotation {
		//collect namespace
		INCLUDE_IN_DATA_EXPORT(new QName(COLLECT_NAMESPACE_URI, "includeInDataExport"), true),
		CALCULATED_ONLY_ONE_TIME(new QName(COLLECT_NAMESPACE_URI, "calculatedOnlyOneTime"), false),
		PHASE_TO_APPLY_DEFAULT_VALUE(new QName(COLLECT_NAMESPACE_URI, "phaseToApplyDefaultValue"), Step.ENTRY),
		EDITABLE(new QName(COLLECT_NAMESPACE_URI, "editable"), true),
		FILE_TYPE(new QName(COLLECT_NAMESPACE_URI, "fileType"), FileType.IMAGE),
		MEASUREMENT(new QName(COLLECT_NAMESPACE_URI, "measurement"), false),
		TEXT_INPUT(new QName(COLLECT_NAMESPACE_URI, "textInput"), TextInput.KEYBOARD),
		TARGET(new QName(COLLECT_NAMESPACE_URI, "target"), SurveyTarget.COLLECT_DESKTOP),
		COLLECT_VERSION(new QName(COLLECT_NAMESPACE_URI, "collectVersion"), "3.4.0"),
		GEOMETRY(new QName(COLLECT_NAMESPACE_URI, "geometry"), false),
		SHOW_IN_MAP_BALLOON(new QName(COLLECT_NAMESPACE_URI, "showInMapBalloon"), true),
		KEY_CHANGE_ALLOWED(new QName(COLLECT_NAMESPACE_URI, "keyChangeAllowed"), true),
		QUALIFIER(new QName(COLLECT_NAMESPACE_URI, UIOptionsConstants.QUALIFIER), false),
		TAXON_ATTRIBUTE_ALLOW_UNLISTED(new QName(COLLECT_NAMESPACE_URI, "allowUnlisted"), true),
		ENUMERATE(new QName(COLLECT_NAMESPACE_URI, "enumerate"), false),
		AUTO_GENERATE_MIN_ITEMS(new QName(COLLECT_NAMESPACE_URI, "autoGenerateMinItems"), false),

		//ui namespace
		TAB_SET(new QName(UI_NAMESPACE_URI, UIOptionsConstants.TAB_SET_NAME)),
		TAB_NAME(new QName(UI_NAMESPACE_URI, UIOptionsConstants.TAB)),
		LAYOUT(new QName(UI_NAMESPACE_URI, UIOptionsConstants.LAYOUT)),
		DIRECTION(new QName(UI_NAMESPACE_URI, UIOptionsConstants.DIRECTION)),
		COUNT_IN_SUMMARY_LIST(new QName(UI_NAMESPACE_URI, UIOptionsConstants.COUNT)),
		SHOW_IN_SUMMARY_LIST(new QName(UI_NAMESPACE_URI, UIOptionsConstants.SUMMARY), false),
		SHOW_ROW_NUMBERS(new QName(UI_NAMESPACE_URI, UIOptionsConstants.SHOW_ROW_NUMBERS)),
		AUTOCOMPLETE(new QName(UI_NAMESPACE_URI, UIOptionsConstants.AUTOCOMPLETE)),
		FIELDS_ORDER(new QName(UI_NAMESPACE_URI, UIOptionsConstants.FIELDS_ORDER)),
		VISIBLE_FIELDS(new QName(UI_NAMESPACE_URI, UIOptionsConstants.VISIBLE_FIELDS)),
		SHOW_ALLOWED_VALUES_PREVIEW(new QName(UI_NAMESPACE_URI, UIOptionsConstants.SHOW_ALLOWED_VALUES_PREVIEW), false),
		HIDE(new QName(UI_NAMESPACE_URI, UIOptionsConstants.HIDE), false),
		HIDE_WHEN_NOT_RELEVANT(new QName(UI_NAMESPACE_URI, UIOptionsConstants.HIDE_WHEN_NOT_RELEVANT), false),
		COLUMN(new QName(UI_NAMESPACE_URI, UIOptionsConstants.COLUMN), 1),
		COLUMN_SPAN(new QName(UI_NAMESPACE_URI, UIOptionsConstants.COLUMN_SPAN), 1),
		CODE_ATTRIBUTE_LAYOUT_DIRECTION(new QName(UI_NAMESPACE_URI, UIOptionsConstants.DIRECTION), Orientation.VERTICAL),
		CODE_ATTRIBUTE_LAYOUT_TYPE(new QName(UI_NAMESPACE_URI, UIOptionsConstants.LAYOUT_TYPE), CodeAttributeLayoutType.TEXT),
		CODE_ATTRIBUTE_SHOW_CODE(new QName(UI_NAMESPACE_URI, UIOptionsConstants.SHOW_CODE), true),
		TAXON_ATTRIBUTE_SHOW_FAMILY(new QName(UI_NAMESPACE_URI, UIOptionsConstants.SHOW_FAMILY), false),
		TAXON_ATTRIBUTE_INCLUDE_UNIQUE_VERNACULAR_NAME(new QName(UI_NAMESPACE_URI, UIOptionsConstants.INCLUDE_UNIQUE_VERNACULAR_NAME), false),
		WIDTH(new QName(UI_NAMESPACE_URI, UIOptionsConstants.WIDTH)),
		LABEL_WIDTH(new QName(UI_NAMESPACE_URI, UIOptionsConstants.LABEL_WIDTH)),
		LABEL_ORIENTATION(new QName(UI_NAMESPACE_URI, UIOptionsConstants.LABEL_ORIENTATION), Orientation.HORIZONTAL),
		AUTO_UPPERCASE(new QName(UI_NAMESPACE_URI, UIOptionsConstants.AUTO_UPPERCASE), false),
		BACKGROUND_COLOR(new QName(UI_NAMESPACE_URI, UIOptionsConstants.BACKGROUND_COLOR)),
		BACKGROUND_ALPHA(new QName(UI_NAMESPACE_URI, UIOptionsConstants.BACKGROUND_ALPHA), 0.5),
		COORDINATE_ATTRIBUTE_SHOW_SRS_FIELD(new QName(UI_NAMESPACE_URI, UIOptionsConstants.SHOW_SRS_FIELD), true),
		COORDINATE_ATTRIBUTE_INCLUDE_ALTITUDE(new QName(UI_NAMESPACE_URI, UIOptionsConstants.INCLUDE_ALTITUDE), false),
		COORDINATE_ATTRIBUTE_INCLUDE_ACCURACY(new QName(UI_NAMESPACE_URI, UIOptionsConstants.INCLUDE_ACCURACY), false),

		//collect earth
		COLLECT_EARTH_FROM_CSV(new QName(COLLECT_EARTH_NAMESPACE_URI, "fromcsv"), false),
		COLLECT_EARTH_HIDE_IN_RECORD_LIST(new QName(COLLECT_EARTH_NAMESPACE_URI, "hideinrecordlist"), false),
		COLLECT_EARTH_SHOW_READONLY_FIELD(new QName(COLLECT_EARTH_NAMESPACE_URI, "showreadonlyfield"), false),
		COLLECT_EARTH_INCLUDE_IN_HEADER(new QName(COLLECT_EARTH_NAMESPACE_URI, "includeinheader"), false),
		COLLECT_EARTH_PLOT_AREA(new QName(COLLECT_EARTH_NAMESPACE_URI, "plotarea"), 1d),
		COLLECT_EARTH_BING_KEY(new QName(COLLECT_EARTH_NAMESPACE_URI, "bingKey"), "GENERATE YOUR OWN BING MAPS KEY AT https://www.bingmapsportal.com"),
		COLLECT_EARTH_PLANET_KEY(new QName(COLLECT_EARTH_NAMESPACE_URI, "planetKey"), "GENERATE YOUR OWN PLANET API KEY AT https://www.planet.com/"),
		COLLECT_EARTH_EXTRA_MAP_URL(new QName(COLLECT_EARTH_NAMESPACE_URI, "extraMapUrl")),
		COLLECT_EARTH_SAMPLE_POINTS(new QName(COLLECT_EARTH_NAMESPACE_URI, "samplepoints"), 25), //0, 1, 9 (3x3), 25 (5x5), 49 (7x7)
		COLLECT_EARTH_OPEN_BING_MAPS(new QName(COLLECT_EARTH_NAMESPACE_URI, "openBingMaps"), false),
		COLLECT_EARTH_OPEN_EARTH_MAP(new QName(COLLECT_EARTH_NAMESPACE_URI, "openEarthMap"), false),
		COLLECT_EARTH_OPEN_PLANET_MAPS(new QName(COLLECT_EARTH_NAMESPACE_URI, "openPlanetMaps"), false),
		COLLECT_EARTH_OPEN_YANDEX_MAPS(new QName(COLLECT_EARTH_NAMESPACE_URI, "openYandexMaps"), false),
		COLLECT_EARTH_OPEN_GEE_EXPLORER(new QName(COLLECT_EARTH_NAMESPACE_URI, "openExplorer"), false),
		COLLECT_EARTH_OPEN_GEE_CODE_EDITOR(new QName(COLLECT_EARTH_NAMESPACE_URI, "openCodeEditor"), false),
		COLLECT_EARTH_OPEN_GEE_APP(new QName(COLLECT_EARTH_NAMESPACE_URI, "openGEEApp"), true),
		COLLECT_EARTH_OPEN_SECUREWATCH(new QName(COLLECT_EARTH_NAMESPACE_URI, "openSecureWatch"), false),
		COLLECT_EARTH_OPEN_STREET_VIEW(new QName(COLLECT_EARTH_NAMESPACE_URI, "openStreetView"), false),

		//Collect Mobile
		COLLECT_MOBILE_ALLOW_ONLY_DEVICE_COORDINATE(new QName(COLLECT_MOBILE_NAMESPACE_URI, "allowOnlyDeviceCoordinate"), false),
		;

		private QName qName;
		private Object defaultValue;

		private Annotation(QName qname) {
			this.qName = qname;
		}

		private Annotation(QName qname, Object defaultValue) {
			this(qname);
			this.defaultValue = defaultValue;
		}

		public QName getQName() {
			return qName;
		}

		@SuppressWarnings("unchecked")
		public <T extends Object> T getDefaultValue() {
			return (T) defaultValue;
		}
	}

	private CollectSurvey survey;

	public CollectAnnotations(CollectSurvey survey) {
		super();
		this.survey = survey;
	}

	public boolean isIncludedInDataExport(NodeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.INCLUDE_IN_DATA_EXPORT);
	}

	public void setIncludeInDataExport(NodeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.INCLUDE_IN_DATA_EXPORT, value);
	}

	public boolean isCalculatedOnlyOneTime(NodeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.CALCULATED_ONLY_ONE_TIME);
	}

	public void setCalculatedOnlyOneTime(NodeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.CALCULATED_ONLY_ONE_TIME, value);
	}

	public String getAutoCompleteGroup(TextAttributeDefinition def) {
		return def.getAnnotation(Annotation.AUTOCOMPLETE.getQName());
	}

	public void setAutoCompleteGroup(TextAttributeDefinition def, String value) {
		def.setAnnotation(Annotation.AUTOCOMPLETE.getQName(), value);
	}

	public Version getCollectVersion() {
		String versionStr = getAnnotationValueString(survey, Annotation.COLLECT_VERSION);
		return new Version(versionStr);
	}

	public void setCollectVersion(Version version) {
		setAnnotationValue(survey, Annotation.COLLECT_VERSION, version.toString());
	}

	public SurveyTarget getSurveyTarget() {
		String val = survey.getAnnotation(Annotation.TARGET.getQName());
		if (StringUtils.isBlank(val)) {
			return (SurveyTarget) Annotation.TARGET.defaultValue;
		} else {
			return SurveyTarget.fromCode(val);
		}
	}

	public void setSurveyTarget(SurveyTarget target) {
		String val = target == null || target == Annotation.TARGET.defaultValue ? null: target.getCode();
		survey.setAnnotation(Annotation.TARGET.getQName(), val);
	}

	public boolean isKeyChangeAllowed() {
		return getAnnotationValueBoolean(survey, Annotation.KEY_CHANGE_ALLOWED);
	}

	public void setKeyChangeAllowed(boolean value) {
		setAnnotationValue(survey, Annotation.KEY_CHANGE_ALLOWED, value);
	}

	public Step getPhaseToApplyDefaultValue(AttributeDefinition def) {
		return (Step) getAnnotationValueEnum(def, Annotation.PHASE_TO_APPLY_DEFAULT_VALUE, Step.class);
	}

	public void setPhaseToApplyDefaultValue(AttributeDefinition def, Step value) {
		setAnnotationValueEnum(def, Annotation.PHASE_TO_APPLY_DEFAULT_VALUE, value);
	}

	public boolean isEditable(AttributeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.EDITABLE);
	}

	public void setEditable(AttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.EDITABLE, value);
	}

	public boolean isQualifier(AttributeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.QUALIFIER);
	}

	public void setQualifier(AttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.QUALIFIER, value);
	}

	public boolean isShowInSummary(AttributeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.SHOW_IN_SUMMARY_LIST);
	}

	public void setShowInSummary(AttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.SHOW_IN_SUMMARY_LIST, value);
	}

	public boolean isMeasurementAttribute(AttributeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.MEASUREMENT);
	}

	public void setMeasurementAttribute(AttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.MEASUREMENT, value);
	}

	public boolean isShowFamily(TaxonAttributeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.TAXON_ATTRIBUTE_SHOW_FAMILY);
	}

	public void setShowFamily(TaxonAttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.TAXON_ATTRIBUTE_SHOW_FAMILY, value);
	}

	public boolean isIncludeUniqueVernacularName(TaxonAttributeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.TAXON_ATTRIBUTE_INCLUDE_UNIQUE_VERNACULAR_NAME);
	}

	public void setIncludeUniqueVernacularName(TaxonAttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.TAXON_ATTRIBUTE_INCLUDE_UNIQUE_VERNACULAR_NAME, value);
	}

	public boolean isAllowUnlisted(TaxonAttributeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.TAXON_ATTRIBUTE_ALLOW_UNLISTED);
	}

	public void setAllowUnlisted(TaxonAttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.TAXON_ATTRIBUTE_ALLOW_UNLISTED, value);
	}

	public boolean isEnumerate(EntityDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.ENUMERATE);
	}

	public void setEnumerate(EntityDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.ENUMERATE, value);
	}

	public Boolean isAutoGenerateMinItems(NodeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.AUTO_GENERATE_MIN_ITEMS);
	}

	public void setAutoGenerateMinItems(NodeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.AUTO_GENERATE_MIN_ITEMS, value);
	}

	public boolean isHideKeyInCollectEarthRecordList(AttributeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.COLLECT_EARTH_HIDE_IN_RECORD_LIST);
	}

	public void setHideKeyInCollectEarthRecordList(AttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.COLLECT_EARTH_HIDE_IN_RECORD_LIST, value);
	}

	public boolean isFromCollectEarthCSV(AttributeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.COLLECT_EARTH_FROM_CSV);
	}

	public void setFromCollectEarthCSV(AttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.COLLECT_EARTH_FROM_CSV, value);
	}

	public boolean isShowReadOnlyFieldInCollectEarth(AttributeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.COLLECT_EARTH_SHOW_READONLY_FIELD);
	}

	public void setShowReadOnlyFieldInCollectEarth(AttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.COLLECT_EARTH_SHOW_READONLY_FIELD, value);
	}

	public boolean isIncludedInCollectEarthHeader(AttributeDefinition defn) {
		return getAnnotationValueBoolean(defn, Annotation.COLLECT_EARTH_INCLUDE_IN_HEADER);
	}

	public void setIncludedInCollectEarthHeader(AttributeDefinition defn, boolean value) {
		setAnnotationValue(defn, Annotation.COLLECT_EARTH_INCLUDE_IN_HEADER, value);
	}

	public Double getCollectEarthPlotArea() {
		return getAnnotationValueDouble(survey, Annotation.COLLECT_EARTH_PLOT_AREA);
	}

	public void setCollectEarthPlotArea(Double value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_PLOT_AREA, value);
	}

	public String getBingMapsKey() {
		return getAnnotationValueString(survey, Annotation.COLLECT_EARTH_BING_KEY);
	}

	public void setBingMapsKey(String value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_BING_KEY, value);
	}

	public String getPlanetMapsKey() {
		return getAnnotationValueString(survey, Annotation.COLLECT_EARTH_PLANET_KEY);
	}

	public void setPlanetMapsKey(String value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_PLANET_KEY, value);
	}

	public String getExtraMapUrl() {
		return getAnnotationValueString(survey, Annotation.COLLECT_EARTH_EXTRA_MAP_URL);
	}

	public void setExtraMapUrl(String value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_EXTRA_MAP_URL, value);
	}

	public boolean isBingMapsEnabled() {
		return getAnnotationValueBoolean(survey, Annotation.COLLECT_EARTH_OPEN_BING_MAPS);
	}

	public void setBingMapsEnabled( boolean value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_OPEN_BING_MAPS, value);
	}

	public boolean isEarthMapEnabled() {
		return getAnnotationValueBoolean(survey, Annotation.COLLECT_EARTH_OPEN_EARTH_MAP);
	}

	public void setEarthMapEnabled( boolean value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_OPEN_EARTH_MAP, value);
	}

	public boolean isPlanetMapsEnabled() {
		return getAnnotationValueBoolean(survey, Annotation.COLLECT_EARTH_OPEN_PLANET_MAPS);
	}

	public void setPlanetMapsEnabled( boolean value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_OPEN_PLANET_MAPS, value);
	}

	public boolean isYandexMapsEnabled() {
		return getAnnotationValueBoolean(survey, Annotation.COLLECT_EARTH_OPEN_YANDEX_MAPS);
	}

	public void setYandexMapsEnabled( boolean value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_OPEN_YANDEX_MAPS, value);
	}

	public boolean isGEEExplorerEnabled() {
		return getAnnotationValueBoolean(survey, Annotation.COLLECT_EARTH_OPEN_GEE_EXPLORER);
	}

	public void setGEEExplorerEnabled( boolean value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_OPEN_GEE_EXPLORER, value);
	}

	public boolean isGEECodeEditorEnabled() {
		return getAnnotationValueBoolean(survey, Annotation.COLLECT_EARTH_OPEN_GEE_CODE_EDITOR);
	}

	public boolean isGEEAppEnabled() {
		return getAnnotationValueBoolean(survey, Annotation.COLLECT_EARTH_OPEN_GEE_APP);
	}

	public boolean isSecureWatchEnabled() {
		return getAnnotationValueBoolean(survey, Annotation.COLLECT_EARTH_OPEN_SECUREWATCH);
	}

	public void setGEECodeEditorEnabled( boolean value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_OPEN_GEE_CODE_EDITOR, value);
	}

	public void setGEEAppEnabled( boolean value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_OPEN_GEE_APP, value);
	}

	public void setSecureWatchEnabled( boolean value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_OPEN_SECUREWATCH, value);
	}

	public boolean isStreetViewEnabled() {
		return getAnnotationValueBoolean(survey, Annotation.COLLECT_EARTH_OPEN_STREET_VIEW);
	}

	public void setStreetViewEnabled( boolean value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_OPEN_STREET_VIEW, value);
	}

	public int getCollectEarthSamplePoints() {
		return getAnnotationValueInteger(survey, Annotation.COLLECT_EARTH_SAMPLE_POINTS);
	}

	public void setCollectEarthSamplePoints(Integer value) {
		setAnnotationValue(survey, Annotation.COLLECT_EARTH_SAMPLE_POINTS, value);
	}

	public boolean isAllowOnlyDeviceCoordinate(CoordinateAttributeDefinition def) {
		return getAnnotationValueBoolean(def, Annotation.COLLECT_MOBILE_ALLOW_ONLY_DEVICE_COORDINATE);
	}

	public void setAllowOnlyDeviceCoordinate(CoordinateAttributeDefinition def, boolean value) {
		setAnnotationValue(def, Annotation.COLLECT_MOBILE_ALLOW_ONLY_DEVICE_COORDINATE, value);
	}

	public FileType getFileType(FileAttributeDefinition def) {
		return (FileType) getAnnotationValueEnum(def, Annotation.FILE_TYPE, FileType.class);
	}

	public void setFileType(FileAttributeDefinition def, FileType fileType) {
		setAnnotationValue(def, Annotation.FILE_TYPE, fileType);
	}

	public TextInput getTextInput(TextAttributeDefinition def) {
		return (TextInput) getAnnotationValueEnum(def, Annotation.TEXT_INPUT, TextInput.class);
	}

	public void setTextInput(TextAttributeDefinition def, TextInput textInput) {
		setAnnotationValue(def, Annotation.TEXT_INPUT, textInput);
	}

	public boolean isGeometry(TextAttributeDefinition def) {
		return getAnnotationValueBoolean(def, Annotation.GEOMETRY);
	}

	public void setGeometry(TextAttributeDefinition def, boolean geometry) {
		setAnnotationValue(def, Annotation.GEOMETRY, geometry);
	}

	public boolean isShowInMapBalloon(AttributeDefinition def) {
		return getAnnotationValueBoolean(def, Annotation.SHOW_IN_MAP_BALLOON);
	}

	public void setShowInMapBalloon(AttributeDefinition def, boolean showInMapBalloon) {
		setAnnotationValue(def, Annotation.SHOW_IN_MAP_BALLOON, showInMapBalloon);
	}

	public String getBackgroundColor(NodeDefinition def) {
		return def.getAnnotation(Annotation.BACKGROUND_COLOR.qName);
	}

	public void setBackgroundColor(NodeDefinition def, String color) {
		def.setAnnotation(Annotation.BACKGROUND_COLOR.qName, color);
	}

	public Double getBackgroundAlpha(NodeDefinition def) {
		return getAnnotationValueDouble(def, Annotation.BACKGROUND_ALPHA);
	}

	public void setBackgroundAlpha(NodeDefinition def, Double alpha) {
		setAnnotationValue(def, Annotation.BACKGROUND_ALPHA, alpha);
	}

	public boolean isShowSrsField(CoordinateAttributeDefinition def) {
		return getAnnotationValueBoolean(def, Annotation.COORDINATE_ATTRIBUTE_SHOW_SRS_FIELD);
	}

	public void setShowSrsField(CoordinateAttributeDefinition def, boolean showSrsField) {
		setAnnotationValue(def, Annotation.COORDINATE_ATTRIBUTE_SHOW_SRS_FIELD, showSrsField);
	}

	public boolean isIncludeCoordinateAltitude(CoordinateAttributeDefinition def) {
		return getAnnotationValueBoolean(def, Annotation.COORDINATE_ATTRIBUTE_INCLUDE_ALTITUDE);
	}

	public void setIncludeCoordinateAltitude(CoordinateAttributeDefinition def, boolean includeAltitude) {
		setAnnotationValue(def, Annotation.COORDINATE_ATTRIBUTE_INCLUDE_ALTITUDE, includeAltitude);
	}

	public boolean isIncludeCoordinateAccuracy(CoordinateAttributeDefinition def) {
		return getAnnotationValueBoolean(def, Annotation.COORDINATE_ATTRIBUTE_INCLUDE_ACCURACY);
	}

	public void setIncludeCoordinateAccuracy(CoordinateAttributeDefinition def, boolean includeAccuracy) {
		setAnnotationValue(def, Annotation.COORDINATE_ATTRIBUTE_INCLUDE_ACCURACY, includeAccuracy);
	}

	private <T extends Enum<T>> Enum<T> getAnnotationValueEnum(AttributeDefinition def, Annotation annotation, Class<T> enumType) {
		String enumName = def.getAnnotation(annotation.getQName());
		if(StringUtils.isBlank(enumName)) {
			return annotation.getDefaultValue();
		} else {
			return Enum.valueOf(enumType, enumName);
		}
	}

	private void setAnnotationValueEnum(Annotatable def, Annotation annotation, Enum<?> value) {
		String enumName;
		if ( value == null || value == annotation.getDefaultValue() ) {
			enumName = null;
		} else {
			enumName = value.name();
		}
		def.setAnnotation(annotation.getQName(), enumName);
	}

	private Boolean getAnnotationValueBoolean(Annotatable annotatable, Annotation annotation) {
		String annotationValue = annotatable.getAnnotation(annotation.getQName());
		if ( StringUtils.isBlank(annotationValue) ) {
			Boolean defaultValue = annotation.getDefaultValue();
			return defaultValue == null ? null : defaultValue.booleanValue();
		} else {
			return Boolean.valueOf(annotationValue);
		}
	}

	private void setAnnotationValue(Annotatable annotatable, Annotation annotation, boolean value) {
		String annotationValue;
		if ( annotation.getDefaultValue() != null && annotation.getDefaultValue().equals(value) ) {
			annotationValue = null;
		} else {
			annotationValue = Boolean.toString(value);
		}
		annotatable.setAnnotation(annotation.getQName(), annotationValue);
	}

	private Integer getAnnotationValueInteger(Annotatable annotatable, Annotation annotation) {
		String annotationValue = annotatable.getAnnotation(annotation.getQName());
		return StringUtils.isBlank(annotationValue)
			? (Integer) annotation.getDefaultValue()
			: Integer.parseInt(annotationValue);
	}

	private Double getAnnotationValueDouble(Annotatable annotatable, Annotation annotation) {
		String annotationValue = annotatable.getAnnotation(annotation.getQName());
		return StringUtils.isBlank(annotationValue)
			? (Double) annotation.getDefaultValue()
			: Double.parseDouble(annotationValue);
	}

	private void setAnnotationValue(Annotatable annotatable, Annotation annotation, Object value) {
		String annotationValue;
		if ( value == null || value.equals(annotation.getDefaultValue()) ) {
			annotationValue = null;
		} else {
			annotationValue = value.toString();
		}
		annotatable.setAnnotation(annotation.getQName(), annotationValue);
	}

	private String getAnnotationValueString(Annotatable annotatable, Annotation annotation) {
		String annotationValue = annotatable.getAnnotation(annotation.getQName());
		return StringUtils.isBlank(annotationValue)
			? (String) annotation.getDefaultValue()
			: annotationValue;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}

}
