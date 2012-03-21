package org.openforis.collect.ui.component.input {
	import flash.events.ContextMenuEvent;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.NodeEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.presenter.InputFieldPresenter;
	import org.openforis.collect.presenter.RemarksPopUpPresenter;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * @author M. Togna
	 * @author S. Ricci 
	 * */
	public class InputFieldContextMenu {
		
		private static const BLANK_ON_FORM_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.blankOnForm"));
		private static const DASH_ON_FORM_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.dashOnForm"));
		private static const ILLEGIBLE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.illegible"));
		private static const EDIT_REMARKS_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.editRemarks"), true);
		private static const REPLACE_BLANKS_WITH_STAR_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.replaceBlanksWithStar"), true);
		private static const REPLACE_BLANKS_WITH_DASH_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.replaceBlanksWithDash"));
		private static const DELETE_ATTRIBUTE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.deleteAttribute"), true);
		private static const DELETE_ENTITY_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.deleteEntity"), true);
		private static const CONFIRM_ERROR_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveError"), true);
		private static const APPROVE_MISSING_VALUE_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveMissingValue"), true);
		private static const APPROVE_MISSING_VALUES_IN_ROW_MENU_ITEM:ContextMenuItem = new ContextMenuItem(Message.get("edit.contextMenu.approveMissingValuesInRow"), true);
		private static const MENU_ITEMS:Array = [
			BLANK_ON_FORM_MENU_ITEM, 
			DASH_ON_FORM_MENU_ITEM, 
			ILLEGIBLE_MENU_ITEM, 
			EDIT_REMARKS_MENU_ITEM, 
			REPLACE_BLANKS_WITH_DASH_MENU_ITEM,
			REPLACE_BLANKS_WITH_STAR_MENU_ITEM, 
			DELETE_ATTRIBUTE_MENU_ITEM, 
			DELETE_ENTITY_MENU_ITEM, 
			CONFIRM_ERROR_MENU_ITEM, 
			APPROVE_MISSING_VALUE_MENU_ITEM
		];
		
		private static var remarksPopUpPresenter:RemarksPopUpPresenter;
		private var _contextMenu:ContextMenu;
		private var _inputField:InputField;

		{
			initEventListeners();
			remarksPopUpPresenter = new RemarksPopUpPresenter();
		}
		
		public function InputFieldContextMenu( inputField:InputField)	{
			this._contextMenu = new ContextMenu();
			this._inputField = inputField;
			updateContextMenuItems();
			_inputField.contextMenu = _contextMenu;
		}
		
		private static function initEventListeners():void {
			//init context menu items' event listener
			var item:ContextMenuItem;
			for each (item in MENU_ITEMS)  {
				item.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, menuItemSelectHandler);
			}
		}
		
		public function updateContextMenuItems():void {
			if(Application.activeRecord != null) {
				var step:CollectRecord$Step = Application.activeRecord.step;
				var items:Array = createMenuItems(step);
				
				_contextMenu.customItems = items;
				_contextMenu.hideBuiltInItems();
			} else {
				_inputField.contextMenu = null;
			}
		}
		
		private function createMenuItems(step:CollectRecord$Step):Array {
			var items:Array = new Array();
			
			if(_inputField.isEmpty()) {
				switch(step) {
					case CollectRecord$Step.ENTRY:
						items.push( BLANK_ON_FORM_MENU_ITEM, DASH_ON_FORM_MENU_ITEM, ILLEGIBLE_MENU_ITEM );
						break;
					case CollectRecord$Step.CLEANSING:
						items.push(APPROVE_MISSING_VALUE_MENU_ITEM);
						break;
				}
			} else if(step == CollectRecord$Step.ENTRY) {
				var hasErrors:Boolean = _inputField.parentEntity.childContainsErrors(_inputField.attributeDefinition.name);
				if(hasErrors) {
					var hasConfirmedError:Boolean = _inputField.parentEntity.hasConfirmedError(_inputField.attributeDefinition.name);
					if(! hasConfirmedError) {
						items.push(CONFIRM_ERROR_MENU_ITEM);
					}
				}
			}
			items.push(EDIT_REMARKS_MENU_ITEM);
			
			var def:AttributeDefinitionProxy = _inputField.attributeDefinition;
			if(def.multiple && ! (def is CodeAttributeDefinitionProxy)) {
				items.push(DELETE_ATTRIBUTE_MENU_ITEM);
			} else if(def.parentLayout == UIUtil.LAYOUT_TABLE) {
				var entityDef:EntityDefinitionProxy = def.parent;
				if(entityDef != null && entityDef.multiple) {
					switch(step) {
						case CollectRecord$Step.ENTRY:
							items.push(REPLACE_BLANKS_WITH_STAR_MENU_ITEM, REPLACE_BLANKS_WITH_DASH_MENU_ITEM );
							break;
						case CollectRecord$Step.CLEANSING:
							items.push(APPROVE_MISSING_VALUES_IN_ROW_MENU_ITEM);
					}
					if( !entityDef.enumerable) {
						items.push(DELETE_ENTITY_MENU_ITEM);
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
			
			var fieldEvent:UIEvent = null; 
			switch(event.target) {
				case BLANK_ON_FORM_MENU_ITEM:
					fieldEvent = new NodeEvent(NodeEvent.UPDATE_SYMBOL);
					(fieldEvent as NodeEvent).symbol = FieldSymbol.BLANK_ON_FORM;
					(fieldEvent as NodeEvent).nodeProxy = inputField.attribute;
					(fieldEvent as NodeEvent).fieldIdx = inputField.fieldIndex;
					break;
				case DASH_ON_FORM_MENU_ITEM:
					fieldEvent = new NodeEvent(NodeEvent.UPDATE_SYMBOL);
					(fieldEvent as NodeEvent).symbol = FieldSymbol.DASH_ON_FORM;
					(fieldEvent as NodeEvent).nodeProxy = inputField.attribute;
					(fieldEvent as NodeEvent).fieldIdx = inputField.fieldIndex;
					break;
				case ILLEGIBLE_MENU_ITEM:
					fieldEvent = new NodeEvent(NodeEvent.UPDATE_SYMBOL);
					(fieldEvent as NodeEvent).symbol = FieldSymbol.ILLEGIBLE;
					(fieldEvent as NodeEvent).nodeProxy = inputField.attribute;
					(fieldEvent as NodeEvent).fieldIdx = inputField.fieldIndex;
					break;
				case EDIT_REMARKS_MENU_ITEM:
					remarksPopUpPresenter.openPopUp(inputField, true);
					break;
				case REPLACE_BLANKS_WITH_DASH_MENU_ITEM:
					//setSymbolInBlankChildren(parentEntity, parentEntityDefn, FieldSymbol.DASH_ON_FORM);
					fieldEvent = new NodeEvent(NodeEvent.UPDATE_SYMBOL);
					(fieldEvent as NodeEvent).symbol = FieldSymbol.DASH_ON_FORM;
					(fieldEvent as NodeEvent).nodeProxy = parentEntity;
					break;
				case REPLACE_BLANKS_WITH_STAR_MENU_ITEM:
					//setSymbolInBlankChildren(parentEntity, parentEntityDefn, FieldSymbol.BLANK_ON_FORM);
					
					fieldEvent = new NodeEvent(NodeEvent.UPDATE_SYMBOL);
					(fieldEvent as NodeEvent).symbol = FieldSymbol.BLANK_ON_FORM;
					(fieldEvent as NodeEvent).nodeProxy = parentEntity;
					//(fieldEvent as NodeEvent).inputField = inputField;
					break;
				case DELETE_ATTRIBUTE_MENU_ITEM:
					//TODO
					
			//		AlertUtil.showConfirm("global.confirmDelete", [inputField.attributeDefinition.getLabelText()], "global.confirmAlertTitle", 
			//						performDeleteAttribute, [inputField]);
					break;
				case DELETE_ENTITY_MENU_ITEM:
					//TODO
					//AlertUtil.showConfirm("edit.confirmDeleteEntity", null, "global.confirmAlertTitle", performDeleteEntity);
					break;
				case CONFIRM_ERROR_MENU_ITEM:
					//inputField.applySymbol(FieldSymbol.CONFIRMED);
					fieldEvent = new NodeEvent(NodeEvent.CONFIRM_ERROR);
					(fieldEvent as NodeEvent).nodeProxy = attribute;
					break;
				case APPROVE_MISSING_VALUE_MENU_ITEM:
					//TODO 
					fieldEvent = new NodeEvent(NodeEvent.APPROVE_MISSING);
					(fieldEvent as NodeEvent).nodeProxy = attribute;
					//inputField.applySymbol(FieldSymbol.CONFIRMED);
					break;
				
				//TODO in the future
				case APPROVE_MISSING_VALUES_IN_ROW_MENU_ITEM:
					//setSymbolInBlankChildren(parentEntity, parentEntityDefn, FieldSymbol.CONFIRMED);
					fieldEvent = new NodeEvent(NodeEvent.APPROVE_MISSING);
					(fieldEvent as NodeEvent).nodeProxy = parentEntity;
					break;
			}
			
			if(fieldEvent != null) {
				fieldEvent['inputField'] = inputField;
				EventDispatcherFactory.getEventDispatcher().dispatchEvent(fieldEvent);
			}
		}
		
	}
	
}