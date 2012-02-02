package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.utils.setTimeout;
	
	import mx.collections.ArrayCollection;
	import mx.core.FlexGlobals;
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CodeProxy;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CodeListItem;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.metamodel.proxy.CodeListProxy;
	
	/**
	 * 
	 * @author S. Ricci
	 * @author M. Togna
	 * */
	public class CodeInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:CodeInputField;
		
		public function CodeInputFieldPresenter(inputField:CodeInputField) {
			this._view = inputField;
			
			super(inputField);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.openImage.addEventListener(MouseEvent.CLICK, openImageClickHandler);
			
			_view.popup.addEventListener(CloseEvent.CLOSE, closePopupHandler);
			_view.popup.cancelButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandelr);
			_view.popup.applyButton.addEventListener(MouseEvent.CLICK, applyButtonClickHandelr);
		}
		
		/**
		 * Close the popup
		 * */
		internal function closePopupHandler(event:CloseEvent):void {
			PopUpManager.removePopUp(_view.popup);
		}
		
		/**
		 * Open the popup
		 * */
		protected function openImageClickHandler(event:Event):void {
			PopUpManager.addPopUp(_view.popup, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(_view.popup);
			
			loadCodes();
		}
		
		protected function loadCodes():void {
			//_view.popup.currentState = "loading";
			
			//call service method
			//setTimeout(loadListDialogDataResultHandler, 1000);
			
			var codeAttributeDef:CodeAttributeDefinitionProxy = _view.attributeDefinition as CodeAttributeDefinitionProxy;
			var list:CodeListProxy = codeAttributeDef.list;
			var parentExpression:String = codeAttributeDef.parentExpression;
			trace(parentExpression);
			
		}
		
		protected function loadListDialogDataResultHandler(event:ResultEvent = null):void {
			//var data:ArrayCollection = event.result as ArrayCollection
			//test data
			/*
			var data:ArrayCollection = new ArrayCollection();
			for(var index:int = 0; index < 9; index ++) {
				data.addItem(new CodeListItem(
					StringUtil.substitute("00{0}", index + 1),
					StringUtil.substitute("00{0} - Item {0}", index + 1, index + 1), 
					index %3 == 0
				));
			}
			data.addItem(new CodeListItem("999", "Other", true, true, "Test"));
			_view.popup.dataGroup.dataProvider = data;
			*/
			_view.popup.currentState = "default";
		}

		protected function applyButtonClickHandelr(event:MouseEvent):void {
			//var data:ArrayCollection = _view.popup.dataGroup.dataProvider;
			
			//call service method
		}
		
		protected function cancelButtonClickHandelr(event:MouseEvent):void {
			PopUpManager.removePopUp(_view.popup);
		}

		override protected function get textValue():String {
			if(_view.attributeDefinition != null) {
				if(_view.attributeDefinition.multiple) {
					if(_view.attributes) {
						var parts:Array = new Array();
						for each (var attribute:AttributeProxy in _view.attributes) {
							var value:CodeProxy = _view.attribute.value as CodeProxy;
							if(value != null) {
								var part:String = value.toString();
								parts.push(part);
							}
						}
						var result:String = org.openforis.collect.util.StringUtil.concat(", ", parts);
						return result;
					}
				} else {
					if(_view.attribute != null) {
						var value:CodeProxy = _view.attribute.value as CodeProxy;
						if(value != null) {
							var text:String = value.toString();
							return text;
						}
					}
				}
			}
			return "";
		}
		
		override public function updateView():void {
			super.updateView();
		}
		
	}
}
