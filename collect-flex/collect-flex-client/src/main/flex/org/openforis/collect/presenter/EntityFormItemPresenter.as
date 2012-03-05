package org.openforis.collect.presenter
{
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.remoting.service.UpdateResponse;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	import org.openforis.collect.ui.component.detail.ValidationDisplayManager;
	import org.openforis.collect.util.UIUtil;
	
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
			
			ChangeWatcher.watch(view, "entityDefinition", entityDefinitionChangeHandler);
		}
		
		protected function entityDefinitionChangeHandler(event:Event):void {
			initNodeDefinitions();
			updateView();
		}
		
		override protected function modelVersionChangeHandler(event:Event):void {
			initNodeDefinitions();
			updateView();
			super.modelVersionChangeHandler(event);
		}
		
		private function get view():EntityFormItem {
			return EntityFormItem(_view);
		}
		
		protected function initNodeDefinitions(event:Event = null):void {
			var temp:IList = null;
			if(view.entityDefinition != null && view.modelVersion != null) {
				temp = UIBuilder.getDefinitionsInVersion(view.entityDefinition.childDefinitions, view.modelVersion);
			}
			view.nodeDefinitions = temp;
		}
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			super.updateResponseReceivedHandler(event);
			if(_view.parentEntity != null) {
				var responses:IList = IList(event.result);
				for each (var response:UpdateResponse in responses) {
					if(response.nodeId == _view.parentEntity.id) {
						updateValidationDisplayManager();
						break;
					}
				}
			}
		}
		
		override protected function updateView():void {
			var entity:EntityProxy = null;
			if(view.parentEntity != null && view.entityDefinition != null && ! view.entityDefinition.multiple) {
				//assign entity
				entity = view.parentEntity.getChild(view.entityDefinition.name, 0) as EntityProxy;
			}
			view.entity = entity;
			updateValidationDisplayManager();
		}
		
		override protected function updateValidationDisplayManager():void {
			if(view.parentEntity != null) {
				validationDisplayManager.initByNode(view.parentEntity, view.entityDefinition);
			}
		}
	}
}