package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public interface EventQueue {

	void publish(RecordTransaction recordTransaction);
	
	void publish(SurveyEvent surveyEvent);
	
	boolean isEnabled();
	
	void setEnabled(boolean enabled);
	
}