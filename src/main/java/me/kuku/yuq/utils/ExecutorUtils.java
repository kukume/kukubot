package me.kuku.yuq.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorUtils {

	private static final ExecutorService executorService;

	static {
		executorService = Executors.newFixedThreadPool(3);
	}

	public static Future<?> submit(Callable<?> task){
		return executorService.submit(task);
	}

	public static void execute(Runnable command){
		executorService.execute(command);
	}
}
