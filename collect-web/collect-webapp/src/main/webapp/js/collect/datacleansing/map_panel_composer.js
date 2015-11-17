Collect.DataCleansing.MapPanelComposer = function(container) {
	this.container = container;
	this.map = null;
};

Collect.DataCleansing.MapPanelComposer.prototype.init = function() {
	var mapCanvas = this.container.find(".map-canvas")[0];
	var mapOptions = {
		//center : new google.maps.LatLng(44.5403, -78.5463),
		center : new google.maps.LatLng(0, 0),
		zoom : 5,
		mapTypeId: google.maps.MapTypeId.TERRAIN
	};
	var map = new google.maps.Map(mapCanvas, mapOptions);
	
	map.data.setStyle(function(feature) {
	    return {
	      icon: "assets/images/bullet-blue-small.png",
	      shape: ""
	    };
	});
	//TODO
	
//	var kmlLayer = new google.maps.KmlLayer('http://127.0.0.1:8280/collect/geo/data/samplingpoints.kml',
//		{
//			suppressInfoWindows: false,
//			map: map
//		}
//	);
//	kmlLayer.setMap(map);
//	
//	 var kmlLayer = new google.maps.KmlLayer({
//		    url: 'http://kml-samples.googlecode.com/svn/trunk/kml/Placemark/placemark.kml',
//		    suppressInfoWindows: true,
//		    map: map
//		  });
//	
//	var bound = new google.maps.LatLngBounds();

//	for (i = 0; i < locations.length; i++) {
//	  bound.extend( new google.maps.LatLng(locations[i][1], locations[i][2]) );
//
//	}

	this.map = map;
	//TODO
	this.loadLayers();

//	collect.geoDataService.loadCoordinateValues(collect.activeSurvey.name, 3, 0, 10, function(lngLats) {
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
	
};

Collect.DataCleansing.MapPanelComposer.prototype.loadLayers = function() {
	var $this = this;
	var survey = collect.activeSurvey;
	var surveyName = survey.name;
	var coordinateAttrDefs = new Array();
	survey.traverse(function(nodeDef) {
		if (nodeDef instanceof Collect.Metamodel.AttributeDefinition && nodeDef.attributeType == "COORDINATE") {
			coordinateAttrDefs.push(nodeDef);
		}
	});
	var bounds = new google.maps.LatLngBounds();
	
	var rootEntityDefinitionId = survey.rootEntities[0].id;
	collect.dataService.countRecords(survey.id, rootEntityDefinitionId, 1, function(recordCount) {
		var blockSize = 100;
		var maxProcessableItems = 5000;
		var totalItems = Math.min(recordCount, maxProcessableItems);
		var blockProcessor = new BlockProcessor(totalItems, blockSize, function(blockOffset, callback) {
			coordinateAttrDefs.forEach(function(coordAttrDef) {
				var layerOverlay = new LayerOverlay();
				collect.geoDataService.loadCoordinateValues(surveyName, coordAttrDef.id, blockOffset, blockSize, function(lngLats) {
					lngLats.forEach(function(lngLatItem) {
						var latLng = new google.maps.LatLng(lngLatItem[1], lngLatItem[0]);
						var point = new google.maps.Circle({
							center : latLng,
							radius : 20000,
							strokeColor : "#0000FF",
							strokeOpacity : 0.8,
							strokeWeight : 2,
							fillColor : "#0000FF",
							fillOpacity : 0.4
						});
						layerOverlay.addOverlay(point);
						bounds.extend(latLng);
					});
					layerOverlay.setMap($this.map);
					$this.map.fitBounds(bounds);
					callback();
				});
			});
		});
		blockProcessor.start();
	});
};

BlockProcessor = function(totalItems, blockSize, processFn) {
	this.totalItems = totalItems;
	this.blockSize = blockSize;
	this.processFn = processFn;
	this.blocks = Math.ceil(totalItems / blockSize);
	this.currentBlockIndex = 0;
}

BlockProcessor.prototype = {
	start: function() {
		this.processNextBlockIfPossible();
	},
	processNextBlock: function() {
		var $this = this;
		var blockOffset = $this.currentBlockIndex * $this.blockSize;
		$this.processFn(blockOffset, function() {
			$this.currentBlockIndex++;
			$this.processNextBlockIfPossible();
		});
	},
	processNextBlockIfPossible: function() {
		if (this.currentBlockIndex < this.blocks) {
			this.processNextBlock();
		}
	}
};
