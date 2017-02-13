Collect.DataCleansing.MapPanelComposer = function(panel) {
	this.$panel = panel;
	this.map = null;
	this.initialized = false;
	this.surveyDataLoaded = false;
	this.dependenciesLoaded = false;
};

Collect.DataCleansing.MapPanelComposer.prototype.init = function(onComplete) {
	var $this = this;
	if ($this.dependenciesLoaded) {
		$this.onDependenciesLoaded(onComplete);
	} else {
		System.import('openlayers').then(function() {
			$this.dependenciesLoaded = true;
			$this.onDependenciesLoaded(onComplete);
		});
	}
}
	
Collect.DataCleansing.MapPanelComposer.prototype.onDependenciesLoaded = function(onComplete) {
	var $this = this;
	
	$(window).resize(function() {
		$this.resizeMapContainer();
	});
	
	var worldTopoMapTileLayer = new ol.layer.Tile({
		// World Topographic Map
    	title: 'Topographic map',
    	type: 'base',
    	visible: false,
        source: new ol.source.XYZ({
            attributions: [new ol.Attribution({
                html: 'Tiles © <a href="https://services.arcgisonline.com/ArcGIS/' +
                'rest/services/World_Topo_Map/MapServer">ArcGIS</a>'
        	})],
            url: 'https://server.arcgisonline.com/ArcGIS/rest/services/' +
                'World_Topo_Map/MapServer/tile/{z}/{y}/{x}'
        })
    });
	
	var satelliteMapTileLayer = new ol.layer.Tile({
    	title: 'Satellite (ESRI)',
    	type: 'base',
    	visible: true,
		// Esri_WorldImagery
        source: new ol.source.XYZ({
            attributions: [new ol.Attribution({
                html: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, ' +
				'AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
        	})],
            url: 'http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
        })
    });
	
	var openStreetMapTileLayer = new ol.layer.Tile({
		title: 'Open Street Map',
		type: 'base',
		visible: false,
		source: new ol.source.OSM()
    });
	
	var surveysOverlayGroup = new ol.layer.Group({
        title: 'Surveys',
        layers: [
        ]
    });

	$this.map = new ol.Map({
		target : 'map',
		layers : [
			new ol.layer.Group({
				title : 'Base maps',
				layers : [
					openStreetMapTileLayer,
					worldTopoMapTileLayer,
					satelliteMapTileLayer
				]
			}),
			surveysOverlayGroup
		],
		view : new ol.View({
			center : ol.proj.fromLonLat([ -121.1, 47.5 ]),
			zoom : 7
		})
	});

	var layerSwitcher = new ol.control.LayerSwitcher({
		tipLabel : 'Leggend' // Optional label for button
	});
	$this.map.addControl(layerSwitcher);
	
	collect.surveyService.loadFullPublishedSurveys(function(surveys) {
		surveys.forEach(function(jsonSurvey) {
			var survey = new Collect.Metamodel.Survey(jsonSurvey);
			var coordinateDataLayers = new Array();
			survey.traverse(function(nodeDef) {
				if (nodeDef.type == 'ATTRIBUTE' && nodeDef.attributeType == 'COORDINATE') {
					var dataLayer = new ol.layer.Vector({
						id: OF.Strings.format('{0}|{1}|data', survey.name, nodeDef.name),
						title: OF.Strings.firstNotBlank(nodeDef.label, nodeDef.name),
						visible: false,
						type: 'coordinate_data',
	                	 survey: survey,
	                	 node_definition: nodeDef
				    });
					coordinateDataLayers.push(dataLayer);
				}
			});
			
			var surveyGroup = new ol.layer.Group({
		        title: OF.Strings.firstNotBlank(survey.projectName, survey.name),
		        layers: [
	                 new ol.layer.Vector({
	                	 title: 'Sampling Points',
	                	 visible: false,
	                	 type: 'sampling_points',
	                	 survey: survey,
	                	 projection: 'EPSG:4326'
	                 }),
	                 new ol.layer.Group({
	                   	 title: 'Data',
	                   	 layers: coordinateDataLayers
                    })
                 ]
		    });
			
			function bindLayerEventListeners(layer) {
				layer.on('change:visible', $this.onTileVisibleChange);
			}

			surveyGroup.getLayers().forEach(function(layer) {
				bindLayerEventListeners(layer);
			});
			coordinateDataLayers.forEach(function(layer) {
				bindLayerEventListeners(layer);
			});
			
			surveysOverlayGroup.getLayers().push(surveyGroup);
		});
			
	}, function(){}, true);
	
	/*
	
	$this.baseMaps = {
			"Satellite (Esri)" : satelliteTileLayer
			, "OpenStreetMap" : openStreetMapTileLayer
	};
	
	$this.samplingPointsLayerGroup = L.layerGroup();
	
	$this.overlayMaps = {
			"Sampling Points" : $this.samplingPointsLayerGroup
	};
	
	satelliteTileLayer.addTo($this.map);
	
	$this.initialized = true;
	
	if (onComplete) {
		onComplete();
	}
	*/
	
	$this.initialized = true;
	
	if (onComplete) {
		onComplete();
	}
}

