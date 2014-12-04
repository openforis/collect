package org.openforis.collect.metamodel.ui;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.UIOptionsBinder;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class UIOptionsMigratorTest extends CollectIntegrationTest {

	@Autowired
	private SurveyContext surveyContext;
	
	private CollectSurvey survey;
	private UIOptions uiOptions;

	@Before
	public void setUp() throws IdmlParseException {
		SurveyIdmlBinder binder = new SurveyIdmlBinder(surveyContext);
		binder.addApplicationOptionsBinder(new UIOptionsBinder());

		InputStream is = ClassLoader.getSystemResourceAsStream("test.idm.xml");
		survey = (CollectSurvey) binder.unmarshal(is);
		
		uiOptions = survey.getUIOptions();
	}

	@Test
	public void testMigration() {
		UIOptionsMigrator migrator = new UIOptionsMigrator();
		UIConfiguration uiModel = migrator.migrateToUIConfiguration(uiOptions);

		assertNotNull(uiModel);
		Schema schema = survey.getSchema();
		{
			EntityDefinition cluster = schema.getRootEntityDefinition("cluster");
			FormSet formSet = uiModel.getFormSetByRootEntityId(cluster.getId());
			List<Form> forms = formSet.getForms();
			assertEquals(3, forms.size());
			
			//cluster form
			{
				Form form = forms.get(0);
				List<FormComponent> children = form.getChildren();
				assertEquals(16, children.size());
				{
					//task
					FormComponent component = children.get(0);
					assertTrue(component instanceof Table);
					Table taskTable = (Table) component;
					assertNotNull(taskTable.getEntityDefinition());
					assertEquals("task", taskTable.getEntityDefinition().getName());
					
					//task/task
					assertEquals(3, taskTable.getHeadingComponents().size());
					TableHeadingComponent heading = taskTable.getHeadingComponents().get(0);
					assertTrue(heading instanceof Column);
					Column col = (Column) heading;
					assertEquals(Integer.valueOf(729), col.getAttributeDefinitionId());
				}
			}
			//plot form
			{
				Form plotForm = forms.get(1);
				List<FormComponent> plotFormChildren = plotForm.getChildren();
				assertEquals(1, plotFormChildren.size());
				FormComponent plotMultipleEntityComponent = plotFormChildren.get(0);
				assertTrue(plotMultipleEntityComponent instanceof FormSection);
				
				FormSection plotMultipleEntityFormSection = (FormSection) plotMultipleEntityComponent;
				
				List<Form> subforms = plotMultipleEntityFormSection.getForms();
				assertEquals(6, subforms.size());
				Form detailForm = subforms.get(0);
				List<FormComponent> children = detailForm.getChildren();
				assertEquals(34, children.size());
				{
					//plot_no
					{
						FormComponent component = children.get(0);
						assertTrue(component instanceof Field);
						Field plotNoField = (Field) component;
						assertEquals(Integer.valueOf(749), plotNoField.getAttributeDefinitionId());
					}
					//time study (single entity -> form section)
					{
						FormComponent component = children.get(2);
						assertTrue(component instanceof FormSection);
						FormSection section = (FormSection) component;
						assertEquals(3, section.getChildren().size());
						//start time
						FormComponent startTimeComp = section.getChildren().get(1);
						assertTrue(startTimeComp instanceof Field);
						Field startTimeField = (Field) startTimeComp;
						assertEquals(Integer.valueOf(753), startTimeField.getAttributeDefinitionId());
					}
				}
			}
		}
	}

}
