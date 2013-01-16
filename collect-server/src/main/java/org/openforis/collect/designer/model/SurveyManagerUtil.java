package org.openforis.collect.designer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.SurveySummary;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class SurveyManagerUtil {

	public static List<SurveyWorkSummary> getSurveySummaries(SurveyManager surveyManager) {
		List<SurveySummary> surveySummaries = surveyManager.getSurveySummaries(null);
		List<SurveySummary> surveyWorkSummaries = surveyManager.getSurveyWorkSummaries();
		List<SurveyWorkSummary> result = new ArrayList<SurveyWorkSummary>();
		Map<String, SurveyWorkSummary> workingSummariesByUri = new HashMap<String, SurveyWorkSummary>();
		for (SurveySummary summary : surveyWorkSummaries) {
			SurveyWorkSummary summaryWork = new SurveyWorkSummary(summary.getId(), summary.getName(), summary.getUri(), false, true);
			result.add(summaryWork);
			workingSummariesByUri.put(summary.getUri(), summaryWork);
		}
		for (SurveySummary summary : surveySummaries) {
			SurveyWorkSummary summaryWork = workingSummariesByUri.get(summary.getUri());
			if ( summaryWork == null ) {
				summaryWork = new SurveyWorkSummary(summary.getId(), summary.getName(), summary.getUri(), true, false);
				result.add(summaryWork);
			} else {
				summaryWork.setPublished(true);
				summaryWork.setPublishedSurveyId(summary.getId());
			}
		}
		sortByName(result);
		return result;
	}

	private static void sortByName(List<SurveyWorkSummary> result) {
		Collections.sort(result, new Comparator<SurveyWorkSummary>() {
	        @Override 
	        public int compare(SurveyWorkSummary s1, SurveyWorkSummary s2) {
	            return s1.getName().compareTo(s2.getName());
	        }
		});
	}

}
