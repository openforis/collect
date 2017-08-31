import { UIModelObjectDefinition } from './UIModelObjectDefinition';
import { TabContainers } from './TabContainers';
import { AttributeDefinition, EntityDefinition } from '../Survey';

export class TableDefinition extends UIModelObjectDefinition {

    headingComponents = [];
    headingRows = [];
    headingColumns = [];
    entityDefinitionId;
    totalHeadingColumns;
    totalHeadingRows;
    row;
    column;
    columnSpan;
    
    constructor(id, parent) {
        super(id, parent);
    }
    
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        this.headingComponents = [];
        let jsonArrObj = jsonObj.headingComponents;
        for (var i = 0; i < jsonArrObj.length; i++) {
            var itemJsonObj = jsonArrObj[i];
            let item;
            switch(itemJsonObj.type) {
            case 'COLUMN_GROUP':
                item = new ColumnGroupDefinition(itemJsonObj.id, this);
                break;
            default:
                item = new ColumnDefinition(itemJsonObj.id, this);
            }
            item.fillFromJSON(itemJsonObj);
            this.headingComponents.push(item);
        }
        
        this.headingRows = this._extractHeadingRowsFromJson(jsonObj.headingRows);
        this.headingColumns = this._extractHeadingColumnsFromJson(jsonObj.headingColumns);
    }
    
    _extractHeadingRowsFromJson(jsonArr) {
        let rows = [];
        for (var i = 0; i < jsonArr.length; i++) {
            var jsonRow = jsonArr[i];
            let row = [];
            for (var j = 0; j < jsonRow.length; j++) {
                let jsonCol = jsonRow[j];
                let col = new ColumnDefinition(jsonCol.id, this);
                col.fillFromJSON(jsonCol);
                row.push(col);
            }
            rows.push(row);
        }
        return rows;
    }
    
    _extractHeadingColumnsFromJson(jsonArr): Array<ColumnDefinition> {
        let columns = [];
        for (var i = 0; i < jsonArr.length; i++) {
            var jsonCol = jsonArr[i];
            let col = new ColumnDefinition(jsonCol.id, this);
            col.fillFromJSON(jsonCol);
            columns.push(col);
        }
        return columns;
    }
    
    get entityDefinition() {
        return this.survey.schema.getDefinitionById(this.entityDefinitionId);
    }
}

export class TableHeadingComponentDefinition extends UIModelObjectDefinition {
    
    constructor(id, parent) {
        super(id, parent);
    }
}

export class ColumnGroupDefinition extends TableHeadingComponentDefinition {
    
    headingComponents = [];
    entityDefinitionId;
    label;
    
    constructor(id, parent) {
        super(id, parent);
    }
    
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        this.headingComponents = [];
        let jsonArrObj = jsonObj.headingComponents;
        for (var i = 0; i < jsonArrObj.length; i++) {
            var itemJsonObj = jsonArrObj[i];
            let item;
            switch(itemJsonObj.type) {
            case 'COLUMN_GROUP':
                item = new ColumnGroupDefinition(itemJsonObj.id, this);
                break;
            default:
                item = new ColumnDefinition(itemJsonObj.id, this);
            }
            item.fillFromJSON(itemJsonObj);
            this.headingComponents.push(item);
        }
    }
    
    get entityDefinition() {
        return this.survey.schema.getDefinitionById(this.entityDefinitionId);
    }
}

export class ColumnDefinition extends TableHeadingComponentDefinition {
    attributeDefinitionId;
    
    constructor(id, parent) {
        super(id, parent);
    }
    
    get attributeDefinition() {
        return this.survey.schema.getDefinitionById(this.attributeDefinitionId);
    }
}

