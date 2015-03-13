package org.openforis.collect.io.metadata.collectearth.balloon;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.metadata.collectearth.balloon.CEField.CEFieldType;
import org.openforis.idm.metamodel.CodeListItem;

import com.jamesmurty.utils.XMLBuilder;


/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class CEComponentHTMLFormatter {

	public String format(CETabSet tabSet) {
		try {
			XMLBuilder builder = createBuilder(tabSet, null);
			return writeToString(builder);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private XMLBuilder createBuilder(CETabSet tabSet, XMLBuilder parentBuilder) throws Exception {
		XMLBuilder tabSetBuilder = parentBuilder == null ? XMLBuilder.create("div") : parentBuilder.e("div");
		tabSetBuilder.a("class", "steps");
		for (CETab tab : tabSet.getTabs()) {
			createBuilder(tab, tabSetBuilder);
		}
		return tabSetBuilder;
	}
	
	private XMLBuilder createBuilder(CETab tab, XMLBuilder parentBuilder) throws Exception {
		parentBuilder.e("h3").t(tab.getLabel());

		XMLBuilder bodyContentBuilder = parentBuilder.e("section");
		bodyContentBuilder.a("class", "step");

		for (CEComponent component : tab.getChildren()) {
			if (component instanceof CEField) {
				createBuilder((CEField) component, true, bodyContentBuilder);
			} else if (component instanceof CEEnumeratedEntityTable) {
				createBuilder((CEEnumeratedEntityTable) component, bodyContentBuilder);
			} else if (component instanceof CEFieldSet) {
				createBuilder((CEFieldSet) component, bodyContentBuilder);
			}
		}
		return bodyContentBuilder;
	}
	
	private XMLBuilder createBuilder(CEEnumeratedEntityTable comp, XMLBuilder parentBuilder) throws Exception {
		XMLBuilder builder =  parentBuilder.e("fieldset");
		String legend = comp.getLabelOrName();
		builder.e("legend").t(legend);
		XMLBuilder tableBuilder = builder.e("table").a("class", "table");
		XMLBuilder headerBuilder = tableBuilder.e("thead").e("tr");
		for (String heading : comp.getHeadings()) {
			headerBuilder.e("th").t(heading);
		}
		XMLBuilder bodyBuilder = tableBuilder.e("tbody");
		List<CETableRow> rows = comp.getRows();
		for (CETableRow row : rows) {
			XMLBuilder rowBuilder = bodyBuilder.e("tr");
			for (CEComponent child : row.getChildren()) {
				XMLBuilder cellBuilder = rowBuilder.e("td");
				if (child instanceof CEEnumeratingCodeField) {
					cellBuilder
						.e("label")
							.a("class", "control-label col-sm-4")
							.t(row.getLabelOrName());
				} else if (child instanceof CEField) {
					createBuilder((CEField) child, false, cellBuilder);
				}
			}
		}
		return builder;
	}
	
	private XMLBuilder createBuilder(CEFieldSet comp, XMLBuilder parentBuilder) throws Exception {
		XMLBuilder fieldSetBuilder =  parentBuilder.e("fieldset");
		fieldSetBuilder.e("legend").t(comp.getLabelOrName());
		for (CEComponent child : comp.getChildren()) {
			if (child instanceof CEField) {
				createBuilder((CEField) child, true, fieldSetBuilder);
			} else {
				throw new IllegalArgumentException("Only attribute fields supported inside single entity");
			}
		}
		return fieldSetBuilder;
	}
	
	private XMLBuilder createBuilder(CEField comp, boolean includeLabel, XMLBuilder parentBuilder) throws Exception {
		//start of external container
		String elId = comp.getHtmlParameterName();
		
		//external form-group container
		XMLBuilder formGroupBuilder = parentBuilder == null ? XMLBuilder.create("div") : parentBuilder.e("div");
		formGroupBuilder.a("class", "form-group");
		if (includeLabel) {
			//label element
			formGroupBuilder.e("label")
				.a("for", elId)
				.a("class", "control-label col-sm-4")
				.t(comp.getLabelOrName());
		}
		//form control external container (for grid alignment)
		XMLBuilder formControlContainer = formGroupBuilder.e("div")
				.a("class", "col-sm-8");
		
		if (comp instanceof CECodeField) {
			switch(((CEField) comp).getType()) {
			case CODE_BUTTON_GROUP:
				buildCodeButtonGroup(formControlContainer, (CECodeField) comp);
				break;
			case CODE_SELECT:
				buildCodeSelect(formControlContainer, (CECodeField) comp);
				break;
			default:
				break;
			}
		} else if (comp instanceof CEEnumeratingCodeField) {
			// skip it
		} else if (comp instanceof CEField) {
			switch (((CEField) comp).getType()) {
			case SHORT_TEXT:
				formControlContainer.e("input")
					.a("id", elId)
					.a("name", elId)
					.a("type", "text")
					.a("class", "form-control");
				break;
			case LONG_TEXT:
				formControlContainer.e("textarea")
					.a("id", elId)
					.a("rows", "3")
					.a("name", elId)
					.a("class", "form-control")
					.t(" ");
				break;
			case INTEGER:
			case REAL:
				formControlContainer.e("input")
					.a("id", elId)
					.a("name", elId)
					.a("type", "text")
					.a("value", "0")
					.a("class", "form-control numeric");
				break;
			case BOOLEAN:
//				formControlContainer.e("input")
//					.a("id", elId)
//					.a("name", elId)
//					.a("type", "checkbox")
//					.a("class", "form-control numeric");
				XMLBuilder container = formControlContainer.e("div").a("class", "boolean-group");
				container.e("input")
					.a("id", elId)
					.a("name", elId)
					.a("type", "hidden")
					.a("class", "form-control")
					.a("data-field-type", comp.getType().name());
				container.e("button")
					.a("type", "button")
					.a("class", "btn btn-info")
					.a("value", "true")
					.t("Yes");
				container.e("button")
					.a("type", "button")
					.a("class", "btn btn-info")
					.a("value", "false")
					.t("No");
				break;
			case COORDINATE:
				break;
			case DATE:
			case TIME:
				formControlContainer.e("div")
					.a("class", "input-group date " + (((CEField) comp).getType() == CEFieldType.DATE ? "datepicker" : "timepicker"))
					.e("input")
						.a("id", elId)
						.a("name", elId)
						.a("class", "form-control")
						.up()
					.e("span")
						.a("class", "input-group-addon")
						.e("span")
							.a("class", "glyphicon glyphicon-time")
					;
				break;
			default:
				break;
			}
		}
		return formGroupBuilder;
	}

	private void buildCodeSelect(XMLBuilder builder, CECodeField comp) {
		String elId = comp.getHtmlParameterName();
		
		//build select
		XMLBuilder selectBuilder = builder.e("select")
			.a("id", elId)
			.a("name", elId)
			.a("data-field-type", comp.getType().name())
			.a("class", "form-control selectboxit show-menu-arrow show-tick")
			.a("data-width", "75px");
		if (comp.getParentName() != null) {
			selectBuilder.a("data-parent-code-field-id", comp.getParentName());
		}
		//add root items, if any
		Map<String, List<CodeListItem>> itemsByParentCode = ((CECodeField) comp).getCodeItemsByParentCode();
		List<CodeListItem> rootItems = itemsByParentCode.get("");
		if (rootItems != null) {
			for (CodeListItem item : rootItems) {
				String itemLabel = getItemLabel(item);
				selectBuilder.e("option")
					.a("value", item.getCode())
					.t(itemLabel);
			}
		}
	}

	private void buildCodeButtonGroup(XMLBuilder formControlContainer, CECodeField comp) {
		String elId = comp.getHtmlParameterName();
		
		//button groups external container
		String groupId = elId + "_group";
		
		XMLBuilder itemsGroupExternalContainer = formControlContainer.e("div")
			.a("id", groupId)
			.a("class", "code-items-group");
		
		XMLBuilder hiddenInputField = itemsGroupExternalContainer.e("input")
				.a("id", elId)
				.a("name", elId)
				.a("type", "hidden")
				.a("class", "form-control")
				.a("data-field-type", comp.getType().name());
		
		if (comp.getParentName() != null) {
			hiddenInputField.a("data-parent-code-field-id", comp.getParentName());
		}
		
		Map<String, List<CodeListItem>> itemsByParentCode = ((CECodeField) comp).getCodeItemsByParentCode();
		for (Entry<String, List<CodeListItem>> entry : itemsByParentCode.entrySet()) {
			//one button group for every list of codes by parent code
			String parentCode = entry.getKey();
			String itemsGroupId = groupId + "_" + parentCode;
			XMLBuilder buttonsGroup = itemsGroupExternalContainer
				.e("div")
					.a("id", itemsGroupId)
					.a("class", "code-items")
					.a("data-toggle", comp.isMultiple() ? "buttons": "buttons-radio")
					.a("data-parent-code", parentCode);
			List<CodeListItem> items = entry.getValue();
			if (items == null || items.isEmpty()) {
				buttonsGroup.t(" "); //always use close tag
			} else {
				for (CodeListItem item : items) {
					String itemLabel = getItemLabel(item);
					XMLBuilder buttonItemBuilder = buttonsGroup
						.e("button")
							.a("type", "button")
							.a("class", "btn btn-info code-item")
							.a("value", item.getCode())
							.t(itemLabel);
					if (StringUtils.isNotBlank(item.getDescription())) {
						buttonItemBuilder.a("title", item.getDescription());
					}
				}
			}
		}
	}

	private String writeToString(XMLBuilder builder) {
		try {
			StringWriter writer = new StringWriter();
			@SuppressWarnings("serial")
			Properties outputProperties = new Properties(){{
				put(javax.xml.transform.OutputKeys.INDENT, "yes");
				put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
				put(javax.xml.transform.OutputKeys.STANDALONE, "yes");
				put(javax.xml.transform.OutputKeys.METHOD, "html");
			}};
			builder.toWriter(writer, outputProperties);
			return writer.toString();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private String getItemLabel(CodeListItem item) {
		String itemLabel = item.getLabel();
		if (StringUtils.isBlank(itemLabel)) {
			itemLabel = item.getCode();
		}
		return itemLabel;
	}

}
