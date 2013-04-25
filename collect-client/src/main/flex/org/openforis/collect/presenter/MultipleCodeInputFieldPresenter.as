package org.openforis.collect.presenter {
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.events.CollectionEvent;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.remoting.service.UpdateResponse;
	import org.openforis.collect.ui.component.input.MultipleCodeInputField;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class MultipleCodeInputFieldPresenter extends CodeInputFieldPresenter {
		
		private var _view:MultipleCodeInputField;
		
		public function MultipleCodeInputFieldPresenter(view:MultipleCodeInputField) {
			_view = view;
			_view.fieldIndex = -1;
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			ChangeWatcher.watch(_view, "attributes", attributesChangeHandler);
		}
		
		protected function attributesChangeHandler(event:Event):void {
			if(! (event is CollectionEvent) && _view.attributes != null && !_view.attributes.hasEventListener(CollectionEvent.COLLECTION_CHANGE)) {
				_view.attributes.addEventListener(CollectionEvent.COLLECTION_CHANGE, attributesChangeHandler);
			}
			updateView();
		}
		
		override protected function setFocusHandler(event:InputFieldEvent):void {
			if ( _view.textInput != null && _view.parentEntity != null && _view.attributeDefinition != null &&
				_view.parentEntity.id == event.parentEntityId && _view.attributeDefinition.name == event.nodeName) {
				_view.textInput.setFocus();
			}
		}
		
		override protected function setFocusOnSiblingEntity(offset:int, circularLookup:Boolean = false, sameFieldIndex:Boolean = true):Boolean {
			var attributeName:String = _view.attributeDefinition.name;
			var inputFieldEvent:InputFieldEvent = new InputFieldEvent(InputFieldEvent.SET_FOCUS);
			inputFieldEvent.nodeName = attributeName;
			inputFieldEvent.fieldIdx = _view.fieldIndex;
			var attributeToFocusIn:AttributeProxy;
			var siblingEntity:EntityProxy = EntityProxy(_view.parentEntity.getSibling(offset, circularLookup));
			if ( siblingEntity != null ) {
				inputFieldEvent.parentEntityId = siblingEntity.id;
				eventDispatcher.dispatchEvent(inputFieldEvent);
				return true;
			} else {
				return false;
			}
		}
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(_view.attributes != null) {
				var responses:IList = IList(event.result);
				for each (var response:UpdateResponse in responses) {
					var attribute:AttributeProxy = CollectionUtil.getItem(_view.attributes, "id", response.nodeId) as AttributeProxy;
					if(attribute != null) {
						updateView();
						return;
					}
				}
			}
		}
		
		override protected function getTextFromValue():String {
			if(_view.attributeDefinition != null) {
				if(CollectionUtil.isNotEmpty(_view.attributes)) {
					var firstAttribute:AttributeProxy = _view.attributes.getItemAt(0) as AttributeProxy;
					var field:FieldProxy = firstAttribute.getField(0);
					if(field.symbol != null) {
						var shortCut:String = FieldProxy.getShortCutForReasonBlank(field.symbol);
						if(shortCut != null) {
							return shortCut;
						}
					}
					var parts:Array = new Array();
					for each (var attribute:AttributeProxy in _view.attributes) {
						var part:String = codeAttributeToText(attribute);
						parts.push(part);
					}
					var result:String = org.openforis.collect.util.StringUtil.concat(", ", parts);
					return result;
				}
			}
			return "";
		}
		
		override public function updateValue():void {
			var text:String = textToRequestValue();
			var removeAttributesOperations:ArrayCollection = new ArrayCollection();
			var o:UpdateRequestOperation;
			//remove old attributes
			for each (var a:AttributeProxy in _view.attributes) {
				o = getUpdateRequestOperation(UpdateRequestOperation$Method.DELETE, a.id);
				removeAttributesOperations.addItem(o);
			}
			//add new attributes
			var addAttributesOperations:ArrayCollection = new ArrayCollection();
			var remarks:String = getRemarks();
			var symbol:FieldSymbol = null;
			if(text != null) {
				var parts:Array = text.split(",");
				if(parts.length == 1 && FieldProxy.isShortCutForReasonBlank(text)) {
					symbol = FieldProxy.parseShortCutForReasonBlank(text);
					o = getUpdateRequestOperation(UpdateRequestOperation$Method.ADD, NaN, null, symbol, remarks);
					addAttributesOperations.addItem(o);
				} else {
					for each (var part:String in parts) {
						var trimmedPart:String = StringUtil.trim(part);
						if(StringUtil.isNotBlank(trimmedPart)) {
							o = getUpdateRequestOperation(UpdateRequestOperation$Method.ADD, NaN, trimmedPart, null, remarks);
							addAttributesOperations.addItem(o);
						}
					}
				}
			} else if(StringUtil.isNotBlank(remarks)) {
				o = getUpdateRequestOperation(UpdateRequestOperation$Method.ADD, NaN, null, null, remarks);
				addAttributesOperations.addItem(o);
			}
			if ( addAttributesOperations.length == 0 ) {
				//add empty attribute
				o = getUpdateRequestOperation(UpdateRequestOperation$Method.ADD, NaN);
				addAttributesOperations.addItem(o);
			}
			var operations:ArrayCollection = new ArrayCollection();
			operations.addAll(removeAttributesOperations);
			operations.addAll(addAttributesOperations);
			var req:UpdateRequest = new UpdateRequest();
			req.operations = operations;
			dataClient.updateActiveRecord(req, updateResultHandler, faultHandler);
		}
		
		override protected function getRemarks():String {
			if(CollectionUtil.isNotEmpty(_view.attributes)) {
				var a:AttributeProxy = AttributeProxy(_view.attributes.getItemAt(0));
				var field:FieldProxy = FieldProxy(a.fields[0]);
				return field.remarks;
			}
			return null;
		}
		
		override protected function updateDescription():void {
			_view.description = "";
			if(_view.attributes != null) {
				var codes:Array = [];
				var code:String;
				var attribute:AttributeProxy;
				for each(attribute in _view.attributes) {
					code = attribute.getField(0).value as String;
					if( StringUtil.isNotBlank(code)) {
						codes.push(code);
					}
				}
				var parentEntityId:int = _view.parentEntity.id;
				var name:String = _view.attributeDefinition.name;
				var responder:IResponder = new AsyncResponder(findItemsResultHandler, faultHandler);
				
				dataClient.getCodeListItems(responder, parentEntityId, name, codes);
			}
		}
	}
}
