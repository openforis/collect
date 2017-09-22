/**
 * 
 */
package org.openforis.idm.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author S. Ricci
 *
 */
public class CalculatedAttributeTest {

	private Survey survey;
	private Record record;
	private RecordUpdater recordUpdater;
	
	@Before
	public void before() {
		recordUpdater = new RecordUpdater();
		survey = createTestSurvey();
		record = createTestRecord(survey);
	}
	
	@Test
	@Ignore
	public void testNotNullValues() throws InvalidExpressionException {
		Entity rootEntity = record.getRootEntity();
		Entity item = addItem(rootEntity, 10, 5.5d);
		RealAttribute total = (RealAttribute) item.getChild("total", 0);
		RealValue calculatedTotal = total.getValue();
		assertEquals(new RealValue(55d, null), calculatedTotal);
	}
	
	@Test
	@Ignore
	public void testNullValue() throws InvalidExpressionException {
		Entity rootEntity = record.getRootEntity();
		Entity item = addItem(rootEntity, null, 5.5d);
		RealAttribute total = (RealAttribute) item.getChild("total", 0);
		assertEquals(Double.valueOf(0d), total.getValue().getValue());
	}
	
	@Test
	@Ignore
	public void testUpdateAttribute() throws InvalidExpressionException {
		Entity rootEntity = record.getRootEntity();
		Entity item = addItem(rootEntity, 10, 5.5d);
		
		RealAttribute total = (RealAttribute) item.getChild("total", 0);
		RealValue calculatedTotal = total.getValue();
		assertEquals(new RealValue(55d, null), calculatedTotal);
		
		//change dependent attribute
		IntegerAttribute qty = (IntegerAttribute) item.getChild("qty", 0);
		qty.setNumber(20);
//		qty.clearDependentCalculatedAttributes();
		
		calculatedTotal = total.getValue();
		assertEquals(new RealValue(110d, null), calculatedTotal);
	}
	
	
	
//	@Test
//	@Ignore
//	public void testComplexFormula() throws InvalidExpressionException {
//		Entity rootEntity = record.getRootEntity();
//		
//		double priceValue = 5.5d;
//		Entity item = addItem(rootEntity, 15, priceValue);
//
//		IntegerAttribute qty = (IntegerAttribute) item.getChild("qty");
//		IntegerAttribute discountPercent = (IntegerAttribute) item.getChild("discount_percent");
//		RealAttribute total = (RealAttribute) item.getChild("total");
//		
//		List<Attribute<?,?>> dependentCalculatedAttributes = qty.getDependentCalculatedAttributes();
//		assertEquals(2, dependentCalculatedAttributes.size());
//		assertEquals(item.get("discount_percent", 0), dependentCalculatedAttributes.get(0));
//		assertEquals(item.get("total", 0), dependentCalculatedAttributes.get(1));
//		
//		{
//			int qtyValue = 15;
//			qty.setValue(new IntegerValue(qtyValue, null));
//			qty.evaluateDependentCalculatedAttributes();
//			Integer expectedDiscountValue = 10;
//			assertEquals(new IntegerValue(expectedDiscountValue, null), discountPercent.getValue());
//			assertEquals(new RealValue(calculateTotal(priceValue, qtyValue, expectedDiscountValue), null), total.getValue());
//		}
//		{
//			int qtyValue = 25;
//			qty.setValue(new IntegerValue(qtyValue, null));
//			qty.evaluateDependentCalculatedAttributes();
//			Integer expectedDiscountValue = 20;
//			assertEquals(new IntegerValue(expectedDiscountValue, null), discountPercent.getValue());
//			assertEquals(new RealValue(calculateTotal(priceValue, qtyValue, expectedDiscountValue), null), total.getValue());
//		}
//		{
//			int qtyValue = 75;
//			qty.setValue(new IntegerValue(qtyValue, null));
//			qty.evaluateDependentCalculatedAttributes();
//			Integer expectedDiscountValue = 30;
//			assertEquals(new IntegerValue(expectedDiscountValue, null), discountPercent.getValue());
//			assertEquals(new RealValue(calculateTotal(priceValue, qtyValue, expectedDiscountValue), null), total.getValue());
//		}
//	}
//
//	private double calculateTotal(double price, int qty, Integer discount) {
//		return qty * (price - price * discount/100);
//	}
//	
	private Record createTestRecord(Survey survey) {
		Record record = new Record(survey, null, "bill");
		recordUpdater.initializeNewRecord(record);
		return record;
	}

	protected Entity addItem(Entity parentEntity, Integer qtyValue, Double priceValue) {
		EntityDefinition rootEntityDefn = parentEntity.getDefinition();
		EntityDefinition itemDefn = (EntityDefinition) rootEntityDefn.getChildDefinition("item");
		Entity item = (Entity) itemDefn.createNode();
		
		if ( qtyValue != null ) {
			NodeDefinition qtyDefn = itemDefn.getChildDefinition("qty");
			IntegerAttribute qty = (IntegerAttribute) qtyDefn.createNode();
			qty.setValue(new IntegerValue(qtyValue, null));
			item.add(qty);
		}
		if ( priceValue != null ) {
			NumericAttributeDefinition priceDefn = (NumericAttributeDefinition) itemDefn.getChildDefinition("price");
			RealAttribute price = (RealAttribute) priceDefn.createNode();
			price.setValue(new RealValue(priceValue, null));
			item.add(price);
		}
		NumberAttributeDefinition totalDefn = (NumberAttributeDefinition) itemDefn.getChildDefinition("total");
		RealAttribute total = (RealAttribute) totalDefn.createNode();
		item.add(total);
		
		NumberAttributeDefinition discountDefn = (NumberAttributeDefinition) itemDefn.getChildDefinition("discount_percent");
		IntegerAttribute discount = (IntegerAttribute) discountDefn.createNode();
		item.add(discount);
		
		EntityBuilder.addValue(item, "time", new Time(110, 5));
		
		parentEntity.add(item);
		recordUpdater.initializeRecord(record);
		return item;
	}

	/**
	 * Creates a test survey in which there is a bill with a list of items.
	 * For each item there is a price, a quantity and a total 
	 * (calculated using the an expression or a constant).
	 * 
	 * @return
	 */
	private Survey createTestSurvey() {
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
		total.setCalculated(true);
		total.addAttributeDefault(new AttributeDefault("qty * (price - (price * discount_percent div 100))"));
		item.addChildDefinition(total);
		TimeAttributeDefinition time = schema.createTimeAttributeDefinition();
		time.setName("time");
		time.addAttributeDefault(new AttributeDefault("idm:currentTime()"));
		item.addChildDefinition(time);
		TimeAttributeDefinition timeAlias = schema.createTimeAttributeDefinition();
		timeAlias.setName("time_alias");
		timeAlias.setCalculated(true);
		timeAlias.addAttributeDefault(new AttributeDefault("time"));
		item.addChildDefinition(timeAlias);
		
		NumberAttributeDefinition discountPercent = schema.createNumberAttributeDefinition();
		discountPercent.setType(Type.INTEGER);
		discountPercent.setName("discount_percent");
		discountPercent.setCalculated(true);
		discountPercent.addAttributeDefault(new AttributeDefault("30", "qty > 50"));
		discountPercent.addAttributeDefault(new AttributeDefault("20", "qty > 20"));
		discountPercent.addAttributeDefault(new AttributeDefault("10", "qty > 10"));
		discountPercent.addAttributeDefault(new AttributeDefault("0", "true()"));
		item.addChildDefinition(discountPercent);
		
		return survey;
	}
	
}
