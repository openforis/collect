Collect.DataManager.MapPanelComposer = function(panel) {
	var $this = this;

	this.$panel = panel;
	this.map = null;
	this.initialized = false;
	this.dependenciesLoaded = false;
	
	this.verticalPadding = 220;
	this.horizontalPadding = 50;
	
	this.startLat = 12.1;
	this.startLon = 41.5;
	this.startZoom = 3;
}

Collect.DataManager.MapPanelComposer.prototype.init = function(onComplete) {
	var $this = this;
	
	$this.resizeMapContainer();
	
	this.popupContainer = $(
		'<div class="ol-popup">' +
			'<a href="#" class="ol-popup-closer"></a>' +
			'<div class="popup-content"></div>' +
		'</div>'
	);

	this.$panel.append(this.popupContainer);

	this.popupContent = this.popupContainer.find('.popup-content');
	
	/**
     * Create an overlay to anchor the popup to the map.
     */
	this.overlay = new ol.Overlay( /** @type {olx.OverlayOptions} */ ({
		element : $this.popupContainer[0],
		autoPan : true,
		autoPanAnimation : {
			duration : 250
		}
	}));

	/**
	 * Add a click handler to hide the popup.
	 * @return {boolean} Don't follow the href.
	 */
	var popupCloser = this.popupContainer.find('.ol-popup-closer');
	popupCloser.click(function() {
		$this.overlay.setPosition(undefined);
		popupCloser.blur();
		return false;
	});
	
	if ($this.dependenciesLoaded) {
		$this.onDependenciesLoaded(onComplete);
	} else {
		System.import('openlayers').then(function() {
			$this.dependenciesLoaded = true;
			$this.onDependenciesLoaded(onComplete);
		});
	}
}

Collect.DataManager.MapPanelComposer.prototype.onDependenciesLoaded = function(onComplete) {
	var $this = this;

	$(window).resize(function() {
		$this.resizeMapContainer();
	});

	var surveysOverlayGroup = new ol.layer.Group({
		title : 'Surveys',
		layers : []
	});

	$this.map = new ol.Map({
		target : 'map',
		layers : [
			$this.createBaseMapsLayer(),
			surveysOverlayGroup
		],
		view : new ol.View({
			center : ol.proj.fromLonLat([ $this.startLat, $this.startLon ]),
			zoom : $this.startZoom
		}),
		overlays : [ $this.overlay ],
	});

	var layerSwitcher = new ol.control.LayerSwitcher({
		tipLabel : 'Layers' // Optional label for button
	});
	$this.map.addControl(layerSwitcher);

	var displayFeatureInfo = function(pixel, coordinate) {
		var feature = $this.map.forEachFeatureAtPixel(pixel, function(feature) {
			return feature;
		});
		if (feature) {
			var htmlContent;
			
			switch (feature.get('type')) {
			case 'sampling_point':
				function printLevelCodes(levelCodes) {
					var result = "";
					for (var i = 0; i < levelCodes.length; i++) {
						result += "level " + (i + 1) + ": "
						+ levelCodes[i]
						+ "<br>";
					}
					return result;
				}
				var levelCodes = feature.get('name').split('|');
				htmlContent = OF.Strings.format(
						//TODO improve level codes formatting
						"<b>Sampling Point</b>"
						+ "<br>"
						+ "{0}"
						+ "latitude: {1}"
						+ "<br>"
						+ "longitude: {2}"
						+ "<br>"
						, printLevelCodes(levelCodes), coordinate[0], coordinate[1]);
				break;
			case 'coordinate_attribute_value':
				var point = feature.get('point');
				var survey = feature.get('survey');
				
				htmlContent = OF.Strings.format("<b>{0}</b>"
				+ "<br>"
				+ "<b>record</b>: {1}"
				+ "<br>"
				+ "latitude: {2}" 
				+ "<br>"
				+ "longitude: {3}"
				+ "<br>" 
				+ "{4}"
				+ "<br>"
				+ "<a href=\"javascript:void(0);\" onclick=\"Collect.DataManager.MapPanelComposer.openRecordEditPopUp({5}, {6}, '{7}')\">Edit</a>"
				, survey.getDefinition(point.attributeDefinitionId).label
				, point.recordKeys
				, point.lat
				, point.lon
				, (isNaN(point.distanceToExpectedLocation) ? "" : "distance to expected location: " +
						Math.round(point.distanceToExpectedLocation) + "m")
				, survey.id
				, point.recordId
				, point.recordKeys);
				break;
			}
			$this.popupContent.html(htmlContent);
			$this.overlay.setPosition(coordinate);
		}
	};

	$this.map.on('singleclick', function(evt) {
		displayFeatureInfo(evt.pixel, evt.coordinate);
	});

	collect.surveyService.loadFullPublishedSurveys(function(surveys) {
		surveys.forEach(function(jsonSurvey) {
			var survey = new Collect.Metamodel.Survey(jsonSurvey);
			var surveyGroup = $this.createSurveyLayerGroup(survey);
			surveysOverlayGroup.getLayers().push(surveyGroup);
		});
	}, function() {}, true);

	$this.initialized = true;

	if (onComplete) {
		onComplete();
	}
}

