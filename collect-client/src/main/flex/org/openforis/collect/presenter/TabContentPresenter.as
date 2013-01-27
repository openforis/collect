package org.openforis.collect.presenter {
	import mx.binding.utils.BindingUtils;
	import mx.binding.utils.ChangeWatcher;
	
	import org.openforis.collect.metamodel.proxy.UIOptionsProxy;
	import org.openforis.collect.metamodel.proxy.UITabProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.ui.UIBuilder;
	import org.openforis.collect.ui.component.detail.TabContentContainer;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class TabContentPresenter extends AbstractPresenter {
		
		private var _view:TabContentContainer;
		
		public function TabContentPresenter(view:TabContentContainer) {
			_view = view;
			super();
			buildView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			BindingUtils.bindSetter(setParentEntity, _view, "parentEntity");
		}
		
		protected function setParentEntity(value:EntityProxy):void {
			buildView();
		}
		
		protected function buildView():void {
			_view.definitionsPerCurrentTab = UIOptionsProxy.getDefinitionsPerTab(_view.entityDefinition, _view.modelVersion, _view.uiTabSet);
			_view.innerUITabs = UIOptionsProxy.getInnerTabs(_view.entityDefinition, _view.modelVersion, _view.uiTabSet);
		}
		/*
		public function resetScrollbars():void {
			if(_view.currentState == STATE_WITH_TABS) {
				for each(var form:ScrollableFormContainer in formContainers) {
					form.resetScrollBars();
				}
			} else {
				withoutTabsContainer.resetScrollBars();
			}
		}
		*/
	}
}