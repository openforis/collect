package org.openforis.collect.presenter {
	import mx.binding.utils.BindingUtils;
	
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.UIOptionsProxy;
	import org.openforis.collect.metamodel.proxy.UITabProxy;
	import org.openforis.collect.metamodel.proxy.UITabSetProxy;
	import org.openforis.collect.ui.component.detail.TabbedFormContainer;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class TabbedFormPresenter extends AbstractPresenter {
		
		private var _view:TabbedFormContainer;
		
		public function TabbedFormPresenter(view:TabbedFormContainer) {
			_view = view;
			super();
			buildView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			BindingUtils.bindSetter(setUITabSet, _view, "uiTabSet");
			BindingUtils.bindSetter(setEntityDefinition, _view, "entityDefinition");
			BindingUtils.bindSetter(setModelVersion, _view, "modelVersion");
		}
		
		protected function setUITabSet(value:UITabSetProxy):void {
			buildView();
		}
		
		protected function setEntityDefinition(value:EntityDefinitionProxy):void {
			buildView();
		}
		
		protected function setModelVersion(value:ModelVersionProxy):void {
			buildView();
		}
		
		protected function buildView():void {
			if ( _view.entityDefinition != null && _view.uiTabSet != null ) {
				if ( _view.uiTabSet is UITabProxy ) {
					_view.definitionsPerCurrentTab = UIOptionsProxy.getDefinitionsPerTab(_view.entityDefinition, _view.modelVersion, UITabProxy(_view.uiTabSet));
				}
				_view.innerUITabs = UIOptionsProxy.getInnerTabs(_view.entityDefinition, _view.modelVersion, _view.uiTabSet);
			}
		}
	}
}