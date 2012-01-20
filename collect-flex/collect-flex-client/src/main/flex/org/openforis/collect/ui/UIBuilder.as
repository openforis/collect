package org.openforis.collect.ui {
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.model.UIConfiguration;
	import org.openforis.collect.model.UITab;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.component.datagroup.DataGroupItemRenderer;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.detail.FormsContainer;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.ui.component.detail.SingleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.SingleEntityFormItem;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.StringInputField;
	
	import spark.components.gridClasses.GridColumn;
	
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
				//form.initialize();
				
				formContainer.rootFormContainer =form;
				form.label = entity.getLabelText();
				
				var uiConfig:UIConfiguration = Application.activeSurvey.uiConfiguration;
				var uiTab:UITab = null;
				if(uiConfig != null) {
						var tabs:ListCollectionView = uiConfig.tabs;
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
				column.dataField = keyAttributeDef.name;
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
						column.dataField = entityDef.name;
						column.labelFunction = RecordSummaryDataGrid.recordSummariesCountEntityLabelFunction;
						column.width = 150;
						columns.addItem(column);
					}
				}
			}
			
			//errors count column
			column = new GridColumn();
			column.headerText = Message.get("list.errorCount");
			column.dataField = "errorCount";
			column.width = 150;
			columns.addItem(column);
			//warnings count column
			column = new GridColumn();
			column.headerText = Message.get("list.warningCount");
			column.dataField = "warningCount";
			column.width = 150;
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
				//TODO iterate over the tabs
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
		
		public static function getAttributeFormItem(definition:AttributeDefinitionProxy):AttributeFormItem {
				var formItem:AttributeFormItem = null;
				if(definition.multiple) {
					formItem = new MultipleAttributeFormItem();
				} else {
					formItem = new SingleAttributeFormItem();
				}
				formItem.attributeDefinition = definition;
				return formItem;
		}
		
		public static function getEntityFormItem(definition:EntityDefinitionProxy):EntityFormItem {
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
		
		//TODO
		public static function getInputField(def:AttributeDefinitionProxy):InputField {
			var inputField:InputField = null;
			inputField = new StringInputField();
			//var type:String = def.
			/*switch(type) {
				case 'string':
					inputField = new StringInputField();
					break;
				case 'date':
					inputField = new DateInputField();
					break;
				case 'time':
					//inputField = new TIF();
					break;
				case 'code':
					inputField = new CodeInputField();
					break;
				case 'number':
					inputField = new NumericInputField();
					break;
				case 'range':
					inputField = new RangeInputField();
					break;
				case 'boolean':
					inputField = new BooleanInputField();
					break;
				case 'file':
					//inputField = new FIS();
					break;
			}*/
			return inputField;
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