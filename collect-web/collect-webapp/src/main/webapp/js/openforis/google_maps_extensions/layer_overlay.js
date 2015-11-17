var LayerOverlay = function() {
	this.overlays = [];
}
LayerOverlay.prototype = new google.maps.OverlayView();
LayerOverlay.prototype.addOverlay = function(overlay) {
	this.overlays.push(overlay);
};
LayerOverlay.prototype.updateOverlays = function() {
	for (var i = 0; i < this.overlays.length; i++) {
		this.overlays[i].setMap(this.getMap());
	}
};
LayerOverlay.prototype.draw = function() {
};
LayerOverlay.prototype.onAdd = LayerOverlay.prototype.updateOverlays;
LayerOverlay.prototype.onRemove = LayerOverlay.prototype.updateOverlays;