package org.openforis.collect.io.metadata.samplingdesign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jooq.tools.StringUtils;
import org.openforis.collect.io.metadata.parsing.ReferenceDataLine;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignLine extends ReferenceDataLine {
	
	private SamplingDesignLineCodeKey key;
	private String x;
	private String y;
	private String srsId;
	
	public SamplingDesignLineCodeKey getKey() {
		return key;
	}
	
	public void setKey(SamplingDesignLineCodeKey key) {
		this.key = key;
	}
	
	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public String getSrsId() {
		return srsId;
	}

	public void setSrsId(String srsId) {
		this.srsId = srsId;
	}
	
	public boolean hasEqualLocation(SamplingDesignLine other){
		return getX().equals(other.getX()) 
				&& getY().equals(other.getY()) 
				&& getSrsId().equals(other.getSrsId());
	}

	public SamplingDesignItem toSamplingDesignItem(CollectSurvey survey, List<String> infoColumnNames) {
		SamplingDesignItem item = new SamplingDesignItem();
		item.setSurveyId(survey.getId());
		item.setX(Double.parseDouble(x));
		item.setY(Double.parseDouble(y));
		item.setSrsId(srsId);
		item.setLevelCodes(getKey().getLevelCodes());
		
		List<String> infoAttributeNames = new ArrayList<String>();
		for ( String colName : infoColumnNames ) {
			infoAttributeNames.add(getInfoAttribute(colName));
		}
		item.setInfoAttributes(infoAttributeNames);
		return item;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static class SamplingDesignLineCodeKey implements Comparable<SamplingDesignLineCodeKey> {
		
		private List<String> levelCodes;

		public SamplingDesignLineCodeKey(String ...levelCodes) {
			this(Arrays.asList(levelCodes));
		}
		
		public SamplingDesignLineCodeKey(List<String> levelCodes) {
			super();
			this.levelCodes = levelCodes;
		}
		
		@Override
		public int compareTo(SamplingDesignLineCodeKey o) {
			int maxSize = Math.max(this.levelCodes.size(), o.levelCodes.size());
			List<String> normalizedThisLevelCodes = normalizeLevelCodes(this.levelCodes, maxSize);
			List<String> normalizedOtherLevelCodes = normalizeLevelCodes(o.levelCodes, maxSize);
			Iterator<String> thisLevelCodesIt = normalizedThisLevelCodes.iterator();
			Iterator<String> otherLevelCodesIt = normalizedOtherLevelCodes.iterator();
			while (thisLevelCodesIt.hasNext()) {
				String thisCode = thisLevelCodesIt.next();
				String otherCode = otherLevelCodesIt.next();
				int result = compareLevelCodes(thisCode, otherCode);
				if (result != 0) {
					return result;
				}
			}
			return 0;
		}

		private List<String> normalizeLevelCodes(List<String> levelCodes, int size) {
			List<String> result = new ArrayList<String>(size);
			result.addAll(levelCodes);
			result.addAll(Collections.nCopies(size - levelCodes.size(), "0"));
			return result;
		}

		/**
		 * Compares level codes with natural sort order
		 */
		private int compareLevelCodes(String code1, String code2) {
			int maxLength = Math.max(code1.length(), code2.length());
			String code1Pad = StringUtils.leftPad(code1, maxLength, '0');
			String code2Pad = StringUtils.leftPad(code2, maxLength, '0');
			int result = code1Pad.compareTo(code2Pad);
			if (result == 0 && code1.length() != code2.length()) {
				//avoid codes collision
				return code1.compareTo(code2);
			} else {
				return result;
			}
		}
		
		public List<String> getLevelCodes() {
			return levelCodes;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((levelCodes == null) ? 0 : levelCodes.hashCode());
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
			SamplingDesignLineCodeKey other = (SamplingDesignLineCodeKey) obj;
			if (levelCodes == null) {
				if (other.levelCodes != null)
					return false;
			} else if (!levelCodes.equals(other.levelCodes))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return levelCodes.toString();
		}
	}

}