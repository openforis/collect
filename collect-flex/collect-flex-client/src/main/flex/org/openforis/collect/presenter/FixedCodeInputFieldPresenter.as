package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	import mx.core.FlexGlobals;
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CodeProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.FixedCodeInputField;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.components.Label;
	
	/**
	 * 
	 * @author S. Ricci
	 * @author M. Togna
	 * */
	public class FixedCodeInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:FixedCodeInputField;
		private var _items:IList;
		
		public function FixedCodeInputFieldPresenter(inputField:FixedCodeInputField) {
			this._view = inputField;
			
			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
		}
		
		override protected function attributeChangeHandler(event:Event):void {
			_items = null;
			super.attributeChangeHandler(event);
		}
		
		override protected function updateView():void {
			super.updateView();
		}
		
		override protected function valueToText():String {
			if(_items == null) {
				updateDescription();
			}
			return getDescription();
		}
		
		protected function updateDescription():void {
			if(_view.attribute != null || _view.attributes != null) {
				var codes:Array = [];
				var attribute:AttributeProxy;
				if(_view.attributeDefinition.multiple) {
					for each(attribute in _view.attributes) {
						if( attribute.value != null && StringUtil.isNotBlank(attribute.value.code)) {
							codes.push(attribute.value.code);
						}
					}
				} else {
					attribute = _view.attribute;
					if(attribute != null && attribute.value != null && StringUtil.isNotBlank(attribute.value.code)) {
						codes.push(attribute.value.code);
					}
				}
				if(ArrayUtil.isNotEmpty(codes)) {
					var parentEntityId:int = _view.parentEntity.id;
					var name:String = _view.attributeDefinition.name;
					var responder:IResponder = new AsyncResponder(findItemsResultHandler, faultHandler);
					
					ClientFactory.dataClient.getCodeListItems(responder, parentEntityId, name, codes);
				}
			}
		}
		
		protected function findItemsResultHandler(event:ResultEvent, token:Object = null):void {
			_items = event.result as IList;
			if(_items != null) {
				updateView();
			}
				
		}
		
		protected function getDescription():String {
			var description:String = null;
			if(CollectionUtil.isNotEmpty(_items)) {
				var parts:Array = new Array();
				for each (var item:CodeListItemProxy in _items) {
					parts.push(item.getLabelText());
				}
				description = StringUtil.concat("\n", parts);
			}
			return description;
		}
	}
}
