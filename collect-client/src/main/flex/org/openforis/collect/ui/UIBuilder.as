package org.openforis.collect.ui {
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.core.ClassFactory;
	import mx.core.IFactory;
	import mx.core.IVisualElement;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.BooleanAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CalculatedAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CoordinateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.DateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.FileAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NumericAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.RangeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.SchemaProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.metamodel.proxy.TaxonAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.TextAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.TextAttributeDefinitionProxy$Type;
	import org.openforis.collect.metamodel.proxy.TimeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.UnitProxy;
	import org.openforis.collect.metamodel.ui.UIOptions$CoordinateAttributeFieldsOrder;
	import org.openforis.collect.metamodel.ui.UIOptions$Direction;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.ui.component.datagrid.CompleteColumnItemRenderer;
	import org.openforis.collect.ui.component.datagrid.RecordOwnerColumnItemRenderer;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryErrorsColumnItemRenderer;
	import org.openforis.collect.ui.component.datagroup.DataGridHeaderRenderer;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;
	import org.openforis.collect.ui.component.detail.AttributeItemRenderer;
	import org.openforis.collect.ui.component.detail.CodeAttributeFormItem;
	import org.openforis.collect.ui.component.detail.CompositeAttributeFormItem;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.detail.MultipleAttributeDataGroupFormItem;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.MultipleEntityAsTableFormItem;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.ui.component.detail.SingleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.SingleEntityFormItem;
	import org.openforis.collect.ui.component.input.AutoCompleteInputField;
	import org.openforis.collect.ui.component.input.BooleanInputField;
	import org.openforis.collect.ui.component.input.CalculatedAttributeField;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CoordinateAttributeRenderer;
	import org.openforis.collect.ui.component.input.DateAttributeRenderer;
	import org.openforis.collect.ui.component.input.FixedCodeInputField;
	import org.openforis.collect.ui.component.input.ImageInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.IntegerInputField;
	import org.openforis.collect.ui.component.input.MemoInputField;
	import org.openforis.collect.ui.component.input.MultipleCodeInputField;
	import org.openforis.collect.ui.component.input.NumericAttributeRenderer;
	import org.openforis.collect.ui.component.input.NumericInputField;
	import org.openforis.collect.ui.component.input.RangeAttributeRenderer;
	import org.openforis.collect.ui.component.input.RangeInputField;
	import org.openforis.collect.ui.component.input.StringInputField;
	import org.openforis.collect.ui.component.input.TaxonAttributeRenderer;
	import org.openforis.collect.ui.component.input.TimeAttributeRenderer;
	import org.openforis.collect.util.UIUtil;
	
	import spark.components.Group;
	import spark.components.HGroup;
	import spark.components.Label;
	import spark.components.SkinnableContainer;
	import spark.components.VGroup;
	import spark.components.gridClasses.GridColumn;
	import spark.layouts.HorizontalLayout;
	import spark.layouts.VerticalLayout;
	import spark.layouts.supportClasses.LayoutBase;
	
	/**
	 * @author Mino Togna
	 * @author S. Ricci
	 * 
	 * */
	public class UIBuilder {
		
		private static const TABLE_VERTICAL_GAP:int = 2;
		private static const TABLE_HORIZONTAL_GAP:int = 4;
		private static const DATA_GROUP_HEADER_STYLE:String = "dataGroupHeader";
		private static const HEADER_LABEL_STYLE:String = "bold";
		private static const ATTRIBUTE_INPUT_FIELD_HEIGHT:Number = 22;
		private static const VALIDATION_DISPLAY_BORDER_SIZE:Number = 1;
		private static const VALIDATION_DISPLAY_DOUBLE_BORDER_SIZE:Number = 2 * VALIDATION_DISPLAY_BORDER_SIZE;
		private static const ATTRIBUTE_RENDERER_HEIGHT:Number = ATTRIBUTE_INPUT_FIELD_HEIGHT + VALIDATION_DISPLAY_DOUBLE_BORDER_SIZE;
		public static const COMPOSITE_ATTRIBUTE_H_GAP:int = TABLE_HORIZONTAL_GAP + VALIDATION_DISPLAY_DOUBLE_BORDER_SIZE;
		public static const COMPOSITE_ATTRIBUTE_LABELS_V_GAP:int = 6;
		public static const GROUPING_LABEL_PADDING_TOP:int = 4;
		
		public static function buildForm(rootEntity:EntityDefinitionProxy, version:ModelVersionProxy):FormContainer {
			var formContainer:FormContainer = new FormContainer();
			formContainer.rootEntityDefinition = rootEntity;
			formContainer.version = version;
			return formContainer;
		}
		
		public static function getRecordSummaryListColumns(rootEntity:EntityDefinitionProxy):IList {
			var columns:IList = new ArrayList();
			var column:GridColumn;
			//key attributes columns
			var position:int = 1;
			var survey:SurveyProxy = rootEntity.survey;
			var schema:SchemaProxy = survey.schema;
			var keyAttributeDefs:IList = schema.getKeyAttributeDefinitions(rootEntity);
			var headerText:String, dataField:String, width:Number, labelFunction:Function;
			for each(var keyAttributeDef:AttributeDefinitionProxy in keyAttributeDefs) {
				headerText = keyAttributeDef.getInstanceOrHeadingLabelText();
				dataField = "key" + position;
				width = NaN;
				labelFunction = RecordSummaryDataGrid.keyLabelFunction;
				column = getGridColumn(headerText, dataField, width, labelFunction, true);
				columns.addItem(column);
				position ++;
			}
			//count entity columns
			var firstLevelDefs:IList = rootEntity.childDefinitions;
			position = 1;
			for each(var nodeDef:NodeDefinitionProxy in firstLevelDefs) {
				if(nodeDef is EntityDefinitionProxy) {
					var entityDef:EntityDefinitionProxy = EntityDefinitionProxy(nodeDef);
					if(entityDef.countInSummaryList) {
						//headerText = Message.get("list.headerCount", [entityDef.getLabelText()]);
						headerText = entityDef.getHeadingOrInstanceLabelText();
						dataField = "count" + position;
						width = 80;
						labelFunction = RecordSummaryDataGrid.entityCountLabelFunction;
						column = getGridColumn(headerText, dataField, width, labelFunction, true);
						columns.addItem(column);
						position ++;
					}
				}
			}
			//errors count column
			column = getGridColumn(Message.get("list.errors"), "errors", 80, RecordSummaryDataGrid.errorsCountLabelFunction, false, new ClassFactory(RecordSummaryErrorsColumnItemRenderer));
			columns.addItem(column);
			//warnings count column
			column = getGridColumn(Message.get("list.warnings"), "warnings", 80, UIUtil.gridColumnNumberLabelFunction);
			columns.addItem(column);
			//creation date column
			column = getGridColumn(Message.get("list.creationDate"), "creationDate", 120, UIUtil.gridColumnDateTimeLabelFunction);
			columns.addItem(column);
			//date modified column
			column = getGridColumn(Message.get("list.modifiedDate"), "modifiedDate", 120, UIUtil.gridColumnDateTimeLabelFunction);
			columns.addItem(column);
			//owner column
			column = getGridColumn(Message.get("list.owner"), "ownerName", 120, null, true, new ClassFactory(RecordOwnerColumnItemRenderer));
			columns.addItem(column);
			//entry completed column
			column = getGridColumn(Message.get("list.entryComplete"), "entryComplete", 70, 
				null, true, new ClassFactory(CompleteColumnItemRenderer));
			columns.addItem(column);
			//cleansing completed column
			column = getGridColumn(Message.get("list.cleansingComplete"), "cleansingComplete", 70, 
				null, true, new ClassFactory(CompleteColumnItemRenderer));
			columns.addItem(column);
			return columns;
		}
		
		public static function getAttributeFormItem(def:AttributeDefinitionProxy):AttributeFormItem {
			var parentLayout:String = def.parentLayout;
			var formItem:AttributeFormItem = null;
			if(def is CodeAttributeDefinitionProxy) {
				formItem = new CodeAttributeFormItem();
			} else if(def is CoordinateAttributeDefinitionProxy || def is TaxonAttributeDefinitionProxy) {
				formItem = new CompositeAttributeFormItem();
			} else if(def.multiple) {
				if(parentLayout == UIUtil.LAYOUT_TABLE){
					formItem = new MultipleAttributeDataGroupFormItem();
				} else {
					formItem = new MultipleAttributeFormItem();
				}
			} else {
				formItem = new SingleAttributeFormItem();
			}
			formItem.attributeDefinition = def;
			return formItem;
		}
		
		public static function getEntityFormItem(definition:EntityDefinitionProxy):EntityFormItem {
			var entityFormItem:EntityFormItem = null;
			if(definition.multiple) {
				if ( definition.layout == UIUtil.LAYOUT_FORM ) {
					entityFormItem = new MultipleEntityFormItem();
				} else {
					entityFormItem = new MultipleEntityAsTableFormItem();
					MultipleEntityAsTableFormItem(entityFormItem).entitiesDirection = 
						definition.direction == UIOptions$Direction.BY_COLUMNS ? 
							MultipleEntityAsTableFormItem.DIRECTION_BY_COLUMNS:
							MultipleEntityAsTableFormItem.DIRECTION_BY_ROWS;
				}
			} else {
				entityFormItem = new SingleEntityFormItem();
			}
			entityFormItem.entityDefinition = definition;
			return entityFormItem;
		}
		
		public static function getInputFieldWidth(def:AttributeDefinitionProxy):Number {
			var parentLayout:String = def.parentLayout;
			if(def is BooleanAttributeDefinitionProxy) {
				var headerText:String = def.getInstanceOrHeadingLabelText();
				var headerWidth:Number = UIUtil.measureGridHeaderWidth(headerText);
				var width:Number = Math.max(headerWidth, 20);
				return width;
			} else if ( def is CalculatedAttributeDefinitionProxy ) {
				return 100;
			} else if(def is CodeAttributeDefinitionProxy) {
				if(parentLayout == UIUtil.LAYOUT_TABLE) {
					if(def.key && def.parent.enumerable) {
						//return NaN;
						return 150;
					} else {
						return 85;
					}
				} else {
					return NaN;
				}
			} else if(def is CoordinateAttributeDefinitionProxy) {
				if(parentLayout == UIUtil.LAYOUT_TABLE) {
					return 100 + 70 + 70 + COMPOSITE_ATTRIBUTE_H_GAP * 2;
				} else {
					return 100;
				}
			} else if(def is DateAttributeDefinitionProxy) {
				return 130;
			} else if(def is FileAttributeDefinitionProxy) {
				return 300;
			} else if(def is NumericAttributeDefinitionProxy) {
				var units:IList = NumericAttributeDefinitionProxy(def).units;
				var gap:int = 2;
				var numericInputFieldWidth:int = def is RangeAttributeDefinitionProxy ? 120: 70;
				var unitDropDownWidth:int = 70;
				var result:int = numericInputFieldWidth;
				if(units.length > 1) {
					result += gap + unitDropDownWidth;
				} else if ( units.length == 1 && def.parentLayout == UIUtil.LAYOUT_FORM ) {
					var unit:UnitProxy = units.getItemAt(0) as UnitProxy;
					var unitWidth:Number = UIUtil.measureUnitWidth(unit.getAbbreviation());
					result += gap + unitWidth;
				}
				return result;
			} else if(def is TaxonAttributeDefinitionProxy) {
				if(parentLayout == UIUtil.LAYOUT_TABLE) {
					var total:int = 0;
					var visibleFieldsCount:int = 0;
					var taxonDefn:TaxonAttributeDefinitionProxy = TaxonAttributeDefinitionProxy(def);
					if ( taxonDefn.codeVisible ) {
						total += TaxonAttributeRenderer.CODE_WIDTH;
						visibleFieldsCount ++;
					}
					if ( taxonDefn.scientificNameVisible ) {
						total += TaxonAttributeRenderer.SCIENTIFIC_NAME_WIDTH;
						visibleFieldsCount ++;
					}
					if ( taxonDefn.vernacularNameVisible ) {
						total += TaxonAttributeRenderer.VERNACULAR_NAME_WIDTH;
						visibleFieldsCount ++;
					}
					if ( taxonDefn.languageCodeVisible ) {
						total += TaxonAttributeRenderer.LANGUAGE_CODE_WIDTH;
						visibleFieldsCount ++;
					}
					if ( taxonDefn.languageVarietyVisible ) {
						total += TaxonAttributeRenderer.LANGUAGE_VARIETY_WIDTH;
						visibleFieldsCount ++;
					}
					total += COMPOSITE_ATTRIBUTE_H_GAP * (visibleFieldsCount - 1);
					return total;
				} else {
					return TaxonAttributeRenderer.SCIENTIFIC_NAME_WIDTH;
				}
			} else if(def is TextAttributeDefinitionProxy) {
				var textAttributeDef:TextAttributeDefinitionProxy = TextAttributeDefinitionProxy(def);
				var type:TextAttributeDefinitionProxy$Type = textAttributeDef.type;
				switch(type) {
					case TextAttributeDefinitionProxy$Type.MEMO:
						return 300;
					case TextAttributeDefinitionProxy$Type.SHORT:
						return 100;
					default:
						return 150;
				}
			} else if(def is TimeAttributeDefinitionProxy) {
				return 64;
			} else {
				return 100;
			}
		}
		
		public static function getAttributeDataGroupHeaderWidth(def:AttributeDefinitionProxy, ancestorEntity:EntityProxy):Number {
			var parentEntityDefn:EntityDefinitionProxy = def.parent;
			var directionByColumns:Boolean = parentEntityDefn != null && parentEntityDefn.direction == UIOptions$Direction.BY_COLUMNS;
			if(ancestorEntity != null && parentEntityDefn.enumerable && def.key && def is CodeAttributeDefinitionProxy) {
				var width:Number = getEnumeratedCodeHeaderWidth(def, ancestorEntity);
				return width + VALIDATION_DISPLAY_DOUBLE_BORDER_SIZE;
			} else if ( directionByColumns ) {
				return NaN;
			} else {
				var inputFieldWidth:Number = getInputFieldWidth(def);
				if(!isNaN(inputFieldWidth)) {
					return inputFieldWidth + VALIDATION_DISPLAY_DOUBLE_BORDER_SIZE;
				} else {
					return NaN;
				}
			}
		}

		public static function getAttributeDataGroupHeaderHeight(defn:AttributeDefinitionProxy, ancestorEntity:EntityProxy):Number {
			var result:Number;
			var directionByColumns:Boolean = defn.parent != null && defn.parent.direction == UIOptions$Direction.BY_COLUMNS;
			if ( directionByColumns ) {
				if ( defn is CoordinateAttributeDefinitionProxy ) {
					result = 3 * ATTRIBUTE_INPUT_FIELD_HEIGHT + 3 * COMPOSITE_ATTRIBUTE_LABELS_V_GAP;
				} else if ( defn is TaxonAttributeDefinitionProxy ) {
					result = 5 * ATTRIBUTE_INPUT_FIELD_HEIGHT + 4 * COMPOSITE_ATTRIBUTE_LABELS_V_GAP;
				} else {
					result = ATTRIBUTE_INPUT_FIELD_HEIGHT;
				}
			} else {
				result = ATTRIBUTE_INPUT_FIELD_HEIGHT;
			}
			result += VALIDATION_DISPLAY_DOUBLE_BORDER_SIZE;
			return result;
		}
		
		public static function getEnumeratedCodeHeaderWidth(def:AttributeDefinitionProxy, ancestorEntity:EntityProxy):Number {
			var parentEntityDefn:EntityDefinitionProxy = def.parent;
			var enumeratedCodeWidth:Number = ancestorEntity.getEnumeratedCodeWidth(parentEntityDefn.name);
			var headerText:String = def.getNumberAndHeadingLabelText();
			var headerWidth:Number = UIUtil.measureGridHeaderWidth(headerText);
			var width:Number = Math.max(headerWidth, enumeratedCodeWidth);
			return width;
		}
		
		public static function getInputField(def:AttributeDefinitionProxy):InputField {
			var parentLayout:String = def.parentLayout;
			var inputField:InputField = null;
			if(def is BooleanAttributeDefinitionProxy) {
				inputField = new BooleanInputField();
			} else if ( def is CalculatedAttributeDefinitionProxy ) {
				inputField = new CalculatedAttributeField();
			} else if(def is CodeAttributeDefinitionProxy) {
				var codeDef:CodeAttributeDefinitionProxy = CodeAttributeDefinitionProxy(def);
				if(parentLayout == UIUtil.LAYOUT_TABLE && codeDef.parent.enumerable && codeDef.key) {
					inputField = new FixedCodeInputField();
				} else if(def.multiple) {
					inputField = new MultipleCodeInputField();
				} else {
					inputField = new CodeInputField();
				}
			} else if(def is FileAttributeDefinitionProxy) {
				//TODO use different input fields for text and image files
				inputField = new ImageInputField();
			} else if(def is NumberAttributeDefinitionProxy) {
				var numberAttributeDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(def);
				if (numberAttributeDefn.integer) {
					inputField = new IntegerInputField();
				} else {
					inputField = new NumericInputField();
				}
			} else if(def is RangeAttributeDefinitionProxy) {
				inputField = new RangeInputField();
			} else if(def is TextAttributeDefinitionProxy) {
				var textAttributeDef:TextAttributeDefinitionProxy = TextAttributeDefinitionProxy(def);
				var type:TextAttributeDefinitionProxy$Type = textAttributeDef.type;
				switch(type) {
					case TextAttributeDefinitionProxy$Type.MEMO:
						inputField = new MemoInputField();
						break;
					case TextAttributeDefinitionProxy$Type.SHORT:
					default:
						if(def.autocomplete) {
							inputField = new AutoCompleteInputField();
						} else {
							inputField = new StringInputField();
						}
						break;
				}
			}
			inputField.width = getInputFieldWidth(def);
			inputField.attributeDefinition = def;
			return inputField;
		}
		
		public static function getAttributeItemRenderer(def:AttributeDefinitionProxy):AttributeItemRenderer {
			var renderer:AttributeItemRenderer;
			if(def is CoordinateAttributeDefinitionProxy) {
				renderer = new CoordinateAttributeRenderer();
			} else if(def is DateAttributeDefinitionProxy) {
				renderer = new DateAttributeRenderer();
			} else if(def is NumericAttributeDefinitionProxy) {
				renderer = def is NumberAttributeDefinitionProxy ? new NumericAttributeRenderer(): new RangeAttributeRenderer;
				var width:Number = getInputFieldWidth(def);
				var borderWidth:Number = 1;
				renderer.width = width + borderWidth * 2;
			} else if(def is TaxonAttributeDefinitionProxy) {
				renderer = new TaxonAttributeRenderer();
			} else if(def is TimeAttributeDefinitionProxy) {
				renderer = new TimeAttributeRenderer();
			}
			if(renderer == null) {
				renderer = new AttributeItemRenderer();
				var inputField:InputField = getInputField(def);
				inputField.fieldIndex = 0;
				renderer.addElement(inputField);
				BindingUtils.bindProperty(inputField, "parentEntity", renderer, "parentEntity");
				BindingUtils.bindProperty(inputField, "attribute", renderer, "attribute");
				if ( inputField.hasOwnProperty("attributes") ) {
					BindingUtils.bindProperty(inputField, "attributes", renderer, "attributes");
				}
			}
			renderer.attributeDefinition = def;
			return renderer;
		}
		
		public static function getDataGroupHeader(defn:NodeDefinitionProxy, parentEntity:EntityProxy = null):IVisualElement {
			var elem:IVisualElement = null;
			if(defn is AttributeDefinitionProxy){
				elem = getAttributeDataGroupHeader(defn as AttributeDefinitionProxy, parentEntity);
			} else if(defn is EntityDefinitionProxy) {
				elem = getEntityDataGroupHeader(defn as EntityDefinitionProxy, parentEntity);
			}
			return elem;
		}
		
		private static function getEntityDataGroupHeader(defn:EntityDefinitionProxy, parentEntity:EntityProxy = null):IVisualElement {
			var directionByColumns:Boolean = defn.parent != null && defn.parent.direction == UIOptions$Direction.BY_COLUMNS;
			var result:SkinnableContainer = new SkinnableContainer();
			result.styleName = DATA_GROUP_HEADER_STYLE;
			var l:Label = new Label();
			l.styleName = HEADER_LABEL_STYLE;
			l.text = defn.getNumberAndHeadingLabelText();
			var layout:LayoutBase;
			if ( directionByColumns ) {
				layout = new HorizontalLayout();
				l.width = 200;
			} else {
				layout = new VerticalLayout();
				result.percentHeight = 100;
			}
			result.layout = layout;
			
			var entityLabelContainer:VGroup = new VGroup();
			entityLabelContainer.paddingTop = 4;
			entityLabelContainer.addElement(l);
			result.addElement(entityLabelContainer);
			
			var childDefinitionsContainer:Group;
			if ( directionByColumns ) {
				var vGroup:VGroup = new VGroup();
				vGroup.gap = TABLE_VERTICAL_GAP;
				childDefinitionsContainer = vGroup;
			} else {
				var hGroup:HGroup = new HGroup();
				hGroup.gap = TABLE_HORIZONTAL_GAP;
				hGroup.verticalAlign = "bottom";
				hGroup.percentHeight = 100;
				childDefinitionsContainer = hGroup;
			}
			var childDefn:ListCollectionView = defn.childDefinitions;
			for each (var childDef:NodeDefinitionProxy in childDefn) {
				var elem:IVisualElement = getDataGroupHeader(childDef, null);
				childDefinitionsContainer.addElement(elem);
			}
			result.addElement(childDefinitionsContainer);
			
			return result;
		}
		
		private static function getAttributeDataGroupHeader(defn:AttributeDefinitionProxy, parentEntity:EntityProxy = null):IVisualElement {
			var result:SkinnableContainer = new SkinnableContainer();
			result.styleName = DATA_GROUP_HEADER_STYLE;
			var width:Number = getAttributeDataGroupHeaderWidth(defn, parentEntity);
			result.width = width;
			var directionByColumns:Boolean = defn.parent != null && defn.parent.direction == UIOptions$Direction.BY_COLUMNS;
			if ( directionByColumns ) {
				result.height = getAttributeDataGroupHeaderHeight(defn, parentEntity);
				var layout:HorizontalLayout = new HorizontalLayout();
				layout.paddingTop = GROUPING_LABEL_PADDING_TOP;
				result.layout = layout;
			} else {
				result.percentHeight = 100;
			}
			var compositeAttributeLabelsGroup:Group;
			if ( directionByColumns ) {
				compositeAttributeLabelsGroup = new VGroup();
				(compositeAttributeLabelsGroup as VGroup).gap = COMPOSITE_ATTRIBUTE_LABELS_V_GAP;
			} else {
				compositeAttributeLabelsGroup = new HGroup();
				(compositeAttributeLabelsGroup as HGroup).gap = COMPOSITE_ATTRIBUTE_H_GAP;
			}
			
			var l:Label;
			var defnLabel:String = defn.getNumberAndHeadingLabelText();
			if(defn is TaxonAttributeDefinitionProxy) {
				var taxonAttr:TaxonAttributeDefinitionProxy = TaxonAttributeDefinitionProxy(defn);
				//attribute label
				l = getLabel(defnLabel, 100, HEADER_LABEL_STYLE, directionByColumns);
				result.addElement(l);
				//subheader
				if ( taxonAttr.codeVisible ) {
					l = getLabel(Message.get('edit.taxon.code'), TaxonAttributeRenderer.CODE_WIDTH, HEADER_LABEL_STYLE, directionByColumns);
					l.height = ATTRIBUTE_INPUT_FIELD_HEIGHT;
					compositeAttributeLabelsGroup.addElement(l);
				}
				if ( taxonAttr.scientificNameVisible ) {
					l = getLabel(Message.get('edit.taxon.scientificName'), TaxonAttributeRenderer.SCIENTIFIC_NAME_WIDTH, HEADER_LABEL_STYLE, directionByColumns);
					l.height = ATTRIBUTE_INPUT_FIELD_HEIGHT;
					compositeAttributeLabelsGroup.addElement(l);
				}
				if ( taxonAttr.vernacularNameVisible ) {
					l = getLabel(Message.get('edit.taxon.vernacularName'), TaxonAttributeRenderer.VERNACULAR_NAME_WIDTH, HEADER_LABEL_STYLE, directionByColumns);
					l.height = ATTRIBUTE_INPUT_FIELD_HEIGHT;
					compositeAttributeLabelsGroup.addElement(l);
				}
				if ( taxonAttr.languageCodeVisible ) {
					l = getLabel(Message.get('edit.taxon.languageCode'), TaxonAttributeRenderer.LANGUAGE_CODE_WIDTH, HEADER_LABEL_STYLE, directionByColumns);
					l.height = ATTRIBUTE_INPUT_FIELD_HEIGHT;
					compositeAttributeLabelsGroup.addElement(l);
				}
				if ( taxonAttr.languageVarietyVisible ) {
					l = getLabel(Message.get('edit.taxon.languageVariety'), TaxonAttributeRenderer.LANGUAGE_VARIETY_WIDTH, HEADER_LABEL_STYLE, directionByColumns);
					l.height = ATTRIBUTE_INPUT_FIELD_HEIGHT;
					compositeAttributeLabelsGroup.addElement(l);
				}
				result.addElement(compositeAttributeLabelsGroup);
			} else if(defn is CoordinateAttributeDefinitionProxy) {
				//attribute label
				//l = getLabel(defnLabel, 100 + 70 + 70 + COMPOSITE_ATTRIBUTE_H_GAP * 2, HEADER_LABEL_STYLE, directionByColumns);
				l = getLabel(defnLabel, 100, HEADER_LABEL_STYLE, directionByColumns);
				result.addElement(l);
				//subheader
				addCoordinateAttributeLabels(compositeAttributeLabelsGroup, CoordinateAttributeDefinitionProxy(defn), directionByColumns);
				result.addElement(compositeAttributeLabelsGroup);
			} else if (defn is NumberAttributeDefinitionProxy && NumberAttributeDefinitionProxy(defn).defaultUnit != null || 
				defn is RangeAttributeDefinitionProxy && RangeAttributeDefinitionProxy(defn).defaultUnit != null ) {
				var defaultUnit:UnitProxy;
				if (defn is NumberAttributeDefinitionProxy) {
					defaultUnit = NumberAttributeDefinitionProxy(defn).defaultUnit;
				} else {
					defaultUnit = RangeAttributeDefinitionProxy(defn).defaultUnit;
				}
				var labStr:String = defnLabel + " (" + defaultUnit.getAbbreviation() + ")";
				l = getLabel(labStr, width, HEADER_LABEL_STYLE, directionByColumns);
				result.addElement(l);
			} else {
				l = getLabel(defnLabel, width, HEADER_LABEL_STYLE, directionByColumns);
				result.addElement(l);
			}
			return result;
		}
		
		private static function addCoordinateAttributeLabels(compositeAttributeLabelsGroup:Group, 
															 defn:CoordinateAttributeDefinitionProxy, directionByColumns:Boolean):void {
			var fieldsOrder:UIOptions$CoordinateAttributeFieldsOrder = CoordinateAttributeDefinitionProxy(defn).fieldsOrder; 
			var srsLabel:Label = getLabel(Message.get('edit.coordinate.srs'), 100, HEADER_LABEL_STYLE, directionByColumns);
			srsLabel.height = ATTRIBUTE_INPUT_FIELD_HEIGHT;
			var xLabel:Label = getLabel(Message.get('edit.coordinate.x'), 70, HEADER_LABEL_STYLE, directionByColumns);
			xLabel.height = ATTRIBUTE_INPUT_FIELD_HEIGHT;
			var yLabel:Label = getLabel(Message.get('edit.coordinate.y'), 70, HEADER_LABEL_STYLE, directionByColumns);
			yLabel.height = ATTRIBUTE_INPUT_FIELD_HEIGHT;
			switch(fieldsOrder) {
				case UIOptions$CoordinateAttributeFieldsOrder.SRS_X_Y:
					compositeAttributeLabelsGroup.addElement(srsLabel);
					compositeAttributeLabelsGroup.addElement(xLabel);
					compositeAttributeLabelsGroup.addElement(yLabel);
					break;
				case UIOptions$CoordinateAttributeFieldsOrder.SRS_Y_X:
					compositeAttributeLabelsGroup.addElement(srsLabel);
					compositeAttributeLabelsGroup.addElement(yLabel);
					compositeAttributeLabelsGroup.addElement(xLabel);
					break;
			}
		}
		
		public static function getGridColumn(headerText:String, dataField:String, width:Number, 
											 labelFunction:Function = null, headerTextWrap:Boolean = false,
											 itemRenderer:IFactory = null
											):GridColumn {
			var c:GridColumn = new GridColumn();
			c.headerText = headerText;
			c.dataField = dataField;
			c.labelFunction = labelFunction;
			c.width = width;
			if(headerTextWrap) {
				c.headerRenderer = new ClassFactory(DataGridHeaderRenderer);
			}
			if(itemRenderer != null) {
				c.itemRenderer = itemRenderer;
			}
			return c;
		}

		public static function getLabel(text:String, width:Number = NaN, styleName:String = null, displayOneRow:Boolean = false):Label {
			var l:Label = new Label();
			l.text = text;
			l.width = width;
			l.styleName = styleName;
			if ( displayOneRow ) {
				l.maxDisplayedLines = 1;
				l.showTruncationTip = true;
			}
			return l;
		}

	}
}