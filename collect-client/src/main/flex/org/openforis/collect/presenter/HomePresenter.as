package org.openforis.collect.presenter {
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	import mx.collections.ArrayList;
	import mx.collections.IList;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.ui.component.user.UserManagementPopUp;
	import org.openforis.collect.ui.view.HomePageView;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.PopUpUtil;
	
	import spark.events.IndexChangeEvent;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class HomePresenter extends AbstractPresenter {

		private static const DATA_MANAGEMENT_MENU_ITEM:Object = {label: Message.get('home.dataManagement')};
		private static const USERS_MANAGEMENT_MENU_ITEM:Object = {label: Message.get('home.usersManagement')};
		private static const DESIGNER_MENU_ITEM:Object = {label: Message.get('home.databaseDesigner')};
		
		private var _view:HomePageView;
		
		public function HomePresenter(view:HomePageView) {
			this._view = view;
			super();
			
			_view.functionList.dataProvider = createFunctionsList();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			_view.functionList.addEventListener(IndexChangeEvent.CHANGE, functionListChangeHandler);
		}
		
		protected function createFunctionsList():IList {
			var result:IList = new ArrayList();
			result.addItem(DATA_MANAGEMENT_MENU_ITEM);
			if ( Application.user.hasEffectiveRole(UserProxy.ROLE_ADMIN) ) {
				result.addItem(DESIGNER_MENU_ITEM);
				result.addItem(USERS_MANAGEMENT_MENU_ITEM);
			}
			return result;
		}
		
		protected function functionListChangeHandler(event:IndexChangeEvent):void {
			var items:IList = _view.functionList.dataProvider;
			var item:Object = items.getItemAt(event.newIndex);
			switch (item) {
				case DESIGNER_MENU_ITEM:
					navigateToURL(new URLRequest(ApplicationConstants.DESIGNER_URL), "_self");
					break;
				case USERS_MANAGEMENT_MENU_ITEM:
					PopUpUtil.createPopUp(UserManagementPopUp, true);
					break;
				case DATA_MANAGEMENT_MENU_ITEM:
					eventDispatcher.dispatchEvent(new UIEvent(UIEvent.SHOW_LIST_OF_RECORDS));
					break;
			}
		}
		
		
	}
}