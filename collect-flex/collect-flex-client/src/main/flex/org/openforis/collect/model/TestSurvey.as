package org.openforis.collect.model {
	import mx.collections.ArrayCollection;
	
	import org.openforis.collect.idm.model.impl.EntityImpl;
	import org.openforis.idm.metamodel.impl.AbstractAttributeDefinition;
	import org.openforis.idm.metamodel.impl.DateAttributeDefinitionImpl;
	import org.openforis.idm.metamodel.impl.EntityDefinitionImpl;
	import org.openforis.idm.metamodel.impl.ModelObjectLabelImpl;
	import org.openforis.idm.metamodel.impl.ModelVersionImpl;
	import org.openforis.idm.metamodel.impl.SchemaImpl;
	import org.openforis.idm.metamodel.impl.SurveyImpl;
	
	public class TestSurvey extends SurveyImpl {
		
		public function TestSurvey() {
			super();
			
			this.name = "Test Survey";
			
			//start versions
			this.versions = new ArrayCollection();
			
			var version:ModelVersionImpl = new ModelVersionImpl();
			version.name = "version 1";
			version.date = new Date(2010, 1, 1);
			
			this.versions.addItem(version);
			
			version = new ModelVersionImpl();
			version.name = "version 2";
			version.date = new Date(2011, 1, 1);
			
			this.versions.addItem(version);
			
			//end versions
			
			//start schema
			var schemaImpl:SchemaImpl = new SchemaImpl();
			this.schema = schemaImpl;
			
			schemaImpl.rootEntityDefinitions = new ArrayCollection();
			
			//RootEntity 1
			var entityDefinition:EntityDefinitionImpl = new EntityDefinitionImpl();
			entityDefinition.name = "RootEntity1";
			entityDefinition.childDefinitions = new ArrayCollection();
			
			var childDefinition:AbstractAttributeDefinition = new DateAttributeDefinitionImpl();
			childDefinition.name = "date";
			childDefinition.labels = new ArrayCollection();
			
			var label:ModelObjectLabelImpl = new ModelObjectLabelImpl();
			label.text = "Date";
			childDefinition.labels.addItem(label);
			entityDefinition.childDefinitions.addItem(childDefinition);
				
			var rootEntity:EntityImpl = new EntityImpl();
			rootEntity.path = "/";
			rootEntity.definition = entityDefinition;
			
			schemaImpl.rootEntityDefinitions.addItem(rootEntity);
			
			//RootEntity 2
			/*
			entityDefinition = new EntityDefinitionImpl();
			entityDefinition.name = "RootEntity2";
			
			rootEntity = new EntityImpl();
			rootEntity.path = "/";
			rootEntity.definition = entityDefinition;
			
			schemaImpl.rootEntityDefinitions.addItem(rootEntity);
			*/
			//end schema
		}
	}
}