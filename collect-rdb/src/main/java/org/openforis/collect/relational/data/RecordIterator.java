package org.openforis.collect.relational.data;

import java.io.Closeable;
import java.util.Iterator;

import org.openforis.collect.model.CollectRecord;

public interface RecordIterator extends Iterator<CollectRecord>, Closeable {
	
	int size();
	
}