package org.openforis.collect.ui.component.input {
	import flash.events.ContextMenuEvent;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.event.NodeEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.presenter.RemarksPopUpPresenter;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * @author M. Togna
	 * @author S. Ricci 
	 * */
	public class InputFieldContextMenu {
		
		private static const SET_STAR:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.blankOnForm"));
		private static const SET_DASH:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.dashOnForm"));
		private static const SET_ILLEGIBLE:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.illegible"));
		private static const EDIT_REMARKS_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.editRemarks"), true);
		private static const SET_STAR_IN_ROW:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.replaceBlanksWithStar"), true);
		private static const SET_DASH_IN_ROW:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.replaceBlanksWithDash"));
		private static const DELETE_ATTRIBUTE:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.deleteAttribute"), true);
		private static const DELETE_ENTITY:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.deleteEntity"), true);
		private static const CONFIRM_ERROR:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveError"), true);
		private static const APPROVE_MISSING:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveMissingValue"), true);
		private static const APPROVE_MISSING_IN_ROW:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveMissingValuesInRow"), true);
		private static const MENU_ITEMS:Array = [
			SET_STAR, 
			SET_DASH, 
			SET_ILLEGIBLE, 
			EDIT_REMARKS_MENU_ITEM, 
			SET_STAR_IN_ROW, 
			SET_DASH_IN_ROW,
			DELETE_ATTRIBUTE, 
			DELETE_ENTITY, 
			CONFIRM_ERROR, 
			APPROVE_MISSING,
			APPROVE_MISSING_IN_ROW
		];
		
		private static var remarksPopUpPresenter:RemarksPopUpPresenter;
		private var _contextMenu:ContextMenu;
		private var _inputField:InputField;

		{
			initEventListeners();
			remarksPopUpPresenter = new RemarksPopUpPresenter();
		}
		
		public function InputFieldContextMenu(inputField:InputField) {
			this._contextMenu = new ContextMenu();
			this._inputField = inputField;
			updateItems();
			_inputField.contextMenu = _contextMenu;
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
			
			if(_inputField.isEmpty()) {
				switch(step) {
					case CollectRecord$Step.ENTRY:
						items.push( SET_STAR, SET_DASH, SET_ILLEGIBLE );
						break;
					case CollectRecord$Step.CLEANSING:
						items.push(APPROVE_MISSING);
						break;
				}
			} else if(step == CollectRecord$Step.ENTRY) {
				var hasErrors:Boolean = _inputField.parentEntity.childContainsErrors(_inputField.attributeDefinition.name);
				if(hasErrors) {
					var hasConfirmedError:Boolean = _inputField.parentEntity.hasConfirmedError(_inputField.attributeDefinition.name);
					if(! hasConfirmedError) {
						items.push(CONFIRM_ERROR);
					}
				}
			}
			items.push(EDIT_REMARKS_MENU_ITEM);
			
			var def:AttributeDefinitionProxy = _inputField.attributeDefinition;
			if(def.multiple && ! (def is CodeAttributeDefinitionProxy)) {
				items.push(DELETE_ATTRIBUTE);
			} else if(def.parentLayout == UIUtil.LAYOUT_TABLE) {
				var entityDef:EntityDefinitionProxy = def.parent;
				if(entityDef != null && entityDef.multiple) {
					switch(step) {
						case CollectRecord$Step.ENTRY:
							items.push(SET_STAR_IN_ROW, SET_DASH_IN_ROW );
							break;
						case CollectRecord$Step.CLEANSING:
							items.push(APPROVE_MISSING_IN_ROW);
					}
					if( !entityDef.enumerable) {
						items.push(DELETE_ENTITY);
					}
				}
			}
			
			return items;
		}
		
		public static function menuItemSelectHandler(event:ContextMenuEvent):void {
			var inputField:InputField = event.contextMenuOwner as InputField;
			var attrDefn:AttributeDefinitionProxy = inputField.attributeDefinition;
			var attribute:AttributeProxy = inputField.attribute;
			var parentEntity:EntityProxy = inputField.parentEntity;
			var parentEntityDefn:EntityDefinitionProxy = attrDefn.parent;
			
			var nodeEvent:NodeEvent = null; 
			switch(event.target) {
				case SET_STAR:
					nodeEvent = createNodeEvent(NodeEvent.UPDATE_SYMBOL, inputField);
					nodeEvent.symbol = FieldSymbol.BLANK_ON_FORM;
					break;
				case SET_DASH:
					nodeEvent = createNodeEvent(NodeEvent.UPDATE_SYMBOL, inputField);
					nodeEvent.symbol = FieldSymbol.DASH_ON_FORM;
					break;
				case SET_ILLEGIBLE:
					nodeEvent = createNodeEvent(NodeEvent.UPDATE_SYMBOL, inputField);
					nodeEvent.symbol = FieldSymbol.ILLEGIBLE;
					break;
				case EDIT_REMARKS_MENU_ITEM:
					remarksPopUpPresenter.openPopUp(inputField, true);
					break;
				case SET_DASH_IN_ROW:
					nodeEvent = new NodeEvent(NodeEvent.UPDATE_SYMBOL);
					nodeEvent.symbol = FieldSymbol.DASH_ON_FORM;
					nodeEvent.nodeProxy = parentEntity;
					break;
				case SET_STAR_IN_ROW:
					nodeEvent = new NodeEvent(NodeEvent.UPDATE_SYMBOL);
					nodeEvent.symbol = FieldSymbol.BLANK_ON_FORM;
					nodeEvent.nodeProxy = parentEntity;
					break;
				case DELETE_ATTRIBUTE:
					var attrLabel:String = inputField.attributeDefinition.getLabelText();
					AlertUtil.showConfirm("global.confirmDelete", [attrLabel], "global.confirmAlertTitle", performDeleteNode, [attribute]);
					break;
				case DELETE_ENTITY:
					var entityLabel:String = inputField.attributeDefinition.parent.getLabelText();
					AlertUtil.showConfirm("global.confirmDelete", [entityLabel], "global.confirmAlertTitle", performDeleteNode, [parentEntity]);
					break;
				case CONFIRM_ERROR:
					nodeEvent = new NodeEvent(NodeEvent.CONFIRM_ERROR);
					nodeEvent.nodeProxy = attribute;
					break;
				case APPROVE_MISSING:
					nodeEvent = new NodeEvent(NodeEvent.APPROVE_MISSING);
					nodeEvent.parentEntity = parentEntity;
					nodeEvent.nodeName = attrDefn.name;
					break;
				case APPROVE_MISSING_IN_ROW:
					nodeEvent = new NodeEvent(NodeEvent.APPROVE_MISSING);
					nodeEvent.nodeProxy = parentEntity;
					break;
			}
			
			if(nodeEvent != null) {
				nodeEvent['inputField'] = inputField;
				EventDispatcherFactory.getEventDispatcher().dispatchEvent(nodeEvent);
			}
		}
		
		private static function createNodeEvent(type:String, inputField:InputField):NodeEvent {
			var event:NodeEvent = new NodeEvent(type);
			var attrDefn:AttributeDefinitionProxy = inputField.attributeDefinition;
			if(attrDefn.multiple) {
				event.nodes = inputField.parentEntity.getChildren(attrDefn.name);
			} else {
				event.nodeProxy = inputField.attribute;
				event.fieldIdx = inputField.fieldIndex;
			}
			return event;
		}
		
		private static function performDeleteNode(node:NodeProxy):void {
			var event:NodeEvent = new NodeEvent(NodeEvent.DELETE_NODE);
			event.nodeProxy = node;
			EventDispatcherFactory.getEventDispatcher().dispatchEvent(event);
		}
		
	}
	
}