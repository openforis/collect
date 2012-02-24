package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.ui.Keyboard;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	import mx.core.IToolTip;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.AttributeSymbol;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.model.proxy.ValidationResultsProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.ContextMenuBuilder;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.ToolTipUtil;
	import org.openforis.collect.util.UIUtil;
	
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
			
			_updateResponder = new AsyncResponder(updateResultHandler, updateFaultHandler);
			
			super();
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			eventDispatcher.addEventListener(ApplicationEvent.UPDATE_RESPONSE_RECEIVED, updateResponseReceivedHandler);
			
			if(_view.textInput != null) {
				_view.textInput.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
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
			var inputFieldEvent:InputFieldEvent = new InputFieldEvent(InputFieldEvent.CHANGING);
			_view.dispatchEvent(inputFieldEvent);
		}
		
		protected function focusOutHandler(event:FocusEvent):void {
			if(_view.applyChangesOnFocusOut && _changed) {
				applyChanges();
			} else {
				//TODO perform validation only
			}
		}
		
		protected function keyDownHandler(event:KeyboardEvent):void {
			var keyCode:uint = event.keyCode;
			switch(keyCode) {
				case Keyboard.ESCAPE:
					undoLastChange();
					break;
			}
		}
		
		public function applyChanges():void {
			var req:UpdateRequest = new UpdateRequest();
			var def:AttributeDefinitionProxy = _view.attributeDefinition;
			req.parentEntityId = _view.parentEntity.id;
			req.nodeName = def.name;
			req.value = createRequestValue();
			req.fieldIndex = _view.fieldIndex;
			if(_view.attribute != null) {
				var a:AttributeProxy = _view.attribute;
				var field:FieldProxy = a.getField(_view.fieldIndex);
				req.nodeId = a.id;
				req.method = UpdateRequest$Method.UPDATE;
				//preserve remarks
				req.remarks = field.remarks;
			} else {
				req.method = UpdateRequest$Method.ADD;
			}
			dataClient.updateActiveRecord(_updateResponder, req);
		}
		
		public function undoLastChange():void {
			_changed = false;
			updateView();
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
				var field:FieldProxy = _view.attribute.getField(_view.fieldIndex);
				if(field.symbol != null) {
					var shortKey:String = getReasonBlankShortCut(field.symbol);
					if(shortKey != null) {
						return shortKey;
					}
				}
				var value:Object = field.value;
				if(value != null && StringUtil.isNotBlank(value.toString())) {
					return value.toString();
				}
			}
			return "";
		}

		protected function createRequestValue():String {
			var result:String = null;
			var text:String = _view.text;
			if(StringUtil.isNotBlank(text)) {
				result = text;
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
			req.fieldIndex = _view.fieldIndex;
			if(_view.attribute != null) {
				req.nodeId = _view.attribute.id;
				req.method = UpdateRequest$Method.UPDATE_SYMBOL;
			} else {
				req.method = UpdateRequest$Method.ADD;
			}
			dataClient.updateActiveRecord(_updateResponder, req);
		}
		
		protected function updateView():void {
			//update view according to attribute (generic text value)
			
			if(_view.attributeDefinition != null) {
				var text:String = getTextValue();
				_view.text = text;
				if(_view.attribute != null) {
					var a:AttributeProxy = _view.attribute;
					//TODO show remarks icon on field
					var f:FieldProxy = field;
					if(StringUtil.isNotBlank(f.remarks)) {
						
					}
				}
				_view.contextMenu = ContextMenuBuilder.buildContextMenu(_view);
			}
		}
		
		public static function getReasonBlankShortCut(symbol:AttributeSymbol):String {
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
		
		protected function get field():FieldProxy {
			if(_view.attribute != null && _view.fieldIndex >= 0) {
				return _view.attribute.getField(_view.fieldIndex);
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
