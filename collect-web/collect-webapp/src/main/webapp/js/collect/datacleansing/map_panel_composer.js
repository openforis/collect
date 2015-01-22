Collect.DataCleansing.MapPanelComposer = function(container) {
	this.container = container;
	
	this.init();
};

Collect.DataCleansing.MapPanelComposer.prototype.init = function() {
	var mapCanvas = this.container.find(".map-canvas")[0];
	var mapOptions = {
		//center : new google.maps.LatLng(44.5403, -78.5463),
		zoom : 8,
		mapTypeId : google.maps.MapTypeId.SATELLITE
	}
	var map = new google.maps.Map(mapCanvas, mapOptions);
	
	map.data.setStyle(function(feature) {
	    return {
	      icon: "assets/images/bullet-blue-small.png",
	      shape: ""
	    };
	});
	map.data.loadGeoJson('geo/data/samplingpoints.json');
	
	var bound = new google.maps.LatLngBounds();

//	for (i = 0; i < locations.length; i++) {
//	  bound.extend( new google.maps.LatLng(locations[i][1], locations[i][2]) );
//
//	}

};
