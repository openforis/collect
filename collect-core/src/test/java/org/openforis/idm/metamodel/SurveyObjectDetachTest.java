package org.openforis.idm.metamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.path.InvalidPathException;

/**
 * @author G. Miceli
 */
public class SurveyObjectDetachTest extends AbstractTest {

	private static final String DEGREES_UNIT = "deg";
	private static final String HECTARES_UNIT = "ha";
	private static final String ACRES_UNIT = "ac";
	private static final String VERSION_1_1 = "1.1";
	private static final String VERSION_2_0 = "2.0";

	@Test
	public void testRemoveUnit() throws InvalidPathException, IdmlParseException {
		Survey survey = createTestSurvey();
		Schema schema = survey.getSchema();
		Unit acresUnit = survey.getUnit(ACRES_UNIT);
		Unit hectaresUnit = survey.getUnit(HECTARES_UNIT);
		Unit degreesUnit = survey.getUnit(DEGREES_UNIT);
		NumericAttributeDefinition plotDirection = (NumericAttributeDefinition) schema.getDefinitionByPath("/cluster/plot_direction");
		RangeAttributeDefinition individuallyOwned = (RangeAttributeDefinition) schema.getDefinitionByPath("/household/land_access/individually_owned/area");
		survey.removeUnit(acresUnit);
		survey.removeUnit(degreesUnit);
		assertNull(survey.getUnit(ACRES_UNIT));
		assertNull(survey.getUnit(DEGREES_UNIT));
		assertNotNull(survey.getUnit(HECTARES_UNIT));
		assertEquals(false, useUnit(plotDirection, degreesUnit));
		assertEquals(false, useUnit(individuallyOwned, acresUnit));
		assertEquals(true, useUnit(individuallyOwned, hectaresUnit));
	}

	protected boolean useUnit(NumericAttributeDefinition defn, Unit unit) {
		String unitName = unit.getName();
		List<Unit> units = defn.getUnits();
		for (Unit un : units) {
			if ( un.getName().equals(unitName) ) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testRemoveVersion() throws InvalidPathException, IdmlParseException {
		Survey survey = createTestSurvey();
		ModelVersion version1_1 = survey.getVersion(VERSION_1_1);
		ModelVersion version2 = survey.getVersion(VERSION_2_0);
		CodeList measurementCodeList = survey.getCodeList("measurement");
		CodeList soilStructureCodeList = survey.getCodeList("soil_structure");
		CodeListItem codeListItem = soilStructureCodeList.getItem("5");
		CodeList vegetationTypeCodeList = survey.getCodeList("vegetation_type");
		CodeListItem codeListItem2 = vegetationTypeCodeList.getItem("504");
		
		Schema schema = survey.getSchema();
		EntityDefinition task = (EntityDefinition) schema.getDefinitionByPath("/cluster/task");
		EntityDefinition soilLayer = (EntityDefinition) schema.getDefinitionByPath("/cluster/plot/soil/layer");
		BooleanAttributeDefinition soilSample = (BooleanAttributeDefinition) schema.getDefinitionByPath("/cluster/plot/soil/sample");
		
		survey.removeVersion(version2);
		assertNull(survey.getVersion(VERSION_2_0));
		assertNull(measurementCodeList.getSinceVersion());
		assertNull(codeListItem.getSinceVersion());
		assertNull(task.getSinceVersion());
		assertEquals(version1_1, soilLayer.getSinceVersion());
		
		survey.removeVersion(version1_1);
		assertNull(codeListItem2.getDeprecatedVersion());
		assertNull(soilSample.getDeprecatedVersion());
	}
	
}
