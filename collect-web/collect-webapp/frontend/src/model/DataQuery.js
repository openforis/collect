export class RDBQuery {
	surveyName
	recordStep
	contextEntityDefinitionId
	columns = []
	filter = []
	page = 1
	recordsPerPage = 20
	sortBy = []
}

export class QueryComponent {
	id
	attributeDefinition
	filterCondition
	
	constructor(id, attributeDef) {
		this.id = id
		this.attributeDefinition = attributeDef
	}
}

export class FilterCondition {
	type = null
	minValue = null
	maxValue = null
	values = null
	value = null

	get empty() {
		switch(this.type) {
			case 'IN':
				return this.values === null || this.values.length === 0
			case 'BETWEEN':
				return this.min === null && this.max === null
			default:
				return this.value === null
		}
	}
}