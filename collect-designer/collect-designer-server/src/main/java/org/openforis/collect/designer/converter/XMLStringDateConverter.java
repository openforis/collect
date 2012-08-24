package org.openforis.collect.designer.converter;

import java.util.Date;

import org.openforis.collect.util.DateUtil;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

public class XMLStringDateConverter implements Converter<Date, String, Component> {
    /**
     * Convert String to Date.
     * @param val date in xml format to be converted
     * @param comp associated component
     * @param ctx bind context for associate Binding and extra parameter (e.g. format)
     * @return the converted String
     */
	public Date coerceToUi(String val, Component comp, BindContext ctx) {
        final Date xmlDateTime = DateUtil.parseXMLDateTime(val);
        return xmlDateTime;
    }
     
    /**
     * Convert Date into a date string in xml format.
     * @param val date in string form
     * @param comp associated component
     * @param ctx bind context for associate Binding and extra parameter (e.g. format)
     * @return the converted Date
     */
    public String coerceToBean(Date val, Component comp, BindContext ctx) {
        String result = DateUtil.formatDateToXML(val);
        return result;
    }
}
