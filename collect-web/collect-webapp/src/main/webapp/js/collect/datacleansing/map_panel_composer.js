Collect.DataCleansing.MapPanelComposer = function(panel) {
	this.$panel = panel;
	this.map = null;
	this.initialized = false;
	this.surveyDataLoaded = false;
};

Collect.DataCleansing.MapPanelComposer.prototype.init = function() {
	var $this = this;
	
	$(window).resize(function() {
		$this.resizeMapContainer();
	});
	
	$this.map = L.map('map').setView([ 51.505, -0.09 ], 3);

	var satelliteTileLayer = L.tileLayer(
		// Esri_WorldImagery
		'http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
		{
			attribution : 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, ' +
					'AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
		}).addTo(this.map);
	
	var openStreetMapTileLayer = L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
		maxZoom: 19,
		attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
	});
	
	this.baseMaps = {
		"Satellite (Esri)" : satelliteTileLayer
		, "OpenStreetMap" : openStreetMapTileLayer
	};

	this.samplingPointsLayerGroup = L.layerGroup();

	this.overlayMaps = {
		"Sampling Points" : this.samplingPointsLayerGroup
	};
	
	this.initialized = true;
}

Collect.DataCleansing.MapPanelComposer.prototype.reset = function() {
	this.map.remove();
	this.init();
}

Collect.DataCleansing.MapPanelComposer.prototype.onPanelShow = function() {
	if (this.map == null) {
		this.resizeMapContainer();
		this.init();
		if (! this.surveyDataLoaded) {
			this.onSurveyChanged();
		}
	}
}

Collect.DataCleansing.MapPanelComposer.prototype.resizeMapContainer = function() {
	$("#map").height($(window).height() - 220);
	$("#map").width($(window).width() - 50);
}

Collect.DataCleansing.MapPanelComposer.prototype.onSurveyChanged = function() {
	if (this.initialized) {
		this.reset();
	} else {
		return;
	}
	var $this = this;
	collect.geoDataService.loadSamplingPointCoordinates(collect.activeSurvey.name, 0, 1000000000,
		function(samplingPointItems) {
			samplingPointItems.forEach(function(item) {
				var circle = L
						.circle([ item.y, item.x ],	10,
							{
								color : 'white',
								fillColor : determineSamplingPointCoordinateFillColor(item.level),
								fillOpacity : 0.5
							});
				circle.bindPopup("<b>Sampling Point</b>"
						+ "<br>" + printLevelCodes(item)
						+ "latitude: " + item.y + "<br>"
						+ "longitude: " + item.x + "<br>");
				$this.samplingPointsLayerGroup.addLayer(circle);

				function printLevelCodes(item) {
					var result = "";
					for (var i = 0; i < item.levelCodes.length; i++) {
						result += "level " + (i + 1) + ": "
								+ item.levelCodes[i]
								+ "<br>";
					}
					return result;
				}
			});
		});

	var coordinateAttributes = [];
	collect.activeSurvey.traverse(function(node) {
		if (node instanceof Collect.Metamodel.AttributeDefinition
				&& node.attributeType == 'COORDINATE') {
			coordinateAttributes.push(node);
		}
	});

	var coordinateAttributeLayers = [];
	for (i = 0; i < coordinateAttributes.length; i++) {
		var coordinateAttribute = coordinateAttributes[i];
		var coordinateAttributeLayer = L.layerGroup();
		coordinateAttributeLayers.push(coordinateAttributeLayer);
		this.overlayMaps[coordinateAttribute.label] = coordinateAttributeLayer;
	}

	L.control.layers(this.baseMaps, this.overlayMaps).addTo(this.map);

	this.surveyDataLoaded = true;
	
	this.map.on('overlayadd', function(e) {
		if (OF.Arrays.contains(coordinateAttributeLayers, e.layer)) {
			if (e.layer.getLayers().length == 0) {
				var index = coordinateAttributeLayers.indexOf(e.layer);
				var coordinateAttribute = coordinateAttributes[index];
				
				var step = 1;
				var rootEntityDefinitionId = collect.activeSurvey.rootEntities[0].id;
				collect.dataService.countRecords(collect.activeSurvey.id, rootEntityDefinitionId, step, function(recordCount) {
					var blockSize = 200;
					var maxProcessableItems = 1000000000;
					var totalItems = Math.min(recordCount, maxProcessableItems);
					
					var jobDialog = new OF.UI.JobDialog();

					var startTime = new Date().getTime();
					
					var blockProcessor = new BlockProcessor(totalItems, blockSize, function(blockOffset, callback) {
						collect.geoDataService.loadCoordinateValues(
							collect.activeSurvey.name, step, coordinateAttribute.id, blockOffset, blockSize, function(coordinateValues) {
								for (i = 0; i < coordinateValues.length; i++) {
									var value = coordinateValues[i];
									var circle = L.circle([ value.lat, value.lon ], 10, 
										{
											color : 'blue',
											fillColor : '#30f',
											fillOpacity : 0.5
										});
									circle.bindPopup(
											"<b>" + coordinateAttribute.label + "</b>" 
											+ "<br>" 
											+ "<b>record</b>: " + value.recordKeys 
											+ "<br>" 
											+ "latitude: " + value.lat 
											+ "<br>" 
											+ "longitude: " + value.lon 
											+ "<br>" 
											+ (isNaN(value.distanceToExpectedLocation) ? "" : "distance to expected location: " + 
													Math.round(value.distanceToExpectedLocation) + "m")
											);
									e.layer.addLayer(circle);
								}
								
								if (blockProcessor.progressPercent == 100) {
									jobDialog.close();
								} else {
									var fakeProgressJob = {
										status: "RUNNING"
										, elapsedTime: new Date().getTime() - startTime
										, remainingMinutes: 0
										, progressPercent: blockProcessor.progressPercent
									};
									jobDialog.updateUI(fakeProgressJob);
								}
								callback();
							});
					});
					
					jobDialog.cancelBtn.click(function() {
						blockProcessor.stop();
						jobDialog.close();
					});
					
					blockProcessor.start();
				});
			}
		}
	});

	function determineSamplingPointCoordinateFillColor(level) {
		var percentage = 1 - 0.2 * (level - 1);
		var color_part_dec = percentage * 255;
		var color_part_hex = dec2hex(color_part_dec);
		var color = "#" + color_part_hex + color_part_hex + color_part_hex;
	}

	function dec2hex(dec) {
		return Number(parseInt(dec, 10)).toString(16);
	}
};

BlockProcessor = function(totalItems, blockSize, processFn) {
	this.totalItems = totalItems;
	this.blockSize = blockSize;
	this.processFn = processFn;
	this.blocks = Math.ceil(totalItems / blockSize);
	this.nextBlockIndex = 0;
	this.progressPercent = 0;
	this.running = false;
}

BlockProcessor.prototype = {
	start: function() {
		this.running = true;
		this.processNextBlockIfPossible();
	},
	stop: function() {
		this.running = false;
	},
	processNextBlock: function() {
		var $this = this;
		$this.progressPercent = Math.floor((100 * ($this.nextBlockIndex + 1)) / $this.blocks);
		var blockOffset = $this.nextBlockIndex * $this.blockSize;
		$this.processFn(blockOffset, function() {
			if ($this.running) {
				$this.nextBlockIndex++;
				$this.processNextBlockIfPossible();
			}
		});
	},
	processNextBlockIfPossible: function() {
		if (this.nextBlockIndex < this.blocks) {
			this.processNextBlock();
		} else {
			this.running = false;
		}
	}
};

