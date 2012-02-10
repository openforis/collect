package org.openforis.collect.presenter
{
	import flash.events.Event;
	
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	import org.openforis.collect.ui.component.detail.FormItem;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class EntityFormItemPresenter extends FormItemPresenter
	{
		public function EntityFormItemPresenter(view:EntityFormItem) {
			super(view);
		}
		
		private function get view():EntityFormItem {
			return EntityFormItem(_view);
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