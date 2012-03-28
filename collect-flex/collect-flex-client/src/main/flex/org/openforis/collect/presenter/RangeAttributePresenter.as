package org.openforis.collect.presenter {
	import mx.collections.IList;
	
	import org.openforis.collect.client.UpdateRequestToken;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.metamodel.proxy.RangeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.UnitProxy;
	import org.openforis.collect.remoting.service.UpdateResponse;
	import org.openforis.collect.ui.component.input.RangeAttributeRenderer;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class RangeAttributePresenter extends CompositeAttributePresenter {
		
		public function RangeAttributePresenter(view:RangeAttributeRenderer) {
			_view = view;
			super(view);
			initUnits();
		}
		
		override protected function updateResponseReceivedHandler(event:ApplicationEvent):void {
			super.updateResponseReceivedHandler(event);
			/*
			if(_view.attribute != null) {
				var responses:IList = IList(event.result);
				var token:UpdateRequestToken = event.token as UpdateRequestToken;
				if(token != null && CollectionUtil.contains(token.updatedFields, view.rangeInputField)) {
					for each (var response:UpdateResponse in responses) {
						if(response.nodeId == _view.attribute.id) {
							var rangeFrom:Number = response.updatedFieldValues.get(0);
							if(!isNaN(rangeFrom)) {
							}
						}
					}
				}
			}
			*/
		}
		
		private function get view():RangeAttributeRenderer {
			return RangeAttributeRenderer(_view);
		}
		
		protected function initUnits():void {
			var rangeAttrDefn:RangeAttributeDefinitionProxy = RangeAttributeDefinitionProxy(view.attributeDefinition);
			var units:IList = rangeAttrDefn.units;
			if(units.length > 0) {
				if(units.length == 1) {
					view.currentState = RangeAttributeRenderer.SINGLE_UNIT_STATE;
					var unit:UnitProxy = UnitProxy(units.getItemAt(0));
					view.unitLabel.text = unit.name;
				} else {
					view.currentState = RangeAttributeRenderer.MULTIPLE_UNIT_STATE;
					view.unitDropDownList.dataProvider = units;
					if(rangeAttrDefn.defaultUnit != null) {
						view.unitDropDownList.defaultValue = rangeAttrDefn.defaultUnit.name;
					}
				}
			}
		}
	}
}
