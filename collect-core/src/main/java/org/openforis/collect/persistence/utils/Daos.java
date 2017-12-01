package org.openforis.collect.persistence.utils;

import java.sql.Timestamp;
import java.util.Date;

public class Daos {

	public static Timestamp toTimestamp(Date date) {
		return date == null ? null : new Timestamp(date.getTime());
	}
	
}
