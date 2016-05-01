package org.nightswimming.screener.rec;

import java.lang.reflect.Field;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.nightswimming.screener.RecordingException;
import org.nightswimming.screener.RecordingException.RecordingDevice;
import org.nightswimming.screener.RecordingException.RecordingStage;
import org.nightswimming.screener.enums.RecArea;
import org.nightswimming.screener.enums.VideoContainer;
import org.nightswimming.screener.util.FileGenerator;

public class VideoFrameRecorder extends FFmpegFrameRecorder implements RegenerableFrameRecorder{
	//TODO: Make it immutable
	
	private final VideoContainer format;
	private final FileGenerator fileGenerator;
	private String currentFile;
	
	public VideoFrameRecorder(FileGenerator fileGenerator, RecArea recArea, VideoContainer recContainer){
		//TBD: recCodec.getVideoCodec() is useful here?
		super(new String(),recArea.getWidth(),recArea.getHeight());
		this.currentFile = null;
		this.format = recContainer;
		this.fileGenerator = fileGenerator;
	}

	@Override public void start() throws RecordingException{
		if (currentFile != null) throw new RecordingException(RecordingStage.START, "VideoFrameRecorder is already recording to a file named '"+currentFile+"'" );
		String generatedFileName;
		try{
			generatedFileName = fileGenerator.generateFile(true,format);
		}
		catch (Throwable t){
			throw new RecordingException(RecordingStage.START, "VideoFrameRecorder error creating the screenshot file '" + currentFile + "'" + t.getMessage());	
		}
		
		changeSuperFileNameHack(generatedFileName);
		
		try {
			super.start();
		} catch(Throwable t){
			RecordingException.castAsDomainException(t,RecordingStage.START, RecordingDevice.RECORDER );
		}
	
		currentFile = generatedFileName;
	}

	/*
	 * This is a very dirty workaround to make FFmpegFrameRecorder a regenerable class,
	 * so each start() generates a new file. This is because the filename parameter is 
	 * passed in the constructor and not settable publicly. 
	 * The hack method throws Exception if something failed, probably due to JavaCV API update or
	 * because a SecurityManager is installed locally disallowing the hack
	 */
	private void changeSuperFileNameHack(String currentFile) throws RecordingException{
		try {
			Field field = FFmpegFrameRecorder.class.getDeclaredField("filename");
			field.setAccessible(true);
			field.set(this, currentFile);
			field.setAccessible(false);
		} catch (Throwable  t) {
			throw new RecordingException(RecordingStage.START, "VideoFrameRecorder error setting output filename for the recorder, see @changeSuperFileNameHack method comment: "+t.getMessage());
		}
	}

	@Override public void record(Frame frame) throws RecordingException {
		if (currentFile == null) throw new RecordingException(RecordingStage.TRANSFER, "VideoFrameRecorder should be started before recording." );
		try {
			super.record(frame);
		} catch(Throwable t){
			RecordingException.castAsDomainException(t,RecordingStage.TRANSFER, RecordingDevice.RECORDER );
		}
	}
	
	@Override public String stopAndReturnCreatedFile() throws RecordingException {
		if (currentFile == null) throw new RecordingException(RecordingStage.STOP, "VideoFrameRecorder is already stoppped,");
		String fileNameToReturn = currentFile;
		try {
			super.stop();
		} catch(Throwable t){
			RecordingException.castAsDomainException(t,RecordingStage.STOP, RecordingDevice.RECORDER );
		}
		currentFile = null;
		return fileNameToReturn;
	}
}
