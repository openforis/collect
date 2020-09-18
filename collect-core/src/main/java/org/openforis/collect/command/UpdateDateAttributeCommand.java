package org.openforis.collect.command;

import org.openforis.idm.model.Date;

public class UpdateDateAttributeCommand extends UpdateAttributeCommand {

	private static final long serialVersionUID = 1L;

	private Integer year;
	private Integer month;
	private Integer day;

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

}
