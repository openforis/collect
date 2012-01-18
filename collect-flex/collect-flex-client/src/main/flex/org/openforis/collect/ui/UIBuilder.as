package org.openforis.collect.ui {
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.core.Container;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeLabelProxy$Type;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.component.datagroup.DataGroupItemRenderer;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.detail.RootEntityFormContainer;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.StringInputField;
	
	import spark.components.gridClasses.GridColumn;
	
	/**
	 * @author Mino Togna
	 * */
	public class UIBuilder {
		
		//TODO: use entityDescriptor
		public static function generateEditForms(parentContainer:Container, entityDescriptor:*, version:*):FormContainer {
			//foreach version
				var formContainer:FormContainer = new FormContainer();
				formContainer.version = null;
				//Root entity description
				var form:RootEntityFormContainer = new RootEntityFormContainer();
				form.label = "";
				formContainer.addEntityFormContainer(form);
				//set the version
				addFormItems(form, null);
					
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
		private static function addFormItems(form:EntityFormContainer, entityDescriptor:*):void {
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
		
		private static function addAttributeFormItem(form:EntityFormContainer, attributeDescriptor:*):void {
			/*
			if(attributeDescriptor.multiple) {
				
			} else {
				var inputField:InputField = getInputField(attributeDescripor);
				inputField.presenter.path = null; //TODO
				form.addFormItem(attributeDescripor.label, inputField);
			}
			*/
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
			}
			return inputField;
		}
		
	}
}