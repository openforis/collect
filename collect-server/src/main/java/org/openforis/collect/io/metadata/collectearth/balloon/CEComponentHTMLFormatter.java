package org.openforis.collect.io.metadata.collectearth.balloon;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openforis.idm.metamodel.CodeListItem;


/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class CEComponentHTMLFormatter {

	private static final String NOT_AVAILABLE_ITEM_LABEL = "N/A";
	private static final String NOT_AVAILABLE_ITEM_CODE = "-1";

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
		
		//start of external container
		String elId = comp.getHtmlParameterName();
		sb.append("<div class=\"form-group\">\n");
		sb.append("<label for=\"" + elId + "\">");
		sb.append(StringEscapeUtils.escapeHtml4(comp.getLabel()));
		sb.append("</label>\n");
		
		if (comp instanceof CECodeField) {
//			sb.append("<div class=\"capsuleCat ui-corner-all\" style=\"position: relative;\">\n");
//			sb.append("<div class=\"input-prepend btn-group\">\n");
//			sb.append("<button class=\"btn btn-default\" disabled=\"disabled\">");
//			sb.append(StringEscapeUtils.escapeHtml4(comp.getLabel()));
//			sb.append("</button>\n");
			sb.append("<select id=\"");
			sb.append(elId);
			sb.append("\" name=\"");
			sb.append(elId);
			sb.append("\" class=\"form-control selectboxit show-menu-arrow show-tick\""); 
			sb.append(" data-width=\"75px\" title=\"" + NOT_AVAILABLE_ITEM_LABEL + "\">\n");
			sb.append("<option value=\"" + NOT_AVAILABLE_ITEM_CODE + "\">" + NOT_AVAILABLE_ITEM_LABEL + "</option>\n");

			Map<String, List<CodeListItem>> itemsByParentCode = ((CECodeField) comp).getCodeItemsByParentCode();
			List<CodeListItem> rootItems = itemsByParentCode.get("");
			if (rootItems != null) {
				for (CodeListItem item : rootItems) {
					sb.append("<option value=\"");
					sb.append(item.getCode());
					sb.append("\">");
					sb.append(StringEscapeUtils.escapeHtml4(item.getLabel()));
					sb.append("</option>\n");
				}
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
				
			sb.append("</select>\n");
//			sb.append("</div>\n");
//			sb.append("</div>\n");
		} else if (comp instanceof CEField) {
			switch (((CEField) comp).getType()) {
			case SHORT_TEXT:
				sb.append("<input type=\"text\" style=\"width: 50px\" class=\"form-control\" id=\"");
				sb.append(elId);
				sb.append("\" name=\"");
				sb.append(elId);
				sb.append("\"/>\n");
				break;
			case LONG_TEXT:
				sb.append("<textarea rows=\"3\"  id=\"");
				sb.append(elId);
				sb.append("\" name=\"");
				sb.append(elId);
				sb.append("\"></textarea>\n");
				break;
			case INTEGER:
			case REAL:
				sb.append("<input type=\"text\" style=\"width: 50px\" class=\"form-control\" id=\"");
				sb.append(elId);
				sb.append("\" name=\"");
				sb.append(elId);
				sb.append("\" value=\"0\"/>\n");
				break;
			case BOOLEAN:
				break;
			case COORDINATE:
				break;
			case DATE:
				break;
			case TIME:
				break;
			default:
				break;
			}
		}
		//end of external container
		sb.append("</div>\n");
		return sb.toString();
	}
	
}
