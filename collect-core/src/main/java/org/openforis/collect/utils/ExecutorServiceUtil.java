/**
 * 
 */
package org.openforis.collect.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class ExecutorServiceUtil {

	private static ExecutorService SINGLE_THREAD_EXECUTOR_SERVICE;
	private static ExecutorService CACHED_THREAD_POOL;

	static {
		SINGLE_THREAD_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
		CACHED_THREAD_POOL = Executors.newCachedThreadPool();
	}

	private ExecutorServiceUtil() {
	}

	public static Future<?> executeInCachedPool(Runnable runnable) {
		Future<?> future = CACHED_THREAD_POOL.submit(runnable);
		return future;
	}

	public static <V> Future<V> executeInCachedPool(Callable<V> callable) {
		Future<V> future = CACHED_THREAD_POOL.submit(callable);
		return future;
	}

	public static Future<?> execute(Runnable runnable) {
		Future<?> future = SINGLE_THREAD_EXECUTOR_SERVICE.submit(runnable);
		return future;
	}
	
	public static <V> Future<V> execute(Callable<V> callable) {
		// FutureTask<V> task = new FutureTask<V>(callable);
		Future<V> future = SINGLE_THREAD_EXECUTOR_SERVICE.submit(callable);
		return future;
	}

}
