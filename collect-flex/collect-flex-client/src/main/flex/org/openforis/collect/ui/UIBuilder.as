package org.openforis.collect.ui {
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.core.Container;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeLabelProxy$Type;
	import org.openforis.collect.model.UIConfiguration;
	import org.openforis.collect.model.UITab;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.component.datagroup.DataGroupItemRenderer;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.input.BooleanInputField;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.DateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.NumericInputField;
	import org.openforis.collect.ui.component.input.RangeInputField;
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
				
				
				//Root entity definition				
				var rootEntityForm:EntityFormContainer = new EntityFormContainer();
				rootEntityForm.label = entity.getLabelText();
				formContainer.rootFormContainer =rootEntityForm;
				
				var uiConfig:UIConfiguration = Application.activeSurvey.uiConfiguration;
				var uiTab:UITab = null;
				if(uiConfig != null) {
						var tabs:ListCollectionView = uiConfig.tabs;
						if(tabs != null){
							for each (var tab:UITab in tabs) {
								if(tab.name == entity.name){
									uiTab = tab;
									break;
								}
							}
						} 
				}
				addFormItems(rootEntityForm, entity, version, uiTab);
				
				
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
			var keyAttributeDefs:IList = rootEntity.keyAttributeDefinitions();
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
						if(def is AttributeDefinitionProxy){
							addAttributeFormItem(form, def);
						} else if(def is EntityDefinitionProxy) {
							
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
		
		private static function addAttributeFormItem(form:EntityFormContainer, definition:AttributeDefinitionProxy):void {
			
			if(definition.multiple) {
				//TODO multiple attributes
			} else {
				var inputField:InputField = getInputField(definition);
				inputField.presenter.path = null; //TODO
				form.addFormItem(definition.getLabelText(), inputField);
			}
			
		}
		
		private static function getEntityItemRenderer(entityDescriptor:*):DataGroupItemRenderer {
			var itemRenderer:DataGroupItemRenderer = new DataGroupItemRenderer();
			for each(var modelObjectDefinition:* in entityDescriptor.childDefinitions) {
				//if model object is attribute
				var attributeDescriptor:* = modelObjectDefinition;
				var inputField:InputField = getInputField(attributeDescriptor);
				itemRenderer.addElement(inputField);
			}
			return itemRenderer;
		}
		
		//TODO
		private static function getInputField(attributeDescripor:*):InputField {
			var inputField:InputField = null;
			var type:String = 'string'; //attributeDescripor.type
			switch(type) {
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
			}
			return inputField;
		}
		
	}
}