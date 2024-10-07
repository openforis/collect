package org.openforis.collect.io.metadata.collectearth.balloon;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreatorImpl;
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

	private static final String EXTRA_VALUE_FORMAT = "$[EXTRA_%s]";

	private String language;

	public CEComponentHTMLFormatter(String language) {
		super();
		this.language = language;
	}

	public String format(CETabSet tabSet) {
		try {
			XMLBuilder builder = createBuilder(tabSet, null);
			return writeToString(builder);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private XMLBuilder createBuilder(CETabSet tabSet, XMLBuilder parentBuilder) throws Exception {
		XMLBuilder tabSetBuilder = parentBuilder == null ? XMLBuilder.create("div") : parentBuilder.e("div"); //$NON-NLS-1$ //$NON-NLS-2$
		tabSetBuilder.a("class", "steps"); //$NON-NLS-1$ //$NON-NLS-2$
		for (CETab tab : tabSet.getTabs()) {
			createBuilder(tab, tabSetBuilder);
		}
		return tabSetBuilder;
	}

	private XMLBuilder createBuilder(CETab tab, XMLBuilder parentBuilder) throws Exception {
		parentBuilder.e("h3").t(  HtmlUnicodeEscaperUtil.escapeHtmlUnicode(  tab.getLabel() ) ); //$NON-NLS-1$

		XMLBuilder bodyContentBuilder = parentBuilder.e("section"); //$NON-NLS-1$
		bodyContentBuilder.a("class", "step"); //$NON-NLS-1$ //$NON-NLS-2$

		if (tab.getAncillaryDataHeader() != null) {
			createBuilder( tab.getAncillaryDataHeader(), bodyContentBuilder);
		}

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
		XMLBuilder builder =  parentBuilder.e("fieldset").attr("class", "entity-group" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String legend =  comp.getLabelOrName() ;
		XMLBuilder legendBuilder = builder.e("legend"); //$NON-NLS-1$
		legendBuilder.e("span").text(legend); //$NON-NLS-1$
		addTooltip(legendBuilder, comp.getTooltip());
		XMLBuilder tableBuilder = builder.e("table").a("class", "table"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		XMLBuilder headerBuilder = tableBuilder.e("thead").e("tr"); //$NON-NLS-1$ //$NON-NLS-2$
		for (String heading : comp.getHeadings()) {
			headerBuilder.e("th").t( HtmlUnicodeEscaperUtil.escapeHtmlUnicode( heading) ); //$NON-NLS-1$
		}
		XMLBuilder bodyBuilder = tableBuilder.e("tbody"); //$NON-NLS-1$
		List<CETableRow> rows = comp.getRows();
		for (CETableRow row : rows) {
			XMLBuilder rowBuilder = bodyBuilder.e("tr"); //$NON-NLS-1$
			for (CEComponent child : row.getChildren()) {
				XMLBuilder cellBuilder = rowBuilder.e("td"); //$NON-NLS-1$
				if (child instanceof CEEnumeratingCodeField) {

					cellBuilder
						.e("label") //$NON-NLS-1$
							.a("class", "control-label col-sm-4") //$NON-NLS-1$ //$NON-NLS-2$
							.t( row.getLabelOrName() );


					String tooltip = row.getTooltip();
					addTooltip(cellBuilder, tooltip);

				} else if (child instanceof CEField) {
					createBuilder((CEField) child, false, cellBuilder);
				}
			}
		}
		return builder;
	}

	private XMLBuilder createBuilder(CEFieldSet comp, XMLBuilder parentBuilder) throws Exception {
		XMLBuilder fieldSetBuilder =  parentBuilder.e("fieldset").attr("class", "entity-group" );; //$NON-NLS-1$
		fieldSetBuilder.e("legend").t( comp.getLabelOrName()); //$NON-NLS-1$

		for (CEComponent child : comp.getChildren()) {
			if (child instanceof CEField) {
				createBuilder((CEField) child, true, fieldSetBuilder);
			} else {
				throw new IllegalArgumentException("Only attribute fields supported inside single entity"); //$NON-NLS-1$
			}
		}
		return fieldSetBuilder;
	}

	private XMLBuilder createBuilder(CEAncillaryFields comp, XMLBuilder parentBuilder) throws Exception {
		XMLBuilder informationFieldsBuilder =  parentBuilder.e("div").attr("class", "ancillary-data" ); //$NON-NLS-1$

		boolean firstChild = true;
		for (CEComponent child : comp.getChildren()) {
			if (child instanceof CEField) {
				if( !firstChild ){ // Do not add a break line before the first element
                    informationFieldsBuilder.e("br").up(); //$NON-NLS-1$
                }
				informationFieldsBuilder.e("span" ).t( child.getLabelOrName() + ": $["+  CollectEarthBalloonGenerator.EXTRA_HIDDEN_PREFIX + child.getName()+ "]" ).up();
				firstChild = false;
			} else {
				throw new IllegalArgumentException("Only attribute fields supported inside single entity"); //$NON-NLS-1$
			}
		}

		return informationFieldsBuilder.up(); // Close the div;
	}

	private XMLBuilder createBuilder(CEField comp, boolean includeLabel, XMLBuilder parentBuilder) throws Exception {
		//start of external container
		String elId = comp.getHtmlParameterName();

		//external form-group container
		XMLBuilder formGroupBuilder = parentBuilder == null ? XMLBuilder.create("div") : parentBuilder.e("div"); //$NON-NLS-1$ //$NON-NLS-2$
		formGroupBuilder.a("class", "form-group"); //$NON-NLS-1$ //$NON-NLS-2$
		if (includeLabel) {
			//label element
			formGroupBuilder.e("label") //$NON-NLS-1$
				.a("for", elId) //$NON-NLS-1$
				.a("class", "control-label col-sm-4") //$NON-NLS-1$ //$NON-NLS-2$
				.t( comp.getLabelOrName() );

			String tooltip = comp.getTooltip();
			addTooltip(formGroupBuilder, tooltip);
		}
		//form control external container (for grid alignment)
		XMLBuilder formControlContainer = formGroupBuilder.e("div") //$NON-NLS-1$
				.a("class", "col-sm-8"); //$NON-NLS-1$ //$NON-NLS-2$

		String componentAdditionalClass =  comp.isExtra() || comp.isCalculated() ? " " + CollectEarthBalloonGenerator.EXTRA_HIDDEN_FIELD_CLASS : "";

		if (comp instanceof CECodeField) {
			if (comp.isReadOnly()) {
				XMLBuilder inputBuilder = formControlContainer.e("input") //$NON-NLS-1$
					.a("id", elId) //$NON-NLS-1$
					.a("name", elId) //$NON-NLS-1$
					.a("type", "text") //$NON-NLS-1$ //$NON-NLS-2$
					.a("class", "form-control" + componentAdditionalClass) //$NON-NLS-1$ //$NON-NLS-2$
					.a("disabled", "disabled"); //$NON-NLS-1$ //$NON-NLS-2$
				if (comp.isExtra()) {
					inputBuilder.a("value", String.format(EXTRA_VALUE_FORMAT, comp.getName()));
				}
			} else {
				switch(((CEField) comp).getType()) {
				case CODE_BUTTON_GROUP:
					buildCodeButtonGroup(formControlContainer, (CECodeField) comp);
					break;
				case CODE_SELECT:
					buildCodeSelect(formControlContainer, (CECodeField) comp);
					break;
				case CODE_RANGE:
					buildCodeRange(formControlContainer, (CERangeField) comp);
					break;
				default:
					break;
				}
			}
		} else if (comp instanceof CEEnumeratingCodeField) {
			// skip it
		} else if (comp instanceof CEField) {
			XMLBuilder fieldBuilder;
			switch (((CEField) comp).getType()) {
			case SHORT_TEXT:
				fieldBuilder = formControlContainer.e("input") //$NON-NLS-1$
					.a("id", elId) //$NON-NLS-1$
					.a("name", elId) //$NON-NLS-1$
					.a("type", "text") //$NON-NLS-1$ //$NON-NLS-2$
					.a("class", "form-control" + componentAdditionalClass); //$NON-NLS-1$ //$NON-NLS-2$
				if (comp.isReadOnly()) {
					fieldBuilder.a("disabled", "disabled"); //$NON-NLS-1$ //$NON-NLS-2$
					if (comp.isExtra()) {
						fieldBuilder.a("value", String.format(EXTRA_VALUE_FORMAT, comp.getName()));
					}
				}
				break;
			case LONG_TEXT:
				fieldBuilder = formControlContainer.e("textarea") //$NON-NLS-1$
					.a("id", elId) //$NON-NLS-1$
					.a("rows", "3") //$NON-NLS-1$ //$NON-NLS-2$
					.a("name", elId) //$NON-NLS-1$
					.a("class", "form-control" + componentAdditionalClass) //$NON-NLS-1$ //$NON-NLS-2$
					.t(" "); //$NON-NLS-1$
				if (comp.isReadOnly()) {
					fieldBuilder.a("disabled", "disabled"); //$NON-NLS-1$ //$NON-NLS-2$
					if (comp.isExtra()) {
						fieldBuilder.a("value", String.format(EXTRA_VALUE_FORMAT, comp.getName()));
					}
				}
				break;
			case INTEGER:
			case REAL:
				fieldBuilder = formControlContainer.e("input") //$NON-NLS-1$
					.a("id", elId) //$NON-NLS-1$
					.a("name", elId) //$NON-NLS-1$
					.a("type", "text") //$NON-NLS-1$ //$NON-NLS-2$
					.a("class", "form-control numeric" + componentAdditionalClass); //$NON-NLS-1$ //$NON-NLS-2$
				String value = "0";
				if (comp.isReadOnly()) {
					fieldBuilder.a("disabled", "disabled"); //$NON-NLS-1$ //$NON-NLS-2$
					if (comp.isExtra()) {
						value = String.format(EXTRA_VALUE_FORMAT, comp.getName());
					}
				}
				fieldBuilder.a("value", value );
				break;
			case BOOLEAN:
				if (comp.isReadOnly()) {
					fieldBuilder = formControlContainer.e("input") //$NON-NLS-1$
						.a("id", elId) //$NON-NLS-1$
						.a("name", elId) //$NON-NLS-1$
						.a("type", "text") //$NON-NLS-1$ //$NON-NLS-2$
						.a("class", "form-control" + componentAdditionalClass) //$NON-NLS-1$ //$NON-NLS-2$
						.a("data-field-type", comp.getType().name()) //$NON-NLS-1$
						.a("disabled", "disabled");
					if (comp.isExtra()) {
						fieldBuilder.a("value", String.format(EXTRA_VALUE_FORMAT, comp.getName()));
					}
				} else {
					XMLBuilder containerBuilder = formControlContainer
						.e("div") //$NON-NLS-1$
						.a("class", "boolean-group") //$NON-NLS-1$ //$NON-NLS-2$
						.a("data-toggle", "buttons-radio"); //$NON-NLS-1$ //$NON-NLS-2$
					containerBuilder.e("input") //$NON-NLS-1$
						.a("id", elId) //$NON-NLS-1$
						.a("name", elId) //$NON-NLS-1$
						.a("type", "hidden") //$NON-NLS-1$ //$NON-NLS-2$
						.a("class", "form-control") //$NON-NLS-1$ //$NON-NLS-2$
						.a("data-field-type", comp.getType().name()); //$NON-NLS-1$
					containerBuilder.e("button") //$NON-NLS-1$
						.a("type", "button") //$NON-NLS-1$ //$NON-NLS-2$
						.a("class", "btn btn-info") //$NON-NLS-1$ //$NON-NLS-2$
						.a("value", "true") //$NON-NLS-1$ //$NON-NLS-2$
						.t( HtmlUnicodeEscaperUtil.escapeHtmlUnicode( Messages.getString("CEComponentHTMLFormatter.0", language) ) ); //$NON-NLS-1$
					containerBuilder.e("button") //$NON-NLS-1$
						.a("type", "button") //$NON-NLS-1$ //$NON-NLS-2$
						.a("class", "btn btn-info") //$NON-NLS-1$ //$NON-NLS-2$
						.a("value", "false") //$NON-NLS-1$ //$NON-NLS-2$
						.t( HtmlUnicodeEscaperUtil.escapeHtmlUnicode( Messages.getString("CEComponentHTMLFormatter.88", language) ) ); //$NON-NLS-1$
				}
				break;
			case COORDINATE:
				break;
			case DATE:
			case TIME:
				XMLBuilder groupContainerBuilder = formControlContainer
				.e("div") //$NON-NLS-1$
					.a("class", "input-group date " + (((CEField) comp).getType() == CEFieldType.DATE ? "datepicker" : "timepicker")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					.a("id", elId + "timepicker"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				XMLBuilder inputFieldBuilder = groupContainerBuilder
					.e("input") //$NON-NLS-1$
						.a("id", elId) //$NON-NLS-1$
						.a("name", elId) //$NON-NLS-1$
						.a("class", "form-control"); //$NON-NLS-1$ //$NON-NLS-2$
				groupContainerBuilder.e("span") //$NON-NLS-1$
						.a("class", "input-group-text") //$NON-NLS-1$ //$NON-NLS-2$
						.a("data-td-target", "#" + elId + "timepicker") //$NON-NLS-1$ //$NON-NLS-2$
						
						.e("span") //$NON-NLS-1$
							.a("class", ((CEField) comp).getType() == CEFieldType.DATE ?  "fas fa-calendar" : "fas fa-clock" ) //$NON-NLS-1$ //$NON-NLS-2$
				;
				if (comp.isReadOnly()) {
					inputFieldBuilder.a("disabled", "disabled");
					if (comp.isExtra()) {
						inputFieldBuilder.a("value", String.format(EXTRA_VALUE_FORMAT, comp.getName()));
					}
				}
				break;
			default:
				break;
			}
		}
		return formGroupBuilder;
	}

	public void addTooltip(XMLBuilder formGroupBuilder, String tooltip) {
		if( !StringUtils.isBlank(tooltip) ) {
			formGroupBuilder.e("span")
				.a( "class", "ui-icon  ui-icon-info" )
				.a( "style", "display:inline-block")
				.a( "title", tooltip);
		}
	}

	private void buildCodeSelect(XMLBuilder builder, CECodeField comp) {
		String elId = comp.getHtmlParameterName();

		//build select
		XMLBuilder selectBuilder = builder.e("select") //$NON-NLS-1$
			.a("id", elId) //$NON-NLS-1$
			.a("name", elId) //$NON-NLS-1$
			.a("data-field-type", comp.getType().name()) //$NON-NLS-1$
			.a("class", "form-control selectboxit show-menu-arrow show-tick") //$NON-NLS-1$ //$NON-NLS-2$
			.a("data-width", "75px"); //$NON-NLS-1$ //$NON-NLS-2$
		if (comp.getParentName() != null) {
			selectBuilder.a("data-parent-id-field-id", comp.getParentName()); //$NON-NLS-1$
		}
		//add root items, if any
		Map<Integer, List<CodeListItem>> itemsByParentCode = ((CECodeField) comp).getCodeItemsByParentId();
		List<CodeListItem> rootItems = itemsByParentCode.get(0);
		if (rootItems != null) {

			boolean hasNAoption = false;
			for (CodeListItem item : rootItems) {
				if(
						item.getCode().equalsIgnoreCase("na") ||  //$NON-NLS-1$
						item.getCode().equalsIgnoreCase("n/a") //$NON-NLS-1$
				){
					hasNAoption=true;
				}
			}

			if(!hasNAoption){
				selectBuilder.e("option").a("selected","true").a("value", "").t( Messages.getString("CEComponentHTMLFormatter.119", language) ); //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}

			for (CodeListItem item : rootItems) {
				String itemLabel = getItemLabel(item);
				selectBuilder.e("option") //$NON-NLS-1$
					.a("value", item.getCode()) //$NON-NLS-1$
					.t(itemLabel);
			}

		}
	}

	private void buildCodeRange(XMLBuilder builder, CERangeField comp) {
		String elId = comp.getHtmlParameterName();

		//build select
		XMLBuilder selectBuilder = builder.e("select") //$NON-NLS-1$
			.a("id", elId) //$NON-NLS-1$
			.a("name", elId) //$NON-NLS-1$
			.a("data-field-type", comp.getType().name()) //$NON-NLS-1$
			.a("class", "form-control selectboxit show-menu-arrow show-tick") //$NON-NLS-1$ //$NON-NLS-2$
			.a("data-width", "75px"); //$NON-NLS-1$ //$NON-NLS-2$

		//add root items, if any
		for( int i=comp.getFrom(); i<comp.getTo(); i++ ){
				String item = i+""; //$NON-NLS-1$
				selectBuilder.e("option") //$NON-NLS-1$
					.a("value", item) //$NON-NLS-1$
					.t(item);
		}
	}

	private void buildCodeButtonGroup(XMLBuilder formControlContainer, CECodeField comp) {
		String elId = comp.getHtmlParameterName();

		//button groups external container
		String groupId = elId + "_group"; //$NON-NLS-1$

		XMLBuilder itemsGroupExternalContainer = formControlContainer.e("div") //$NON-NLS-1$
			.a("id", groupId) //$NON-NLS-1$
			.a("class", "code-items-group"); //$NON-NLS-1$ //$NON-NLS-2$

		XMLBuilder hiddenInputField = itemsGroupExternalContainer.e("input") //$NON-NLS-1$
				.a("id", elId) //$NON-NLS-1$
				.a("name", elId) //$NON-NLS-1$
				.a("type", "hidden") //$NON-NLS-1$ //$NON-NLS-2$
				.a("class", "form-control") //$NON-NLS-1$ //$NON-NLS-2$
				.a("data-field-type", comp.getType().name()); //$NON-NLS-1$

		if (comp.getParentName() != null) {
			hiddenInputField.a("data-parent-id-field-id", comp.getParentName()); //$NON-NLS-1$
		}

		Map<Integer, List<CodeListItem>> itemsByParentCode = ((CECodeField) comp).getCodeItemsByParentId();
		for (Entry<Integer, List<CodeListItem>> entry : itemsByParentCode.entrySet()) {
			//one button group for every list of codes by parent code
			Integer parentId = entry.getKey();
			String itemsGroupId = groupId + "_" + parentId; //$NON-NLS-1$
			XMLBuilder buttonsGroup = itemsGroupExternalContainer
				.e("div") //$NON-NLS-1$
					.a("id", itemsGroupId) //$NON-NLS-1$
					.a("class", "code-items") //$NON-NLS-1$ //$NON-NLS-2$
					.a("data-toggle", comp.isMultiple() ? "buttons": "buttons-radio"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (parentId != 0) {
				buttonsGroup
					.a("data-parent-id", parentId.toString()) //$NON-NLS-1$
					.a("style", "display: none;"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			List<CodeListItem> items = entry.getValue();
			if (items == null || items.isEmpty()) {
				buttonsGroup.t(" "); //always use close tag //$NON-NLS-1$
			} else {
				for (CodeListItem item : items) {
					String itemLabel = getItemLabel(item);
					XMLBuilder itemBuilder = buttonsGroup
						.e("button") //$NON-NLS-1$
							.a("type", "button") //$NON-NLS-1$ //$NON-NLS-2$
							.a("class", "btn btn-info code-item") //$NON-NLS-1$ //$NON-NLS-2$
							.a("data-code-item-id", String.valueOf(item.getId())) //$NON-NLS-1$
							.a("value", item.getCode()) //$NON-NLS-1$
							.t(itemLabel);

					String description = getDescription(item);

					if (item.hasUploadedImage()) {
						String imgFilePath = CollectEarthProjectFileCreatorImpl.getCodeListImageFilePath(item);
						String titleText = StringUtils.isBlank(description) ? "" : description; //$NON-NLS-1$
						String htmlTitle = "<span><img src=\"" + imgFilePath + "\" width=\"250\"><br/>" + titleText + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						itemBuilder
							.a("title", htmlTitle) //$NON-NLS-1$
							.a("data-html", "true") //$NON-NLS-1$ //$NON-NLS-2$
							.a("data-placement", "auto bottom"); //$NON-NLS-1$ //$NON-NLS-2$
					} else if (StringUtils.isNotBlank(description)) {
						itemBuilder.a("title", description); //$NON-NLS-1$
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
				put(javax.xml.transform.OutputKeys.INDENT, "yes"); //$NON-NLS-1$
				put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$
				put(javax.xml.transform.OutputKeys.STANDALONE, "yes"); //$NON-NLS-1$
				put(javax.xml.transform.OutputKeys.METHOD, "html"); //$NON-NLS-1$
			}};
			builder.toWriter(writer, outputProperties);
			String result = writer.toString();
			return result.replaceAll("&amp;#", "&#"); //to avoid double escaping unicode characters
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getItemLabel(CodeListItem item) {
		return getItemLabel(item, language);
	}

	private String getDescription(CodeListItem item) {
		return getDescription(item, language);
	}

	public static String getItemLabel(CodeListItem item, String lang) {
		String itemLabel = item.getLabel(lang, true);
		if (StringUtils.isBlank(itemLabel)) {
			itemLabel = item.getCode();
		}
		return   HtmlUnicodeEscaperUtil.escapeHtmlUnicode( itemLabel );
	}

	public static String getDescription(CodeListItem item, String lang) {
		String description = item.getDescription(lang, true);
		return HtmlUnicodeEscaperUtil.escapeHtmlUnicode( description );
	}

}