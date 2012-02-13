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
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.granite.collections.IMap;
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.AttributeSymbol;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TextInput;
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
		protected var _changed:Boolean = false;
		
		public function InputFieldPresenter(inputField:InputField = null) {
			_view = inputField;
			super();
			
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			eventDispatcher.addEventListener(ApplicationEvent.MODEL_CHANGED, modelChangedHandler);
			
			_view.addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
			_view.addEventListener(MouseEvent.MOUSE_OUT, mouseOutHandler);
			
			if(_view.textInput != null) {
				_view.textInput.addEventListener(Event.CHANGE, changeHandler);
				_view.textInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
				_view.textInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			}
			
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
		}
		
		protected function modelChangedHandler(event:Event):void {
			if(_view.attribute != null) {
				var newAttribute:AttributeProxy = Application.activeRecord.getNode(_view.attribute.id) as AttributeProxy;
				if(newAttribute != _view.attribute) {
					//attribute changed
					_view.attribute = newAttribute;
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

		protected function applyChanges(value:* = null):void {
			if(_view.parentEntity == null) {
				throw new Error("Missing parent entity for this attribute");
			}
			if(value == null) {
				value = createValue();
			}
			var req:UpdateRequest = new UpdateRequest();
			var def:AttributeDefinitionProxy = _view.attributeDefinition;
			req.parentEntityId = _view.parentEntity.id;
			req.nodeName = def.name;
			req.value = String(value);
			
			if(_view.attribute != null) {
				req.nodeId = _view.attribute.id;
				req.method = UpdateRequest$Method.UPDATE;
				req.remarks = _view.attribute.remarks;
			} else {
				req.method = UpdateRequest$Method.ADD;
			}
			var responder:AsyncResponder = new AsyncResponder(updateResultHandler, updateFaultHandler);
			ClientFactory.dataClient.updateActiveRecord(responder, req);
		}
		
		protected function focusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function updateResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			Application.activeRecord.update(result);
			eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.MODEL_CHANGED));
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
				var value:Object = attribute.value;
				if(value != null && StringUtil.isNotBlank(value.toString())) {
					return value.toString();
				} else if(attribute.symbol != null) {
					var shortKey:String = getReasonBlankShortKey(attribute.symbol);
					if(shortKey != null) {
						return shortKey;
					}
				}
			}
			return "";
		}

		protected function createValue():* {
			var result:* = _view.text;
			return result;
			/*
			var newAttributeValue:* = new AbstractValue();
			newAttributeValue.text1 = _inputField.text;
			if(value != null) {
				//copy old informations
				newAttributeValue.remarks = value.remarks;
			}
			return newAttributeValue;
			*/
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
				req.method = UpdateRequest$Method.UPDATE;
			} else {
				req.method = UpdateRequest$Method.ADD;
			}
			var responder:AsyncResponder = new AsyncResponder(updateResultHandler, updateFaultHandler);
			ClientFactory.dataClient.updateActiveRecord(responder, req);
		}


		protected function updateView():void {
			//update textInput in view (generic text value)
			if(_view.attributeDefinition != null) {
				var textInput:TextInput = _view.textInput as TextInput;
				if(textInput != null) {
					var text:String = getTextValue();
					textInput.text = text;
					
					if(_view.attribute != null) {
						var a:AttributeProxy = _view.attribute;
						if(StringUtil.isNotBlank(a.remarks)) {
							
						}
					}
				}
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
	}
}
