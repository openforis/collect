package org.openforis.collect.ui.component.detail
{
	import mx.core.IVisualElementContainer;
	import mx.events.FlexEvent;
	
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.presenter.FormItemPresenter;
	
	import spark.components.Group;

	public class FormItem extends Group implements CollectFormItem
	{
		private var _parentEntity:EntityProxy;
		
		private var _childrenAdded:Boolean = false;
		
		private var _isInDataGroup:Boolean = false;
		
		protected var _presenter:FormItemPresenter;
		
		public function FormItem() {
			super();
			this.addEventListener(FlexEvent.CREATION_COMPLETE, creationCompleteHandler);
		}
		
		protected function creationCompleteHandler(event:FlexEvent):void {
			initPresenter();
		}
		
		protected function initPresenter():void {
			_presenter = new FormItemPresenter(this);
		}
		
		public function addTo(parent:IVisualElementContainer):void {
			parent.addElement(this);
		}
		
		[Bindable]
		public function get parentEntity():EntityProxy {
			return _parentEntity;
		}
		
		public function set parentEntity(value:EntityProxy):void {
			_parentEntity = value;
		}
		
		public function get isInDataGroup():Boolean {
			return _isInDataGroup;
		}
		
		public function set isInDataGroup(value:Boolean):void {
			_isInDataGroup = value;
		}
		
		protected function get childrenAdded():Boolean {
			return _childrenAdded;
		}
		
		protected function set childrenAdded(value:Boolean):void {
			_childrenAdded = value;
		}
		
		protected function get presenter():FormItemPresenter {
			return _presenter;
		}
		
		protected function set presenter(value:FormItemPresenter):void {
			_presenter = value;
		}
		
	}
}