Collect.DataManager.MapPanelComposer.prototype.createSurveyLayerGroup = function(survey) {
	var $this = this;
	
	var coordinateDataLayers = new Array();
	survey.traverse(function(nodeDef) {
		if (nodeDef.type == 'ATTRIBUTE' && nodeDef.attributeType == 'COORDINATE') {
			var dataLayer = new ol.layer.Vector({
				title : OF.Strings.firstNotBlank(nodeDef.label, nodeDef.name),
				visible : false,
				type : 'coordinate_data',
				survey : survey,
				coordinate_attribute_def : nodeDef,
				projection : 'EPSG:4326',
				source : null,
				style : new ol.style.Style({
					image : new ol.style.Circle({
						fill : new ol.style.Fill({
							color : getRandomRGBColor().concat([1])
						}),
						radius : 5
					})
				})
			});
			coordinateDataLayers.push(dataLayer);
		}
	});

	var surveyGroup = new ol.layer.Group({
		title : OF.Strings.firstNotBlank(survey.projectName, survey.name),
		layers : [
			new ol.layer.Group({
				title : 'Data',
				layers : coordinateDataLayers
			}),
			new ol.layer.Vector({
				title : 'Sampling Points',
				visible : false,
				type : 'sampling_points',
				survey : survey,
				projection : 'EPSG:4326',
				style : new ol.style.Style({
					image : new ol.style.Circle({
						fill : new ol.style.Fill({
							color : getRandomRGBColor().concat([1])
						}),
						radius : 5
					})
				})
			})
		]
	});

	function bindLayerEventListeners(layer) {
		layer.on('change:visible', $.proxy($this.onTileVisibleChange, $this));
	}

	surveyGroup.getLayers().forEach(function(layer) {
		bindLayerEventListeners(layer);
	});
	coordinateDataLayers.forEach(function(layer) {
		bindLayerEventListeners(layer);
	});

	return surveyGroup;
};

