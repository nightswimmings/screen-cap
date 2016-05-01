package org.nightswimming.screener.rec;

import org.bytedeco.javacv.Frame;
import org.nightswimming.screener.RecordingException;

public interface RegenerableFrameRecorder {
	public void start() throws RecordingException;
	public void	record(Frame frame) throws RecordingException; 
	public String stopAndReturnCreatedFile() throws RecordingException;
}
