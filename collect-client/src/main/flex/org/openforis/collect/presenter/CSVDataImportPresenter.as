package org.openforis.collect.presenter
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.controls.Tree;
	import mx.events.ListEvent;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.CSVDataImportClient;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.NodeItem;
	import org.openforis.collect.ui.view.CSVDataImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.components.DropDownList;
	
	/**
	 * 
	 * @author Ricci, Stefano
	 * 
	 * */
	public class CSVDataImportPresenter extends AbstractReferenceDataImportPresenter {
		
		private static const ALL_STEPS_ITEM:Object = {label: Message.get('global.allItemsLabel')};
		
		private var _importClient:CSVDataImportClient;
		
		public function CSVDataImportPresenter(view:CSVDataImportView) {
			super(view, new MessageKeys());
			_importClient = ClientFactory.csvDataImportClient;
		}
		
		override public function init():void {
			super.init();
			updateImportFileFormatInfoMessage();
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.entitySelectionTree.addEventListener(ListEvent.ITEM_CLICK, entityTreeItemSelectHandler);
			view.importType.addEventListener(Event.CHANGE, importTypeChangeHandler);
		}
		
		protected function importTypeChangeHandler(event:Event):void {
			backToDefaultView();
			view.stepDropDownList.selectedItem = CollectRecord$Step.ENTRY;
		}
		
		protected function entityTreeItemSelectHandler(event:ListEvent):void {
			updateImportFileFormatInfoMessage();
		}
		
		protected function updateImportFileFormatInfoMessage():void {
			var messageArgs:Array = [];
			var tree:Tree = view.entitySelectionTree;
			var selectedTreeItem:NodeItem = tree.selectedItem as NodeItem;
			if ( selectedTreeItem == null ) {
				view.importFileFormatInfo = Message.get("csvDataImport.alert.selectEntity");
			} else {
				var selectedNodeDefn:EntityDefinitionProxy = selectedTreeItem.nodeDefinition as EntityDefinitionProxy;
				messageArgs[0] = StringUtil.concat(", ", getRecordKeyColumnNames(selectedNodeDefn));
				messageArgs[1] = StringUtil.concat(", ", getAncestorKeyColumnNames(selectedNodeDefn));
				messageArgs[2] = StringUtil.concat(", ", getExampleAttributeColumnNames(selectedNodeDefn));
				view.importFileFormatInfo = Message.get(messageKeys.IMPORT_FILE_FORMAT_INFO, messageArgs);
			}
		}
		
		private function getExampleAttributeColumnNames(selectedNodeDefn:EntityDefinitionProxy):Array {
			var result:Array = new Array();
			var children:ListCollectionView = selectedNodeDefn.childDefinitions;
			for ( var i:int = 0; i < children.length && result.length <= 3; i++) {
				var child:NodeDefinitionProxy = children.getItemAt(i) as NodeDefinitionProxy;
				if ( child is AttributeDefinitionProxy && 
						! CollectionUtil.contains(selectedNodeDefn.keyAttributeDefinitions, child, "id") ) {
					result.push(child.name);
				}
			}
			return result;
		}
		
		protected function getRecordKeyColumnNames(nodeDefn:EntityDefinitionProxy):Array {
			var result:Array = new Array();
			var rootEntityDefn:EntityDefinitionProxy = nodeDefn.rootEntity;
			var keyDefns:IList = rootEntityDefn.keyAttributeDefinitions;
			for each (var keyDefn:AttributeDefinitionProxy in keyDefns) {
				result.push(rootEntityDefn.name + "_" + keyDefn.name);
			}
			return result;
		}
		
		protected function getAncestorKeyColumnNames(nodeDefn:EntityDefinitionProxy):Array {
			var result:ArrayCollection = new ArrayCollection();
			var currentParent:EntityDefinitionProxy = nodeDefn;
			while ( currentParent != null && currentParent != nodeDefn.rootEntity ) {
				var keyDefns:IList = currentParent.keyAttributeDefinitions;
				if ( CollectionUtil.isEmpty(keyDefns) ) {
					if ( currentParent.multiple ) {
						result.addItemAt("_" + currentParent.name + "_position", 0);
					} else {
						//TODO consider it for naming following key definitions
					}
				} else {
					var colNames:Array = new Array();
					for each (var keyDefn:AttributeDefinitionProxy in keyDefns) {
						var colName:String = "";
						if ( currentParent != nodeDefn ) {
							colName += currentParent.name + "_";
						}
						colName += keyDefn.name;
						colNames.push(colName);
					}
					result.addAllAt(new ArrayCollection(colNames), 0);
				}
				currentParent = currentParent.parent;
			}
			return result.toArray();
		}
		
		public function getRequiredColumnNamesForSelectedEntity():Array {
			var result:Array = new Array();
			var selectedEntity:EntityDefinitionProxy = view.entitySelectionTree.selectedItem as EntityDefinitionProxy;
			if ( selectedEntity != null ) {
				ArrayUtil.addAll(result, getRecordKeyColumnNames(selectedEntity));
				ArrayUtil.addAll(result, getAncestorKeyColumnNames(selectedEntity));
			}
			return result;
		}
		
		private function get view():CSVDataImportView {
			return CSVDataImportView(_view);
		}
		
		private function get messageKeys():MessageKeys {
			return MessageKeys(_messageKeys);
		}

		override protected function fileReferenceSelectHandler(event:Event):void {
			var importType:String = view.importType.selectedValue as String;
			if ( importType == CSVDataImportView.UPDATE_EXISTING_RECORDS_TYPE ) {
				confirmUploadStart(messageKeys.CONFIRM_IMPORT);
			} else {
				//when inserting new records, do not ask for confirmation
				startUpload();
			}
		}
		
		override protected function checkCanImport():Boolean {
			//validate form
			var importType:String = view.importType.selectedValue as String;
			if ( importType == CSVDataImportView.UPDATE_EXISTING_RECORDS_TYPE ) {
				if ( view.entitySelectionTree.selectedItem == null ) {
					AlertUtil.showError("csvDataImport.alert.selectEntity");
					return false;
				} else if ( ! NodeItem(view.entitySelectionTree.selectedItem).nodeDefinition.multiple ) {
					AlertUtil.showError("csvDataImport.alert.selectMultipleEntity");
					return false;
				} else {
					return true;
				}
			} else if ( view.formVersionDropDownList.selectedItem == null && CollectionUtil.isNotEmpty(view.formVersionDropDownList.dataProvider) ) { 
				AlertUtil.showError("csvDataImport.alert.selectModelVersion");
				return false;
			} else {
				return true;
			}
		}

		override protected function performProcessStart():void {
			var responder:AsyncResponder = new AsyncResponder(startResultHandler, faultHandler);
			var transactional:Boolean = view.transactionalCheckBox.selected;
			var validateRecords:Boolean = view.validateRecordsCheckBox.selected;
			var deleteExistingEntities:Boolean = view.deleteExistingEntitiesCheckBox.selected;
			
			var entityId:int;
			var selectedStep:CollectRecord$Step = null;
			var insertNewRecords:Boolean;
			var newRecordModelVersion:String = null;
			
			if ( view.importType.selectedValue == CSVDataImportView.INSERT_NEW_RECORDS_TYPE ) {
				//insert new records
				insertNewRecords = true;
				entityId = Application.activeRootEntity.id;
				var version:ModelVersionProxy = view.formVersionDropDownList.selectedItem;
				newRecordModelVersion = version == null ? null: version.name;
			} else {
				//update existing records
				insertNewRecords = false;
				entityId = NodeItem(view.entitySelectionTree.selectedItem).id;
				var selectedStepItem:* = view.stepDropDownList.selectedItem;
				selectedStep = selectedStepItem == ALL_STEPS_ITEM ? null: selectedStepItem as CollectRecord$Step;
			}
			_importClient.start(responder, _uploadedTempFileName, entityId, selectedStep, transactional, validateRecords, insertNewRecords, newRecordModelVersion, deleteExistingEntities);
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
		
		override protected function updateViewProcessComplete():void {
			super.updateViewProcessComplete();
			//reload record summaries
			var uiEvent:UIEvent = new UIEvent(UIEvent.RELOAD_RECORD_SUMMARIES);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		override protected function loadInitialData():void {
			initView();
			updateStatus();
		}
		
		override protected function loadSummaries():void {
			//do nothing
		}
		
		protected function initView():void {
			initEntitiesTree();
			initFormVersionsDropDown();
			initStepsDropDown();
			view.importType.selectedValue = CSVDataImportView.UPDATE_EXISTING_RECORDS_TYPE;
			view.transactionalCheckBox.selected = true;
			view.validateRecordsCheckBox.selected = true;
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
		
		protected function initFormVersionsDropDown():void {
			Application.activeSurvey.versions;
			var items:IList = new ArrayCollection(Application.activeSurvey.versions.toArray());
			var dropDownList:DropDownList = view.formVersionDropDownList;
			dropDownList.dataProvider = items;
			
			//if only one model version is defined, select it
			if ( items.length == 1 ) {
				dropDownList.selectedIndex = 0;
			}
		}
		
		protected function initStepsDropDown():void {
			var steps:IList = new ArrayCollection(CollectRecord$Step.constants);
			steps.addItemAt(ALL_STEPS_ITEM, 0);
			var stepDropDownList:DropDownList = view.stepDropDownList;
			stepDropDownList.dataProvider = steps;
			stepDropDownList.callLater(function():void {
				stepDropDownList.selectedIndex = 0;
			});
		}

		override protected function backToDefaultView():void {
			if ( view.importType.selectedValue == CSVDataImportView.UPDATE_EXISTING_RECORDS_TYPE ) {
				view.currentState = CSVDataImportView.STATE_UPDATE_EXISTING_RECORDS;
			} else {
				view.currentState = CSVDataImportView.STATE_INSERT_NEW_RECORDS;
			}
		}
		
	}
}
import org.openforis.collect.presenter.ReferenceDataImportMessageKeys;

class MessageKeys extends ReferenceDataImportMessageKeys {
	/*
	override public function get CONFIRM_CLOSE_TITLE():String {
		return "csvDataImport.confirmClose.title";
	}
	*/
	
	public function get IMPORT_FILE_FORMAT_INFO():String {
		return "csvDataImport.importFileFormatInfo";
	}
	
	override public function get CONFIRM_IMPORT():String {
		return "csvDataImport.confirmImport.message";
	}

}