Collect.DataManager.MapPanelComposer.prototype.createBaseMapsLayer = function() {
	var worldTopoMapTileLayer = new ol.layer.Tile({
		// World Topographic Map
		title : 'Topographic map',
		type : 'base',
		visible : false,
		source : new ol.source.XYZ({
			attributions : [ new ol.Attribution({
				html : 'Tiles © <a href="https://services.arcgisonline.com/ArcGIS/' +
					'rest/services/World_Topo_Map/MapServer">ArcGIS</a>'
			}) ],
			url : 'https://server.arcgisonline.com/ArcGIS/rest/services/' +
				'World_Topo_Map/MapServer/tile/{z}/{y}/{x}'
		})
	});

	var satelliteMapTileLayer = new ol.layer.Tile({
		title : 'Satellite (ESRI)',
		type : 'base',
		visible : true,
		// Esri_WorldImagery
		source : new ol.source.XYZ({
			attributions : [ new ol.Attribution({
				html : 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, ' +
					'AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
			}) ],
			url : 'http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
		})
	});

	var openStreetMapTileLayer = new ol.layer.Tile({
		title : 'Open Street Map',
		type : 'base',
		visible : false,
		source : new ol.source.OSM()
	});

	return new ol.layer.Group({
		title : 'Base maps',
		layers : [
			openStreetMapTileLayer,
			worldTopoMapTileLayer,
			satelliteMapTileLayer
		]
	});
}

Collect.DataManager.MapPanelComposer.prototype.onTileVisibleChange = function(event) {
	var $this = this;
	
	var tile = event.target;
	if (tile.getVisible()) {
		var survey = tile.get('survey');

		if (tile.getSource() == null) {
			switch (tile.get('type')) {
			case 'sampling_points':
				$this.createSamplingPointDataSource(survey, function(source) {
					tile.setSource(source);
				}, function(source) {
					$this.zoomToLayer(tile);
				});
				break;
			case 'coordinate_data':
				var coordinateAttributeDef = tile.get('coordinate_attribute_def');
				
				$this.createCoordinateDataSource(survey, coordinateAttributeDef, function(source) {
					tile.setSource(source);
				}, function(source) {
					$this.zoomToLayer(tile);
				});
				break;
			}
		} else {
			$this.zoomToLayer(tile);
		}
	}

};

Collect.DataManager.MapPanelComposer.prototype.zoomToLayer = function(tile) {
	$this = this;
	if (tile.getSource() != null) {
		var extent = tile.getSource().getExtent();
		if (extent.length > 0 && isFinite(extent[0])) {
			$this.map.getView().fit(extent, {
				maxZoom: 7
			});
		}
	}
}

Collect.DataManager.MapPanelComposer.prototype.createSamplingPointDataSource = function(survey, callback, readyCallback) {
	var url = OF.Strings.format("survey/{0}/sampling-point-data.kml", survey.id);
	var source = new ol.source.Vector({
		url : url,
		format : new ol.format.KML({
			extractStyles : false
		})
	});
	source.on('change', function(event) {
		if (source.getState() == 'ready') {
			source.forEachFeature(function(feature) {
				feature.set('type', 'sampling_point');
				feature.set('survey', survey);
			});
			readyCallback(source);
		}
	});
	callback(source);
};

Collect.DataManager.MapPanelComposer.prototype.createCoordinateDataSource = function(survey, coordinateAttributeDef, callback, readyCallback) {
	var step = 1;
	var rootEntityDefinitionId = survey.rootEntities[0].id;
	
	var source = new ol.source.Vector();
	
	collect.dataService.countRecords(survey.id, rootEntityDefinitionId, step, function(recordCount) {
		if (recordCount == 0) {
			return;
		}
		var batchSize = 200;
		var maxProcessableItems = 1000000000;
		var totalItems = Math.min(recordCount, maxProcessableItems);

		var jobDialog = new OF.UI.JobDialog();

		var startTime = new Date().getTime();

		var processCoordinateValue = function(coordinateAttributePoint) {
			var coordinateFeature = new ol.Feature({
				type : "coordinate_attribute_value",
				point : coordinateAttributePoint,
				survey : survey,
				geometry : new ol.geom.Point([ coordinateAttributePoint.lat, coordinateAttributePoint.lon ])
			});
			source.addFeature(coordinateFeature);
		};

		var processCoordinateValues = function(coordinateAttributePoints) {
			for (i = 0; i < coordinateAttributePoints.length; i++) {
				processCoordinateValue(coordinateAttributePoints[i]);
			}
			
			callback(source);
			
			if (batchProcessor.progressPercent == 100) {
				jobDialog.close();
				readyCallback(source);
			} else {
				var fakeProgressJob = {
					status : "RUNNING",
					elapsedTime : new Date().getTime() - startTime,
					remainingMinutes : 0,
					progressPercent : batchProcessor.progressPercent
				};
				jobDialog.updateUI(fakeProgressJob);
			}
		};

		var batchProcessor = new OF.Batch.BatchProcessor(totalItems, batchSize, function(blockOffset, callback) {
			collect.geoDataService.loadCoordinateValues(survey.id, step, coordinateAttributeDef.id, blockOffset, batchSize, processCoordinateValues);
		});

		jobDialog.cancelBtn.click(function() {
			batchProcessor.stop();
			jobDialog.close();
		});

		batchProcessor.start();
	});
};

