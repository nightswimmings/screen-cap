package org.nightswimming.screener.rec;

import org.bytedeco.javacv.Frame;
import org.nightswimming.screener.RecordingException;

public interface ScreenFrameGrabber {
	public void start() throws RecordingException;
	public Frame grab() throws RecordingException; 
	//When we stop grabber, any previously grabbed frame, who references a direct bytebuffer in memory,
	//is lost, and so any recorder's record operation with that frame crashes at JVM/native level.
	public void stop() throws RecordingException;
}
