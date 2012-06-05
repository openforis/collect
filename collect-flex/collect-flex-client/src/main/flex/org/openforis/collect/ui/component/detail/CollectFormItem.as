package org.openforis.collect.ui.component.detail {
	import mx.events.FlexEvent;
	
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.presenter.FormItemPresenter;
	
	import spark.components.Group;
	
	
	/**
	 * 
	 * @author M. Togna
	 * */
	public class CollectFormItem extends Group {
		
		private var _parentEntity:EntityProxy;
		private var _childrenAdded:Boolean = false;
		private var _occupyEntirePage:Boolean = false;
		private var _labelWidth:Number = 150;
		
		protected var _presenter:FormItemPresenter;
		
		public function CollectFormItem() {
			this.addEventListener(FlexEvent.CREATION_COMPLETE, creationCompleteHandler);
		}
		
		protected function creationCompleteHandler(event:FlexEvent):void {
			initPresenter();
		}
		
		protected function initPresenter():void {
			_presenter = new FormItemPresenter(this);
		}

		[Bindable]
		public function get parentEntity():EntityProxy {
			return _parentEntity;
		}
		
		public function set parentEntity(value:EntityProxy):void {
			_parentEntity = value;
		}
		
		//to be implemented in subclasses
		public function get nodeDefinition():NodeDefinitionProxy {
			return null;
		}
		
		[Bindable]
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
		
		[Bindable]
		public function get occupyEntirePage():Boolean {
			return _occupyEntirePage;
		}
		
		public function set occupyEntirePage(value:Boolean):void {
			_occupyEntirePage = value;
		}
		
		[Bindable]
		public function get labelWidth():Number {
			return _labelWidth;
		}
		
		public function set labelWidth(value:Number):void {
			_labelWidth = value;
		}
		
	}
}