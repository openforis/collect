package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.events.DropdownEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.metamodel.proxy.SpatialReferenceSystemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CoordinateProxy;
	import org.openforis.collect.ui.component.detail.CoordinateAttributeFormItem;
	import org.openforis.collect.ui.component.input.CoordinateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CoordinateAttributeFormItemPresenter extends AbstractPresenter {
		
		private var _view:CoordinateAttributeFormItem;
		
		public function CoordinateAttributeFormItemPresenter(view:CoordinateAttributeFormItem = null) {
			_view = view;

			super();

			_view.srsDropDownList.labelFunction = srsDropDownLabelFunction;
			_view.srsDropDownList.dataProvider = Application.activeSurvey.spatialReferenceSystems;
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
			
			_view.xTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.xTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.yTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.yTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.srsDropDownList.addEventListener(Event.CHANGE, srsDropDownChangeHandler);
			_view.srsDropDownList.addEventListener(DropdownEvent.CLOSE, srsDropDownCloseHandler);
		}
		
		protected function attributeChangeHandler(event:Event):void {
			updateView();
		}
		
		public function updateView():void {
			var attribute:AttributeProxy = this._view.attribute;
			
			//reset view
			this._view.srsDropDownList.selectedItem = null;
			this._view.xTextInput.text = null;
			this._view.yTextInput.text = null;
			
			if(attribute != null) {
				var value:Object = attribute.value;
				if(value is CoordinateProxy) {
					var coordinate:CoordinateProxy = CoordinateProxy(value);
					var srs:Object = null;
					this._view.srsDropDownList.selectedItem = srs;
					this._view.xTextInput.text = String(coordinate.x);
					this._view.yTextInput.text = String(coordinate.y);
				}
			}
		}
		
		protected function srsDropDownLabelFunction(item:Object):String {
			var srs:SpatialReferenceSystemProxy = SpatialReferenceSystemProxy(item);
			return srs.getLabelText();
		}
		
		protected function srsDropDownChangeHandler(event:Event):void {
			//applyChanges();
		}
		
		protected function srsDropDownCloseHandler(event:Event):void {
		}
		
		protected function focusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function focusOutHandler(event:FocusEvent):void {
		}
	}
}
