package org.openforis.collect.ui {
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.containers.GridItem;
	import mx.containers.GridRow;
	import mx.core.IVisualElement;
	import mx.core.IVisualElementContainer;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefaultProxy;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.BooleanAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CoordinateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.DateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.FileAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NumericAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.RangeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.TaxonAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.TextAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.TextAttributeDefinitionProxy$Type;
	import org.openforis.collect.metamodel.proxy.TimeAttributeDefinitionProxy;
	import org.openforis.collect.model.UIConfiguration;
	import org.openforis.collect.model.UITab;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.component.datagroup.DataGroupItemRenderer;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;
	import org.openforis.collect.ui.component.detail.CodeAttributeFormItem;
	import org.openforis.collect.ui.component.detail.CollectFormItem;
	import org.openforis.collect.ui.component.detail.CoordinateAttributeFormItem;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.detail.FormsContainer;
	import org.openforis.collect.ui.component.detail.MultipleAttributeDataGroupFormItem;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.ui.component.detail.SingleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.SingleEntityFormItem;
	import org.openforis.collect.ui.component.detail.TaxonAttributeFormItem;
	import org.openforis.collect.ui.component.input.BooleanInputField;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CoordinateInputField;
	import org.openforis.collect.ui.component.input.DateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.MemoInputField;
	import org.openforis.collect.ui.component.input.NumericInputField;
	import org.openforis.collect.ui.component.input.RangeInputField;
	import org.openforis.collect.ui.component.input.StringInputField;
	import org.openforis.collect.ui.component.input.TaxonInputField;
	import org.openforis.collect.ui.component.input.TimeInputField;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.components.FormItem;
	import spark.components.HGroup;
	import spark.components.Label;
	import spark.components.VGroup;
	import spark.components.gridClasses.GridColumn;
	import spark.components.supportClasses.Range;
	
	/**
	 * @author Mino Togna
	 * */
	public class UIBuilder {
		
		public static function buildForm(entity:EntityDefinitionProxy, version:ModelVersionProxy):FormContainer {
			var formContainer:FormContainer = new FormContainer();
			formContainer.initialize();
			
			var form:EntityFormContainer = new EntityFormContainer();
			form.entityDefinition = entity;
			
			var uiConfig:UIConfiguration = Application.activeSurvey.uiConfiguration;
			var tabs:ListCollectionView = null;
			var uiTab:UITab = null;
			if(uiConfig != null) {
				tabs = uiConfig.tabs;
				if(tabs != null){
					for each (var tab:UITab in tabs) {
						if(tab.name == entity.name) {
							uiTab = tab;
							break;
						}
					}
				} 
			}
			//addFormItems(form, entity, version, uiTab);
			var mainDefinitions:ArrayCollection = new ArrayCollection();
			var defns:ListCollectionView = entity.childDefinitions;
			form.uiTabs = uiTab.tabs;
			if(CollectionUtil.isNotEmpty(defns)){
				for each (var defn:NodeDefinitionProxy in defns) {
					if(isInVersion(defn, version)) {
						if(defn is AttributeDefinitionProxy) {
							if(StringUtil.isBlank(defn.uiTabName)) {
								mainDefinitions.addItem(defn);
							}
						} else if(defn is EntityDefinitionProxy) {
							var proxy:EntityDefinitionProxy = EntityDefinitionProxy(defn);
							if(proxy.uiTabName == null || uiTab.hasChildTab(defn.uiTabName)) {
								mainDefinitions.addItem(defn);
							}
						}
					}
				}
			}
			form.mainDefinitions = mainDefinitions;
			
			formContainer.addEntityFormContainer(form);
			/*
			in this case the parentEntity of the formContainer will be null and 
			the "entity" will be record's "rootEntity"
			*/
			BindingUtils.bindProperty(form, "entity", formContainer, ["record", "rootEntity"]);
			
			if(tabs != null) {
				for each (tab in tabs) {
					var childForm:EntityFormContainer = new EntityFormContainer();
					var child:NodeDefinitionProxy = entity.getChildDefinition(tab.name);
					if(child is EntityDefinitionProxy) {
						var edp:EntityDefinitionProxy = child as EntityDefinitionProxy;
						childForm.entityDefinition = edp;														
						addFormItems(childForm, edp, version, tab);			
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
			for each(var keyAttributeDef:AttributeDefinitionProxy in keyAttributeDefs) {
				column = new GridColumn();
				column.headerText = keyAttributeDef.getLabelText();
				column.labelFunction = RecordSummaryDataGrid.recordSummariesKeyLabelFunction;
				column.dataField = "key" + position;
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
						column = new GridColumn();
						column.headerText = Message.get("list.headerCount", [entityDef.getLabelText()]);
						column.dataField = "count" + position;
						column.labelFunction = RecordSummaryDataGrid.recordSummariesCountEntityLabelFunction;
						column.width = 150;
						columns.addItem(column);
						position ++;
					}
				}
			}
			
			//skipped count column
			column = new GridColumn();
			column.headerText = Message.get("list.skipped");
			column.dataField = "skipped";
			column.labelFunction = RecordSummaryDataGrid.numberLabelFunction;
			column.width = 100;
			columns.addItem(column);
			//missing count column
			column = new GridColumn();
			column.headerText = Message.get("list.missing");
			column.dataField = "missing";
			column.labelFunction = RecordSummaryDataGrid.numberLabelFunction;
			column.width = 100;
			columns.addItem(column);
			//errors count column
			column = new GridColumn();
			column.headerText = Message.get("list.errors");
			column.dataField = "errors";
			column.labelFunction = RecordSummaryDataGrid.numberLabelFunction;
			column.width = 100;
			columns.addItem(column);
			//warnings count column
			column = new GridColumn();
			column.headerText = Message.get("list.warnings");
			column.dataField = "warnings";
			column.labelFunction = RecordSummaryDataGrid.numberLabelFunction;
			column.width = 100;
			columns.addItem(column);
			//creation date column
			column = new GridColumn();
			column.headerText = Message.get("list.creationDate");
			column.dataField = "creationDate";
			column.labelFunction = RecordSummaryDataGrid.dateLabelFunction;
			column.width = 150;
			columns.addItem(column);
			//date modified column
			column = new GridColumn();
			column.headerText = Message.get("list.modifiedDate");
			column.dataField = "modifiedDate";
			column.labelFunction = RecordSummaryDataGrid.dateLabelFunction;
			column.width = 150;
			columns.addItem(column);
			return columns;
		}
		
		private static function addFormItems(form:EntityFormContainer, entity:EntityDefinitionProxy, version:ModelVersionProxy, uiTab:UITab):void {
			var defns:ListCollectionView = entity.childDefinitions;
			form.uiTabs = uiTab.tabs;
			if(defns != null && defns.length >0){
				for each (var defn:NodeDefinitionProxy in defns) {
					if(isInVersion(defn, version)) {
						if(defn is AttributeDefinitionProxy) {
							var attrFormItem:AttributeFormItem = getAttributeFormItem(AttributeDefinitionProxy(defn));
							/*
							var formItem:FormItem = new FormItem();
							formItem.label = defn.getLabelText();
							*/
							/*
							var formItem:HGroup = new HGroup();
							formItem.addElement(label);
							*/
							var formItem:GridRow = new GridRow();
							var labelCol:GridItem = new GridItem();
							var label:Label = new Label();
							label.text = defn.getLabelText();
							labelCol.addChild(label);
							formItem.addChild(labelCol);
							
							if(defn is CoordinateAttributeDefinitionProxy || defn is TaxonAttributeDefinitionProxy){
								form.addFormItem(formItem, defn.uiTabName);
								form.addAttributeFormItem(attrFormItem, defn.uiTabName);
							} else {
								/*formItem.addElement(attrFormItem);
								form.addFormItem(formItem, defn.uiTabName);*/
								var attrCol:GridItem = new GridItem();
								attrCol.addChild(attrFormItem);
								formItem.addChild(attrCol);
								form.addFormItem(formItem);
							}
							//bind parentEntity of each attrFormItem to the "entity" of the form 
							BindingUtils.bindProperty(attrFormItem, "parentEntity", form, "entity");
						} else if(defn is EntityDefinitionProxy) {
							var proxy:EntityDefinitionProxy = EntityDefinitionProxy(defn);
							if(proxy.uiTabName == null || uiTab.hasChildTab(defn.uiTabName)) {
								var entityFormItem:EntityFormItem = getEntityFormItem(proxy);
								form.addEntityFormItem(entityFormItem, defn.uiTabName);
								//bind parentEntity of each attrFormItem to the "entity" of the form 
								BindingUtils.bindProperty(entityFormItem, "parentEntity", form, "entity");
							}
						}
					}
				}
			} 
		}
		
		public static function buildDataGroupItemRendererRow(entity:EntityDefinitionProxy, version:ModelVersionProxy, component:IVisualElementContainer):void {
			var children:ListCollectionView = entity.childDefinitions;
			for each (var defn:NodeDefinitionProxy in children) {
				if(isInVersion(defn, version)){
					if(defn is AttributeDefinitionProxy) {
						var formItem:AttributeFormItem = getAttributeFormItem(defn as AttributeDefinitionProxy, true);
						formItem.addTo(component);
						
						BindingUtils.bindProperty(formItem, "parentEntity", component, "entity");
					} else if(defn is EntityDefinitionProxy) {
						var edp:EntityDefinitionProxy = EntityDefinitionProxy(defn);
						if(edp.multiple) {
							//TODO
						} else {
							buildDataGroupItemRendererRow(edp, version ,component);
						}
					}
				}
			}
		}
		
		public static function getAttributeFormItem(definition:AttributeDefinitionProxy, isInDataGroup:Boolean = false):AttributeFormItem {
			var formItem:AttributeFormItem = null;
			
			if(definition is CodeAttributeDefinitionProxy) {
				formItem = new CodeAttributeFormItem();
			} else if(definition is CoordinateAttributeDefinitionProxy){
				//todo multiple
				formItem = new CoordinateAttributeFormItem();
			} else if(definition is TaxonAttributeDefinitionProxy){
				formItem = new TaxonAttributeFormItem();
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
		
		public static function getInputFieldWidth(def:AttributeDefinitionProxy, isInDataGroup:Boolean = false):int {
			if(def is BooleanAttributeDefinitionProxy) {
				return 20;
			} else if(def is CodeAttributeDefinitionProxy) {
				return 100;
			} else if(def is CoordinateAttributeDefinitionProxy) {
				return 100;
			} else if(def is DateAttributeDefinitionProxy) {
				return 150;
			} else if(def is FileAttributeDefinitionProxy) {
				return 300;
			} else if(def is NumericAttributeDefinitionProxy) {
				return 100;
			} else if(def is RangeAttributeDefinitionProxy) {
				return 120;
			} else if(def is TaxonAttributeDefinitionProxy) {
				return 400;
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
				return 80;
			} else {
				return 100;
			}
		}
		
		public static function getAttributeDataGroupHeaderWidth(def:AttributeDefinitionProxy):int {
			if(def is BooleanAttributeDefinitionProxy) {
				return 100;
			} else {
				return getInputFieldWidth(def, true);
			}
		}
			
		
		public static function getInputField(def:AttributeDefinitionProxy, isInDataGroup:Boolean = false):InputField {
			var inputField:InputField = null;
			if(def is TextAttributeDefinitionProxy) {
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
			} else if(def is DateAttributeDefinitionProxy) {
				inputField = new DateInputField();
			} else if(def is TimeAttributeDefinitionProxy) {
				inputField = new TimeInputField();
			} else if(def is CodeAttributeDefinitionProxy) {
				inputField = new CodeInputField();
			} else if(def is NumericAttributeDefinitionProxy) {
				inputField = new NumericInputField();
			} else if(def is RangeAttributeDefinitionProxy) {
				inputField = new RangeInputField();
			} else if(def is BooleanAttributeDefinitionProxy) {
				inputField = new BooleanInputField();
			} else if(def is CoordinateAttributeDefinitionProxy) {
				inputField = new CoordinateInputField();
			} else if(def is TaxonAttributeDefinitionProxy) {
				inputField = new TaxonInputField();
			} else if(def is FileAttributeDefinitionProxy) {
				//inputField = new FileInputField();
			} else {
				inputField = new StringInputField();
			}
			inputField.width = getInputFieldWidth(def, isInDataGroup);
			inputField.attributeDefinition = def;
			return inputField;
		}
	
		//TODO check ifisinversion
		public static function buildDataGroupHeaders(defn:EntityDefinitionProxy):HGroup {
			var h:HGroup = new HGroup();
			h.gap = 2;
			var childDefn:ListCollectionView = defn.childDefinitions;
			for each (var childDef:NodeDefinitionProxy in childDefn) {
				var elem:IVisualElement = getDataGroupHeader(childDef);
				h.addElement(elem);
			}
			
			return h;
		}
		
		private static function getDataGroupHeader(defn:NodeDefinitionProxy):IVisualElement {
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
				v.width = 406;
				v.percentHeight = 100;
				v.verticalAlign = "bottom";
				//attribute label
				l = new Label();
				l.width = 100;
				l.styleName = "bold";
				l.text = defn.getLabelText();
				v.addElement(l);
				//subheader
				h = new HGroup();
				l = new Label();
				l.width = 100;
				l.styleName = "bold";
				l.text = Message.get('edit.taxon.code');
				h.addElement(l);
				l = new Label();
				l.width = 100;
				l.styleName = "bold";
				l.text = Message.get('edit.taxon.scientificName');
				h.addElement(l);
				l = new Label();
				l.width = 100;
				l.styleName = "bold";
				l.text = Message.get('edit.taxon.vernacularName');
				h.addElement(l);
				l = new Label();
				l.width = 100;
				l.styleName = "bold";
				l.text = Message.get('edit.taxon.vernacularLang');
				h.addElement(l);
				v.addElement(h);
				return v;
			} else if(defn is CoordinateAttributeDefinitionProxy) {
				v = new VGroup();
				v.width = 302;
				v.percentHeight = 100;
				v.verticalAlign = "bottom";
				//attribute label
				l = new Label();
				l.width = 100;
				l.styleName = "bold";
				l.text = defn.getLabelText();
				v.addElement(l);
				//subheader
				h = new HGroup();
				l = new Label();
				l.width = 100;
				l.styleName = "bold";
				l.text = Message.get('edit.coordinate.srs');
				h.addElement(l);
				l = new Label();
				l.width = 100;
				l.styleName = "bold";
				l.text = Message.get('edit.coordinate.x');
				h.addElement(l);
				l = new Label();
				l.width = 100;
				l.styleName = "bold";
				l.text = Message.get('edit.coordinate.y');
				h.addElement(l);
				v.addElement(h);
				return v;
			}else {
				var width:int = getAttributeDataGroupHeaderWidth(defn);
				
				v = new VGroup();
				v.width = width;
				v.percentHeight = 100;
				v.verticalAlign = "bottom";
				
				l = new Label();
				l.width = width;
				l.styleName = "bold";
				l.text = defn.getLabelText();
				v.addElement(l);
				
				return v;
			}
		}
		
		public static function isInVersion(node:NodeDefinitionProxy, currentVersion:ModelVersionProxy):Boolean {
			var since:ModelVersionProxy = node.sinceVersion;
			var deprecated:ModelVersionProxy = node.deprecatedVersion;
			var result:Boolean;
			if(since == null && deprecated == null){
				result = true;
			} else if(since != null && deprecated != null){
				result = currentVersion.compare(since) >= 0 && currentVersion.compare(deprecated) < 0;
			} else if(since != null){
				result = currentVersion.compare(since) >= 0;
			} else if(deprecated != null){
				result = currentVersion.compare(deprecated) < 0;
			}
			return result;
		}
		
	}
}