package org.openforis.collect.web.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.time.DateUtils;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectRecordSummary.StepSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.web.controller.RecordStatsGenerator.RecordsStatsParameters.TimeUnit;
import org.openforis.commons.collection.Visitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecordStatsGenerator {

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SurveyManager surveyManager;
	
	public RecordsStats generate(int surveyId, Date[] period) {
		final RecordsStats stats = new RecordsStats(period);
		CollectSurvey survey = surveyManager.getById(surveyId);
		recordManager.visitSummaries(new RecordFilter(survey), null, new Visitor<CollectRecordSummary>() {
			public void visit(CollectRecordSummary s) {
				Map<Step, StepSummary> summaryByStep = s.getSummaryByStep();
				for (Entry<Step, StepSummary> entry : summaryByStep.entrySet()) {
					Step step = entry.getKey();
					StepSummary stepSummary = entry.getValue();
					{
						PointStats pointStats = stats.getOrCreateDaylyStats(stepSummary.getCreationDate());
						switch (step) {
						case ENTRY:
							pointStats.incrementCreated();
							break;
						case CLEANSING:
							pointStats.incrementEntered();
							break;
						case ANALYSIS:
							pointStats.incrementCleansed();
							break;
						}
					}
				}
				{
					PointStats pointStats = stats.getOrCreateDaylyStats(s.getModifiedDate());
					pointStats.incrementModified();
				}
			}
		}, true);
		return stats;
	}
	
	public static class RecordsStatsParameters {
		
		public enum TimeUnit {
			DAY, MONTH, YEAR
		}
		
		private TimeUnit timeUnit = TimeUnit.DAY;
		private Date from;
		private Date to;
		
		public TimeUnit getTimeUnit() {
			return timeUnit;
		}
		
		public void setTimeUnit(TimeUnit unit) {
			this.timeUnit = unit;
		}
		
		public Date getFrom() {
			return from;
		}
		
		public void setFrom(Date from) {
			this.from = from;
		}
		
		public Date getTo() {
			return to;
		}

		public void setTo(Date to) {
			this.to = to;
		}
	}
	
	public static class RecordsStats {
		
		private Map<Integer, PointStats> dailyStats = new HashMap<Integer, PointStats>();
		private Map<Integer, PointStats> monthlyStats = new HashMap<Integer, PointStats>();
		private Map<Integer, PointStats> yearlyStats = new HashMap<Integer, PointStats>();
		private Date[] period; //start and from date
		
		public RecordsStats(Date[] period) {
			this.period = period;
		}

		public PointStats getOrCreateDaylyStats(Date date) {
			int key = generateKey(date, TimeUnit.DAY);
			PointStats stats = dailyStats.get(key);
			if (stats == null) {
				stats = new PointStats();
				dailyStats.put(key, stats);
			}
			return stats;
		}
		
		public void finalize() {
			this.monthlyStats = generateTimeUnitStats(TimeUnit.MONTH);
			this.yearlyStats = generateTimeUnitStats(TimeUnit.YEAR);
		}

		private Map<Integer, PointStats> generateTimeUnitStats(TimeUnit unit) {
			Map<Integer, PointStats> result = new HashMap<Integer, PointStats>();
			int daysCount = 1;
			Date periodStart = period[0];
			Date periodEnd = period[1];
			Date currentDate = periodStart;
			while (DateUtils.truncatedCompareTo(currentDate, periodEnd, Calendar.DATE) <= 0) {
				int currentYearKey = generateKey(currentDate, unit);
				PointStats currentYearStats = result.get(currentYearKey);
				if (currentYearStats == null) {
					currentYearStats = new PointStats();
					result.put(currentYearKey, currentYearStats);
				}
				int dailyKey = generateKey(currentDate, TimeUnit.DAY);
				PointStats currentDayStats = dailyStats.get(dailyKey);
				if (currentDayStats != null) {
					currentYearStats.incrementAll(currentDayStats);
				}
				currentDate = DateUtils.addDays(periodStart, daysCount++);
			}
			return result;
		}

		private int generateKey(Date date, TimeUnit unit) {
			Calendar cal = DateUtils.toCalendar(date);
			switch(unit) {
			case YEAR:
				return cal.get(Calendar.YEAR);
			case MONTH:
				return cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH);
			case DAY:
				return cal.get(Calendar.YEAR) * 100 * 100 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH);
			default:
				return 0;
			}
		}
		
		public Date[] getPeriod() {
			return period;
		}
		
		public Map<Integer, PointStats> getDailyStats() {
			return dailyStats;
		}
		
		public Map<Integer, PointStats> getMonthlyStats() {
			return monthlyStats;
		}
		
		public Map<Integer, PointStats> getYearlyStats() {
			return yearlyStats;
		}
	}
	
	public static class PointDayKey {
		private int key;
		

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + key;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PointDayKey other = (PointDayKey) obj;
			if (key != other.key)
				return false;
			return true;
		}
	}
	
	public static class PointStats {
		
		private int created;
		private int entered;
		private int cleansed;
		private int modified;
		
		public void incrementAll(PointStats currentDayStats) {
			this.entered += currentDayStats.entered;
			this.cleansed += currentDayStats.cleansed;
			this.created += currentDayStats.created;
			this.modified += currentDayStats.modified;

		}

		public void incrementCreated() {
			this.created ++;
		}

		public void incrementEntered() {
			this.entered ++;
		}

		public void incrementCleansed() {
			this.cleansed ++;
		}

		public void incrementModified() {
			this.modified ++;
		}

		public int getCreated() {
			return created;
		}
		
		public void setCreated(int created) {
			this.created = created;
		}
		
		public int getEntered() {
			return entered;
		}
		
		public void setEntered(int entered) {
			this.entered = entered;
		}
		
		public int getCleansed() {
			return cleansed;
		}
		
		public void setCleansed(int cleansed) {
			this.cleansed = cleansed;
		}
		
		public int getModified() {
			return modified;
		}
		
		public void setModified(int modified) {
			this.modified = modified;
		}
		
	}
}
