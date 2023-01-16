package org.openforis.collect.model.recordUpdater;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

public class UniqueQueue<T> implements Queue<T> {
	private final Queue<T> queue = new LinkedList<T>();
	private final Set<T> set = new HashSet<T>();

	public boolean add(T t) {
		if (set.add(t)) {
			queue.add(t);
		}
		return true;
	}

	public T remove() throws NoSuchElementException {
		T ret = queue.remove();
		set.remove(ret);
		return ret;
	}

	@Override
	public boolean addAll(Collection<? extends T> items) {
		for (T t : items) {
			add(t);
		}
		return true;
	}
	
	@Override
	public void clear() {
		queue.clear();
		set.clear();
	}

	@Override
	public boolean contains(Object item) {
		return set.contains(item);
	}

	@Override
	public boolean containsAll(Collection<?> items) {
		return set.containsAll(items);
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return queue.iterator();
	}

	@Override
	public boolean remove(Object item) {
		set.remove(item);
		return queue.remove(item);
	}

	@Override
	public boolean removeAll(Collection<?> items) {
		set.removeAll(items);
		return queue.removeAll(items);
	}

	@Override
	public boolean retainAll(Collection<?> items) {
		set.retainAll(items);
		return queue.retainAll(items);
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public Object[] toArray() {
		return queue.toArray();
	}

	@Override
	public <E> E[] toArray(E[] arr) {
		return queue.toArray(arr);
	}

	@Override
	public T element() {
		return queue.element();
	}

	@Override
	public boolean offer(T item) {
		if (!set.contains(item)) {
			set.add(item);
			return queue.offer(item);
		}
		return false;
	}

	@Override
	public T peek() {
		return queue.peek();
	}

	@Override
	public T poll() {
		T item = queue.poll();
		if (item != null) {
			set.remove(item);
		}
		return item;
	}

}