package org.nightswimming.screener;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.Frame;
import org.nightswimming.cv.CVFilter;
import org.nightswimming.screener.RecordingException.RecordingStage;
import org.nightswimming.screener.enums.RecArea;
import org.nightswimming.screener.enums.VideoRecFormat;
import org.nightswimming.screener.rec.MediaRecorderFactory;
import org.nightswimming.screener.rec.RegenerableFrameRecorder;
import org.nightswimming.screener.rec.ScreenFrameGrabber;
import org.nightswimming.screener.util.FileGenerator;
import org.nightswimming.screener.util.tuple.Tuple;

public class ScreenRecordingTask implements Callable<String> {
	
	private Optional<CVFilter<Tuple.Void>> filter; //Preconfigured by now
	private final ScreenFrameGrabber grabber;
	private final RegenerableFrameRecorder recorder;
	private final static Lock mutex = new ReentrantLock(false);
	
	private ScreenRecordingTask(ScreenFrameGrabber grabber, RegenerableFrameRecorder recorder, Optional<CVFilter<Tuple.Void>> filter){
		this.filter = filter;
		this.grabber = grabber;
		this.recorder = recorder;
	}
	
	
	public static ScreenRecordingTask of(FileGenerator fileGenerator, boolean snapshot, Optional<CVFilter<Tuple.Void>> filter){	
		avutil.av_log_set_level(avutil.AV_LOG_QUIET);
		ScreenFrameGrabber grabber =        snapshot ? MediaRecorderFactory.createWinScreenshotGrabber()
													 : MediaRecorderFactory.createWinScreenGrabber();
		RegenerableFrameRecorder recorder = snapshot ? MediaRecorderFactory.createScreenshotRecorder(fileGenerator, VideoRecFormat.STILL_STD)
													 : MediaRecorderFactory.createVideoRecorder(fileGenerator, RecArea.FULL, VideoRecFormat.STD, true);
		return new ScreenRecordingTask(grabber, recorder, filter);
	}
	
	@Override public String call() throws RecordingException {
		try{
			if (mutex.tryLock(2, TimeUnit.SECONDS)) {
				try { 
					
					grabber.start();
					recorder.start();
			        
					Frame capturedFrame = null;
			        while (!Thread.interrupted()){
			        	
		        		try{
		        			capturedFrame = grabber.grab();
		        			if (capturedFrame == null) break; //End of input stream
		        			//JavaCV operation has an internal buffer, and CPU an overhead: 
		        			if (filter.isPresent()) capturedFrame = filter.get().applyAsFrame(capturedFrame);
		        			recorder.record(capturedFrame);
		        		} 
		        		catch (RecordingException e){
		        			System.out.println(" Error transfering a frame so the frame is dropped " + e.getMessage());
		        		}
			        }	
			        String outputFileAName = recorder.stopAndReturnCreatedFile();
		        	grabber.stop();
		        	
		        	return outputFileAName;
				} finally {
					try { recorder.stopAndReturnCreatedFile(); } catch (Exception e){}
					try { grabber.stop(); } catch (Exception e){}
					mutex.unlock();
				}
			} else throw new RecordingException(RecordingStage.START, "An ScreenRecording task is already running.");	
		} 
		catch (RecordingException e){ throw e; }
		catch (InterruptedException e) {
			throw new RecordingException(RecordingStage.START, "Task was cancelled while waiting for another ScreenRecording task to release.");
		}
	}
}
