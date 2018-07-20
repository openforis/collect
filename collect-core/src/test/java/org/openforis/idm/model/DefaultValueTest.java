/**
 * 
 */
package org.openforis.idm.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author S. Ricci
 *
 */
public class DefaultValueTest {

	private Survey survey;
	private Record record;
	private Schema schema;
	
	@Before
	public void before() {
		survey = createTestSurvey();
		record = createTestRecord(survey);
		schema = survey.getSchema();
	}
	
	@Test
	public void testNotNullValues() throws InvalidExpressionException {
		NumericAttributeDefinition totalDefn = (NumericAttributeDefinition) schema.getDefinitionByPath("bill/item/total");
		Entity rootEntity = record.getRootEntity();
		Entity item = addItem(rootEntity, 10, 5.5d);
		RealAttribute total = (RealAttribute) item.getChild("total", 0);
		List<AttributeDefault> attributeDefaults = totalDefn.getAttributeDefaults();
		AttributeDefault exprAttributeDefault = attributeDefaults.get(0);
		RealValue calculatedTotal = exprAttributeDefault.evaluate(total);
		assertEquals(new RealValue(55d, null), calculatedTotal);
	}
	
	@Test
	public void testNullValue() throws InvalidExpressionException {
		NumericAttributeDefinition totalDefn = (NumericAttributeDefinition) schema.getDefinitionByPath("bill/item/total");
		List<AttributeDefault> attributeDefaults = totalDefn.getAttributeDefaults();
		AttributeDefault exprAttributeDefault = attributeDefaults.get(0);
		Entity rootEntity = record.getRootEntity();
		Entity item = addItem(rootEntity, null, 5.5d);
		RealAttribute total = (RealAttribute) item.getChild("total", 0);
		RealValue calculatedTotal = exprAttributeDefault.evaluate(total);
		assertEquals(0d, calculatedTotal.getValue(), 0);
	}
	
	@Test
	public void testConditionRespected() throws InvalidExpressionException {
		NumericAttributeDefinition totalDefn = (NumericAttributeDefinition) schema.getDefinitionByPath("bill/item/total");
		List<AttributeDefault> attributeDefaults = totalDefn.getAttributeDefaults();
		Entity rootEntity = record.getRootEntity();
		Entity item = addItem(rootEntity, 10, 0d);
		RealAttribute total = (RealAttribute) item.getChild("total", 0);
		AttributeDefault constantAttributeDefault = attributeDefaults.get(1);
		RealValue calculatedTotal = constantAttributeDefault.evaluate(total);
		assertEquals(new RealValue(0d, null), calculatedTotal);
	}

	@Test
	public void testConditionNotRespected() throws InvalidExpressionException {
		NumericAttributeDefinition totalDefn = (NumericAttributeDefinition) schema.getDefinitionByPath("bill/item/total");
		List<AttributeDefault> attributeDefaults = totalDefn.getAttributeDefaults();
		AttributeDefault exprAttributeDefault = attributeDefaults.get(0);
		Entity rootEntity = record.getRootEntity();
		Entity item = addItem(rootEntity, 10, 0d);
		RealAttribute total = (RealAttribute) item.getChild("total", 0);
		boolean condition = exprAttributeDefault.evaluateCondition(total);
		assertFalse(condition);
	}
	
	private Record createTestRecord(Survey survey) {
		return new Record(survey, null, "bill");
	}

	protected Entity addItem(Entity rootEntity, Integer qtyValue, Double priceValue) {
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		EntityDefinition itemDefn = (EntityDefinition) rootEntityDefn.getChildDefinition("item");
		Entity item = (Entity) itemDefn.createNode();
		if ( qtyValue != null ) {
			NodeDefinition qtyDefn = itemDefn.getChildDefinition("qty");
			IntegerAttribute qty = (IntegerAttribute) qtyDefn.createNode();
			qty.setValue(new IntegerValue(qtyValue, null));
			qty.updateSummaryInfo();
			item.add(qty);
		}
		if ( priceValue != null ) {
			NumericAttributeDefinition priceDefn = (NumericAttributeDefinition) itemDefn.getChildDefinition("price");
			RealAttribute price = (RealAttribute) priceDefn.createNode();
			price.setValue(new RealValue(priceValue, null));
			price.updateSummaryInfo();
			item.add(price);
		}
		NumericAttributeDefinition totalDefn = (NumericAttributeDefinition) itemDefn.getChildDefinition("total");
		RealAttribute total = (RealAttribute) totalDefn.createNode();
		item.add(total);
		rootEntity.add(item);
		return item;
	}

	/**
	 * Creates a test survey in which there is a bill with a list of items.
	 * For each item there is a price, a quantity and a total 
	 * (calculated using the an expression or a constant).
	 * 
	 * @return
	 */
	protected Survey createTestSurvey() {
		SurveyContext surveyContext = new TestSurveyContext();
		Survey survey = surveyContext.createSurvey();
		Schema schema = survey.getSchema();
		EntityDefinition root = schema.createEntityDefinition();
		root.setName("bill");
		schema.addRootEntityDefinition(root);
		EntityDefinition item = schema.createEntityDefinition();
		item.setName("item");
		root.addChildDefinition(item);
		NumberAttributeDefinition qty = schema.createNumberAttributeDefinition();
		qty.setType(Type.INTEGER);
		qty.setName("qty");
		item.addChildDefinition(qty);
		NumberAttributeDefinition price = schema.createNumberAttributeDefinition();
		price.setName("price");
		item.addChildDefinition(price);
		NumberAttributeDefinition total = schema.createNumberAttributeDefinition();
		total.setName("total");
		item.addChildDefinition(total);
		{
			AttributeDefault attributeDefault = new AttributeDefault();
			attributeDefault.setExpression("qty * price");
			attributeDefault.setCondition("price > 0");
			total.addAttributeDefault(attributeDefault);
		}
		{
			AttributeDefault attributeDefault = new AttributeDefault();
			attributeDefault.setValue("0");
			attributeDefault.setCondition("price = 0");
			total.addAttributeDefault(attributeDefault);
		}
		return survey;
	}
	
	
}