function getRandomColor(minimum, maximum) {
	if (! min) {
		min = '#000000';
	}
	if (! max) {
		max = '#FFFFFF';
	}
	var result;
	do {
		result = '#'+('00000'+(Math.random()*(1<<24)|0).toString(16)).slice(-6);
	} while (result < min || result > max);
	return result;
}

function defaultIfNull(value, defaultValue) {
	return value ? value : defaultValue;
}

function getRandomRGBColor(rMin, rMax, gMin, gMax, bMin, bMax) {
	rMin = defaultIfNull(rMin, 0);
	rMax = defaultIfNull(rMax, 255);
	gMin = defaultIfNull(gMin, 0);
	gMax = defaultIfNull(gMax, 255);
	bMin = defaultIfNull(bMin, 0);
	bMax = defaultIfNull(bMax, 255);
	var result = [getRandomValue(rMin, rMax), getRandomValue(gMin, gMax), getRandomValue(bMin, bMax)];
	return result;
	
	function getRandomValue(min, max) {
		var result = 0;
		do {
			result = Math.random() * max;
		} while (result < min);
		return result;
	}
}

Collect.DataManager.MapPanelComposer.prototype.reset = function() {
	//this.map.remove();
	this.init();
}

Collect.DataManager.MapPanelComposer.prototype.onPanelShow = function() {
	var $this = this;
	if ($this.map == null) {
		$this.resizeMapContainer();
		$this.init();
	}
}

Collect.DataManager.MapPanelComposer.prototype.onSurveyChanged = function() {
}

Collect.DataManager.MapPanelComposer.prototype.resizeMapContainer = function() {
	$("#map").height($(window).height() - this.verticalPadding);
	$("#map").width($(window).width() - this.horizontalPadding);
}

Collect.DataManager.MapPanelComposer.openRecordEditPopUp = function(surveyId, recordId, recordKeyString) {
	var modalContainer = $("#record-edit-modal");
	var modalContent = modalContainer.find(".modal-content");
	var recordKeyLabel = modalContainer.find(".record-key-label");
	var iframe = modalContainer.find("iframe");

	recordKeyLabel.text(recordKeyString);

	modalContent.resizable({
		alsoRezize : ".modal-body"
	});
	modalContent.draggable();

	function setInitialRecordEditPopUpSize() {
		iframe.prop("height", $(window).height() - 180);
	}

	function resizeIFrame() {
		iframe.prop("height", modalContent.height() - 100);
	}

	$(modalContainer).on("resize", resizeIFrame);

	modalContainer.on('show.bs.modal', function() {
		iframe.attr("src", "index.htm?edit=true"
			+ "&surveyId=" + surveyId
			+ "&recordId=" + recordId
			+ "&locale=" + OF.i18n.currentLocale());
		$(this).find('.modal-body').css({
			'max-height' : '100%'
		});
	});
	var options = {
		backdrop : "static",
		keyboard : false
	};
	modalContainer.modal(options);

	setInitialRecordEditPopUpSize();
};