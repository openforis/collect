package org.openforis.collect.ui {
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.core.IVisualElement;
	
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
	import org.openforis.collect.metamodel.proxy.TextAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.TimeAttributeDefinitionProxy;
	import org.openforis.collect.model.UIConfiguration;
	import org.openforis.collect.model.UITab;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.component.datagroup.DataGroupItemRenderer;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;
	import org.openforis.collect.ui.component.detail.CoordinateAttributeFormItem;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.detail.FormsContainer;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.ui.component.detail.SingleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.SingleEntityFormItem;
	import org.openforis.collect.ui.component.input.BooleanInputField;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CoordinateInputField;
	import org.openforis.collect.ui.component.input.DateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.NumericInputField;
	import org.openforis.collect.ui.component.input.RangeInputField;
	import org.openforis.collect.ui.component.input.StringInputField;
	import org.openforis.collect.ui.component.input.TimeInputField;
	
	import spark.components.HGroup;
	import spark.components.Label;
	import spark.components.VGroup;
	import spark.components.gridClasses.GridColumn;
	import spark.components.supportClasses.Range;
	
	/**
	 * @author Mino Togna
	 * */
	public class UIBuilder {
		
		//TODO: use entityDescriptor
		public static function buildForm(entity:EntityDefinitionProxy, version:ModelVersionProxy):FormContainer {
			//foreach version
				var formContainer:FormContainer = new FormContainer();
				formContainer.initialize();
				//container.addForm(formContainer, version, entity);
				
				//Root entity definition				
				var form:EntityFormContainer = new EntityFormContainer();
				form.entityDefinitionProxy = entity;
				//form.initialize();
				
				formContainer.rootFormContainer =form;
				//form.label = entity.getLabelText();
				
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
				addFormItems(form, entity, version, uiTab);
				
				if(tabs != null) {
					for each (tab in tabs) {
						var childForm:EntityFormContainer = new EntityFormContainer();
						var child:NodeDefinitionProxy = entity.getChildDefinition(tab.name);
						if(child is EntityDefinitionProxy) {
							var edp:EntityDefinitionProxy = child as EntityDefinitionProxy;
							childForm.entityDefinitionProxy = edp;
							
							formContainer.addEntityFormContainer(childForm);
							
							addFormItems(childForm, edp, version, tab);			
						}
					}
				}
				
				/*
				//foreach main entities
					var entityFormContainer:EntityFormContainer = new EntityFormContainer();
					entityFormContainer.label = "";
					formContainer.addEntityFormContainer(entityFormContainer);
					//BindingUtils.
					entityFormContainer.parentEntity = form.entity;
					//if multiple
						entityFormContainer.insertAddSection();
					addFormItems(entityFormContainer, null);
			
			parentContainer.addElement(formContainer);
				*/
			return formContainer;
		}
		
		public static function getRecordSummaryListColumns(rootEntity:EntityDefinitionProxy):IList {
			var columns:IList = new ArrayList();
			var column:GridColumn;
			//key attributes columns
			var keyAttributeDefs:IList = rootEntity.keyAttributeDefinitions;
			for each(var keyAttributeDef:AttributeDefinitionProxy in keyAttributeDefs) {
				column = new GridColumn();
				column.headerText = keyAttributeDef.getLabelText();
				column.labelFunction = RecordSummaryDataGrid.recordSummariesKeyLabelFunction;
				column.dataField = "key_" + keyAttributeDef.name;
				columns.addItem(column);
			}
			//count entity columns
			var firstLevelDefs:IList = rootEntity.childDefinitions;
			for each(var nodeDef:NodeDefinitionProxy in firstLevelDefs) {
				if(nodeDef is EntityDefinitionProxy) {
					var entityDef:EntityDefinitionProxy = EntityDefinitionProxy(nodeDef);
					if(entityDef.countInSummaryList) {
						column = new GridColumn();
						column.headerText = Message.get("list.headerCount", [entityDef.getLabelText()]);
						column.dataField = "count_" +entityDef.name;
						column.labelFunction = RecordSummaryDataGrid.recordSummariesCountEntityLabelFunction;
						column.width = 150;
						columns.addItem(column);
					}
				}
			}
			
			//skipped count column
			column = new GridColumn();
			column.headerText = Message.get("list.skipped");
			column.dataField = "skipped";
			column.width = 100;
			columns.addItem(column);
			//missing count column
			column = new GridColumn();
			column.headerText = Message.get("list.missing");
			column.dataField = "missing";
			column.width = 100;
			columns.addItem(column);
			//errors count column
			column = new GridColumn();
			column.headerText = Message.get("list.errors");
			column.dataField = "errors";
			column.width = 100;
			columns.addItem(column);
			//warnings count column
			column = new GridColumn();
			column.headerText = Message.get("list.warnings");
			column.dataField = "warnings";
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
			column.headerText = Message.get("list.dateModified");
			column.dataField = "dateModified";
			column.labelFunction = RecordSummaryDataGrid.dateLabelFunction;
			column.width = 150;
			columns.addItem(column);
			return columns;
		}
		
		//TODO
		private static function addFormItems(form:EntityFormContainer, entity:EntityDefinitionProxy, version:ModelVersionProxy, uiTab:UITab):void {
			if(uiTab == null || uiTab.tabs == null || uiTab.tabs.length == 0) {
				var defns:ListCollectionView = entity.childDefinitions;
				if(defns != null && defns.length >0){
					for each (var def:NodeDefinitionProxy in defns) {
						if(isInVersion(def, version)) {
							if(def is AttributeDefinitionProxy){
								var attrFormItem:AttributeFormItem = getAttributeFormItem(AttributeDefinitionProxy(def) );
								//attrFormItem.add(form);
								form.addFormItem(attrFormItem);
							} else if(def is EntityDefinitionProxy) {
								var proxy:EntityDefinitionProxy = EntityDefinitionProxy(def);
								if(proxy.uiTabName==null){
									var entityFormItem:EntityFormItem = getEntityFormItem(proxy);
									form.addEntityFormItem(entityFormItem);
								}
							}
						}
					}
				}
			} else {
				for each (var tab:UITab in uiTab.tabs) {
					
				}
				
			}
			/*
           	for(var childSchemaObjectDescriptor:Object in childrenSchemaObjectDescriptors) {
				if(childSchemaObjectDescriptor.type == 'attribute') {
					var attributeDescription:Object = childSchemaObjectDescriptor as Object;
					addAttributeFormItem(form, attributeDescripor);
				} else {
					
				}
			}
			*/
			//foreach childSchemaObjectDescription
     			//if attribute
      				//if single
      				//else if multiple
  			
      			//else if entity
      				//if single
      				//else if multiple
    				
		}
		
		public static function getAttributeFormItem(definition:AttributeDefinitionProxy, isInDataGroup:Boolean = false):AttributeFormItem {
				var formItem:AttributeFormItem = null;
				if(definition is CoordinateAttributeDefinitionProxy){
					//todo multiple
					formItem = new CoordinateAttributeFormItem();
				} else if(definition.multiple) {
					formItem = new MultipleAttributeFormItem();
				} else {
					formItem = new SingleAttributeFormItem();
				}
				formItem.attributeDefinition = definition;
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
		
/*		private static function getEntityItemRenderer(entityDescriptor:*):DataGroupItemRenderer {
			var itemRenderer:DataGroupItemRenderer = new DataGroupItemRenderer();
			for each(var modelObjectDefinition:* in entityDescriptor.childDefinitions) {
				//if model object is attribute
				var attributeDescriptor:* = modelObjectDefinition;
				var inputField:InputField = getInputField(attributeDescriptor);
				itemRenderer.addElement(inputField);
			}
			return itemRenderer;
		}*/
		
		public static function getInputFieldWidth(def:AttributeDefinitionProxy, isInDataGroup:Boolean = false):int {
			//TODO
			return 100;
		}
		
		public static function getInputField(def:AttributeDefinitionProxy, isInDataGroup:Boolean = false):InputField {
			//TODO
			var inputField:InputField = null;
			if(isInDataGroup) {
				inputField = new StringInputField();
			} else {
				if(def is TextAttributeDefinitionProxy) {
					inputField = new StringInputField();
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
				} else if(def is FileAttributeDefinitionProxy) {
					//inputField = new FileInputField();
				} 
			}
			inputField.width = getInputFieldWidth(def, isInDataGroup);
			
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
			//v.width = getInputFieldWidth(defn, true);
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
			var width:int = getInputFieldWidth(defn, true);
			
			var v:VGroup = new VGroup();
			v.width = width;
			v.percentHeight = 100;
			v.verticalAlign = "bottom";
			
			var l:Label = new Label();
			l.width = width;
			l.styleName = "bold";
			l.text = defn.getLabelText();
			v.addElement(l);
			
			return v;
		}
		
		/*private static function getEntityDataGroupHeaderWidth():int {
			
		}*/
		
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