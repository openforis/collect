package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.DropDownCodeInputField;
	import org.openforis.collect.ui.component.input.PreloadedCodeInputField;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.ObjectUtil;
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
		
		protected function initView():void {
			view.multiple = view.attributeDefinition.multiple;
			view.direction = "horizontal";
		}
		
		override protected function updateView():void {
			if ( view.parentEntity != null ) {
				initDataProvider(function():void {
					/*
					var item:Object = null;
					var attribute:AttributeProxy = view.attribute;
					if(attribute != null) {
						var field:FieldProxy = attribute.getField(view.fieldIndex);
						var value:Object = field.value;
						if(field.symbol != null && FieldProxy.isReasonBlankSymbol(field.symbol)) {
							switch(field.symbol) {
								case FieldSymbol.BLANK_ON_FORM:
									item = DropDownInputFieldPresenter.BLANK_ON_FORM_ITEM;
									break;
								case FieldSymbol.DASH_ON_FORM:
									item = DropDownInputFieldPresenter.DASH_ON_FORM_ITEM;
									break;
								case FieldSymbol.ILLEGIBLE:
									item = DropDownInputFieldPresenter.ILLEGIBLE_ITEM;
									break;
							}
						} else if(value != null) {
							item = getItemByCode(value);
						}
					}
					setSelectedItem(item);
					*/
				});
				var hasRemarks:Boolean = view.attribute == null ? false: StringUtil.isNotBlank(getRemarks());
				view.editable = Application.activeRecordEditable && ! view.attributeDefinition.calculated;
				view.hasRemarks = hasRemarks;
				contextMenu.updateItems();
			}
		}
		/*
		protected function getItemByCode(code:*):Object {
			var item:Object = CollectionUtil.getItem(values, "code", code);
			return item;
		}
		*/
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
		
		override protected function textToRequestValue():String {
			var reasonBlankSelectedItem:Object = getReasonBlankSelectedItem();
			if(reasonBlankSelectedItem != null) {
				return reasonBlankSelectedItem.shortCut;
			} else {
				var parts:Array = new Array();
				for each (var item:CodeListItemProxy in view.selectedItems ) { 
					var codeStr:String = StringUtil.concat(": ", item.code, item.qualifier);
					parts.push(codeStr);
				}
				var result:String = StringUtil.concat(", ", parts);
				return result;
			}
		}
		
		protected function getReasonBlankSelectedItem():Object {
			//todo
			return null;
		}
		
		override protected function changeHandler(event:Event):void {
			super.changeHandler(event);
		}
		
	}
}