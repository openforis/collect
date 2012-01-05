package org.openforis.collect.util {
	import org.granite.collections.BasicMap;
	
	public class ModelClassInitializer {
		
		import org.openforis.idm.metamodel.Annotatable;
		import org.openforis.idm.metamodel.AttributeDefault;
		import org.openforis.idm.metamodel.AttributeDefinition;
		import org.openforis.idm.metamodel.BooleanAttributeDefinition;
		import org.openforis.idm.metamodel.Check;
		import org.openforis.idm.metamodel.Check$Flag;
		import org.openforis.idm.metamodel.CodeAttributeDefinition;
		import org.openforis.idm.metamodel.CodeAttributeDefinition$Type;
		import org.openforis.idm.metamodel.CodeList;
		import org.openforis.idm.metamodel.CodeList$CodeScope;
		import org.openforis.idm.metamodel.CodeList$CodeType;
		import org.openforis.idm.metamodel.CodeListItem;
		import org.openforis.idm.metamodel.CodeListLabel;
		import org.openforis.idm.metamodel.CodeListLabel$Type;
		import org.openforis.idm.metamodel.CodeListLevel;
		import org.openforis.idm.metamodel.CodingScheme;
		import org.openforis.idm.metamodel.ComparisonCheck;
		import org.openforis.idm.metamodel.ComparisonCheck$OPERATION;
		import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
		import org.openforis.idm.metamodel.CustomCheck;
		import org.openforis.idm.metamodel.DateAttributeDefinition;
		import org.openforis.idm.metamodel.DetachedModelDefinitionException;
		import org.openforis.idm.metamodel.DistanceCheck;
		import org.openforis.idm.metamodel.EntityDefinition;
		import org.openforis.idm.metamodel.FileAttributeDefinition;
		import org.openforis.idm.metamodel.InvalidCheckException;
		import org.openforis.idm.metamodel.LanguageSpecificText;
		import org.openforis.idm.metamodel.ModelVersion;
		import org.openforis.idm.metamodel.NodeDefinition;
		import org.openforis.idm.metamodel.NodeDefinitionLabel;
		import org.openforis.idm.metamodel.NodeDefinitionLabel$Type;
		import org.openforis.idm.metamodel.NodeDefinitionPropertyHandler;
		import org.openforis.idm.metamodel.NumberAttributeDefinition;
		import org.openforis.idm.metamodel.NumericAttributeDefinition;
		import org.openforis.idm.metamodel.NumericAttributeDefinition$Type;
		import org.openforis.idm.metamodel.PatternCheck;
		import org.openforis.idm.metamodel.Precision;
		import org.openforis.idm.metamodel.Prompt;
		import org.openforis.idm.metamodel.Prompt$Type;
		import org.openforis.idm.metamodel.RangeAttributeDefinition;
		import org.openforis.idm.metamodel.Schema;
		import org.openforis.idm.metamodel.SchemaExpression;
		import org.openforis.idm.metamodel.SchemaPropertyHandler;
		import org.openforis.idm.metamodel.SpatialReferenceSystem;
		import org.openforis.idm.metamodel.Survey;
		import org.openforis.idm.metamodel.TaxonAttributeDefinition;
		import org.openforis.idm.metamodel.TextAttributeDefinition;
		import org.openforis.idm.metamodel.TextAttributeDefinition$Type;
		import org.openforis.idm.metamodel.TimeAttributeDefinition;
		import org.openforis.idm.metamodel.UniquenessCheck;
		import org.openforis.idm.metamodel.Unit;
		import org.openforis.idm.metamodel.Versionable;

		public static function init():void {
			var array:Array = [
				BasicMap, 
				Annotatable,
				AttributeDefault,
				AttributeDefinition,
				BooleanAttributeDefinition,
				Check,
				Check$Flag,
				CodeAttributeDefinition,
				CodeAttributeDefinition$Type,
				CodeList,
				CodeList$CodeScope,
				CodeList$CodeType,
				CodeListItem,
				CodeListLabel,
				CodeListLabel$Type,
				CodeListLevel,
				CodingScheme,
				ComparisonCheck,
				ComparisonCheck$OPERATION,
				CoordinateAttributeDefinition,
				CustomCheck,
				DateAttributeDefinition,
				DetachedModelDefinitionException,
				DistanceCheck,
				EntityDefinition,
				FileAttributeDefinition,
				InvalidCheckException,
				LanguageSpecificText,
				ModelVersion,
				NodeDefinition,
				NodeDefinitionLabel,
				NodeDefinitionLabel$Type,
				NodeDefinitionPropertyHandler,
				NumberAttributeDefinition,
				NumericAttributeDefinition,
				NumericAttributeDefinition$Type,
				PatternCheck,
				Precision,
				Prompt,
				Prompt$Type,
				RangeAttributeDefinition,
				Schema,
				SchemaExpression,
				SchemaPropertyHandler,
				SpatialReferenceSystem,
				Survey,
				TaxonAttributeDefinition,
				TextAttributeDefinition,
				TextAttributeDefinition$Type,
				TimeAttributeDefinition,
				UniquenessCheck,
				Unit,
				Versionable
			];
		}
		
	}
}