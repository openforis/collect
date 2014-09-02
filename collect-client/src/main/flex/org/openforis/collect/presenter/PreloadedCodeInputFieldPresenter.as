package org.openforis.collect.presenter
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.NodeDeleteRequestProxy;
	import org.openforis.collect.model.proxy.NodeUpdateRequestProxy;
	import org.openforis.collect.model.proxy.NodeUpdateRequestSetProxy;
	import org.openforis.collect.ui.component.input.PreloadedCodeInputField;
	import org.openforis.collect.util.StringUtil;

	/**
	 * @author S. Ricci
	 */
	public class PreloadedCodeInputFieldPresenter extends CodeInputFieldPresenter {
		
		public function PreloadedCodeInputFieldPresenter(view:PreloadedCodeInputField) {
			super(view);
		}
		
		private function get view():PreloadedCodeInputField {
			return PreloadedCodeInputField(_view);
		}
		
		override public function init():void {
			super.init();
			initView();
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.addEventListener("apply", applyEventHandler);
		}
		
		protected function applyEventHandler(event:Event):void {
			super.changeHandler(event);
		}
		
		protected function initView():void {
			view.multiple = view.attributeDefinition.multiple;
			view.direction = CodeAttributeDefinitionProxy(view.attributeDefinition).layoutDirection;
		}
		
		override protected function updateView():void {
			if ( view.parentEntity != null ) {
				initDataProvider();
				var hasRemarks:Boolean = view.attribute == null ? false: StringUtil.isNotBlank(getRemarks());
				view.editable = Application.activeRecordEditable && ! view.attributeDefinition.calculated;
				view.hasRemarks = hasRemarks;
				contextMenu.updateItems();
			}
		}

		protected function initDataProvider(resultHandler:Function = null):void {
			view.currentState = PreloadedCodeInputField.STATE_LOADING;
			view.items = null;
			view.selectedItems = null;
			
			function loadCodesResultHandler(event:ResultEvent, token:Object = null):void {
				var data:IList = event.result as IList;
				
				var selectedItems:ArrayCollection;
				var notSelectedItems:ArrayCollection;
				if ( data == null ) {
					selectedItems = notSelectedItems = new ArrayCollection();
				} else {
					selectedItems = new ArrayCollection();
					for each (var item:CodeListItemProxy in data) {
						if (item.selected) {
							selectedItems.addItem(item);
						}
					}
					notSelectedItems = new ArrayCollection(data.toArray());
					notSelectedItems.filterFunction = function(item:CodeListItemProxy):Boolean {
						return ! (item.selected);
					}
					notSelectedItems.refresh();
				}
				view.items = data;
				view.selectedItems = selectedItems;
				view.notSelectedItems = notSelectedItems;
				
				var reasonBlankItems:ArrayCollection = new ArrayCollection();
				//TODO enable by attribute definition
				reasonBlankItems.addItem(DropDownInputFieldPresenter.EMPTY_ITEM);
				reasonBlankItems.addItem(DropDownInputFieldPresenter.BLANK_ON_FORM_ITEM);
				reasonBlankItems.addItem(DropDownInputFieldPresenter.DASH_ON_FORM_ITEM);
				reasonBlankItems.addItem(DropDownInputFieldPresenter.ILLEGIBLE_ITEM);

				view.reasonBlankItems = reasonBlankItems;
				
				if ( CodeAttributeDefinitionProxy(view.attributeDefinition).allowValuesSorting ) {
					view.currentState = PreloadedCodeInputField.STATE_VALUES_SORTING_ALLOWED;
				} else {
					view.currentState = PreloadedCodeInputField.STATE_DEFAULT;
				}
				
				if ( resultHandler != null ) {
					resultHandler();
				}
			}
			loadCodes(view, loadCodesResultHandler);
		}
		
		/*
		override protected function textToRequestValue():String {
			var reasonBlankSelectedItem:Object = getReasonBlankSelectedItem();
			if(reasonBlankSelectedItem == null) {
				var parts:Array = new Array();
				for each (var item:CodeListItemProxy in view.selectedItems ) { 
					var codeStr:String = StringUtil.concat(": ", item.code, item.qualifier);
					parts.push(codeStr);
				}
				var result:String = StringUtil.concat(", ", parts);
				return result;
			} else {
				return reasonBlankSelectedItem.shortCut;
			}
		}
		*/
		override public function updateValue():void {
			var removeAttributesOperations:ArrayCollection = new ArrayCollection();
			var r:NodeUpdateRequestProxy;
			//remove old attributes
			for each (var a:AttributeProxy in view.attributes) {
				r = new NodeDeleteRequestProxy();
				NodeDeleteRequestProxy(r).nodeId = a.id;
				removeAttributesOperations.addItem(r);
			}
			//add new attributes
			var addAttributesOperations:ArrayCollection = new ArrayCollection();
			var remarks:String = getRemarks();
			var symbol:FieldSymbol = null;
			if(view.selectedItems.length > 0) {
				for each (var item:CodeListItemProxy in view.selectedItems) {
					r = createAttributeAddRequest(item.code, symbol, remarks);
					addAttributesOperations.addItem(r);
				}
			} else if(StringUtil.isNotBlank(remarks)) {
				//add empty attribute
				r = createAttributeAddRequest(null, null, remarks);
				addAttributesOperations.addItem(r);
			}
			var requests:ArrayCollection = new ArrayCollection();
			requests.addAll(removeAttributesOperations);
			requests.addAll(addAttributesOperations);
			var req:NodeUpdateRequestSetProxy = new NodeUpdateRequestSetProxy();
			req.requests = requests;
			sendUpdateRequestSet(req);
		}
		
		protected function getReasonBlankSelectedItem():Object {
			//todo
			return null;
		}
		
	}
}