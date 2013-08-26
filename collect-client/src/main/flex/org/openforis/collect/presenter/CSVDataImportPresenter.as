package org.openforis.collect.presenter
{
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.controls.Tree;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.CSVDataImportClient;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.NodeItem;
	import org.openforis.collect.ui.view.CSVDataImportView;
	
	import spark.components.DropDownList;
	
	/**
	 * 
	 * @author Ricci, Stefano
	 * 
	 * */
	public class CSVDataImportPresenter extends AbstractReferenceDataImportPresenter {
		
		private static const UPLOAD_FILE_NAME_PREFIX:String = "data_import";
		private static const ALL_STEPS_ITEM:Object = {label: Message.get('global.allItemsLabel')};
		
		private var _importClient:CSVDataImportClient;
		
		public function CSVDataImportPresenter(view:CSVDataImportView) {
			_importClient = ClientFactory.csvDataImportClient;
			super(view, new MessageKeys(), UPLOAD_FILE_NAME_PREFIX);
			view.importFileFormatInfo = Message.get(messageKeys.IMPORT_FILE_FORMAT_INFO);
		}
		
		private function get view():CSVDataImportView {
			return CSVDataImportView(_view);
		}
		
		private function get messageKeys():MessageKeys {
			return MessageKeys(_messageKeys);
		}
		
		override protected function performProcessStart():void {
			var responder:AsyncResponder = new AsyncResponder(startResultHandler, faultHandler);
			var enityId:int = NodeItem(view.entitySelectionTree.selectedItem).id;
			_importClient.start(responder, enityId, true);
		}
		
		override protected function performImportCancel():void {
			var responder:AsyncResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			_importClient.cancel(responder);
		}
		
		override protected function performCancelThenClose():void {
			var responder:AsyncResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			_importClient.cancel(responder);
			
			function cancelResultHandler(event:ResultEvent, token:Object = null):void {
				closePopUp();
			}
		}
		
		override protected function updateStatus():void {
			_importClient.getStatus(_getStatusResponder);
		}
		
		override protected function loadInitialData():void {
			initView();
			updateStatus();
		}
		
		override protected function loadSummaries(offset:int=0):void {
			//do nothing
		}
		
		protected function initView():void {
			initEntitiesTree();
			/*initStepsDropDown();*/
		}
		
		protected function initEntitiesTree():void {
			var rootEntity:EntityDefinitionProxy = Application.activeRootEntity;
			var rootNodeItem:NodeItem = NodeItem.fromNodeDef(rootEntity, true, false, false);
			var tree:Tree = view.entitySelectionTree;
			tree.dataProvider = new ArrayCollection([rootNodeItem]);
			tree.callLater(function():void {
				tree.expandItem(rootNodeItem, true);
			});
		}
		
		/*protected function initStepsDropDown():void {
			var steps:IList = new ArrayCollection(CollectRecord$Step.constants);
			steps.addItemAt(ALL_STEPS_ITEM, 0);
			var stepDropDownList:DropDownList = view.stepDropDownList;
			stepDropDownList.dataProvider = steps;
			stepDropDownList.callLater(function():void {
				stepDropDownList.selectedIndex = 0;
			});
		}*/
	}
}
import org.openforis.collect.presenter.ReferenceDataImportMessageKeys;

class MessageKeys extends ReferenceDataImportMessageKeys {
	/*
	override public function get CONFIRM_CLOSE_TITLE():String {
		return "csvDataImport.confirmClose.title";
	}
	*/
	
	public function get FILE_NOT_SELECTED():String {
		return "csvDataImport.fileNotSelected";
	}
	
	public function get IMPORT_POPUP_TITLE():String {
		return "csvDataImport.importPopUpTitle";
	}

	public function get IMPORT_FILE_FORMAT_INFO():String {
		return "csvDataImport.importFileFormatInfo";
	}

}
