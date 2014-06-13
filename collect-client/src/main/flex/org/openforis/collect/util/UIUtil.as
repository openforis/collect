package org.openforis.collect.util
{
	import flash.display.DisplayObject;
	import flash.text.AntiAliasType;
	import flash.text.GridFitType;
	import flash.text.TextLineMetrics;
	
	import mx.containers.ViewStack;
	import mx.core.Container;
	import mx.core.FlexGlobals;
	import mx.core.IVisualElement;
	import mx.core.UIComponent;
	import mx.core.UITextFormat;
	import mx.managers.IFocusManager;
	import mx.managers.IFocusManagerComponent;
	import mx.styles.CSSStyleDeclaration;
	import mx.styles.IStyleManager2;
	import mx.utils.ObjectUtil;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.CollectRecord$Step;
	
	import spark.components.Application;
	import spark.components.NavigatorContent;
	import spark.components.Scroller;
	import spark.components.gridClasses.GridColumn;
	import spark.formatters.DateTimeFormatter;
	
	/**
	 * @author S. Ricci
	 */
	public class UIUtil {
		
		public static const LAYOUT_FORM:String = "form";
		public static const LAYOUT_TABLE:String = "table";
		
		private static var fixedCodeTextFormat:UITextFormat
		private static var unitTextFormat:UITextFormat;
		private static var gridHeaderFormat:UITextFormat;
		
		private static var _dataTimeFormatter:DateTimeFormatter;
		//init static variables
		{
			_dataTimeFormatter = new DateTimeFormatter();
			_dataTimeFormatter.dateTimePattern = ApplicationConstants.DATE_TIME_PATTERN;
			fixedCodeTextFormat = createUITextFormat("spark.components.Label.fixedCode");
			unitTextFormat = createUITextFormat("spark.components.Label.unit");
			gridHeaderFormat = createUITextFormat("spark.components.Label.dataGroupHeader");
		}
		
		private static function createUITextFormat(styleName:String):UITextFormat {
			var application:Application = FlexGlobals.topLevelApplication as Application;
			var styleManager:IStyleManager2 = application.styleManager;
			var styleDeclaration:CSSStyleDeclaration = styleManager.getMergedStyleDeclaration(styleName);
			var fontSize:* = styleDeclaration.getStyle("fontSize");
			var fontWeight:* = styleDeclaration.getStyle("fontWeight");
			var font:* = styleDeclaration.getStyle("fontFamily");
			var result:UITextFormat = new UITextFormat(application.systemManager, font, fontSize);
			result.bold = fontWeight == "bold";
			result.italic = fontWeight == "italic";
			result.antiAliasType = AntiAliasType.NORMAL;
			result.gridFitType = GridFitType.NONE;
			return result;
		}
		
		public static function resetScrollBars(uiComponent:UIComponent):void {
			if(uiComponent is Scroller) {
				var scroller:Scroller = Scroller(uiComponent);
				if(scroller.verticalScrollBar) {
					scroller.verticalScrollBar.value = 0;
				}
				if(scroller.horizontalScrollBar) {
					scroller.horizontalScrollBar.value = 0;
				}
			} else if(uiComponent is ViewStack) {
				for each (var child:UIComponent in (uiComponent as ViewStack).getChildren())  {
					if(child is Container) {
						resetScrollBars(child);
					} else if(child is NavigatorContent && (child as NavigatorContent).numElements > 0 && 
							(child as NavigatorContent).getElementAt(0) is Scroller) {
						resetScrollBars((child as NavigatorContent).getElementAt(0) as Scroller);
					}
				}
			} else if(uiComponent is Container) {
				Container(uiComponent).verticalScrollPosition = 0;
				Container(uiComponent).horizontalScrollPosition = 0;
			}
		}
		
		public static function resetScrollBarsOnScroller(scroller:Scroller):void {
			if(scroller.verticalScrollBar)
				scroller.verticalScrollBar.value = 0;
			if(scroller.horizontalScrollBar)
				scroller.horizontalScrollBar.value = 0;
		} 
		
		public static function getFirstAncestor(field:Object, clazz:Class):* {
			var result:* = null;
			var currentContainer:Object = field;
			var parent:Object = currentContainer;
			while(parent != null) {
				currentContainer = parent;
				if(currentContainer.hasOwnProperty("parent")) {
					parent = currentContainer.parent;
					if(parent is clazz) {
						result = parent;
						break;
					}
				} else {
					break;
				}
			}
			return result;
		}
		
		public static function ensureElementIsVisible(field:Object):void {
			var scroller:Scroller = UIUtil.getFirstAncestor(field, Scroller);
			while ( scroller != null ) {
				var elementToShow:IVisualElement = field as IVisualElement;
				if(elementToShow == null) {
					elementToShow = getFirstAncestor(field, IVisualElement);
				}
				if(elementToShow != null) {
					scroller.ensureElementIsVisible(elementToShow);
					
					elementToShow = scroller;
					scroller = UIUtil.getFirstAncestor(scroller, Scroller);
				}
			}
		}
		
		public static function addStyleName(component:UIComponent, styleName:String, applyImmediately:Boolean = true):String {
			var componentStyleName:String = component.styleName as String;
			if(!hasStyleName(component, styleName)) {
				if(StringUtil.isBlank(componentStyleName)){
					componentStyleName = "";
				} else {
					componentStyleName += " ";
				}
				componentStyleName += styleName;
				if(applyImmediately) {
					component.styleName = componentStyleName;
				}
			}
			return componentStyleName;
		}
			
		public static function addStyleNames(component:UIComponent, styleNames:Array, applyImmediately:Boolean = true):String {
			for each(var styleName:String in styleNames) {
				addStyleName(component, styleName, applyImmediately);
			}
			return component.styleName as String;
		}
		
		public static function removeStyleName(component:UIComponent, styleName:String, applyImmediately:Boolean = true):String {
			var componentStyleName:String = component.styleName as String;
			if(hasStyleName(component, styleName)) {
				componentStyleName = componentStyleName.replace(styleName, "");
				componentStyleName = StringUtil.trimToNull(componentStyleName);
				if(applyImmediately) {
					component.styleName = componentStyleName;
				}
			}
			return componentStyleName;
		}
		
		public static function removeStyleNames(component:UIComponent, styleNames:Array, applyImmediately:Boolean = true):String {
			for each(var styleName:String in styleNames) {
				removeStyleName(component, styleName, applyImmediately);
			}
			return component.styleName as String;
		}
		
		public static function toggleStyleName(component:UIComponent, styleName:String, apply:Boolean = true, applyImmediately:Boolean = true):String {
			if ( apply ) {
				return addStyleName(component, styleName, applyImmediately);
			} else {
				return removeStyleName(component, styleName, applyImmediately);
			}
		}

		public static function hasStyleName(component:UIComponent, styleName:String):Boolean {
			var componentStyleName:String = component.styleName as String;
			if(componentStyleName != null) {
				return componentStyleName.indexOf(styleName) >= 0;
			}
			return false;
		}
		
		public static function replaceStyleNames(component:UIComponent, newStyles:Array, oldStyles:Array, applyImmediately:Boolean = true):String {
			var cleanedStyle:String = removeStyleNames(component, oldStyles);
			var newStylesConcat:String = StringUtil.concat(" ", newStyles);
			var result:String = StringUtil.concat(" ", [cleanedStyle, newStylesConcat]);
			result = StringUtil.trimToNull(result);
			if ( applyImmediately ) {
				var oldStyleName:String = StringUtil.trimToNull(component.styleName as String);
				if ( result != oldStyleName ) {
					component.styleName = result;
				}
			}
			return result;
		}
		
		public static function getMaxAvailableHeight(component:UIComponent):Number {
			var paddingTop:Number = component.getStyle("paddingBottom");
			var paddingBottom:Number = component.getStyle("paddingBottom");
			var result:Number = component.height;
			if(!isNaN(result)) {
				if(!isNaN(paddingTop)) {
					result -= paddingTop;
				}
				if(!isNaN(paddingBottom)) {
					result -= paddingBottom;
				}
			}
			return result;
		}

		public static function getMaxAvailableWidth(component:UIComponent):Number {
			var paddingLeft:Number = component.getStyle("paddingLeft");
			var paddingRight:Number = component.getStyle("paddingRight");
			var result:Number = component.width;
			if(!isNaN(result)) {
				if(!isNaN(paddingLeft)) {
					result -= paddingLeft;
				}
				if(!isNaN(paddingRight)) {
					result -= paddingRight;
				}
			}
			return result;
		}
		
		public static function measureFixedCodeWidth(text:String):Number {
			var measure:TextLineMetrics = fixedCodeTextFormat.measureText(text);
			return measure.width;
		}
		
		public static function measureUnitWidth(text:String):Number {
			var measure:TextLineMetrics = unitTextFormat.measureText(text);
			return measure.width;
		}
		
		public static function measureGridHeaderWidth(text:String):Number {
			var measure:TextLineMetrics = gridHeaderFormat.measureText(text);
			return measure.width;
		}
		
		public static function isFocussed(component:UIComponent):Boolean {
			if ( component != null ) {
				var app:Application = FlexGlobals.topLevelApplication as Application;
				var focussed:UIComponent = app.focusManager.getFocus() as UIComponent;
				return focussed != null && ( focussed == component || isDescendantOf(component, focussed) );
			} else {
				return false;
			}
		}
		
		public static function moveFocus(backward:Boolean = false, focusManager:IFocusManager = null):Boolean {
			if ( focusManager == null ) {
				focusManager = FlexGlobals.topLevelApplication.focusManager;
			}
			var focussed:Boolean = false;
			var candidate:IFocusManagerComponent = focusManager.getNextFocusManagerComponent(backward);
			if ( candidate != null ) {
				var focussedComponent:IFocusManagerComponent = focusManager.getFocus();
				focusManager.setFocus(candidate);
				focussedComponent = focusManager.getFocus();
				focussed = focussedComponent == candidate;
			}
			return focussed;
		}

		public static function gridColumnDateTimeLabelFunction(item:Object, column:Object):String {
			var result:String = null;
			var value:* = org.openforis.collect.util.ObjectUtil.getValue(item, column.dataField);
			if ( value != null && value is Date ) {
				var date:Date = value as Date;
				result = _dataTimeFormatter.format(date);
			}
			return result;
		}
		
		public static function gridColumnStepLabelFunction(item:Object, column:Object):String {
			var dataField:String = column.dataField;
			var step:CollectRecord$Step = org.openforis.collect.util.ObjectUtil.getValue(item, dataField) as CollectRecord$Step;
			if ( step != null ) {
				switch ( step.name ) {
					case CollectRecord$Step.ENTRY.name:
						return Message.get('phase.short.ENTRY');
					case CollectRecord$Step.CLEANSING.name:
						return Message.get('phase.short.CLEANSING');
					case CollectRecord$Step.ANALYSIS.name:
						return Message.get('phase.short.ANALYSIS');
				}
			}
			return "";
		}
		
		public static function gridColumnNumberLabelFunction(item:Object, gridColumn:GridColumn):String {
			var dataField:String = gridColumn.dataField;
			var value:Object = org.openforis.collect.util.ObjectUtil.getValue(item, dataField);
			if ( value != null && !isNaN(Number(value)) ) {
				return value.toString();
			} else {
				return "";
			}
		}
		
		public static function gridColumnSortCompareFunction(propertyName:String, o1:Object, o2:Object):int {
			var value1:* = org.openforis.collect.util.ObjectUtil.getValue(o1, propertyName);
			var value2:* = org.openforis.collect.util.ObjectUtil.getValue(o2, propertyName);
			return mx.utils.ObjectUtil.compare(value1, value2);
		}
		
		public static function isDescendantOf(parent:DisplayObject, component:DisplayObject):Boolean {
			var currentComponent:DisplayObject = component;
			do {
				if ( currentComponent.hasOwnProperty("parent") ) {
					var currentParent:DisplayObject = currentComponent["parent"] as DisplayObject;
					if ( parent == currentParent ) {
						return true;
					}
					currentComponent = currentParent;
				} else {
					currentComponent = null;
				}
			} while (currentComponent != null);
			
			return false;
		}
		
	}
}