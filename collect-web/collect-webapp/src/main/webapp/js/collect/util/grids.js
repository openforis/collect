Collect.Grids = {
};

Collect.Grids.createRootEntityKeyColumns = function(survey, rootEntity) {
	if (! rootEntity) {
		rootEntity = survey.rootEntities[0];
	}
	var columns = new Array();
	var keyDefs = survey.getKeyDefinitions(rootEntity)
	for (i = 0; i < keyDefs.length; i++) { 
		var def = keyDefs[i];
		columns.push({field: "key" + (i+1), title: def.label, width: 50});
	}
	return columns;
};