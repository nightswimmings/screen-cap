package org.nightswimming.screener;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.nightswimming.cv.CVFilter;
import org.nightswimming.screener.util.FileGenerator;
import org.nightswimming.screener.util.tuple.Tuple;

//@Singleton
public final class ScreenCapApp implements NativeKeyListener, Runnable {

	private boolean exitSignal = false;
	private boolean recording = false;
	private final static Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
	private final ScreenRecorder screenRecorder;
	private final ScreenRecordingTask screenVideoRecordingTask, screenShotRecordingTask;
	
	public ScreenCapApp(ScreenRecorder screenRecorder, ScreenRecordingTask screenVideoRecordingTask, 
													   ScreenRecordingTask screenShotRecordingTask,
													   Level loggingLevel) throws NativeHookException{
		this.screenRecorder = screenRecorder;
		this.screenVideoRecordingTask = screenVideoRecordingTask;
		this.screenShotRecordingTask = screenShotRecordingTask;
		logger.setLevel(loggingLevel);
		initHooks();
		beep();
	}
	
	@Override public void run() {
		while (!exitSignal) processNextResult(1);
	}
	
	private boolean processNextResult(int i) {
        String result = screenRecorder.getLastResult(5);
        if (result != null) logger.log(Level.WARNING, result);
        return (result!=null);
	}

	private void initHooks() throws NativeHookException{
		GlobalScreen.registerNativeHook(); 
		GlobalScreen.addNativeKeyListener(this);
	}

	public static void main(String[] args) throws IOException {
		String logFileName = "log.out";
		String outputFolderPathName = "./Recordings/"; //Refactor into app params or hardcoded?
		Optional<CVFilter<Tuple.Void>> filter = Optional.empty(); //This is for starting to test RT processing -> Optional.of(CVFilter.stylize(60f,0.45f));
				
		int exitCode = 0;
		if(System.console() == null){
			PrintStream outputLog = new PrintStream(logFileName, StandardCharsets.UTF_8.name());
			System.setOut(outputLog);
			System.setErr(outputLog);
		}
		FileGenerator fileGenerator = new FileGenerator(Paths.get(outputFolderPathName));
		ScreenRecordingTask screenVideoRecordingTask = ScreenRecordingTask.of(fileGenerator, false, filter);
		ScreenRecordingTask screenSnapshotRecordingTask = ScreenRecordingTask.of(fileGenerator, true, filter);
				
		ScreenRecorder screenRecorder = ScreenRecorder.getScreenRecorder();
		try { 
			ScreenCapApp screenerApp = new ScreenCapApp(screenRecorder, screenVideoRecordingTask, screenSnapshotRecordingTask, Level.WARNING);
			logger.log(Level.WARNING, ">>> ScreenCapApp running...");
			screenerApp.run();
			logger.log(Level.WARNING, ">>> ScreenCapApp shutdowning...");
			GlobalScreen.unregisterNativeHook();
			screenRecorder.close(); //Waits for tasks to finish
			while(screenerApp.processNextResult(1)){}
		}
		catch (NativeHookException e) {
			logger.log(Level.SEVERE, "There was a problem registering the native hook and listeners: "+ e.getMessage());
	        exitCode = 1;
	    } catch (InterruptedException e) {
	    	logger.log(Level.SEVERE, "App was interrupted when closing: "+ e.getMessage());
	    	exitCode = 2;
		} catch (Throwable t){
			logger.log(Level.SEVERE, "Unknown error: "+ t.toString());
	    	exitCode = 3;
		}
		finally {
			beep();
			logger.log(Level.WARNING, (">>> ScreenCapApp exiting..."));
			System.exit(exitCode);
		}
	}
	
	@Override public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VC_F12) {
			this.exitSignal = true;	  
		} 
		else if (e.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN) {
    		logger.log(Level.WARNING, ">>> Screenshot Submitted ");
    		screenRecorder.rec(screenShotRecordingTask);
		}
    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {	
    	if (e.getKeyCode() == NativeKeyEvent.VC_SCROLL_LOCK) {
			if (recording) {
				screenRecorder.stop();
				recording = false;
				beep();
			} else  {
				screenRecorder.rec(screenVideoRecordingTask);
				recording = true;
				logger.log(Level.WARNING, ">>> Screen Recording Submitted ");
			}
		}
    }
    @Override public void nativeKeyTyped(NativeKeyEvent e){}
    
    private static void beep(){
		//Not good when running through GUI: System.out.println("\007");
		java.awt.Toolkit.getDefaultToolkit().beep();
    }
}
