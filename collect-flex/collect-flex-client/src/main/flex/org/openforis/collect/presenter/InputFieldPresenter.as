package org.openforis.collect.presenter {
	import flash.display.DisplayObjectContainer;
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.core.UIComponent;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.granite.collections.IMap;
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.AttributeSymbol;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.ContextMenuBuilder;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
	import spark.components.supportClasses.ItemRenderer;
	
	/**
	 * 
	 * @author M. Togna
	 * @author S. Ricci
	 * */
	public class InputFieldPresenter extends AbstractPresenter {
		
		private var _view:InputField;
		private var _changed:Boolean = false;
		protected var _updateResponder:IResponder;
		private var _dataClient:DataClient;
		
		public function InputFieldPresenter(inputField:InputField = null) {
			_view = inputField;
			_dataClient = ClientFactory.dataClient;
			super();
			
			_updateResponder = new AsyncResponder(updateResultHandler, updateFaultHandler);
			
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			
			_view.addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
			_view.addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
			
			if(_view.textInput != null) {
				_view.textInput.addEventListener(Event.CHANGE, changeHandler);
				_view.textInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
				_view.textInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			}
			
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
		}
		
		protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(_view.attribute != null) {
				var result:IList = event.result as IList;
				if(result != null) {
					var newAttribute:AttributeProxy = CollectionUtil.getItem(result, "id", _view.attribute.id) as AttributeProxy;
					if(newAttribute != null && newAttribute != _view.attribute) {
						//attribute changed
						_view.attribute = newAttribute;
					}
				}
			}
		}
		
		protected function attributeChangeHandler(event:Event):void {
			updateView();
		}
		
		protected function changeHandler(event:Event):void {
			//TODO if autocomplete enabled show autocomplete popup...
			_changed = true;
		}
		
		protected function focusOutHandler(event:FocusEvent):void {
			if(_changed) {
				applyChanges();
			} else {
				//TODO perform validation only
			}
		}
		
		protected function mouseOverHandler(event:MouseEvent):void {
			var target:UIComponent = event.currentTarget as UIComponent;
			if(target != null && target.document != null) {
				var inputFieldEvent:InputFieldEvent = new InputFieldEvent(InputFieldEvent.INPUT_FIELD_MOUSE_OVER);
				inputFieldEvent.inputField = target.document as InputField;
				eventDispatcher.dispatchEvent(inputFieldEvent);
			}
		}
		
		protected function mouseOutHandler(event:MouseEvent):void {
			var target:UIComponent = event.currentTarget as UIComponent;
			if(target != null && target.document != null) {
				var inputFieldEvent:InputFieldEvent = new InputFieldEvent(InputFieldEvent.INPUT_FIELD_MOUSE_OUT);
				inputFieldEvent.inputField = target.document as InputField;
				eventDispatcher.dispatchEvent(inputFieldEvent);
			}
		}

		public function applyChanges():void {
			var req:UpdateRequest = new UpdateRequest();
			var def:AttributeDefinitionProxy = _view.attributeDefinition;
			req.parentEntityId = _view.parentEntity.id;
			req.nodeName = def.name;
			req.values = createRequestValues();
			if(_view.attribute != null) {
				req.nodeId = _view.attribute.id;
				req.method = UpdateRequest$Method.UPDATE;
				req.remarks = _view.attribute.remarks;
			} else {
				req.method = UpdateRequest$Method.ADD;
			}
			dataClient.updateActiveRecord(_updateResponder, req);
		}
		
		protected function focusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function updateResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			Application.activeRecord.update(result);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = result;
			eventDispatcher.dispatchEvent(appEvt);
			_changed = false;
			//_view.currentState = InputField.STATE_SAVE_COMPLETE;
		}

		protected function updateFaultHandler(event:FaultEvent, token:Object = null):void {
			//_view.currentState = InputField.STATE_ERROR_SAVING;
			faultHandler(event, token);
		}
		
		protected function getTextValue():String {
			var attribute:AttributeProxy = _view.attribute;
			if(attribute != null) {
				if(attribute.symbol != null) {
					var shortKey:String = getReasonBlankShortKey(attribute.symbol);
					if(shortKey != null) {
						return shortKey;
					}
				} else {
					var value:Object = attribute.value;
					if(value != null && StringUtil.isNotBlank(value.toString())) {
						return value.toString();
					}
				}
			}
			return "";
		}

		protected function createRequestValues():Array {
			var result:Array = null;
			var text:String = _view.text;
			if(StringUtil.isNotBlank(text)) {
				result = [text];
			}
			return result;
		}
		
		public function changeSymbol(symbol:AttributeSymbol, remarks:String = null):void {
			var req:UpdateRequest = new UpdateRequest();
			var def:AttributeDefinitionProxy = _view.attributeDefinition;
			req.parentEntityId = _view.parentEntity.id;
			req.nodeName = def.name;
			req.symbol = symbol;
			req.remarks = remarks;
			if(_view.attribute != null) {
				req.nodeId = _view.attribute.id;
				req.method = UpdateRequest$Method.UPDATE_SYMBOL;
			} else {
				req.method = UpdateRequest$Method.ADD;
			}
			dataClient.updateActiveRecord(_updateResponder, req);
		}


		protected function updateView():void {
			//update textInput in view (generic text value)
			if(_view.attributeDefinition != null) {
				var text:String = getTextValue();
				_view.text = text;
				if(_view.attribute != null) {
					var a:AttributeProxy = _view.attribute;
					//TODO remarks
					if(StringUtil.isNotBlank(a.remarks)) {
						
					}
				}
				_view.contextMenu = ContextMenuBuilder.buildContextMenu(_view);
			}
		}
		
		public static function getReasonBlankShortKey(symbol:AttributeSymbol):String {
			if(symbol != null) {
				switch(symbol) {
					case AttributeSymbol.BLANK_ON_FORM:
						return '*';
					case AttributeSymbol.DASH_ON_FORM:
						return '-';
					case AttributeSymbol.ILLEGIBLE:
						return '?';
				}
			}
			return null;
		}
		
		protected function get dataClient():DataClient {
			return _dataClient;
		}

		[Bindable]
		public function get changed():Boolean {
			return _changed;
		}
		
		public function set changed(value:Boolean):void {
			_changed = value;
		}
}
}
