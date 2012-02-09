package org.openforis.collect.presenter
{
	import flash.events.Event;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.IList;
	
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class AttributeFormItemPresenter extends AbstractPresenter {
		
		protected var _view:AttributeFormItem;
		
		public function AttributeFormItemPresenter(view:AttributeFormItem) {
			_view = view;
			
			super();
			
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			eventDispatcher.addEventListener(ApplicationEvent.MODEL_CHANGED, modelChangedHandler);
			ChangeWatcher.watch(_view, "parentEntity", parentEntityChangeHandler);
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
		}
		
		protected function parentEntityChangeHandler(event:Event):void {
			assignAttribute();
		}
		
		protected function attributeChangeHandler(event:Event):void {
			updateView();
		}
		
		protected function modelChangedHandler(event:ApplicationEvent):void {
			if(_view.attributes == null && _view.attribute == null) {
				assignAttribute();
			}
		}
		
		/**
		 * get the attribute (or attributes) from the parentEntity
		 */
		protected function assignAttribute():void {
			if (_view.parentEntity != null && _view.attributeDefinition != null) {
				var name:String = _view.attributeDefinition.name;
				if (_view.attributeDefinition.multiple) {
					var attributes:IList = _view.parentEntity.getChildren(name);
					_view.attributes = attributes;
				} else {
					var attribute:AttributeProxy = _view.parentEntity.getSingleAttribute(name);
					_view.attribute = attribute;
				}
			}
		}
		
		protected function updateView():void {
			
		}
		
	}
}