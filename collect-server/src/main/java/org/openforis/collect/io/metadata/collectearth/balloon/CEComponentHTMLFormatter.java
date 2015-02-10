package org.openforis.collect.io.metadata.collectearth.balloon;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openforis.idm.metamodel.CodeListItem;


/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class CEComponentHTMLFormatter {

	public String format(CEComponent comp) {
		StringBuilder sb = new StringBuilder();
//		if (comp instanceof CEField) {
//			String parameterName = comp.getHtmlParameterName();
//			sb.append("<input type=\"hidden\" id=\"");
//			sb.append(parameterName);
//			sb.append("\" name=\"");
//			sb.append(parameterName);
//			sb.append("\"  />");
//			sb.append('\n');
//		}
		if (comp instanceof CECodeField) {
			sb.append("<div class=\"capsuleCat ui-corner-all\" style=\"position: relative;\">\n");

			sb.append("<div class=\"input-prepend btn-group\">\n");
			sb.append("<button class=\"btn btn-default\" disabled=\"disabled\">");
			sb.append(StringEscapeUtils.escapeHtml4(comp.getLabel()));
			sb.append("</button>\n");
			sb.append("<select id=\"collect_select_");
			sb.append(comp.getName());
			sb.append("\" name=\"collect_select_");
			sb.append(comp.getName());
			sb.append("\" class=\"selectboxit show-menu-arrow show-tick\""); 
			sb.append(" data-width=\"75px\" title=\"N/A\">\n");
			sb.append("<option value=\"-1\">N/A</option>\n");

			Map<String, List<CodeListItem>> itemsByParentCode = ((CECodeField) comp).getCodeItemsByParentCode();
			Set<String> parentCodes = itemsByParentCode.keySet();
			for (String parentCode : parentCodes) {
				List<CodeListItem> items = itemsByParentCode.get(parentCode);
				
				for (CodeListItem item : items) {
					sb.append("<option value=\"");
					sb.append(item.getCode());
					sb.append("\">");
					sb.append(item.getLabel());
					sb.append("</option>\n");
				}
				
//				sb.append("<div class=\"btn-group independentToggle btnSubcategoryType\" data-toggle=\"buttons-radio\" data-parent-code=\"");
//				sb.append(parentCode);
//				sb.append("\">\n");
//				for (CodeListItem item : items) {
//					sb.append("<button type=\"button\" class=\"btn btn-info\" value=\"");
//					sb.append(item.getCode());
//					sb.append("\" title=\"");
//					sb.append(item.getDescription());
//					sb.append("\">");
//					sb.append(item.getLabel());
//					sb.append("</button>\n");
//				}
//				sb.append("</div>\n");
			}
			sb.append("</select>\n");
			sb.append("</div>\n");
			sb.append("</div>\n");
		} else if (comp instanceof CEField) {
			switch (((CEField) comp).getType()) {
			case SHORT_TEXT:
				sb.append("<input type=\"text\" style=\"width: 50px\" class=\"form-control\" id=\"");
				sb.append(comp.getHtmlParameterName());
				sb.append("\" name=\"");
				sb.append(comp.getHtmlParameterName());
				sb.append("\"/>\n");
				break;
			case LONG_TEXT:
				sb.append("<textarea rows=\"3\"  id=\"");
				sb.append(comp.getHtmlParameterName());
				sb.append("\" name=\"");
				sb.append(comp.getHtmlParameterName());
				sb.append("\"></textarea>\n");
				break;
			case INTEGER:
			case REAL:
				sb.append("<input type=\"text\" style=\"width: 50px\" class=\"form-control\" id=\"");
				sb.append(comp.getHtmlParameterName());
				sb.append("\" name=\"");
				sb.append(comp.getHtmlParameterName());
				sb.append("\" value=\"0\"/>\n");
				break;
			}
		}
		return sb.toString();
	}
	
}
