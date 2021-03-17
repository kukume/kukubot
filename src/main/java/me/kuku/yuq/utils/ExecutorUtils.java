package me.kuku.yuq.utils;

import java.util.concurrent.*;

public class ExecutorUtils {

	private static final ExecutorService executorService;
	private static final ScheduledExecutorService scheduledExecutorService;

	static {
		executorService = Executors.newFixedThreadPool(3);
		scheduledExecutorService = Executors.newScheduledThreadPool(3);
	}

	public static Future<?> submit(Callable<?> task){
		return executorService.submit(task);
	}

	public static void execute(Runnable command){
		executorService.execute(command);
	}

	public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit){
		return scheduledExecutorService.schedule(command, delay, unit);
	}

	public static ScheduledFuture<?> schedule(Callable<?> callable, long delay, TimeUnit unit){
		return scheduledExecutorService.schedule(callable, delay, unit);
	}
}
