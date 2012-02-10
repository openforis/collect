package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.events.DropdownEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.metamodel.proxy.SpatialReferenceSystemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CoordinateProxy;
	import org.openforis.collect.ui.component.detail.CoordinateAttributeFormItem;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CoordinateAttributeFormItemPresenter extends AttributeFormItemPresenter {
		
		public function CoordinateAttributeFormItemPresenter(view:CoordinateAttributeFormItem) {
			super(view);

			view.srsDropDownList.labelFunction = srsDropDownLabelFunction;
			view.srsDropDownList.dataProvider = Application.activeSurvey.spatialReferenceSystems;
		}
		
		private function get view():CoordinateAttributeFormItem {
			return CoordinateAttributeFormItem(_view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			view.xTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			view.xTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			view.yTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			view.yTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			view.srsDropDownList.addEventListener(Event.CHANGE, srsDropDownChangeHandler);
			view.srsDropDownList.addEventListener(DropdownEvent.CLOSE, srsDropDownCloseHandler);
		}
		
		override protected function updateView():void {
			var attribute:AttributeProxy = this.view.attribute;
			
			//reset view
			view.srsDropDownList.selectedItem = null;
			view.xTextInput.text = null;
			view.yTextInput.text = null;
			
			if(attribute != null) {
				var value:Object = attribute.value;
				if(value is CoordinateProxy) {
					var coordinate:CoordinateProxy = CoordinateProxy(value);
					var srs:Object = null;
					view.srsDropDownList.selectedItem = srs;
					view.xTextInput.text = String(coordinate.x);
					view.yTextInput.text = String(coordinate.y);
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
