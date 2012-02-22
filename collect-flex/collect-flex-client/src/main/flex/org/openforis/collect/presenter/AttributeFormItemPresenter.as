package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.remoting.service.UpdateRequest$Method;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 *  
	 */
	public class AttributeFormItemPresenter extends FormItemPresenter {
		
		public function AttributeFormItemPresenter(view:AttributeFormItem) {
			super(view);
			
			assignAttribute();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			ChangeWatcher.watch(_view, "attribute", attributeChangeHandler);
		}
		
		protected function focusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function focusOutHandler(event:FocusEvent):void {
		}
		
		override protected function parentEntityChangeHandler(event:Event):void {
			assignAttribute();
		}
		
		protected function attributeChangeHandler(event:Event):void {
			updateView();
		}
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			/*
				re-assign the attribute to the form item if the parent entity of the attribute has been deleted
				or the attribute has just been created
				or the already assigned attribute has been updated
				(not necessary for multiple attributes because of the data binding to the collection of attributes)
			*/
			if(view.parentEntity != null && view.attributeDefinition != null) {
				var result:IList = event.result as IList;
				var parentId:Number = view.parentEntity.id;
				for each (var item:NodeProxy in result) {
					if((item.id == parentId && item.deleted) ||
						(item.parentId == parentId && item.name == view.attributeDefinition.name &&
							(view.attribute != null && view.attribute.id == item.id) ||
							(! view.attributeDefinition.multiple) ||
							(view.attributes == null)
						)
					) {
						assignAttribute();
						return;
					}
				}
			}
		}
		
		private function get view():AttributeFormItem {
			return AttributeFormItem(_view);
		}
		
		override protected function updateView():void {
			super.updateView();
		}

		/**
		 * get the attribute (or attributes) from the parentEntity
		 */
		protected function assignAttribute():void {
			if (view.parentEntity != null && view.attributeDefinition != null) {
				var name:String = view.attributeDefinition.name;
				if (view.attributeDefinition.multiple) {
					var attributes:IList = view.parentEntity.getChildren(name);
					view.attributes = attributes;
				} else {
					var attribute:AttributeProxy = view.parentEntity.getSingleAttribute(name);
					view.attribute = attribute;
				}
			} else {
				view.attribute = null;
				view.attributes = null;
			}
		}
		
	}
}