package org.nightswimming.screener;



public class RecordingException extends RuntimeException {
	private static final long serialVersionUID = 163172336899143456L;
	
	public static enum RecordingDevice { GRABBER, RECORDER }
	public static enum RecordingStage { START, TRANSFER, STOP }
	
	public static void castAsDomainException(Throwable t, RecordingStage stage, RecordingDevice dev) throws RecordingException{
		if (t instanceof org.bytedeco.javacv.FrameGrabber.Exception){
			throw new RecordingException(stage, (org.bytedeco.javacv.FrameGrabber.Exception) t);
		}
		else if (t instanceof org.bytedeco.javacv.FrameRecorder.Exception){
			throw new RecordingException(stage, (org.bytedeco.javacv.FrameRecorder.Exception) t);
		}
		else {
			if (dev == RecordingDevice.GRABBER)	 throw new RecordingException(stage, new org.bytedeco.javacv.FrameGrabber.Exception(t.toString(),t));
			if (dev == RecordingDevice.RECORDER) throw new RecordingException(stage, new org.bytedeco.javacv.FrameRecorder.Exception(t.toString(),t));
		}
	}
	public RecordingException(RecordingStage stage, org.bytedeco.javacv.FrameGrabber.Exception e) { 
		super("Grabber failed on " + stage.name() + ": " + e.getMessage());
	}
	public RecordingException(RecordingStage stage, org.bytedeco.javacv.FrameRecorder.Exception e) { 
		super("Recorder failed on " + stage.name() + ": " + e.getMessage());
	}
	public RecordingException(RecordingStage stage, String message){
		super("Fail on " + stage.name() + ": " + message);
	}		
}