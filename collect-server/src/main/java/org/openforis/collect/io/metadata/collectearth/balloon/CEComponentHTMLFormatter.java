package org.openforis.collect.io.metadata.collectearth.balloon;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openforis.collect.io.metadata.collectearth.balloon.CEField.CEFieldType;
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
		String label = StringEscapeUtils.escapeHtml4(comp.getLabel());
		
		sb.append("<div class=\"form-group\">\n");
		if (isLabelIncluded(comp)) {
			sb.append("<label for=\"" + elId + "\" class=\"control-label col-sm-4\">");
			sb.append(label);
			sb.append("</label>\n");
		}
		sb.append("<div class=\"col-sm-8\">\n");
		
		if (comp instanceof CECodeField) {
//			sb.append("<div class=\"capsuleCat ui-corner-all\" style=\"position: relative;\">\n");
//			sb.append("<div class=\"input-prepend btn-group\">\n");
//			sb.append("<button class=\"btn btn-default\" disabled=\"disabled\">");
//			sb.append(StringEscapeUtils.escapeHtml4(comp.getLabel()));
//			sb.append("</button>\n");
			switch(((CEField) comp).getType()) {
			case CODE_BUTTON_GROUP:
				appendCodeButtonGroup(sb, (CECodeField) comp);
				break;
			case CODE_SELECT:
				appendCodeSelect(sb, (CECodeField) comp);
				break;
			default:
				break;
			}
		} else if (comp instanceof CEField) {
			switch (((CEField) comp).getType()) {
			case SHORT_TEXT:
				sb.append("<input type=\"text\" class=\"form-control\" id=\"");
				sb.append(elId);
				sb.append("\" name=\"");
				sb.append(elId);
				sb.append("\"/>\n");
				break;
			case LONG_TEXT:
				sb.append("<textarea rows=\"3\" class=\"form-control\" id=\"");
				sb.append(elId);
				sb.append("\" name=\"");
				sb.append(elId);
				sb.append("\"></textarea>\n");
				break;
			case INTEGER:
			case REAL:
				sb.append("<input type=\"text\" class=\"form-control numeric\" id=\"");
				sb.append(elId);
				sb.append("\" name=\"");
				sb.append(elId);
				sb.append("\" value=\"0\"/>\n");
				break;
			case BOOLEAN:
				sb.append("<input type=\"checkbox\" class=\"form-control\" id=\"");
				sb.append(elId);
				sb.append("\" name=\"");
				sb.append(elId);
				sb.append("\"/>");
				break;
			case COORDINATE:
				break;
			case DATE:
			case TIME:
				String className = ((CEField) comp).getType() == CEFieldType.DATE ? "datepicker" : "timepicker";
				sb.append("<div class='input-group date " + className + "'>\n");
				sb.append("<input type=\"text\" class=\"form-control\" id=\"");
				sb.append(elId);
				sb.append("\" name=\"");
				sb.append(elId);
				sb.append("\"/>\n");
				sb.append("<span class=\"input-group-addon\"><span class=\"glyphicon glyphicon-time\"></span></span>\n");
				sb.append("</div>\n");
				break;
			default:
				break;
			}
		}
		//end of external container
		sb.append("</div>\n");
		sb.append("</div>\n");
		return sb.toString();
	}

	private void appendCodeSelect(StringBuilder sb, CECodeField comp) {
		String elId = comp.getHtmlParameterName();
		
		sb.append("<select id=\"");
		sb.append(elId);
		sb.append("\" name=\" data-field-type=\"code\"");
		sb.append(elId);
		sb.append("\" class=\"form-control selectboxit show-menu-arrow show-tick\""); 
		sb.append(" data-width=\"75px\">\n");
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
		sb.append("</select>\n");
	}

	private void appendCodeButtonGroup(StringBuilder sb, CECodeField comp) {
		String elId = comp.getHtmlParameterName();
		
		//button groups external container
		String groupId = elId + "_group";
		sb.append("<div id=\"");
		sb.append(groupId);
		sb.append("\" class=\"code-items-group\">\n");
		
		//hidden field that stores the actual value
		sb.append("<input type=\"hidden\" class=\"form-control\" data-field-type=\"code\" data-parent-code-field=\"");
		sb.append(comp.getParentName());
		sb.append("\" id=\"");
		sb.append(elId);
		sb.append("\" name=\"");
		sb.append(elId);
		sb.append("\"/>\n");
		
		Map<String, List<CodeListItem>> itemsByParentCode = ((CECodeField) comp).getCodeItemsByParentCode();
		for (Entry<String, List<CodeListItem>> entry : itemsByParentCode.entrySet()) {
			//one button group for every list of codes by parent code
			String parentCode = entry.getKey();
			String itemsGroupId = groupId + "_" + parentCode;
			sb.append("<div id=\"");
			sb.append(itemsGroupId);
			sb.append("\" class=\"code-items\">\n");
			List<CodeListItem> items = entry.getValue();
			if (items != null) {
				for (CodeListItem item : items) {
					sb.append("<button type=\"button\" class=\"btn btn-info code-item\" value=\"");
					sb.append(item.getCode());
					sb.append("\" title=\"");
					sb.append(item.getDescription());
					sb.append("\">");
					sb.append(StringEscapeUtils.escapeHtml4(item.getLabel()));
					sb.append("</button>\n");
				}
			}
			sb.append("</div>\n");
		}
		sb.append("</div>\n");
	}

	private boolean isLabelIncluded(CEComponent comp) {
		return true;
//		return comp instanceof CEField && ((CEField) comp).getType() != CEFieldType.BOOLEAN;
	}
	
}
