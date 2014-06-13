package org.openforis.collect.util {
	import flash.display.DisplayObject;
	import flash.geom.Rectangle;
	
	import mx.collections.IList;
	import mx.controls.ToolTip;
	import mx.core.IToolTip;
	import mx.core.IUIComponent;
	import mx.managers.ToolTipManager;
	
	import org.openforis.collect.i18n.Message;
	
	/**
	 * @author M. Togna
	 * @author S. Ricci
	 * */
	public final class ToolTipUtil {
		
		public static const STYLE_NAME_ERROR:String = "error";
		public static const STYLE_NAME_WARNING:String = "warning";
		public static const STYLE_NAME_WARNING_CONFIRMED_ERROR:String = "warningConfirmedError";
		
		public static function create(object:DisplayObject, messages:IList, styleName:String = STYLE_NAME_ERROR):ToolTip {
			var pt:Rectangle = object.stage.getBounds(object);
			var yPos:Number = -(pt.y);
			var xPos:Number = -(pt.x);
			
			var multipleMessages:Boolean = messages.length > 1;
			var title:String = getValidationToolTipTitle(styleName, multipleMessages);
			var content:String = getValidationToolTipContent(title, messages);
			
			var toolTip:ToolTip = ToolTipManager.createToolTip(content, xPos + object.width, yPos, null, object as IUIComponent) as ToolTip;
			toolTip.styleName = styleName;
			
			var position:String;
			if(PopUpUtil.exceedViewport(toolTip)) {
				position = PopUpUtil.alignToField(toolTip, object, PopUpUtil.POSITION_RIGHT);
			} else {
				position = PopUpUtil.POSITION_RIGHT;
			}
			var borderStyle:String = getBorderStyle(position);
			toolTip.setStyle("borderStyle", borderStyle);
			return toolTip;
		}
		
		private static function getValidationToolTipTitle(styleName:String, multipleMessages:Boolean = false):String {
			var title:String;
			switch ( styleName ) {
				case STYLE_NAME_ERROR:
					if(multipleMessages) {
						title = Message.get("edit.validationToolTipTitle.errors");
					} else {
						title = Message.get("edit.validationToolTipTitle.error");
					}
					break;
				case STYLE_NAME_WARNING:
					if(multipleMessages) {
						title = Message.get("edit.validationToolTipTitle.warnings");
					} else {
						title = Message.get("edit.validationToolTipTitle.warning");
					}
					break;
				case STYLE_NAME_WARNING_CONFIRMED_ERROR:
					if(multipleMessages) {
						title = Message.get("edit.validationToolTipTitle.warningsConfirmedError");
					} else {
						title = Message.get("edit.validationToolTipTitle.warningConfirmedError");
					}
					break;
			}
			return title;
		}
		
		private static function getValidationToolTipContent(title:String, messages:IList):String {
			var content:String = title;
			if(messages.length == 1) {
				content += " " + messages[0];
			} else {
				for each (var message:String in messages) {
					content += "\n - " + message;
				}
			}
			return content;
		}
		
		public static function destroy(toolTip:IToolTip):void{
			if(toolTip != null){
				ToolTipManager.destroyToolTip(toolTip);
			}
		}
		
		private static function getBorderStyle(position:String):String {
			switch(position) {
				case PopUpUtil.POSITION_LEFT:
					return "tipLeft";
				case PopUpUtil.POSITION_ABOVE:
					return "tipAbove";
				case PopUpUtil.POSITION_BELOW:
					return "tipBelow";
				case PopUpUtil.POSITION_RIGHT:
				default:
					return "tipRight";
					
			}
		}
		
	}
}