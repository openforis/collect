package org.openforis.collect.api.command;

import java.util.Date;

import org.openforis.collect.api.User;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class Command {

	public final int recordId;
	public final Date timestamp;
	public final User user;
	
	public Command(int recordId, User user) {
		super();
		this.recordId = recordId;
		this.user = user;
		this.timestamp = new Date();
	}
	
}
