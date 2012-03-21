package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.core.FlexGlobals;
	import mx.events.CloseEvent;
	import mx.events.CollectionEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.UpdateRequestToken;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CodeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequestOperation;
	import org.openforis.collect.remoting.service.UpdateRequestOperation$Method;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CodeListDialog;
	import org.openforis.collect.ui.component.input.MultipleCodeInputField;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.util.ArrayUtil;
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
			if(! (event is CollectionEvent) && _view.attributes != null) {
				_view.attributes.addEventListener(CollectionEvent.COLLECTION_CHANGE, attributesChangeHandler);
			}
			updateView();
		}
		
		override protected function getTextFromValue():String {
			if(_view.attributeDefinition != null) {
				if(CollectionUtil.isNotEmpty(_view.attributes)) {
					var firstAttribute:AttributeProxy = _view.attributes.getItemAt(0) as AttributeProxy;
					var field:FieldProxy = firstAttribute.getField(0);
					if(field.symbol != null) {
						var shortCut:String = getShortCutForReasonBlank(field.symbol);
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
		
		override public function applyValue():void {
			var text:String = textToRequestValue();
			var operations:ArrayCollection = new ArrayCollection();
			var o:UpdateRequestOperation;
			for each (var a:AttributeProxy in _view.attributes) {
				o = getUpdateRequestOperation(UpdateRequestOperation$Method.DELETE, a.id);
				operations.addItem(o);
			}
			var remarks:String = getRemarks();
			var symbol:FieldSymbol = null;
			if(text != null) {
				var parts:Array = text.split(",");
				if(parts.length == 1 && isShortCutForReasonBlank(text)) {
					symbol = parseShortCutForReasonBlank(text);
					o = getUpdateRequestOperation(UpdateRequestOperation$Method.ADD, NaN, null, symbol, remarks);
					operations.addItem(o);
				} else {
					for each (var part:String in parts) {
						var trimmedPart:String = StringUtil.trim(part);
						if(StringUtil.isNotBlank(trimmedPart)) {
							o = getUpdateRequestOperation(UpdateRequestOperation$Method.ADD, NaN, trimmedPart, null, remarks);
							operations.addItem(o);
						}
					}
				}
			} else if(StringUtil.isNotBlank(remarks)) {
				o = getUpdateRequestOperation(UpdateRequestOperation$Method.ADD, NaN, null, null, remarks);
				operations.addItem(o);
			}
			var req:UpdateRequest = new UpdateRequest();
			req.operations = operations;
			var token:UpdateRequestToken = new UpdateRequestToken(UpdateRequestToken.TYPE_UPDATE_VALUE);
			token.symbol = symbol;
			token.remarks = remarks;
			dataClient.updateActiveRecord(req, token, updateResultHandler, faultHandler);
		}
		
		override public function applyRemarks(remarks:String):void {
			var updatedFields:ArrayCollection = new ArrayCollection();
			var operations:ArrayCollection = new ArrayCollection();
			for each (var a:AttributeProxy in _view.attributes) {
				var value:String = codeAttributeToText(a);
				var field:FieldProxy = a.getField(0);
				var symbol:FieldSymbol = field.symbol;
				var o:UpdateRequestOperation = getUpdateRequestOperation(UpdateRequestOperation$Method.UPDATE, 
					a.id, value, symbol, remarks);
				operations.addItem(o);
				updatedFields.addAll(a.fields);
			}
			var req:UpdateRequest = new UpdateRequest();
			req.operations = operations;
			var token:UpdateRequestToken = new UpdateRequestToken(UpdateRequestToken.TYPE_UPDATE_REMARKS);
			token.remarks = remarks;
			token.updatedFields = updatedFields;
			dataClient.updateActiveRecord(req, token, updateResultHandler, faultHandler);
		}
		
		override public function applySymbol(symbol:FieldSymbol):void {
			var updatedFields:ArrayCollection = new ArrayCollection();
			var operations:ArrayCollection = new ArrayCollection();
			var remarks:String = getRemarks();
			for each (var a:AttributeProxy in _view.attributes) {
				var value:String = codeAttributeToText(a);
				var o:UpdateRequestOperation = getUpdateRequestOperation(UpdateRequestOperation$Method.UPDATE, 
					a.id, value, symbol, remarks);
				operations.addItem(o);
				updatedFields.addAll(a.fields);
			}
			var req:UpdateRequest = new UpdateRequest();
			req.operations = operations;
			var token:UpdateRequestToken = new UpdateRequestToken(UpdateRequestToken.TYPE_UPDATE_SYMBOL);
			token.updatedFields = updatedFields;
			token.symbol = symbol;
			dataClient.updateActiveRecord(req, token, updateResultHandler, faultHandler);
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
				if(ArrayUtil.isNotEmpty(codes)) {
					var parentEntityId:int = _view.parentEntity.id;
					var name:String = _view.attributeDefinition.name;
					var responder:IResponder = new AsyncResponder(findItemsResultHandler, faultHandler);
					
					dataClient.getCodeListItems(responder, parentEntityId, name, codes);
				}
			}
		}
	}
}
