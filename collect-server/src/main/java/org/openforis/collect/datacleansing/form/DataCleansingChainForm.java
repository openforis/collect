package org.openforis.collect.datacleansing.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingStep;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingChainForm extends DataCleansingItemForm<DataCleansingChain> {

	private String title;
	private String description;
	private List<DataCleansingStepForm> steps;
	
	public DataCleansingChainForm() {
		super();
		this.steps = new ArrayList<DataCleansingStepForm>();
	}
	
	public DataCleansingChainForm(DataCleansingChain chain) {
		super(chain);
		List<DataCleansingStep> steps = chain.getSteps();
		this.steps = new ArrayList<DataCleansingStepForm>(steps.size());
		for (DataCleansingStep step : steps) {
			DataCleansingStepForm stepForm = new DataCleansingStepForm(step);
			this.steps.add(stepForm);
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
	
	public void setSteps(List<DataCleansingStepForm> steps) {
		this.steps = steps;
	}
	
}
