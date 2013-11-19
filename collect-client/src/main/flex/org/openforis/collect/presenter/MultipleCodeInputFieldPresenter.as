package org.openforis.collect.presenter {
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeChangeProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.model.proxy.NodeChangeProxy;
	import org.openforis.collect.model.proxy.NodeChangeSetProxy;
	import org.openforis.collect.model.proxy.NodeDeleteRequestProxy;
	import org.openforis.collect.model.proxy.NodeUpdateRequestProxy;
	import org.openforis.collect.model.proxy.NodeUpdateRequestSetProxy;
	import org.openforis.collect.ui.CollectFocusManager;
	import org.openforis.collect.ui.component.input.MultipleCodeInputField;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
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
		
		override protected function setFocusHandler(event:InputFieldEvent):void {
			if ( _view.textInput != null && _view.parentEntity != null && _view.attributeDefinition != null &&
				_view.parentEntity.id == event.parentEntityId && _view.attributeDefinition.name == event.nodeName) {
				_view.textInput.setFocus();
			}
		}
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			if(_view.attributes != null) {
				var changeSet:NodeChangeSetProxy = NodeChangeSetProxy(event.result);
				for each (var change:NodeChangeProxy in changeSet.changes) {
					if ( change is AttributeChangeProxy ) {
						var nodeId:int = AttributeChangeProxy(change).nodeId;
						var attribute:AttributeProxy = CollectionUtil.getItem(_view.attributes, "id", nodeId) as AttributeProxy;
						if(attribute != null) {
							updateView();
							return;
						}
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
			var r:NodeUpdateRequestProxy;
			//remove old attributes
			for each (var a:AttributeProxy in _view.attributes) {
				r = new NodeDeleteRequestProxy();
				NodeDeleteRequestProxy(r).nodeId = a.id;
				removeAttributesOperations.addItem(r);
			}
			//add new attributes
			var addAttributesOperations:ArrayCollection = new ArrayCollection();
			var remarks:String = getRemarks();
			var symbol:FieldSymbol = null;
			if(text != null) {
				var parts:Array = text.split(",");
				if(parts.length == 1 && FieldProxy.isShortCutForReasonBlank(text)) {
					symbol = FieldProxy.parseShortCutForReasonBlank(text);
					r = createAttributeAddRequest(null, symbol, remarks);
					addAttributesOperations.addItem(r);
				} else {
					for each (var part:String in parts) {
						var trimmedPart:String = StringUtil.trim(part);
						if(StringUtil.isNotBlank(trimmedPart)) {
							r = createAttributeAddRequest(trimmedPart, symbol, remarks);
							addAttributesOperations.addItem(r);
						}
					}
				}
			} else if(StringUtil.isNotBlank(remarks)) {
				r = createAttributeAddRequest(null, null, remarks);
				addAttributesOperations.addItem(r);
			}
			if ( addAttributesOperations.length == 0 ) {
				//add empty attribute
				r = createAttributeAddRequest(null, null, remarks);
				addAttributesOperations.addItem(r);
			}
			var requests:ArrayCollection = new ArrayCollection();
			requests.addAll(removeAttributesOperations);
			requests.addAll(addAttributesOperations);
			var req:NodeUpdateRequestSetProxy = new NodeUpdateRequestSetProxy();
			req.requests = requests;
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
			if (  ! CodeAttributeDefinitionProxy(_view.attributeDefinition).external &&
					_view.attributes != null && _view.attributeDefinition.parentLayout == UIUtil.LAYOUT_FORM ) {
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
		
		override protected function moveFocusOnNextField(horizontalMove:Boolean, offset:int):Boolean {
			var focusChanged:Boolean = false;
			var attributes:IList = _view.attributes;
			if ( CollectionUtil.isNotEmpty(attributes) ) {
				var attribute:AttributeProxy = AttributeProxy(attributes.getItemAt(0));
				var fieldIndex:int = _view.fieldIndex;
				var field:FieldProxy = attribute.getField(fieldIndex);
				focusChanged = CollectFocusManager.moveFocusOnNextField(field, horizontalMove, offset);
			}
			if ( ! focusChanged ) {
				focusChanged = UIUtil.moveFocus(offset < 0);
			}
			return focusChanged;
		}
	}
}
