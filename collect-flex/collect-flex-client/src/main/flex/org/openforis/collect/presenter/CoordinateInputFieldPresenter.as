package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.events.DropdownEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.metamodel.proxy.SpatialReferenceSystemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CoordinateProxy;
	import org.openforis.collect.ui.component.input.CoordinateInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CoordinateInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:CoordinateInputField;
		
		public function CoordinateInputFieldPresenter(inputField:CoordinateInputField) {
			_view = inputField;
			super(inputField);

			_view.srsDropDownList.labelFunction = srsDropDownLabelFunction;
			//TODO binding
			_view.srsDropDownList.dataProvider = Application.activeSurvey.spatialReferenceSystems;
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			_view.xTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.xTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.yTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.yTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			_view.srsDropDownList.addEventListener(Event.CHANGE, srsDropDownChangeHandler);
			_view.srsDropDownList.addEventListener(DropdownEvent.CLOSE, srsDropDownCloseHandler);
		}
		
		override protected function updateView():void {
			super.updateView();
			
			var attribute:AttributeProxy = _view.attribute;
			
			//reset view
			_view.srsDropDownList.selectedItem = null;
			_view.xTextInput.text = null;
			_view.yTextInput.text = null;
			
			if(attribute != null) {
				var value:Object = attribute.value;
				if(value != null && value is CoordinateProxy) {
					var coordinate:CoordinateProxy = CoordinateProxy(value);
					var srs:Object = CollectionUtil.getItem(Application.activeSurvey.spatialReferenceSystems, "id", coordinate.srsId);
					_view.srsDropDownList.selectedItem = srs;
					_view.xTextInput.text = StringUtil.nullToBlank(coordinate.x);
					_view.yTextInput.text = StringUtil.nullToBlank(coordinate.y);
				}
			}
		}
		
		override protected function createValue():* {
			var srs:SpatialReferenceSystemProxy = _view.srsDropDownList.selectedItem as SpatialReferenceSystemProxy;
			var srsId:String = "";
			if(srs != null) {
				srsId = srs.id;
			}
			var x:String = StringUtil.nullToBlank(_view.xTextInput.text);
			var y:String = StringUtil.nullToBlank(_view.yTextInput.text);
			
			var result:String = "SRID=" + srsId + ";POINT(" + x + " " + y + ")";
			return result;
		}
		
		
		protected function srsDropDownLabelFunction(item:Object):String {
			var srs:SpatialReferenceSystemProxy = SpatialReferenceSystemProxy(item);
			return srs.getLabelText();
		}
		
		protected function srsDropDownChangeHandler(event:Event):void {
			applyChanges();
		}
		
		protected function srsDropDownCloseHandler(event:Event):void {
		}
		
	}
}
