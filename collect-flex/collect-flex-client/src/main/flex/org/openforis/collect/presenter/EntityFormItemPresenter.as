package org.openforis.collect.presenter
{
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class EntityFormItemPresenter extends FormItemPresenter
	{
		public function EntityFormItemPresenter(view:EntityFormItem) {
			super(view);
			initNodeDefinitions();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			ChangeWatcher.watch(view, "entityDefinition", initNodeDefinitions);
			ChangeWatcher.watch(view, "modelVersion", initNodeDefinitions);
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
		
		override protected function updateView():void {
			if(view.parentEntity != null && view.entityDefinition != null) {
				//assign entity
				var entity:EntityProxy = view.parentEntity.getChild(view.entityDefinition.name, 0) as EntityProxy;
				view.entity = entity;
			}
		}
	}
}