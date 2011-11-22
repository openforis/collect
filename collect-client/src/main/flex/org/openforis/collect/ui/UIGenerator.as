package org.openforis.collect.ui {
	import mx.binding.utils.BindingUtils;
	
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.detail.RecordDetail;
	import org.openforis.collect.ui.component.detail.RootEntityFormContainer;
	
	/**
	 * @author Mino Togna
	 * */
	public class UIGenerator {
		
		public function UIGenerator() {
		}
		
		//TODO: use entityDescriptor
		public static function generateDetailPageForms(entityDescriptor:*):void {
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
		}
		
		//TODO
		private static function addFormItems(form:EntityFormContainer, entityDescription:*):void {
           			
			//foreach childSchemaObjectDescription
     			//if attribute
      				//if single
      				//else if multiple
  			
      			//else if entity
      				//if single
      				//else if multiple
    				
		}
		
		//TODO
		private static function getInputField(attributeDefinition:*):void {
			
		}
		
	}
}