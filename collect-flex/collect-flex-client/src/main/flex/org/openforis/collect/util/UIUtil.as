package org.openforis.collect.util
{
	import mx.containers.ViewStack;
	import mx.controls.TextInput;
	import mx.core.Container;
	import mx.core.IVisualElement;
	import mx.core.UIComponent;
	
	import spark.components.NavigatorContent;
	import spark.components.Scroller;
	
	/**
	 * @author S. Ricci
	 */
	public class UIUtil {
		
		public static const LAYOUT_FORM:String = "form";
		public static const LAYOUT_TABLE:String = "table";
		
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
			if(scroller != null) {
				var elementToShow:IVisualElement = field as IVisualElement;
				if(elementToShow == null) {
					elementToShow = getFirstAncestor(field, IVisualElement);
				}
				if(elementToShow != null) {
					scroller.ensureElementIsVisible(elementToShow);
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
			var result:String = cleanedStyle + " " + newStylesConcat;
			if(applyImmediately) {
				component.styleName = result;
			}
			return result;
		}
		
	}
}