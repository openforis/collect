Collect.DataManager.MapPageComposer = function() {
	
}

Collect.DataManager.MapPageComposer.prototype.init = function() {
	this.mapPanelComposer = new Collect.DataManager.MapPanelComposer($("#map-panel"));
	
	this.mapPanelComposer.verticalPadding = 130;
	this.mapPanelComposer.horizontalPadding = 0;
	
	this.mapPanelComposer.init();
}

$(function() {
	collect.datamanager.map_page_composer = new Collect.DataManager.MapPageComposer();
	collect.datamanager.map_page_composer.init();
});