package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	import mx.events.DropdownEvent;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.SpatialReferenceSystemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.CoordinateProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.detail.CoordinateAttributeFormItem;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CoordinateAttributeFormItemPresenter extends AttributeFormItemPresenter {
		
		protected var _changed:Boolean = false;
		
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
		
		override protected function focusOutHandler(event:FocusEvent):void {
			if(_changed) {
				applyChanges();
			}
		}
		
		protected function applyChanges(value:* = null):void {
			if(value == null) {
				value = createValue();
			}
			var req:UpdateRequest = new UpdateRequest();
			var def:AttributeDefinitionProxy = view.attributeDefinition;
			req.parentEntityId = view.parentEntity.id;
			req.nodeName = def.name;
			req.value = String(value);
			
			if(view.attribute != null) {
				req.nodeId = view.attribute.id;
				req.method = UpdateRequest$Method.UPDATE;
				req.remarks = view.attribute.remarks;
			} else {
				req.method = UpdateRequest$Method.ADD;
			}
			var responder:AsyncResponder = new AsyncResponder(updateResultHandler, faultHandler);
			ClientFactory.dataClient.updateActiveRecord(responder, req);
		}
		
		protected function createValue():* {
			var srs:SpatialReferenceSystemProxy = view.srsDropDownList.selectedItem as SpatialReferenceSystemProxy;
			var srsId:String = "";
			if(srs != null) {
				srsId = srs.id;
			}
			var x:String = view.xTextInput.text;
			var y:String = view.yTextInput.text;
			
			var result:String = "SRID=" + srsId + ";POINT(" + x + " " + y + ")";
			return result;
		}
		
		protected function updateResultHandler(event:ResultEvent, token:Object = null):void {
			var result:IList = event.result as IList;
			Application.activeRecord.update(result);
			eventDispatcher.dispatchEvent(new ApplicationEvent(ApplicationEvent.MODEL_CHANGED));
			_changed = false;
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
					var srs:Object = CollectionUtil.getItem(Application.activeSurvey.spatialReferenceSystems, "id", coordinate.srsId);
					view.srsDropDownList.selectedItem = srs;
					view.xTextInput.text = StringUtil.nullToBlank(coordinate.x);
					view.yTextInput.text = StringUtil.nullToBlank(coordinate.y);
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
		
	}
}
