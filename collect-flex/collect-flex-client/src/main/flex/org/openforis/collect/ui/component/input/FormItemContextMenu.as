package org.openforis.collect.ui.component.input {
	import flash.events.ContextMenuEvent;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.event.NodeEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.ui.component.detail.CollectFormItem;

	/**
	 * @author M. Togna
	 * @author S. Ricci 
	 * */
	public class FormItemContextMenu {
		
		private static const APPROVE_MISSING:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveMissingValue"), true);
		private static const MENU_ITEMS:Array = [
			APPROVE_MISSING
		];
		
		private var _contextMenu:ContextMenu;
		private var _formItem:CollectFormItem;

		{
			initEventListeners();
		}
		
		public function FormItemContextMenu(formItem:CollectFormItem) {
			this._contextMenu = new ContextMenu();
			this._formItem = formItem;
			updateItems();
			this._formItem.contextMenu = this._contextMenu;
		}
		
		private static function initEventListeners():void {
			//init context menu items' event listener
			var item:ContextMenuItem;
			for each (item in MENU_ITEMS)  {
				item.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, menuItemSelectHandler);
			}
		}
		
		public function updateItems():void {
			var items:Array = null;
			if(Application.activeRecord != null) {
				var step:CollectRecord$Step = Application.activeRecord.step;
				items = createMenuItems(step);
				_contextMenu.hideBuiltInItems();
			}
			_contextMenu.customItems = items;
		}
		
		private function createMenuItems(step:CollectRecord$Step):Array {
			var items:Array = new Array();
			if(_formItem.parentEntity != null) {
				if(step == CollectRecord$Step.CLEANSING) {
					var nodeName:String = _formItem.nodeDefinition.name;
					var count:int = _formItem.parentEntity.getCount(nodeName);
					if(count == 0) {
						items.push(APPROVE_MISSING);
					}
				}
			}
			return items;
		}
		
		public static function menuItemSelectHandler(event:ContextMenuEvent):void {
			var formItem:CollectFormItem = event.contextMenuOwner as CollectFormItem;
			var parentEntity:EntityProxy = formItem.parentEntity;
			var nodeDefinition:NodeDefinitionProxy = formItem.nodeDefinition;
			var nodeEvent:NodeEvent = null; 
			switch(event.target) {
				case APPROVE_MISSING:
					nodeEvent = new NodeEvent(NodeEvent.APPROVE_MISSING);
					nodeEvent.parentEntity = parentEntity;
					nodeEvent.nodeName = nodeDefinition.name;
					break;
			}
			if(nodeEvent != null) {
				EventDispatcherFactory.getEventDispatcher().dispatchEvent(nodeEvent);
			}
		}
		
	}
	
}