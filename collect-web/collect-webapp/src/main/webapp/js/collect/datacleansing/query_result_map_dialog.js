Collect.DataCleansing.QueryResultMapDialog = function(container, queryId) {
	this.container = container;
	this.queryId = queryId;
	this.map = null;
};

Collect.DataCleansing.QueryResultMapDialog.prototype.init = function() {
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
	
	this.map = map;

	this.loadLayers();
};

Collect.DataCleansing.QueryResultMapDialog.prototype.loadLayers = function() {
	var $this = this;
	var survey = collect.activeSurvey;
	var surveyId = survey.id;
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
				collect.geoDataService.loadCoordinateValues(surveyId, coordAttrDef.id, blockOffset, blockSize, function(lngLats) {
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
