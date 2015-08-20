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

Collect.Grids.createDeleteColumn = function(deleteFunction, context) {
	return {
		formatter: '<span class="glyphicon glyphicon-remove-circle" style="cursor: pointer"></span>', 
		width: 30, 
		events: {
			"click span": function(event, value, item, index) {
				deleteFunction.apply(context, [item]);
			}
		}
	}
};