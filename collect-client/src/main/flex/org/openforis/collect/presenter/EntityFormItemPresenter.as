package org.openforis.collect.presenter
{
	import mx.collections.IList;
	
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
		}
		
		override protected function onAfterCreation():void {
			initNodeDefinitions();
			super.onAfterCreation();
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