package org.openforis.collect.datacleansing.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.commons.web.Forms;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingChainForm extends DataCleansingItemForm<DataCleansingChain> {

	private String title;
	private String description;
	private List<DataCleansingStepForm> steps;
	private List<Integer> stepIds;
	
	public DataCleansingChainForm() {
		super();
		this.steps = new ArrayList<DataCleansingStepForm>();
		this.stepIds = new ArrayList<Integer>();
	}
	
	public DataCleansingChainForm(DataCleansingChain chain) {
		super(chain);
		List<DataCleansingStep> steps = chain.getSteps();
		this.steps = Forms.toForms(steps, DataCleansingStepForm.class);
		this.stepIds = new ArrayList<Integer>(steps.size());
		for (DataCleansingStep step : steps) {
			this.stepIds.add(step.getId());
		}
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<DataCleansingStepForm> getSteps() {
		return steps;
	}
	
	public List<Integer> getStepIds() {
		return stepIds;
	}
	
	public void setStepIds(List<Integer> stepIds) {
		this.stepIds = stepIds;
	}
	
}
