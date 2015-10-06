package org.openforis.collect.datacleansing.json;

import java.io.IOException;
import java.util.Date;

import org.openforis.collect.utils.Dates;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectDateSerializer extends JsonSerializer<Date> {
	
    @Override
    public void serialize(Date value, JsonGenerator generator, SerializerProvider serializerProvider) 
    		throws  IOException, JsonProcessingException {      
    	String formattedDateTime = Dates.formatDateTime(value);
        generator.writeString(formattedDateTime);
    }
}
