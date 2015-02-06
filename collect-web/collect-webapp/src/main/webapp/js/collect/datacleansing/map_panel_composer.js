Collect.DataCleansing.MapPanelComposer = function(container) {
	this.container = container;
	this.map = null;
};

Collect.DataCleansing.MapPanelComposer.prototype.init = function() {
	var mapCanvas = this.container.find(".map-canvas")[0];
	var mapOptions = {
		center : new google.maps.LatLng(0, 0),
		zoom : 8,
		mapTypeId : google.maps.MapTypeId.SATELLITE
	}
	var map = new google.maps.Map(mapCanvas, mapOptions);
	
	map.data.loadGeoJson('geo/data/samplingpoints.json');
	
	map.data.setStyle(function(feature) {
	    return {
	      icon: "assets/images/bullet-blue-small.png",
	      shape: ""
	    };
	});
	
	this.map = map;
	
//	collect.geoDataService.loadCoordinateValues(3341, 0, 10, function(lngLats) {
//		lngLats.forEach(function(lngLat) {
//			var point = new google.maps.Circle({
//				center : new google.maps.LatLng(lngLat[1], lngLat[0]),
//				radius : 20000,
//				strokeColor : "#0000FF",
//				strokeOpacity : 0.8,
//				strokeWeight : 2,
//				fillColor : "#0000FF",
//				fillOpacity : 0.4
//			});
//			point.setMap(map);
//		});
//	});
	
	this.loadLayers();
};

Collect.DataCleansing.MapPanelComposer.prototype.loadLayers = function() {
	var $this = this;
	var surveyName = collect.activeSurvey.name;
	var coordinateAttrDefs = new Array();
	collect.activeSurvey.traverse(function(nodeDef) {
		if (nodeDef.type == "ATTRIBUTE" && nodeDef.attributeType == "COORDINATE") {
			coordinateAttrDefs.push(nodeDef);
		}
	});
	var bounds = new google.maps.LatLngBounds();
	
	coordinateAttrDefs.forEach(function(coordAttrDef) {
		var layerOverlay = new LayerOverlay();
		collect.geoDataService.loadCoordinateValues(surveyName, coordAttrDef.id, 0, 1000000, function(lngLats) {
			lngLats.forEach(function(lngLatItem) {
				var latLng = new google.maps.LatLng(lngLatItem[1], lngLatItem[0]);
				var point = new google.maps.Circle({
					center : latLng,
					radius : 200,
					strokeColor : "#00FF00",
					strokeOpacity : 0.8,
					strokeWeight : 2,
					fillColor : "#00FF00",
					fillOpacity : 0.4,
					map: $this.map
				});
				layerOverlay.addOverlay(point);
				bounds.extend(latLng);
			});
		});
		$this.map.fitBounds(bounds);
		layerOverlay.setMap($this.map);
		
//		var layer = new google.maps.KmlLayer("http://168.202.56.188:8080/collect/geo/data/" + surveyName + "/" + coordAttrDef.id + "/coordinates.kml?_r=" + new Date().getTime(),
//			{
//		        suppressInfoWindows: true,
//		        map: $this.map
//		    }
//		);
	});
};
