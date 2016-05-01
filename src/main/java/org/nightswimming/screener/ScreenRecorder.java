package org.nightswimming.screener;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.nightswimming.screener.util.CancellableCompletionService;

public class ScreenRecorder implements AutoCloseable {

	private final CancellableCompletionService<String> executor;
	
	private ScreenRecorder(CancellableCompletionService<String> executor){
		this.executor = executor;
	}
	public static ScreenRecorder getScreenRecorder() {
		//We use a 2-thread pool ao cancelled/timeouted tasks can be processed while one job is running
		CancellableCompletionService<String> customExecutor = 
				     new CancellableCompletionService<>(x -> x.toString(), 2, 2, 1, TimeUnit.SECONDS, 
				 				new LinkedBlockingQueue<Runnable>(2), // SynchronousQueue<Runnable>() for 0-queue
				 				Executors.defaultThreadFactory(), 
				 				new ThreadPoolExecutor.AbortPolicy() //DiscardPolicy for silent discarding when queue is full
	     );
		
		return new ScreenRecorder(customExecutor);
	}

	public Future<String> rec(ScreenRecordingTask recordingTask) throws RejectedExecutionException {
		return executor.submit(recordingTask);	
	}
	
	public void stop() {
		executor.cancelExecution(true);
	}
	
	//Use -1 for indefinite time
	public String getLastResult(long timeoutSecs){
    	try {
    		Future<String> nextCompletedTask = (timeoutSecs < 0) ?
    										   executor.take():
    										   executor.poll(timeoutSecs, 
    												   				TimeUnit.SECONDS);
			if (nextCompletedTask == null) return null;
    		String taskId = nextCompletedTask.get();
			return "ScreenRecording job of "+taskId+" completed successfully.";
		} 
		catch (InterruptedException e ) { return "Interruption while retrieving a job result. This should never happen as the task is supposed to be already completed according by the CompletionService."; } 
		catch (CancellationException e) { return "ScreenRecording task was cancelled while waiting in the queue to be executed."; }  
    	catch (ExecutionException e)    { return "ScreenRecording task threw internal exception while running: " + e.getCause().getMessage();}
	}
	
	//Blocking
	public void close() throws InterruptedException {
		executor.shutdownNowAfterCancel(true);
		executor.awaitTermination(5L, TimeUnit.MINUTES);
	}	
}

