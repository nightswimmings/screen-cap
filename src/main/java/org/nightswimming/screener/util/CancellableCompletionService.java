package org.nightswimming.screener.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class CancellableCompletionService<T> extends ThreadPoolExecutor implements CancellableCompletionExecutorService<T>{     

	private final Function<Object, T> resultAdapter;
	private final LinkedBlockingQueue<Future<T>> completionQueue;
	private final List<Thread> runningTasks = Collections.synchronizedList(new ArrayList<>());
		
	public CancellableCompletionService(Function<Object,T> resultAdapter, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler){
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
		this.completionQueue = new LinkedBlockingQueue<Future<T>>();
		this.resultAdapter = resultAdapter;
	}

	/***********************************************************
	 * ThreadPoolExecutor (ExecutorService) overridden methods *
	 ***********************************************************/
	
	@Override
	public void execute(Runnable runnable){
		if (runnable instanceof RunnableFuture){ 
			super.execute(new CancellableQueueingFuture((RunnableFuture<?>) runnable));
		} else {
			//A bare Runnable should only happen if this very method is 
			//called directly from outside. We make it pass through the queue workflow.
			super.submit(runnable);
		}
	}
	
	@Override protected void beforeExecute(Thread t, Runnable r) {
	    super.beforeExecute(t, r);
	    runningTasks.add(t);
	}
	@Override protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		runningTasks.remove(Thread.currentThread());
	}
	
	/**************************************************
	 * CompletionService implemented methods          *
	 **************************************************/
	
	@Override
	@SuppressWarnings("unchecked")
	public Future<T> submit(@SuppressWarnings("rawtypes") Callable task) {
		//Wrapping is performed when used as a CompletionService or ExecutionService as well (even though should not be necessary)
		Callable<T> taskAdapterToExpectedType = new Callable<T>(){
			public T call() throws Exception{
				return resultAdapter.apply(task.call());
			}
		};
		return super.submit(taskAdapterToExpectedType);
	}
	 
	@Override
	@SuppressWarnings("unchecked")
	public Future<T> submit(Runnable task, Object result) {
		//Wrapping is performed when used as a CompletionService or ExecutionService as well (even though should not be necessary)
		return super.submit(task, resultAdapter.apply(result));
	}
	
	@Override 
	public Future<T> take() throws InterruptedException {
		return completionQueue.take();
	}

	@Override
	public Future<T> poll() {
		return completionQueue.poll();
	}

	@Override
	public Future<T> poll(long timeout, TimeUnit unit) throws InterruptedException{
	 	return completionQueue.poll(timeout, unit);
	}
	
	/**************************************************
	 * CancellableExecutorService implemented methods *
	 **************************************************/
	
	@Override
	public void cancelQueuedTasks(boolean notifyUnqueuedToCompletion){
		this.getQueue().forEach(this::cancelQueuedRunnable); 
		if (!notifyUnqueuedToCompletion) this.purge();
	}
	
	@Override
	public void interruptRunningTasks(){
		runningTasks.forEach(Thread::interrupt);
	}
	
	@Override
	public void cancelExecution(boolean notifyUnqueuedToCompletion){
		this.cancelQueuedTasks(notifyUnqueuedToCompletion);
		this.interruptRunningTasks();
	}
	
	private boolean cancelQueuedRunnable(Runnable runnable){
		
		//TODO (TBD):: From submit, and from ExecutorService.invoke* , which don't get into the completionqueue!!!
		//We are working with Futures and therefore, we must cancel it
		//through that interface to be consistent with the workflow.
		//However, a custom implementation for the CompletionService is 
		//needed in order to handle the cancel (FutureTask acts as a wrapper
		//for another FutureTask, so this outer cancel must trigger inner's one.
		//If not, that future stays in an inconsistent state that does 
		//block its get() forever
		if (runnable instanceof RunnableFuture ){
			return ((RunnableFuture<?>) runnable).cancel(false); //Through submit()
		} else {
			//We made sure all the inputs are passed through Future processinf
			//so this should not be never reached. Anyway, if we were not 
			//working with futures, we could never cancel the queued task, so we just drop it
			return this.remove(runnable);
		}
	}
	
	@Override 
	public void shutdownNowAfterCancel(boolean notifyUnqueuedToCompletion){
		this.cancelExecution(notifyUnqueuedToCompletion); //Cancels everything, both active and queued tasks
		this.shutdown(); //Stop accepting tasks and wait for queued and active ones to finish (without blocking)
	}
	
	/* ExecutorCompletionService<V> is virtually a final class with that many private fields
	 * So we need our custom implementation from scratch.
	 * We need all of this duplication only to override inner class CancellableQueueingFuture's cancel method
	 * in order to trigger its wrapped FutureTask cancelation, so the executor queue can cancel it.
	 * We take profit and use it for converting results to expected type as well by means of the passed adapter function
	 */
	private class CancellableQueueingFuture extends FutureTask<T> {
		
		private final Future<?> task;
		
		CancellableQueueingFuture(RunnableFuture<?> task) {
			super(task, null);
			this.task = task;
		}
		//This is the only method different from the ExecutorCompletionService default implementation
		public boolean cancel(boolean mayInterruptIfRunning){
			super.cancel(mayInterruptIfRunning);
			return this.task.cancel(mayInterruptIfRunning);
		}
			
		protected void done() {
			super.done();
			completionQueue.add(this); 
		}
		
		public T get() throws InterruptedException, ExecutionException{
			//TODO (TBD):: super.get?
			return resultAdapter.apply(task.get());
		}
		
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException{
			//TODO (TBD): super.get?
			return resultAdapter.apply(task.get(timeout, unit));
		}
	}
}
