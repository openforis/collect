package org.openforis.collect.ui.component.detail
{
	import mx.core.UIComponent;
	
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 * */
	public class RelevanceDisplayManager {
		
		public static const STYLE_NAME_NOT_RELEVANT:String = "notRelevant";
		
		/**
		 * Display of the error (stylename "error" or "warning" will be set on this component)
		 */
		private var _display:UIComponent;
		
		public function RelevanceDisplayManager(display:UIComponent) {
			this._display = display;
		}
		
		public function displayNodeRelevance(parentEntity:EntityProxy, defn:NodeDefinitionProxy):void {
			if(parentEntity != null && defn != null) {
				var name:String = defn.name;
				var relevant:Boolean = parentEntity.childrenRelevanceMap.get(name);
				apply(relevant);
			}
		}
		
		public function reset():void {
			UIUtil.removeStyleNames(_display, [
				STYLE_NAME_NOT_RELEVANT
			]);
		}
		
		public function apply(relevant:Boolean):void {
			UIUtil.toggleStyleName(_display, STYLE_NAME_NOT_RELEVANT, ! relevant);
		}
		
	}
}