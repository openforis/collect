Collect.DataCleansing.DataQueryPanelController = function($panel) {
	Collect.AbstractItemPanel.apply(this, [$panel, "Data Query", 
			Collect.DataQueryDialogController,
			collect.dataQueryService, 
			Collect.DataCleansing.DATA_QUERY_DELETED]);
};

Collect.DataCleansing.DataQueryPanelController.prototype = Object.create(Collect.AbstractItemPanel.prototype);

Collect.DataCleansing.DataQueryPanelController.prototype.initDataGrid = function() {
	var $this = this;
	var gridContainer = $("#dataquerygrid");
	
	var getPrettyNodeName = function(nodeId) {
		if (! nodeId) {
			return null;
		}
		var survey = collect.activeSurvey;
		var def = survey.getDefinition(nodeId);
		return "[" + def.name + "] " + def.label;
	};
	
	var nodePrettyNameSorter = function (a, b) {
		var aName = getPrettyNodeName(a);
		var bName = getPrettyNodeName(b);
		if (aName == null && bName == null) {
			return 0;
		} else if (aName == null) {
			return -1;
		} else if (bName == null) {
			return 1;
		} else if (aName > bName) {
			return 1;
		} else if (aName < bName) {
			return -1;
		} else {
			return 0;
		}
	};
	
	gridContainer.bootstrapTable({
	    url: "datacleansing/dataqueries/list.json",
	    cache: false,
	    clickToSelect: true,
	    singleSelect: true,
	    detailView: true,
	    detailFormatter: detailFormatter,
	    height: 400,
	    width: "950px",
	    columns: [
	        {field: "id", title: "Id", visible: false},
          	{field: "selected", title: "", radio: true},
			{field: "title", title: "Title", sortable: true, width: "40%"},
			{field: "entityDefinitionId", title: "Entity", sortable: true, width: "20%",  
				sorter: nodePrettyNameSorter, formatter: getPrettyNodeName},
			{field: "attributeDefinitionId", title: "Attribute", sortable: true, width: "20%", 
				sorter: nodePrettyNameSorter, formatter: getPrettyNodeName},
			{field: "creationDate", title: "Creation Date", sortable: true, width: "10%", 
				formatter: OF.Dates.formatToPrettyDateTime},
			{field: "modifiedDate", title: "Modified Date", sortable: true, width: "10%", 
				formatter: OF.Dates.formatToPrettyDateTime},
			$this.createGridItemDeleteColumn()
		]
	});
	
	function detailFormatter(index, query) {
		var html = [];
		html.push('<p><b>Conditions:</b> ' + query.conditions + '</p>');
		html.push('<p><b>Description:</b> ' + query.description + '</p>');
		return html.join('');
	}
	
	$this.dataGrid = gridContainer.data('bootstrap.table');
};
