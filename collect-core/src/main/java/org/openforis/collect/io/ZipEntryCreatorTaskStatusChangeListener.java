package org.openforis.collect.io;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openforis.concurrency.WorkerStatusChangeEvent;
import org.openforis.concurrency.WorkerStatusChangeListener;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class ZipEntryCreatorTaskStatusChangeListener implements WorkerStatusChangeListener {
	
	private ZipOutputStream zipOutputStream;
	private String entryName;

	public ZipEntryCreatorTaskStatusChangeListener(ZipOutputStream zipOutputStream, String entryName) {
		this.zipOutputStream = zipOutputStream;
		this.entryName = entryName;
	}
	
	@Override
	public void statusChanged(WorkerStatusChangeEvent event) {
		try {
			switch ( event.getTo() ) {
			case RUNNING:
				zipOutputStream.putNextEntry(new ZipEntry(entryName));
				break;
			case COMPLETED:
				zipOutputStream.closeEntry();
				break;
			default:
				break;
			}
		} catch ( IOException e ) {
			throw new RuntimeException("Error creating or closing the zip entry: " + e.getMessage(), e);
		}
	}

}