Collect.Grids = {
};

Collect.Grids.createRootEntityKeyColumns = function(survey, rootEntity, sortable) {
	if (! rootEntity) {
		rootEntity = survey.rootEntities[0];
	}
	var columns = new Array();
	var keyDefs = survey.getKeyDefinitions(rootEntity)
	for (i = 0; i < keyDefs.length; i++) { 
		var def = keyDefs[i];
		columns.push({field: "key" + (i+1), title: def.label, width: 50, sortable: sortable == true});
	}
	return columns;
};

Collect.Grids.getDeleteColumnIconTemplate = function(label) {
	var content = OF.Strings.escapeHtml(label ? label: "");
	return '<span title="Delete" class="glyphicon glyphicon-remove-circle remove-icon">' + content + '</span>';
};

Collect.Grids.createDeleteColumn = function(onClickFunction, context) {
	return {
		formatter: Collect.Grids.getDeleteColumnIconTemplate(), 
		width: 30, 
		events: {
			"click .remove-icon": function(event, value, item, index) {
				onClickFunction.apply(context, [item]);
			}
		}
	}
};

Collect.Grids.createEditColumn = function(onClickFunction, context) {
	return {
		formatter: Collect.Grids.getEditColumnIconTemplate(), 
		width: 50, 
		align: "center",
		events: {
			"click .edit-icon": function(event, value, item, index) {
				onClickFunction.apply(context, [item]);
			}
		}
	}
};

Collect.Grids.getEditColumnIconTemplate = function(label) {
	var content = OF.Strings.escapeHtml(label ? label: "");
	return '<span title="Edit" class="glyphicon glyphicon-edit edit-icon">' + content + '</span>';
};

Collect.Grids.createDuplicateColumn = function(onClickFunction, context) {
	return {
		formatter: Collect.Grids.getDuplicateColumnIconTemplate(), 
		width: 30, 
		align: "center",
		events: {
			"click .duplicate-icon": function(event, value, item, index) {
				onClickFunction.apply(context, [item]);
			}
		}
	}
};

Collect.Grids.getDuplicateColumnIconTemplate = function(label) {
	var content = OF.Strings.escapeHtml(label ? label: "");
	return '<span title="Duplicate" class="glyphicon glyphicon-repeat duplicate-icon">' + content + '</span>';
};


