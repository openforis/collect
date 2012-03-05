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
		
		private var _displayStyleName:String;
		
		public function RelevanceDisplayManager(display:UIComponent) {
			this._display = display;
		}
		
		public function displayNodeRelevance(parentEntity:EntityProxy, defn:NodeDefinitionProxy):void {
			_displayStyleName = "";
			if(parentEntity != null && defn != null) {
				var name:String = defn.name;
				var relevant:Boolean = parentEntity.childrenRelevanceMap.get(name);
				if(! relevant) {
					_displayStyleName += " " + STYLE_NAME_NOT_RELEVANT;
				}
			}
			apply();
		}
		
		public function reset():void {
			UIUtil.removeStyleNames(_display, [
				STYLE_NAME_NOT_RELEVANT
			]);
		}
		
		protected function apply():void {
			reset();
			if(_displayStyleName != null) {
				UIUtil.addStyleName(_display, _displayStyleName);
			}
		}
		
	}
}