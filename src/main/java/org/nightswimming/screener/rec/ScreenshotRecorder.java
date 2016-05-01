package org.nightswimming.screener.rec;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.nightswimming.screener.RecordingException;
import org.nightswimming.screener.RecordingException.RecordingStage;
import org.nightswimming.screener.enums.VideoContainer;
import org.nightswimming.screener.enums.VideoRecFormat;
import org.nightswimming.screener.util.FileGenerator;

public class ScreenshotRecorder implements RegenerableFrameRecorder{
	//TODO: Make it immutable?
	private final VideoContainer format;
	private final FileGenerator fileGenerator;
	private final Java2DFrameConverter converter;
	private volatile String currentFile;
	
	public ScreenshotRecorder(FileGenerator fileGenerator, VideoRecFormat recCodec){
		//TBD: recCodec.getVideoCodec() is useful here?
		this.currentFile = null;
		this.format = recCodec.getContainer();
		this.fileGenerator = fileGenerator;
		this.converter = new Java2DFrameConverter();
	}

	@Override public void start() throws RecordingException {
		if (currentFile != null) throw new RecordingException(RecordingStage.START, "ScreenshotRecorder is already recording to a file named '"+currentFile+"'" );
		try{
			currentFile = fileGenerator.generateFile(true,format);
		}
		catch (Throwable e){
			throw new RecordingException(RecordingStage.START, "ScreenshotRecorder error creating the screenshot file '"+currentFile+"'" + e.getMessage());
		}
		
	}

	@Override
	public void record(Frame frame) throws RecordingException {
		if (currentFile == null) throw new RecordingException(RecordingStage.TRANSFER, "ScreenshotRecorder should be started before recording." );
		
		try{
			BufferedImage frameImage = converter.convert(frame);
			//TBD: FormatLabel optimizes transcoding according to input fomat?
			//TBD: USe NIO for faster speed?
			ImageIO.write(frameImage, format.getFormatLabel(), new File(currentFile));
		} 
		catch (Throwable e){
			throw new RecordingException(RecordingStage.TRANSFER, "ScreenshotRecorder error transcoding frame to the screenshot file '"+currentFile+"': "+e.getMessage());
		}
	}

	@Override
	public String stopAndReturnCreatedFile() throws RecordingException {
		if (currentFile == null) throw new RecordingException(RecordingStage.STOP, "ScreenshotRecorder is already stoppped,");
		String fileNameToReturn = currentFile;
		currentFile = null;
		return fileNameToReturn;
	}
}
