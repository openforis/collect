package org.openforis.collect.util
{
	import org.openforis.collect.model.proxy.AttributeSymbol;

	public class ReasonBlankUtil {
		
		public static const SHORTCUT_BLANK_ON_FORM:String = "*";
		public static const SHORTCUT_DASH_ON_FORM:String = "-";
		public static const SHORTCUT_ILLEGIBLE:String = "?";
		
		public static function getShortCut(symbol:AttributeSymbol):String {
			switch(symbol) {
				case AttributeSymbol.BLANK_ON_FORM:
					return SHORTCUT_BLANK_ON_FORM;
				case AttributeSymbol.DASH_ON_FORM:
					return SHORTCUT_DASH_ON_FORM;
				case AttributeSymbol.ILLEGIBLE:
					return SHORTCUT_ILLEGIBLE;
				default:
					return null;
			}
		}
		
		public static function parseShortCut(text:String):AttributeSymbol {
			switch(text) {
				case SHORTCUT_BLANK_ON_FORM:
					return AttributeSymbol.BLANK_ON_FORM;
				case SHORTCUT_DASH_ON_FORM:
					return AttributeSymbol.DASH_ON_FORM;
				case SHORTCUT_ILLEGIBLE:
					return AttributeSymbol.ILLEGIBLE;
				default:
					return null;
			}
		}
		
		public static function isShortCut(text:String):Boolean {
			return ArrayUtil.isIn([SHORTCUT_BLANK_ON_FORM, SHORTCUT_DASH_ON_FORM, SHORTCUT_ILLEGIBLE], text);
		}
	}
}