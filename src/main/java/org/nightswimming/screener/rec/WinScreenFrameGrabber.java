package org.nightswimming.screener.rec;

import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_NONE;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.nightswimming.screener.RecordingException;
import org.nightswimming.screener.RecordingException.RecordingDevice;
import org.nightswimming.screener.RecordingException.RecordingStage;

//As soon as OpenCV supports ScreenRecording, we should move to the javacv API
public class WinScreenFrameGrabber extends FFmpegFrameGrabber implements ScreenFrameGrabber {

	private boolean stopped;
	private boolean finished;
	private final boolean snapshot;
	
	public WinScreenFrameGrabber(boolean screenshot) {
		//super("video=screen-capture-recorder");
	    //super.setFormat("dshow"); //Not working?
	    
		super("desktop");
		super.setFormat("gdigrab");
		
		stopped = true;
		finished = false;
		snapshot = screenshot;
	}
	// Force Immutablity
	@Override public void setFormat(String format){
		throw new UnsupportedOperationException("Setting the format to a WinScreenFrameGrabber is not allowed.");
	}
	
	@Override
	public void start() throws RecordingException {
		if (!stopped) throw new RecordingException(RecordingStage.START, "WinScreenFrameGrabber is already grabbing." );
		if (snapshot){
			this.setAudioChannels(0);
			this.setAudioCodec(AV_CODEC_ID_NONE);
		}
		try {
			super.start();
		} catch(Throwable t){
			RecordingException.castAsDomainException(t,RecordingStage.START, RecordingDevice.GRABBER );
		}
		stopped = false;
		finished = false;
	}
	
	@Override
	public Frame grab() throws RecordingException {
		if (stopped) throw new RecordingException(RecordingStage.TRANSFER, "WinScreenFrameGrabber should be started before recording." );
		Frame result = null;
		if (!finished){
			if (snapshot) { //Snaspshot (attempt) is performedt
				finished = true; 
			}
			try {
				result = super.grab();
			} catch(Throwable t){
				RecordingException.castAsDomainException(t,RecordingStage.TRANSFER, RecordingDevice.GRABBER );
			}
		}
		return result;
	}
	@Override
	public void stop() throws RecordingException {
		if (stopped) throw new RecordingException(RecordingStage.STOP, "WinScreenFrameGrabber is already stoppped,");
		try {
			super.stop();
		} catch(Throwable t){
			RecordingException.castAsDomainException(t,RecordingStage.STOP, RecordingDevice.GRABBER );
		}
		stopped = true;
	} 		
}