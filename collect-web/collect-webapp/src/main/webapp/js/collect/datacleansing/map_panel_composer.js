Collect.DataCleansing.MapPanelComposer = function(container) {
	this.container = container;
	this.map = null;
};

Collect.DataCleansing.MapPanelComposer.prototype.init = function() {
	var $this = this;
	
	$(window).resize(function() {
		$this.resizeDataGrid();
	});
	
	this.map = L.map('map').setView([ 51.505, -0.09 ], 13);

	var satelliteTileLayer = L.tileLayer(
		// Esri_WorldImagery
		'http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
		{
			attribution : 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, '
					+ 'AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
		}).addTo(this.map);

	this.baseMaps = {
		"Satellite" : satelliteTileLayer
	};

	this.samplingPointsLayerGroup = L.layerGroup();

	this.overlayMaps = {
		"Sampling Points" : this.samplingPointsLayerGroup
	};
}

Collect.DataCleansing.MapPanelComposer.prototype.resizeDataGrid = function() {
	$("#map").height($(window).height() - 250);
	$("#map").width($(window).width() - 50);
}

Collect.DataCleansing.MapPanelComposer.prototype.refreshDataGrid = function() {
	var $this = this;
	collect.geoDataService.loadSamplingPointCoordinates(collect.activeSurvey.name, 0, 10,
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

	this.map.on('overlayadd', function(e) {
		if (OF.Arrays.contains(coordinateAttributeLayers, e.layer)) {
			if (e.layer.getLayers().length == 0) {
				var index = coordinateAttributeLayers.indexOf(e.layer);
				var coordinateAttribute = coordinateAttributes[index];
				collect.geoDataService.loadCoordinateValues(
					collect.activeSurvey.name, 1, coordinateAttribute.id, 0,
					10, function(coordinateValues) {
						for (i = 0; i < coordinateValues.length; i++) {
							var value = coordinateValues[i];
							var circle = L.circle([ value.lat, value.lon ], 10,
								{
									color : 'blue',
									fillColor : '#30f',
									fillOpacity : 0.5
								});
							circle.bindPopup("<b>" + coordinateAttribute.label
									+ "</b>" + "<br>" + "record: "
									+ value.recordKeys + "<br>" + "latitude: "
									+ value.lat + "<br>" + "longitude: "
									+ value.lon + "<br>");
							e.layer.addLayer(circle);
						}
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

