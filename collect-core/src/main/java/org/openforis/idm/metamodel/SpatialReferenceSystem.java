/**
 * 
 */
package org.openforis.idm.metamodel;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.openforis.commons.lang.DeepComparable;


/**
 * @author G. Miceli
 * @author M. Togna
 */
public class SpatialReferenceSystem implements Serializable, DeepComparable {

	private static final long serialVersionUID = 1L;

	public static final String WGS84_SRS_ID = "EPSG:4326";
	public static final String WEB_MARCATOR_SRS_ID = "EPSG:3857";

	public static final SpatialReferenceSystem LAT_LON_SRS = new SpatialReferenceSystem(
	            WGS84_SRS_ID, 
	            "GEOGCS[\"WGS 84\",\n" +
	            "    DATUM[\"WGS_1984\",\n" +
	            "        SPHEROID[\"WGS 84\",6378137,298.257223563,\n" +
	            "            AUTHORITY[\"EPSG\",\"7030\"]],\n" +
	            "        AUTHORITY[\"EPSG\",\"6326\"]],\n" +
	            "    PRIMEM[\"Greenwich\",0,\n" +
	            "        AUTHORITY[\"EPSG\",\"8901\"]],\n" +
	            "    UNIT[\"degree\",0.01745329251994328,\n" +
	            "        AUTHORITY[\"EPSG\",\"9122\"]],\n" +
	            "    AUTHORITY[\"EPSG\",\"4326\"]]", "Lat Lon");

	public static final SpatialReferenceSystem WEB_MARCATOR_SRS = new SpatialReferenceSystem(
			WEB_MARCATOR_SRS_ID,
			"   GEOGCS[\"WGS 84\",\n        DATUM[\"WGS_1984\",\n            SPHEROID[\"WGS 84\",6378137,298.257223563,\n                AUTHORITY[\"EPSG\",\"7030\"]],\n            AUTHORITY[\"EPSG\",\"6326\"]],\n        PRIMEM[\"Greenwich\",0,\n            AUTHORITY[\"EPSG\",\"8901\"]],\n        UNIT[\"degree\",0.0174532925199433,\n            AUTHORITY[\"EPSG\",\"9122\"]],\n        AUTHORITY[\"EPSG\",\"4326\"]],\n    PROJECTION[\"Mercator_1SP\"],\n    PARAMETER[\"central_meridian\",0],\n    PARAMETER[\"scale_factor\",1],\n    PARAMETER[\"false_easting\",0],\n    PARAMETER[\"false_northing\",0],\n    UNIT[\"metre\",1,\n        AUTHORITY[\"EPSG\",\"9001\"]],\n    AXIS[\"X\",EAST],\n    AXIS[\"Y\",NORTH],\n    EXTENSION[\"PROJ4\",\"+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs\"],\n    AUTHORITY[\"EPSG\",\"3857\"]]"
		    , "Web Marcator");
	
	private String id;
	private LanguageSpecificTextMap labels;
	private LanguageSpecificTextMap descriptions;
	private String wellKnownText;
	
	public SpatialReferenceSystem() {
	}
	
	public SpatialReferenceSystem(String id, String wellKnownText, String defaultLabel) {
		this.id = id;
		this.wellKnownText = wellKnownText;
		addLabel(new LanguageSpecificText(Locale.ENGLISH.getLanguage(), defaultLabel));
	}

	public SpatialReferenceSystem(SpatialReferenceSystem srs) {
		this.id = srs.id;
		this.wellKnownText = srs.wellKnownText;
		this.labels = srs.labels == null ? null : new LanguageSpecificTextMap(srs.labels);
		this.descriptions = srs.descriptions == null ? null : new LanguageSpecificTextMap(srs.descriptions);
	}
	
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public List<LanguageSpecificText> getLabels() {
		if ( this.labels == null ) {
			return Collections.emptyList();
		} else {
			return this.labels.values();
		}
	}
	
	public String getLabel(String language) {
		return labels == null ? null: labels.getText(language);
	}
	
	public void addLabel(LanguageSpecificText label) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.add(label);
	}

	public void setLabel(String language, String text) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.setText(language, text);
	}
	
	public void removeLabel(String language) {
		labels.remove(language);
	}

	public List<LanguageSpecificText> getDescriptions() {
		if ( this.descriptions == null ) {
			return Collections.emptyList();
		} else {
			return this.descriptions.values();
		}
	}

	public String getDescription(String language) {
		return descriptions == null ? null: descriptions.getText(language);
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

	public String getWellKnownText() {
		return this.wellKnownText;
	}
	
	public void setWellKnownText(String wellKnownText) {
		this.wellKnownText = wellKnownText;
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
		SpatialReferenceSystem other = (SpatialReferenceSystem) obj;
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
		SpatialReferenceSystem other = (SpatialReferenceSystem) obj;
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
		if (labels == null) {
			if (other.labels != null)
				return false;
		} else if (!labels.equals(other.labels))
			return false;
		if (wellKnownText == null) {
			if (other.wellKnownText != null)
				return false;
		} else if (!wellKnownText.trim().equals(other.wellKnownText.trim()))
			return false;
		return true;
	}
	
}
