package org.openforis.collect.ui {
	import mx.core.Container;
	
	import org.openforis.collect.ui.component.datagroup.DataGroupItemRenderer;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.detail.RootEntityFormContainer;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.StringInputField;
	
	/**
	 * @author Mino Togna
	 * */
	public class UIBuilder {
		
		//TODO: use entityDescriptor
		public static function generateDetailPageForms(parentContainer:Container, entityDescriptor:*, version:*):FormContainer {
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
		
		//TODO
		private static function addFormItems(form:EntityFormContainer, entityDescriptor:*):void {
           	for(var childSchemaObjectDescriptor:Object in childrenSchemaObjectDescriptors) {
				if(childSchemaObjectDescriptor.type == 'attribute') {
					var attributeDescription:Object = childSchemaObjectDescriptor as Object;
					addAttributeFormItem(form, attributeDescripor);
				} else {
					
				}
			}
			//foreach childSchemaObjectDescription
     			//if attribute
      				//if single
      				//else if multiple
  			
      			//else if entity
      				//if single
      				//else if multiple
    				
		}
		
		private static function addAttributeFormItem(form:EntityFormContainer, attributeDescripor:*):void {
			if(attributeDescriptor.multiple) {
				
			} else {
				var inputField:InputField = getInputField(attributeDescripor);
				inputField.presenter.path = null; //TODO
				form.addFormItem(attributeDescripor.label, inputField);
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
			}
			return inputField;
		}
		
	}
}