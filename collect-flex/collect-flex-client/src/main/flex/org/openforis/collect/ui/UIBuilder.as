package org.openforis.collect.ui {
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.core.ClassFactory;
	import mx.core.IFactory;
	import mx.core.IVisualElement;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.BooleanAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CoordinateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.DateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.FileAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.RangeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.TaxonAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.TextAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.TextAttributeDefinitionProxy$Type;
	import org.openforis.collect.metamodel.proxy.TimeAttributeDefinitionProxy;
	import org.openforis.collect.model.UIConfiguration;
	import org.openforis.collect.model.UITab;
	import org.openforis.collect.ui.component.datagrid.CompleteColumnItemRenderer;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.component.datagroup.DataGridHeaderRenderer;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;
	import org.openforis.collect.ui.component.detail.AttributeItemRenderer;
	import org.openforis.collect.ui.component.detail.CodeAttributeFormItem;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.detail.MultipleAttributeDataGroupFormItem;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.ui.component.detail.SingleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.SingleEntityFormItem;
	import org.openforis.collect.ui.component.input.BooleanInputField;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CoordinateAttributeRenderer;
	import org.openforis.collect.ui.component.input.DateAttributeRenderer;
	import org.openforis.collect.ui.component.input.FixedCodeInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.MemoInputField;
	import org.openforis.collect.ui.component.input.NumericInputField;
	import org.openforis.collect.ui.component.input.RangeInputField;
	import org.openforis.collect.ui.component.input.StringInputField;
	import org.openforis.collect.ui.component.input.TaxonAttributeRenderer;
	import org.openforis.collect.ui.component.input.TimeAttributeRenderer;
	
	import spark.components.HGroup;
	import spark.components.Label;
	import spark.components.VGroup;
	import spark.components.gridClasses.GridColumn;
	
	/**
	 * @author Mino Togna
	 * */
	public class UIBuilder {
		
		public static function buildForm(rootEntity:EntityDefinitionProxy, version:ModelVersionProxy):FormContainer {
			var formContainer:FormContainer = new FormContainer();
			formContainer.initialize();
			
			var form:EntityFormContainer = new EntityFormContainer();
			form.entityDefinition = rootEntity;
			form.modelVersion = version;
			
			var uiConfig:UIConfiguration = Application.activeSurvey.uiConfiguration;
			var tabs:ListCollectionView = null;
			var uiTab:UITab = null;
			if(uiConfig != null) {
				tabs = uiConfig.tabs;
				uiTab = uiConfig.getTab(rootEntity.name);
			}
			form.uiTabs = uiTab != null ? uiTab.tabs: null;
			form.build();
			
			formContainer.addEntityFormContainer(form);
			/*
			in this case the parentEntity of the formContainer will be null and 
			the "entity" will be record's "rootEntity"
			*/
			BindingUtils.bindProperty(form, "entity", formContainer, ["record", "rootEntity"]);
			
			if(tabs != null) {
				for each (var tab:UITab in tabs) {
					var childForm:EntityFormContainer = new EntityFormContainer();
					
					var child:NodeDefinitionProxy = rootEntity.getChildDefinitionByTabName(tab.name);
					if(child is EntityDefinitionProxy) {
						var edp:EntityDefinitionProxy = child as EntityDefinitionProxy;
						childForm.entityDefinition = edp;
						childForm.modelVersion = version;
						childForm.uiTabs = tab.tabs;
						childForm.build();
						formContainer.addEntityFormContainer(childForm);
						/*
						in this case the parentEntity will be the record's rootEntity
						*/
						BindingUtils.bindProperty(childForm, "parentEntity", formContainer, ["record", "rootEntity"]);
					}
				}
			}
			return formContainer;
		}
		
		public static function getRecordSummaryListColumns(rootEntity:EntityDefinitionProxy):IList {
			var columns:IList = new ArrayList();
			var column:GridColumn;
			//key attributes columns
			var position:int = 1;
			var keyAttributeDefs:IList = rootEntity.keyAttributeDefinitions;
			var headerText:String, dataField:String, width:Number, labelFunction:Function;
			for each(var keyAttributeDef:AttributeDefinitionProxy in keyAttributeDefs) {
				headerText = keyAttributeDef.getLabelText();
				dataField = "key" + position;
				width = NaN;
				labelFunction = RecordSummaryDataGrid.recordSummariesKeyLabelFunction;
				column = getGridColumn(headerText, dataField, width, labelFunction);
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
						headerText = Message.get("list.headerCount", [entityDef.getLabelText()]);
						dataField = "count" + position;
						width = 70;
						labelFunction = RecordSummaryDataGrid.recordSummariesCountEntityLabelFunction;
						column = getGridColumn(headerText, dataField, width, labelFunction);
						columns.addItem(column);
						position ++;
					}
				}
			}
			
			//skipped count column
			column = getGridColumn(Message.get("list.skipped"), "skipped", 80, RecordSummaryDataGrid.numberLabelFunction);
			columns.addItem(column);
			//missing count column
			column = getGridColumn(Message.get("list.missing"), "missing", 80, RecordSummaryDataGrid.numberLabelFunction);
			columns.addItem(column);
			//errors count column
			column = getGridColumn(Message.get("list.errors"), "errors", 80, RecordSummaryDataGrid.numberLabelFunction);
			columns.addItem(column);
			//warnings count column
			column = getGridColumn(Message.get("list.warnings"), "warnings", 80, RecordSummaryDataGrid.numberLabelFunction);
			columns.addItem(column);
			//creation date column
			column = getGridColumn(Message.get("list.creationDate"), "creationDate", 150, RecordSummaryDataGrid.dateTimeLabelFunction);
			columns.addItem(column);
			//date modified column
			column = getGridColumn(Message.get("list.modifiedDate"), "modifiedDate", 150, RecordSummaryDataGrid.dateTimeLabelFunction);
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
		
		public static function getAttributeFormItem(definition:AttributeDefinitionProxy, isInDataGroup:Boolean = false):AttributeFormItem {
			var formItem:AttributeFormItem = null;
			
			if(definition is CodeAttributeDefinitionProxy) {
				formItem = new CodeAttributeFormItem();
			} else if(definition.multiple) {
				if(isInDataGroup){
					formItem = new MultipleAttributeDataGroupFormItem();
				} else {
					formItem = new MultipleAttributeFormItem();
				}
			} else {
				formItem = new SingleAttributeFormItem();
			}
			
			formItem.attributeDefinition = definition;
			formItem.isInDataGroup = isInDataGroup;
			return formItem;
		}
		
		public static function getEntityFormItem(definition:EntityDefinitionProxy, isInDataGroup:Boolean = false):EntityFormItem {
			var entityFormItem:EntityFormItem = null;
			if(definition.multiple) {
				entityFormItem = new MultipleEntityFormItem();
			} else {
				entityFormItem = new SingleEntityFormItem();
			}
			entityFormItem.entityDefinition = definition;
			return entityFormItem;
		}
		
		public static function getInputFieldWidth(def:AttributeDefinitionProxy, isInDataGroup:Boolean = false):Number {
			if(def is BooleanAttributeDefinitionProxy) {
				return 100;
			} else if(def is CodeAttributeDefinitionProxy) {
				if(isInDataGroup) {
					if(def.key && def.parent.enumerated) {
						//return NaN;
						return 150;
					} else {
						return 100;
					}
				} else {
					return 200;
				}
			} else if(def is CoordinateAttributeDefinitionProxy) {
				if(isInDataGroup) {
					return 300;
				} else {
					return 100;
				}
			} else if(def is DateAttributeDefinitionProxy) {
				return 132;
			} else if(def is FileAttributeDefinitionProxy) {
				return 300;
			} else if(def is RangeAttributeDefinitionProxy) {
				return 120;
			} else if(def is TaxonAttributeDefinitionProxy) {
				if(isInDataGroup) {
					return 460;
				} else {
					return 100;
				}
			} else if(def is TextAttributeDefinitionProxy) {
				var textAttributeDef:TextAttributeDefinitionProxy = TextAttributeDefinitionProxy(def);
				var type:TextAttributeDefinitionProxy$Type = textAttributeDef.type;
				switch(type) {
					case TextAttributeDefinitionProxy$Type.MEMO:
						return 300;
					case TextAttributeDefinitionProxy$Type.SHORT:
					default:
						return 100;
				}
			} else if(def is TimeAttributeDefinitionProxy) {
				return 64;
			} else {
				return 100;
			}
		}
		
		public static function getAttributeDataGroupHeaderWidth(def:AttributeDefinitionProxy):Number {
			var inputFieldWidth:Number = getInputFieldWidth(def, true);
			if(!isNaN(inputFieldWidth)) {
				return inputFieldWidth + 2; //consider validation display border container
			} else {
				return NaN;
			}
		}
		
		public static function getInputField(def:AttributeDefinitionProxy, isInDataGroup:Boolean = false):InputField {
			var inputField:InputField = null;
			if(def is BooleanAttributeDefinitionProxy) {
				inputField = new BooleanInputField();
			} else if(def is CodeAttributeDefinitionProxy) {
				var codeDef:CodeAttributeDefinitionProxy = CodeAttributeDefinitionProxy(def);
				if(isInDataGroup && codeDef.parent.enumerated && codeDef.key) {
					inputField = new FixedCodeInputField();
				} else {
					inputField = new CodeInputField();
				}
			} else if(def is FileAttributeDefinitionProxy) {
				//inputField = new FileInputField();
			} else if(def is NumberAttributeDefinitionProxy) {
				inputField = new NumericInputField();
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
						inputField = new StringInputField();
						break;
				}
			} else {
				inputField = new StringInputField();
			}
			inputField.width = getInputFieldWidth(def, isInDataGroup);
			inputField.attributeDefinition = def;
			inputField.isInDataGroup = isInDataGroup;
			return inputField;
		}
		
		public static function getAttributeItemRenderer(def:AttributeDefinitionProxy, isInDataGroup:Boolean = false):AttributeItemRenderer {
			var renderer:AttributeItemRenderer;
			if(def is CoordinateAttributeDefinitionProxy) {
				renderer = new CoordinateAttributeRenderer();
			} else if(def is DateAttributeDefinitionProxy) {
				renderer = new DateAttributeRenderer();
			} else if(def is TaxonAttributeDefinitionProxy) {
				renderer = new TaxonAttributeRenderer();
			} else if(def is TimeAttributeDefinitionProxy) {
				renderer = new TimeAttributeRenderer();
			} else {
				renderer = new AttributeItemRenderer();
				var inputField:InputField = getInputField(def, isInDataGroup);
				inputField.fieldIndex = 0;
				renderer.addElement(inputField);
				BindingUtils.bindProperty(inputField, "parentEntity", renderer, "parentEntity");
				BindingUtils.bindProperty(inputField, "attribute", renderer, "attribute");
			}
			renderer.isInDataGroup = isInDataGroup;
			renderer.attributeDefinition = def;
			return renderer;
		}
		
		public static function getDataGroupHeader(defn:NodeDefinitionProxy):IVisualElement {
			var elem:IVisualElement = null;
			if(defn is AttributeDefinitionProxy){
				elem = getAttributeDataGroupHeader(defn as AttributeDefinitionProxy);							
			} else if(defn is EntityDefinitionProxy) {
				elem = getEntityDataGroupHeader(defn as EntityDefinitionProxy);
			}
			return elem;
		}
		
		private static function getEntityDataGroupHeader(defn:EntityDefinitionProxy):IVisualElement {
			var v:VGroup = new VGroup();
			v.percentHeight = 100;
			v.verticalAlign = "bottom";
			var l:Label = new Label();
			l.styleName = "bold";
			l.text = defn.getLabelText();
			v.addElement(l);
			
			var hGroup:HGroup = new HGroup();
			hGroup.percentHeight = 100;
			hGroup.verticalAlign = "bottom";
			var childDefn:ListCollectionView = defn.childDefinitions;
			var width:int = 0;
			for each (var childDef:NodeDefinitionProxy in childDefn) {
				var elem:IVisualElement = getDataGroupHeader(childDef);
				width += elem.width;
				hGroup.addElement(elem);
			}
			v.width = width;
			
			return v;
		}
		
		private static function getAttributeDataGroupHeader(defn:AttributeDefinitionProxy):IVisualElement {
			var v:VGroup;
			var h:HGroup;
			var l:Label;
			if(defn is TaxonAttributeDefinitionProxy) {
				v = new VGroup();
				v.width = 466;
				v.percentHeight = 100;
				v.verticalAlign = "bottom";
				//attribute label
				l = getLabel(defn.getLabelText(), 100, "bold");
				v.addElement(l);
				//subheader
				h = new HGroup();
				h.gap = 4;
				l = getLabel(Message.get('edit.taxon.code'), 50, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.taxon.scientificName'), 100, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.taxon.vernacularName'), 100, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.taxon.languageCode'), 100, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.taxon.languageVariety'), 100, "bold");
				h.addElement(l);
				v.addElement(h);
				return v;
			} else if(defn is CoordinateAttributeDefinitionProxy) {
				v = new VGroup();
				v.width = 308;
				v.percentHeight = 100;
				v.verticalAlign = "bottom";
				//attribute label
				l = getLabel(defn.getLabelText(), 100, "bold");
				v.addElement(l);
				//subheader
				h = new HGroup();
				h.gap = 4;
				l = getLabel(Message.get('edit.coordinate.srs'), 100, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.coordinate.x'), 100, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.coordinate.y'), 100, "bold");
				h.addElement(l);
				v.addElement(h);
				return v;
			}else {
				var width:Number = getAttributeDataGroupHeaderWidth(defn);
				
				v = new VGroup();
				v.width = width;
				v.percentHeight = 100;
				v.verticalAlign = "bottom";
				
				l = getLabel(defn.getLabelText(), width, "bold");
				v.addElement(l);
				
				return v;
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

		public static function getLabel(text:String, width:Number = NaN, styleName:String = null):Label {
			var l:Label = new Label();
			l.text = text;
			l.width = width;
			l.styleName = styleName;
			return l;
		}

		public static function getDefinitionsInVersion(defs:IList, currentVersion:ModelVersionProxy):IList {
			var result:IList = new ArrayCollection();
			for each (var defn:NodeDefinitionProxy in defs) {
				if(currentVersion.isApplicable(defn)){
					result.addItem(defn);
				}
			}
			return result;
		}
	}
}