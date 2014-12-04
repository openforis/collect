package org.openforis.idm.model;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class Coordinate implements Value{

	private static final Pattern PATTERN = Pattern.compile("SRID=(.+);POINT\\((-?\\d+(\\.\\d+)?)\\s(-?\\d+(\\.\\d+)?)\\)");
	private static final String TO_STRING_FORMAT = "SRID=%s;POINT(%f %f)";
	
	private Double x;
	private Double y;
	private String srsId;


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
			throw new IllegalArgumentException("Unable to convert " + string + " to a valid coordinate");
		}
	}
	
	public Coordinate(Double x, Double y, String srsId) {
		this.x = x;
		this.y = y;
		this.srsId = srsId;
	}

	public boolean isComplete() {
		return x != null && y != null && srsId != null;
	}

	public Double getX() {
		return x;
	}

	public Double getY() {
		return y;
	}

	public String getSrsId() {
		return srsId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((srsId == null) ? 0 : srsId.hashCode());
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
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
		return true;
	}

	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, TO_STRING_FORMAT, srsId, x, y);
	}

}
