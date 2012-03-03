package org.openforis.collect.presenter {
	import flash.events.Event;
	
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.ui.component.input.FixedCodeInputField;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
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
		
		override protected function getTextFromValue():String {
			if(_items == null) {
				updateDescription();
			}
			return getDescription();
		}
		
		protected function updateDescription():void {
			if(_view.attribute != null || _view.attributes != null) {
				var codes:Array = [];
				var code:String;
				var attribute:AttributeProxy;
				if(_view.attributeDefinition.multiple) {
					for each(attribute in _view.attributes) {
						code = attribute.getField(0).value as String;
						if( StringUtil.isNotBlank(code)) {
							codes.push(code);
						}
					}
				} else {
					attribute = _view.attribute;
					code = attribute.getField(0).value as String;
					if( StringUtil.isNotBlank(code)) {
						codes.push(code);
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
