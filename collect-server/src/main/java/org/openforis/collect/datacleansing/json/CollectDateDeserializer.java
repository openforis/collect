package org.openforis.collect.datacleansing.json;

import java.io.IOException;
import java.util.Date;

import org.openforis.collect.utils.Dates;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectDateDeserializer extends JsonDeserializer<Date> {
	
	@Override
	public Date deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		String dateStr = parser.getText();
		Date date = Dates.parseDateTime(dateStr);
		return date;
	}
}
