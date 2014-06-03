package org.openforis.collect.presenter {
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.controls.Tree;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.Proxy;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.io.proxy.SurveyBackupJobProxy;
	import org.openforis.collect.manager.dataexport.proxy.DataExportStatusProxy;
	import org.openforis.collect.manager.process.ProcessStatus$Step;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.NodeItem;
	import org.openforis.collect.ui.component.DataExportPopUp;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.concurrency.proxy.JobProxy;
	import org.openforis.concurrency.proxy.JobProxy$Status;
	
	import spark.components.DropDownList;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class DataExportPopUpPresenter extends PopUpPresenter {
		
		private static const PROGRESS_DELAY:int = 2000;
		private static const ALL_STEPS_ITEM:Object = {label: Message.get('global.allItemsLabel')};
		
		private static const TYPE_FULL:String = "full";
		private static const TYPE_PARTIAL:String = "partial";
		
		private var _cancelResponder:IResponder;
		private var _exportResponder:IResponder;
		private var _getStateResponder:IResponder;
		private var _progressTimer:Timer;
		private var _type:String;
		private var _job:Proxy;
		private var _firstOpen:Boolean = true;
		
		public function DataExportPopUpPresenter(view:DataExportPopUp) {
			this._exportResponder = new AsyncResponder(exportResultHandler, faultHandler);
			this._cancelResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			this._getStateResponder = new AsyncResponder(getStateResultHandler, faultHandler);
			super(view);
			
			initView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
			DataExportPopUp(_view).closeButton1.addEventListener(MouseEvent.CLICK, closeHandler);
			DataExportPopUp(_view).typeGroup.addEventListener(Event.CHANGE, typeChangeHandler);
			DataExportPopUp(_view).exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
			DataExportPopUp(_view).cancelExportButton.addEventListener(MouseEvent.CLICK, cancelExportButtonClickHandler);
			DataExportPopUp(_view).downloadButton.addEventListener(MouseEvent.CLICK, downloadButtonClickHandler);
			DataExportPopUp(_view).closeButton3.addEventListener(MouseEvent.CLICK, closeHandler);
		}
		
		override protected function closeHandler(event:Event = null):void {
			if ( _job != null && (
					(_job is DataExportStatusProxy && DataExportStatusProxy(_job).step == ProcessStatus$Step.RUN) ||
					(_job is JobProxy && JobProxy(_job).running)
				)) {
				AlertUtil.showMessage("export.cannotClosePopUp");
			} else {
				PopUpManager.removePopUp(_view);
			}
		}
		
		internal function rootEntitySelectedHandler(event:UIEvent):void {
			initView();
		}
		
		protected function typeChangeHandler(event:Event):void {
			var type:String = DataExportPopUp(_view).typeGroup.selectedValue as String;
			_type = type;
			initStepsDropDown();
			switch ( type ) {
				case TYPE_FULL:
					_view.currentState = DataExportPopUp.STATE_PARAMETERS_SELECTION;
					break;
				case TYPE_PARTIAL:
					_view.currentState = DataExportPopUp.STATE_PARTIAL_EXPORT_PARAMETERS_SELECTION;
					break;
			}
		}
		
		protected function exportButtonClickHandler(event:MouseEvent):void {
			if ( ! validateForm() ) {
				return;
			}
			var onlyOwnedRecords:Boolean = DataExportPopUp(_view).onlyOwnedRecordsCheckBox.selected;
			var rootEntityKeys:Array = getInsertedKeyValues();
			
			var step:Object;
			var stepNumber:Number;
			switch ( _type ) {
			case TYPE_PARTIAL:
				var rootEntity:String = Application.activeRootEntity.name;
				
				step = DataExportPopUp(_view).stepDropDownList.selectedItem;
				stepNumber = Application.getRecordStepNumber(CollectRecord$Step(step));
				var exportAll:Boolean = DataExportPopUp(_view).exportAllCheckBox.selected;
				var entityId:Number = NaN;
				if ( ! exportAll ) {
					var selectedEntity:NodeItem = DataExportPopUp(_view).rootTree.selectedItem as NodeItem;
					entityId = selectedEntity.id;
				}
				var includeAllAncestorAttributes:Boolean = DataExportPopUp(_view).includeAllAncestorAttributesCheckBox.selected;
				
				ClientFactory.dataExportClient.export(_exportResponder, rootEntity, stepNumber, entityId, 
						includeAllAncestorAttributes, onlyOwnedRecords, rootEntityKeys);
				
				_view.currentState = DataExportPopUp.STATE_EXPORTING;
				DataExportPopUp(_view).progressBar.setProgress(0, 0);
				break;
			case TYPE_FULL:
				var includeRecordFiles:Boolean = DataExportPopUp(_view).includeRecordFilesCheckBox.selected;
				ClientFactory.dataExportClient.fullExport(_exportResponder, includeRecordFiles, onlyOwnedRecords, rootEntityKeys);
				_view.currentState = DataExportPopUp.STATE_EXPORTING;
				DataExportPopUp(_view).progressBar.setProgress(0, 0);
				break;
			}
		}
		
		private function validateForm():Boolean {
			switch ( _type ) {
			case TYPE_PARTIAL:
				var step:CollectRecord$Step = DataExportPopUp(_view).stepDropDownList.selectedItem;
				//validate step
				if ( step == null ) {
					AlertUtil.showError("export.error.selectStep");
					return false;
				} else {
					//validate selected entity
					var exportAll:Boolean = DataExportPopUp(_view).exportAllCheckBox.selected;
					if ( exportAll ) {
						return true;
					} else {
						var selectedEntity:NodeItem = DataExportPopUp(_view).rootTree.selectedItem as NodeItem;
						if ( selectedEntity == null ) {
							AlertUtil.showMessage("export.selectAnEntity");
							return false;
						} else if ( ! selectedEntity.nodeDefinition.multiple ) {
							AlertUtil.showMessage("export.selectMultipleEntity");
							return false;
						} else {
							return true;
						}
					}
				}
				break;
			default:
				return true;
			}
		}
		
		protected function downloadButtonClickHandler(event:MouseEvent):void {
			var url:String = ApplicationConstants.DOWNLOAD_EXPORTED_DATA_URL;
			navigateToURL(new URLRequest(url), "_new");
		}
		
		protected function cancelExportButtonClickHandler(event:MouseEvent):void {
			ClientFactory.dataExportClient.abort(_cancelResponder);
		}
		
		protected function exportResultHandler(event:ResultEvent, token:Object = null):void {
			_job = event.result as Proxy;
			updateView();
		}
		
		protected function startProgressTimer():void {
			if ( _progressTimer == null ) {
				_progressTimer = new Timer(PROGRESS_DELAY);
				_progressTimer.addEventListener(TimerEvent.TIMER, progressTimerHandler);
			}
			_progressTimer.start();
		}
		
		protected function stopProgressTimer():void {
			if ( _progressTimer != null ) {
				_progressTimer.stop();
				_progressTimer = null;
			}
		}
		
		protected function progressTimerHandler(event:TimerEvent):void {
			updateExportState();
		}
		
		protected function updateExportState():void {
			ClientFactory.dataExportClient.getCurrentJob(_getStateResponder);
		}
		
		protected function cancelResultHandler(event:ResultEvent, token:Object = null):void {
			resetView();
		}
		
		protected function getStateResultHandler(event:ResultEvent, token:Object = null):void {
			_job = event.result as Proxy;
			updateView();
		}
		
		protected function updateView():void {
			if ( _job != null ) {
				if ( _job is DataExportStatusProxy ) {
					var _state:DataExportStatusProxy = _job as DataExportStatusProxy;
					var processed:int = _state.processed;
					var step:ProcessStatus$Step = _state.step;
					if ( step == ProcessStatus$Step.RUN && processed <= _state.total ) {
						_view.currentState = DataExportPopUp.STATE_EXPORTING;
						DataExportPopUp(_view).progressBar.setProgress(processed, _state.total);
						var progressText:String;
						if ( _state.total == 0 ) {
							progressText = Message.get("export.processing");
						} else {
							progressText = Message.get("export.progressLabel", [processed, _state.total]);
						}
						DataExportPopUp(_view).progressLabel.text = progressText;
						if ( _progressTimer == null ) {
							startProgressTimer();
						}
					} else if ( !_firstOpen && step == ProcessStatus$Step.COMPLETE ) {
						_view.currentState = DataExportPopUp.STATE_COMPLETE;
						stopProgressTimer();
					} else {
						if ( !_firstOpen ) {
							if ( step == ProcessStatus$Step.ERROR ) {
								AlertUtil.showError("export.error");
								resetView();
							} else if ( step == ProcessStatus$Step.CANCEL ) {
								AlertUtil.showError("export.cancelled");
								resetView();
							} else {
								//process starting in a while...
								startProgressTimer();
							}
						} else {
							resetView();
						}
					}
				} else if ( _job is SurveyBackupJobProxy ) {
					var job:JobProxy = _job as JobProxy;
					var progress:int = job.progressPercent;
					var status:JobProxy$Status = job.status;
					if ( status == JobProxy$Status.RUNNING && progress <= 100 ) {
						_view.currentState = DataExportPopUp.STATE_EXPORTING;
						DataExportPopUp(_view).progressBar.setProgress(progress, 100);
						var progressText:String = Message.get("export.processing");
						DataExportPopUp(_view).progressLabel.text = progressText;
						if ( _progressTimer == null ) {
							startProgressTimer();
						}
					} else if ( !_firstOpen && status == JobProxy$Status.COMPLETED ) {
						_view.currentState = DataExportPopUp.STATE_COMPLETE;
						stopProgressTimer();
					} else {
						if ( !_firstOpen ) {
							if ( status == JobProxy$Status.FAILED ) {
								AlertUtil.showError("export.error");
								resetView();
							} else if ( status == JobProxy$Status.ABORTED ) {
								AlertUtil.showError("export.cancelled");
								resetView();
							} else {
								//process starting in a while...
								startProgressTimer();
							}
						} else {
							resetView();
						}
					}
				} else {
					//not supported job type
					resetView();
				}
			} else {
				resetView();
			}
			_firstOpen = false;
		}
		
		protected function resetView():void {
			_job = null;
			_view.currentState = DataExportPopUp.STATE_TYPE_SELECTION;
			DataExportPopUp(_view).typeGroup.selection = null;
			stopProgressTimer();
		}
		
		protected function initView():void {
			checkEnabledFields();
			initEntitiesTree();
			initStepsDropDown();
			//try to see if there is an export still running
			updateExportState();
			
			populateForm();
		}
		
		protected function checkEnabledFields():void {
			//only owned records checkbox
			if ( Application.user.canViewDifferentOwnedRecords ) {
				DataExportPopUp(_view).onlyOwnedRecordsCheckBox.enabled = true;
				DataExportPopUp(_view).onlyOwnedRecordsCheckBox.selected = false;
			} else {
				DataExportPopUp(_view).onlyOwnedRecordsCheckBox.enabled = false;
				DataExportPopUp(_view).onlyOwnedRecordsCheckBox.selected = true;
			}
		}
		
		protected function populateForm():void {
			var keyDefns:IList = Application.activeRootEntity.keyAttributeDefinitions;
			DataExportPopUp(_view).rootEntityKeyDefinitions = keyDefns;
		}
		
		protected function initEntitiesTree():void {
			var rootEntity:EntityDefinitionProxy = Application.activeRootEntity;
			var rootNodeItem:NodeItem = NodeItem.fromNodeDef(rootEntity, true, false, false);
			var tree:Tree = DataExportPopUp(_view).rootTree;
			tree.dataProvider = new ArrayCollection([rootNodeItem]);
			tree.callLater(function():void {
				tree.expandItem(rootNodeItem, true);
			});
		}
		
		protected function initStepsDropDown():void {
			var steps:IList = new ArrayCollection(CollectRecord$Step.constants);
			if ( _type == TYPE_FULL ) {
				steps.addItemAt(ALL_STEPS_ITEM, 0);
			}
			var stepDropDownList:DropDownList = DataExportPopUp(_view).stepDropDownList;
			stepDropDownList.dataProvider = steps;
			stepDropDownList.callLater(function():void {
				if ( _type == TYPE_FULL ) {
					stepDropDownList.selectedIndex = 0;
				}
			});
		}
		
		private function getInsertedKeyValues():Array {
			var result:Array = new Array();
			for (var idx:int = 0; idx < Application.activeRootEntity.keyAttributeDefinitions.length; idx++) {
				var textInput:TextInput = TextInput(DataExportPopUp(_view).rootEntityKeyTextInput[idx]);
				var value:String = StringUtil.trimToNull(textInput.text);
				result.push(value);
			}
			return result;
		}
		
	}
}