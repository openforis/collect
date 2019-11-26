package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingChain extends DataCleansingItem {

	private static final long serialVersionUID = 1L;
	
	private String title;
	private String description;
	private List<DataCleansingStep> steps = new ArrayList<DataCleansingStep>();;
	
	public DataCleansingChain(CollectSurvey survey) {
		super(survey);
	}

	public DataCleansingChain(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
	}

	public void addStep(DataCleansingStep step) {
		steps.add(step);
	}
	
	public void addAllSteps(List<DataCleansingStep> steps) {
		for (DataCleansingStep step : steps) {
			addStep(step);
		}
	}

	public void removeStep(DataCleansingStep step) {
		steps.remove(step);
	}
	
	public void removeAllSteps() {
		steps.clear();
	}
	
	public List<DataCleansingStep> getSteps() {
		return steps;
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

	@Override
	public boolean deepEquals(Object obj, boolean ignoreId) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj, ignoreId))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataCleansingChain other = (DataCleansingChain) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (! CollectionUtils.deepEquals(steps, other.steps, ignoreId))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

}
