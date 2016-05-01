package org.nightswimming.screener;

import static org.nightswimming.screener.util.Assert.assertThrown;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.jnativehook.GlobalScreen;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.nightswimming.screener.RecordingException;
import org.nightswimming.screener.ScreenRecorder;
import org.nightswimming.screener.ScreenRecordingTask;
import org.nightswimming.screener.enums.RecArea;
import org.nightswimming.screener.enums.VideoRecFormat;
import org.nightswimming.screener.rec.MediaRecorderFactory;
import org.nightswimming.screener.rec.RegenerableFrameRecorder;
import org.nightswimming.screener.rec.ScreenFrameGrabber;
import org.nightswimming.screener.util.FileGenerator;

public class ScreenRecorderTest {

	@Rule public final TemporaryFolder tmpFolder = new TemporaryFolder();
	@Rule public final ExpectedException exception = ExpectedException.none();
	
	@BeforeClass
	public static void setUp(){
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);
	}
	
	@Test
	public void screenRecorderShouldWork() throws IOException, InterruptedException {
		Path temporaryFolder = tmpFolder.newFolder().toPath();
		
		//TODO: Move to BeforeClass
		FileGenerator fileGenerator = new FileGenerator(temporaryFolder);
		ScreenRecordingTask screenRecordingTask = ScreenRecordingTask.of(fileGenerator, false, Optional.empty());
		ScreenRecorder screenRecorder = ScreenRecorder.getScreenRecorder();
		
		//Current implementation consists of 2 threads and 2 queue positions
		screenRecorder.rec(screenRecordingTask); //This goes to 1st thread and starts recording
		screenRecorder.rec(screenRecordingTask); //This goes to 2nd thread and keeps trygin for the lock for 2 secs
		screenRecorder.rec(screenRecordingTask); //This goes to the 1st position in the queue
		screenRecorder.rec(screenRecordingTask); //This goes to the second position in the queue
		assertThrown(() -> {screenRecorder.rec(screenRecordingTask);}, RejectedExecutionException.class); //This is rejected
		
		// We make sure the second thread finally gives up, now the 1st in the queue tries, and room for an extra queued task is dispatched
		Thread.sleep(6000);
		screenRecorder.rec(screenRecordingTask).cancel(false); //As soon as the second queued job enters, it is cancelled
		screenRecorder.stop(); //Stop everything: First thread starts interruption, while the one locked and the remaining of the queue get cancelled
		
		String result;
		while ((result=screenRecorder.getLastResult(5)) != null){
			System.out.println(result);
		}
		screenRecorder.close();
		//TODO: Assert files exist
	}
	
	@Test
	public void screenshotCaputrerShouldWork() throws IOException, InterruptedException {
		Path temporaryFolder = tmpFolder.newFolder().toPath();
		
		//TODO: Move to BeforeClass
		FileGenerator fileGenerator = new FileGenerator(temporaryFolder);
		ScreenRecordingTask screenRecordingTask = ScreenRecordingTask.of(fileGenerator, true, Optional.empty());
		ScreenRecorder screenRecorder = ScreenRecorder.getScreenRecorder();
		
		//Current implementation consists of 2 threads and 2 queue positions
		screenRecorder.rec(screenRecordingTask); //This goes to 1st thread and starts screenshot
		screenRecorder.rec(screenRecordingTask); //This goes to 2nd thread and keeps tryin for the lock for 2 secs, but as an screenshot is fast, it will get it
		screenRecorder.rec(screenRecordingTask); //This goes to the 1st position in the queue and will follow same path as 2
		screenRecorder.rec(screenRecordingTask); //This goes to the 2nd position in the queue and will follow same path as 2
		assertThrown(() -> {screenRecorder.rec(screenRecordingTask);}, RejectedExecutionException.class); //This is rejected
		
		Thread.sleep(2000); //All screenshots should be taken
		screenRecorder.rec(screenRecordingTask); //Let's see if this is faster enough to not be interrupted by stop
		screenRecorder.stop(); //Stop everything: First thread starts interruption, while the one locked and the remaining of the queue get cancelled
		
		String result;
		while ((result=screenRecorder.getLastResult(5)) != null){
			System.out.println(result);
		}
		screenRecorder.close();
		//TODO: Assert files exist
	}
	
	@Test
	public void singleThreadScreenshotCapturerShouldWork() throws IOException, RecordingException, InterruptedException{
		Path temporaryFolder = tmpFolder.newFolder().toPath();
	
		avutil.av_log_set_level(avutil.AV_LOG_QUIET);
		FileGenerator fileGenerator = new FileGenerator(temporaryFolder);
		ScreenFrameGrabber grabber = MediaRecorderFactory.createWinScreenshotGrabber();
		RegenerableFrameRecorder recorder = MediaRecorderFactory.createScreenshotRecorder(fileGenerator, VideoRecFormat.STILL_STD);
	
		grabber.start();
		Frame snapshot = grabber.grab();
		CanvasFrame frame = new CanvasFrame("Screen Capture");
	        while (frame.isVisible()) {
	            frame.showImage(snapshot);
	        }
	    frame.dispose();
		recorder.start();
		recorder.record(snapshot);
		System.out.println(recorder.stopAndReturnCreatedFile());
		recorder.start();
		recorder.record(snapshot);
		System.out.println(recorder.stopAndReturnCreatedFile());
		recorder.start();
		recorder.record(snapshot);
		System.out.println(recorder.stopAndReturnCreatedFile());		
		grabber.stop(); 
		//TODO: Assert files exist
	}
	
	@Test
	public void singleThreadScreenRecordingShouldWork() throws IOException, RecordingException, InterruptedException{
		boolean silent = false;
		Path temporaryFolder = tmpFolder.newFolder().toPath();
		avutil.av_log_set_level(avutil.AV_LOG_QUIET);
		FileGenerator fileGenerator = new FileGenerator(temporaryFolder);
		ScreenFrameGrabber grabber = MediaRecorderFactory.createWinScreenGrabber();
		RegenerableFrameRecorder recorder = MediaRecorderFactory.createVideoRecorder(fileGenerator, RecArea.FULL, VideoRecFormat.STD, silent);
	
		grabber.start();
		
		for (int i=0; i<3; i++){
			recorder.start();
			for (int j = 0; j<30; j++){
				Frame snapshot = grabber.grab();
				recorder.record(snapshot);
			}
			System.out.println(recorder.stopAndReturnCreatedFile());
		}
		grabber.stop();
		//TODO: Assert files exist
	}
}
