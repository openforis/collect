package org.openforis.collect.presenter {
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.ui.Images;
	import org.openforis.collect.ui.component.BackupRestorePopUp;
	import org.openforis.collect.ui.component.BackupView;
	import org.openforis.collect.ui.component.ConfigurationPopUp;
	import org.openforis.collect.ui.component.SaikuPopUp;
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

		private static const DATA_MANAGEMENT_MENU_ITEM:Object = {
			label: Message.get('home.dataManagement'), 
			icon: Images.DATA_MANAGEMENT,
			enabled: true};
			
		private static const MAP_VISUALIZER_MENU_ITEM:Object = {
			label: Message.get('home.map_visualizer'),
			icon: Images.MAP_VISUALIZER,
			enabled: true};
		
		private static const USERS_MANAGEMENT_MENU_ITEM:Object = {
			label: Message.get('home.userAccounts'),
			icon: Images.USER_MANAGEMENT,
			enabled: true};
		
		private static const DESIGNER_MENU_ITEM:Object = {
			label: Message.get('home.surveyDesigner'),
			icon: Images.DATABASE_DESIGNER,
			enabled: true};

		private static const DATA_CLEANSING_MENU_ITEM:Object = {
			label: Message.get('home.dataCleansing'),
			icon: Images.DATA_CLEANSING,
			enabled: true};
		
		private static const SAIKU_MENU_ITEM:Object = {
			label: Message.get('home.saiku'),
			icon: Images.SAIKU,
			enabled: false};

		private static const BACKUP_RESTORE_MENU_ITEM:Object = {
			label: Message.get('home.backup_restore'),
			icon: Images.BACKUP,
			enabled: true};

		private static const CONFIGURATION_MENU_ITEM:Object = {
			label: Message.get('home.configuration'),
			icon: Images.CONFIGURATION,
			enabled: true};

		public function HomePresenter(view:HomePageView) {
			super(view);
		}
		
		private function get view():HomePageView {
			return HomePageView(_view);
		}
		
		override public function init():void {
			super.init();
			view.functionList.dataProvider = createFunctionsList();

			checkSaikuAvailability();
		}

		private function checkSaikuAvailability():void {
			var responder:AsyncResponder = new AsyncResponder(function(event:ResultEvent, token:Object = null):void {
				SAIKU_MENU_ITEM.enabled = event.result == true;
				SAIKU_MENU_ITEM.toolTip = event.result ? "": Message.get("saiku.module_not_available");
				view.functionList.dataProvider = createFunctionsList();
			}, faultHandler);
			ClientFactory.saikuClient.isSaikuAvailable(responder);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.functionList.addEventListener(IndexChangeEvent.CHANGING, functionListChangingHandler);
		}
		
		protected function createFunctionsList():IList {
			var result:IList = new ArrayList();
			result.addItem(DATA_MANAGEMENT_MENU_ITEM);
			result.addItem(MAP_VISUALIZER_MENU_ITEM);
			
			if ( Application.user.hasEffectiveRole(UserProxy.ROLE_ADMIN) ) {
				result.addItem(DESIGNER_MENU_ITEM);
				result.addItem(BACKUP_RESTORE_MENU_ITEM);
				result.addItem(DATA_CLEANSING_MENU_ITEM);
				result.addItem(SAIKU_MENU_ITEM);
				result.addItem(USERS_MANAGEMENT_MENU_ITEM);
				result.addItem(CONFIGURATION_MENU_ITEM);
			} else if ( Application.user.hasEffectiveRole(UserProxy.ROLE_VIEW) ) {
				result.addItem(SAIKU_MENU_ITEM);
			}
			return result;
		}
		
		protected function functionListChangingHandler(event:IndexChangeEvent):void {
			var items:IList = view.functionList.dataProvider;
			var item:Object = items.getItemAt(event.newIndex);
			event.preventDefault();
			switch (item) {
				case DATA_MANAGEMENT_MENU_ITEM:
					eventDispatcher.dispatchEvent(new UIEvent(UIEvent.SHOW_LIST_OF_RECORDS));
					break;
				case MAP_VISUALIZER_MENU_ITEM:
					navigateToURL(new URLRequest(ApplicationConstants.MAP_VISUALIZER_URL), "_self");
					break;
				case DESIGNER_MENU_ITEM:
					navigateToURL(new URLRequest(ApplicationConstants.DESIGNER_URL), "_self");
					break;
				case DATA_CLEANSING_MENU_ITEM:
					navigateToURL(new URLRequest(ApplicationConstants.DATA_CLEANSING_URL), "_self");
					break;
				case SAIKU_MENU_ITEM:
					PopUpUtil.createPopUp(SaikuPopUp, true);
					break;
				case BACKUP_RESTORE_MENU_ITEM:
					PopUpUtil.createPopUp(BackupRestorePopUp, true);
					break;
				case USERS_MANAGEMENT_MENU_ITEM:
					PopUpUtil.createPopUp(UserManagementPopUp, true);
					break;
				case CONFIGURATION_MENU_ITEM:
					PopUpUtil.createPopUp(ConfigurationPopUp, true);
					break;
			}
		}
		
	}
}