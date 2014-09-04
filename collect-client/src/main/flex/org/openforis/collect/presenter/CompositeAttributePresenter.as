package org.openforis.collect.presenter
{
	import mx.binding.utils.ChangeWatcher;
	
	import org.openforis.collect.metamodel.ui.UIOptions$Direction;
	import org.openforis.collect.ui.component.detail.CompositeAttributeRenderer;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * @author S. Ricci
	 * */
	public class CompositeAttributePresenter extends AttributePresenter {
		
		public function CompositeAttributePresenter(view:CompositeAttributeRenderer) {
			super(view);
		}
		
		override public function init():void {
			super.init();
			initViewState();
		}
		
		private function get view():CompositeAttributeRenderer {
			return CompositeAttributeRenderer(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			var inputFields:Array = view.inputFields;
			if(inputFields != null) {
				for each (var f:InputField in inputFields) {
					ChangeWatcher.watch(f, "visited", fieldVisitedHandler);
				}
			}
		}
		
		protected function initViewState():void {
			if(view.attributeDefinition.parentLayout == UIUtil.LAYOUT_TABLE) {
				if ( view.attributeDefinition.parent.direction == UIOptions$Direction.BY_COLUMNS ) {
					view.currentState = CompositeAttributeRenderer.STATE_VERTICAL;
				} else {
					view.currentState = CompositeAttributeRenderer.STATE_HORIZONTAL;
				}
			} else {
				view.currentState = CompositeAttributeRenderer.STATE_VERTICAL_FORM;
			}
		}
	}
}