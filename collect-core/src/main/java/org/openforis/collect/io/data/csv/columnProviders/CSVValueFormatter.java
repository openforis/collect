package org.openforis.collect.io.data.csv.columnProviders;

import java.text.NumberFormat;
import java.util.Locale;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.File;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.NumberValue;
import org.openforis.idm.model.NumericRange;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
class CSVValueFormatter {
	
	public String format(AttributeDefinition defn, Value value) {
		if (value == null) {
			return "";
		} else if (value instanceof BooleanValue) {
			return ((BooleanValue) value).getValue().toString();
		} else if (value instanceof Code) {
			CodeListService codeListService = defn.getSurvey().getContext().getCodeListService();
			CodeList list = ((CodeAttributeDefinition) defn).getList();
			if (codeListService.hasQualifiableItems(list)) {
				return String.format("%s: %s", ((Code) value).getCode(), ((Code) value).getQualifier());
			} else {
				return ((Code) value).getCode();
			}
		} else if (value instanceof Coordinate) {
			return value.toString();
		} else if (value instanceof Date) {
			Date date = (Date) value;
			return String.format("%02d/%02d/%04d", ((Date) value).getDay(), ((Date) value).getMonth(), date.getYear());
		} else if (value instanceof File) {
			return ((File) value).getFilename();
		} else if (value instanceof NumberValue) {
			Number val = ((NumberValue<?>) value).getValue();
			NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
			String formattedVal = numberFormat.format(val);
			return formattedVal;
		} else if (value instanceof NumericRange) {
			Number from = ((NumericRange<?>) value).getFrom();
			Number to = ((NumericRange<?>) value).getFrom();
			String format;
			if (value instanceof IntegerRange) {
				format = "%d-%d";
			} else {
				format = "%f-%f";
			}
			String formattedValue = String.format(Locale.ENGLISH, format, from, to);
			return formattedValue;
		} else if (value instanceof TextValue) {
			 return ((TextValue) value).getValue();
		} else if (value instanceof Time) {
			Time time = (Time) value;
			return String.format("%02d:%02d", time.getHour(), time.getMinute());
		} else throw new IllegalArgumentException("Unsupported attribute value type: " + value.getClass().getName());
	}
}