package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.List;

public abstract class RandomValuesGenerator {

	public static <T> List<T> generateRandomSubset(List<T> collection, float percentageOverTotal) {
		List<T> result = new ArrayList<T>();
		int numberOfRecordsToGenerate = (int) Math.ceil(((collection.size() * percentageOverTotal) / 100));
		List<T> currentCollection = new ArrayList<T>(collection);
		for (int i = 0; i < numberOfRecordsToGenerate; i++) {
			int existingRecordIndex = (int) Math.floor(Math.random() * currentCollection.size());
			T item = currentCollection.get(existingRecordIndex);
			result.add(item);
			currentCollection.remove(existingRecordIndex);
		}
		return result;
	}
	
}