Collect.DataCleansing.MapPanelComposer.prototype.onTileVisibleChange = function(event) {
 	var tile = event.target;
 	if (tile.getVisible()) {
 		var survey = tile.get('survey');
 		
 		switch(tile.get('type')) {
 		case 'sampling_points':
 			collect.geoDataService.loadSamplingPointCoordinates(survey.name, 0, 1000000000,
				function(samplingPointItems) {
					var geojsonObject = {
						'type': 'FeatureCollection',
						'features': []
					};
					samplingPointItems.forEach(function(item) {
						geojsonObject.features.push({
							'type': 'Point',
							'coordinates': [item.y, item.x]
						});
						/*
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
						*/
					});
					 var vectorSource = new ol.source.Vector({
				        features: (new ol.format.GeoJSON()).readFeatures(geojsonObject),
				        format: new ol.format.GeoJSON({

				            defaultDataProjection : 'EPSG:4326', 
				            projection: 'EPSG:4326'

				         })
				      });
					 tile.setSource(vectorSource);
				});
 			break;
 		case 'coordinate_data':
 			
 			break;
 		}
 	}
	
};

Collect.DataCleansing.MapPanelComposer.prototype.reset = function() {
	//this.map.remove();
	this.init();
}

Collect.DataCleansing.MapPanelComposer.prototype.onPanelShow = function() {
	var $this = this;
	if ($this.map == null) {
		$this.resizeMapContainer();
		$this.init(function() {
			if (! $this.surveyDataLoaded) {
				//$this.onSurveyChanged();
			}
		});
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
	
	setTimeout(function() {
		L.control.layers($this.baseMaps, $this.overlayMaps).addTo($this.map);
	}, 1000);

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
									var circle = createCoordinateAttributeCircle(coordinateAttribute, value);
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
	
	function createCoordinateAttributeCircle(coordinateAttribute, point) {
		var circle = L.circle([ point.lat, point.lon ], 10, 
			{
				color : 'blue',
				fillColor : '#30f',
				fillOpacity : 0.5
			});
		
		circle.bindPopup(
				"<b>" + coordinateAttribute.label + "</b>" 
				+ "<br>" 
				+ "<b>record</b>: " + point.recordKeys 
				+ "<br>" 
				+ "latitude: " + point.lat 
				+ "<br>" 
				+ "longitude: " + point.lon 
				+ "<br>" 
				+ (isNaN(point.distanceToExpectedLocation) ? "" : "distance to expected location: " + 
						Math.round(point.distanceToExpectedLocation) + "m")
				+ "<br>"
				+ "<a href=\"javascript:void(0);\" onclick=\"Collect.DataCleansing.MapPanelComposer.openRecordEditPopUp(" + point.recordId + ", '" + point.recordKeys + "')\">Edit</a>"
				);
		return circle;
	}

	function determineSamplingPointCoordinateFillColor(level) {
		var percentage = 1 - 0.2 * (level - 1);
		var color_part_dec = percentage * 255;
		var color_part_hex = dec2hex(color_part_dec);
		var color = "#" + color_part_hex + color_part_hex + color_part_hex;
		return color;
	}

	function dec2hex(dec) {
		return Number(parseInt(dec, 10)).toString(16);
	}
};

Collect.DataCleansing.MapPanelComposer.openRecordEditPopUp = function(recordId, recordKeyString) {
	var modalContainer = $("#record-edit-modal");
	var modalContent = modalContainer.find(".modal-content");
	var recordKeyLabel = modalContainer.find(".record-key-label");
	var iframe = modalContainer.find("iframe");
	
	recordKeyLabel.text(recordKeyString);
	
	modalContent.resizable({
		alsoRezize: ".modal-body"
	}); 
	modalContent.draggable();
	
	function setInitialRecordEditPopUpSize() {
		iframe.prop("height", $(window).height() - 180);
	}
	
	function resizeIFrame() {
		iframe.prop("height", modalContent.height() - 100);
	}
	
	$(modalContainer).on("resize", resizeIFrame);
	
	modalContainer.on('show.bs.modal', function () {
		iframe.attr("src", "index.htm?edit=true"
				+ "&surveyId=" + collect.activeSurvey.id 
				+ "&recordId=" + recordId 
				+ "&locale=" + OF.i18n.currentLocale());
		$(this).find('.modal-body').css({
			'max-height':'100%'
		});
	});
	var options = {
		backdrop: "static",
		keyboard: false
	};
	modalContainer.modal(options);
	
	setInitialRecordEditPopUpSize();
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

