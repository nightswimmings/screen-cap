package org.nightswimming.screener.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface CancellableCompletionExecutorService<T> extends ExecutorService, CompletionService<T> {
	/* Collision of parents signature. ExecutorService interface uses generic method type, and CompletionService interface its Class-type.
	 * Otherwise, the erasure is the same, so we raw the incoming parameter. 
	 * when acting as ExecutorService, any type is allowed, but the output is enforced to be the same, no matter the instance class type
	 * when acting as CompletionService, only class type is allowed for input and output.
	 * So problematic behavior is only fired through the very implementor class without castings, then only output is forced to be the class-type, but no input.
	 * We solve that by using a constructor with a Function<Object,T> to ensure unknown result-type tasks into our desired class.
	 * We still can use this interface with the type Object, and simply pass an UnaryOperator<Object>.identity() to gather all future types without worrying about the type.
	 */
	@SuppressWarnings("unchecked") public Future<T> submit(@SuppressWarnings("rawtypes") Callable task);
	@SuppressWarnings("unchecked") public Future<T> submit(Runnable task, Object result);
	
	public void cancelQueuedTasks(boolean notifyUnqueuedToCompletion);
	public void interruptRunningTasks();
	public void cancelExecution(boolean notifyUnqueuedToCompletion);
	/*
	 * ShutdownNow() stops running tasks and returns queued ones, 
	 * Shutdown() waits for queued tasks but does not interrupt the active ones
	 * so ShutdownAfterCancel() cancels queued tasks and processes them after interrupting currently active ones
	 * This does not block
	 */
	public void shutdownNowAfterCancel(boolean notifyUnqueuedToCompletion);
}