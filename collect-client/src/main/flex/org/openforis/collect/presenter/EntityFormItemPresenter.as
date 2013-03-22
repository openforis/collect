package org.openforis.collect.presenter
{
	import mx.binding.utils.BindingUtils;
	import mx.collections.IList;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class EntityFormItemPresenter extends FormItemPresenter {
		
		public function EntityFormItemPresenter(view:EntityFormItem) {
			super(view);
			initNodeDefinitions();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			BindingUtils.bindSetter(setModelVersion, _view, "modelVersion");
			BindingUtils.bindSetter(setEntityDefinition, view, "entityDefinition");
		}
		
		protected function setEntityDefinition(entityDefinition:EntityDefinitionProxy):void {
			initNodeDefinitions();
			updateView();
		}
		
		protected function setModelVersion(version:ModelVersionProxy):void {
			initNodeDefinitions();
			updateView();
		}
		
		private function get view():EntityFormItem {
			return EntityFormItem(_view);
		}
		
		protected function initNodeDefinitions():void {
			var temp:IList = null;
			if(view.entityDefinition != null) {
				temp = view.entityDefinition.getDefinitionsInVersion(view.modelVersion);
			}
			view.nodeDefinitions = temp;
		}
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			super.updateResponseReceivedHandler(event);
		}
		
		override protected function updateView():void {
			var entity:EntityProxy = null;
			if(view.parentEntity != null && view.entityDefinition != null && ! view.entityDefinition.multiple) {
				//assign entity
				entity = view.parentEntity.getChild(view.entityDefinition.name, 0) as EntityProxy;
			}
			view.entity = entity;
			super.updateView();
		}
		
		override protected function updateValidationDisplayManager():void {
			super.updateValidationDisplayManager();
			if(view.parentEntity != null && view.entityDefinition != null) {
				validationDisplayManager.displayMinMaxCountValidationErrors(view.parentEntity, view.entityDefinition);
			}
		}
		
		override protected function updateRelevanceDisplayManager():void {
			super.updateRelevanceDisplayManager();
			relevanceDisplayManager.displayNodeRelevance(view.parentEntity, view.entityDefinition);
		}
	}
}