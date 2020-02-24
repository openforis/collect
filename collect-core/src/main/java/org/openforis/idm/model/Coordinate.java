package org.openforis.idm.model;

import static org.openforis.idm.metamodel.CoordinateAttributeDefinition.SRS_FIELD_NAME;
import static org.openforis.idm.metamodel.CoordinateAttributeDefinition.X_FIELD_NAME;
import static org.openforis.idm.metamodel.CoordinateAttributeDefinition.Y_FIELD_NAME;
import static org.openforis.idm.metamodel.CoordinateAttributeDefinition.ALTITUDE_FIELD_NAME;
import static org.openforis.idm.metamodel.CoordinateAttributeDefinition.ACCURACY_FIELD_NAME;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class Coordinate extends AbstractValue {

	private static final Pattern PATTERN = Pattern.compile("SRID=(.+);POINT\\((-?\\d+(\\.\\d+)?)\\s(-?\\d+(\\.\\d+)?)\\)");
	private static final String TO_STRING_FORMAT = "SRID=%s;POINT(%f %f)";
	
	private Double x;
	private Double y;
	private String srsId;
	private Double altitude;
	private Double accuracy;

	/**
	 * Returns a Coordinate parsed from the string in input the string representation is based on posgis data type
	 * http://postgis.refractions.net/docs/ch04.html#OpenGISWKBWKT SRID=32632;POINT(0 0) -- XY with SRID
	 * 
	 * @param coord
	 */
	public static Coordinate parseCoordinate(Object coord) {
		if ( coord == null ) {
			return null;
		} 
		String string = coord.toString();
		if ( StringUtils.isBlank(string) ) {
			return null;
		}
		Matcher matcher = PATTERN.matcher(string);
		if (matcher.matches()) {
			String srsId = matcher.group(1);
			double x = Double.parseDouble(matcher.group(2));
			double y = Double.parseDouble(matcher.group(4));
			Coordinate coordinate = new Coordinate(x, y, srsId);
			return coordinate;
		} else {
			//throw new IllegalArgumentException("Unable to convert " + string + " to a valid coordinate");
			return null;
		}
	}
	
	public Coordinate() {
	}
	
	public Coordinate(Double x, Double y, String srsId) {
		this(x, y, srsId, null, null);
	}
	
	public Coordinate(Double x, Double y, String srsId, Double altitude, Double accuracy) {
		this.x = x;
		this.y = y;
		this.srsId = srsId;
		this.altitude = altitude;
		this.accuracy = accuracy;
	}
	
	@Override
	@SuppressWarnings("serial")
	public Map<String, Object> toMap() {
		return new HashMap<String, Object>() {{
			put(X_FIELD_NAME, x);
			put(Y_FIELD_NAME, y);
			put(SRS_FIELD_NAME, srsId);
			put(ALTITUDE_FIELD_NAME, altitude);
			put(ACCURACY_FIELD_NAME, accuracy);
		}};
	}
	
	public boolean isComplete() {
		return x != null && y != null && srsId != null && !srsId.equals("null");
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}
	
	public Double getY() {
		return y;
	}
	
	public void setY(Double y) {
		this.y = y;
	}

	public String getSrsId() {
		return srsId;
	}
	
	public void setSrsId( String srsId) {
		this.srsId = srsId;
	}
	
	public Double getAltitude() {
		return altitude;
	}
	
	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}
	
	public Double getAccuracy() {
		return accuracy;
	}
	
	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}

	@Override
	public String toPrettyFormatString() {
		return String.format(Locale.ENGLISH, TO_STRING_FORMAT, srsId, x, y);
	}

	@Override
	public String toString() {
		return toPrettyFormatString();
	}
	
	@Override
	public String toInternalString() {
		return toPrettyFormatString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((srsId == null) ? 0 : srsId.hashCode());
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		result = prime * result + ((altitude == null) ? 0 : altitude.hashCode());
		result = prime * result + ((accuracy == null) ? 0 : accuracy.hashCode());
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
		Coordinate other = (Coordinate) obj;
		if (srsId == null) {
			if (other.srsId != null)
				return false;
		} else if (!srsId.equals(other.srsId))
			return false;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		if (altitude == null) {
			if (other.altitude != null)
				return false;
		} else if (!altitude.equals(other.altitude))
			return false;
		if (accuracy == null) {
			if (other.accuracy != null)
				return false;
		} else if (!accuracy.equals(other.accuracy))
			return false;		
		return true;
	}

}
