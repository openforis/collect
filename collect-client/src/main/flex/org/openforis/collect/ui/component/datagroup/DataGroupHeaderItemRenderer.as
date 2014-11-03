package org.openforis.collect.ui.component.datagroup
{
	import mx.core.IVisualElement;
	import mx.events.FlexEvent;
	
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.ui.UIBuilder;
	
	import spark.components.supportClasses.ItemRenderer;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 * 
	 **/
	public class DataGroupHeaderItemRenderer extends ItemRenderer {
		
		private var _parentEntity:EntityProxy;
		
		public function DataGroupHeaderItemRenderer() {
			super();
			
			autoDrawBackground = false;
			addEventListener(FlexEvent.DATA_CHANGE, dataChangeHandler);
		}
		
		override protected function createChildren():void {
			if(data != null && parentEntity != null) {
				super.createChildren();
				var elem:IVisualElement = UIBuilder.getDataGroupHeader(data as NodeDefinitionProxy, parentEntity);
				addElement(elem);
			}
		}
		
		protected function dataChangeHandler(event:FlexEvent):void {
			removeAllElements();
			initialized = false;
			initialize();
		}
		
		[Bindable]
		public function get parentEntity():EntityProxy {
			return _parentEntity;
		}

		public function set parentEntity(value:EntityProxy):void {
			_parentEntity = value;
			removeAllElements();
			initialized = false;
			initialize();
		}
		
	}
}