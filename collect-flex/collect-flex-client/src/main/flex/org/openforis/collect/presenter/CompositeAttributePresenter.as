package org.openforis.collect.presenter
{
	import mx.binding.utils.ChangeWatcher;
	
	import org.openforis.collect.ui.component.detail.CompositeAttributeRenderer;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * @author S. Ricci
	 * */
	public class CompositeAttributePresenter extends AttributePresenter {
		
		public function CompositeAttributePresenter(view:CompositeAttributeRenderer) {
			var inputFields:Array = view.inputFields;
			if(inputFields != null) {
				for each (var f:InputField in inputFields) {
					ChangeWatcher.watch(f, "visited", fieldVisitedHandler);
				}
			}
			super(view);
			initViewState();
		}
		
		protected function initViewState():void {
			if(_view.attributeDefinition.parentLayout == UIUtil.LAYOUT_TABLE) {
				_view.currentState = CompositeAttributeRenderer.STATE_HORIZONTAL;
			} else {
				_view.currentState = CompositeAttributeRenderer.STATE_VERTICAL;
			}
		}
	}
}