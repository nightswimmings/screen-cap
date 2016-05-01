package org.nightswimming.screener.rec;

import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_H264;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_NONE;
import static org.nightswimming.screener.enums.VideoRecFormat.PRESERVATION;
import static org.nightswimming.screener.enums.VideoRecFormat.PRODUCTION;
import static org.nightswimming.screener.enums.VideoRecFormat.PRODUCTION2;

import java.util.EnumSet;

import org.nightswimming.screener.enums.RecArea;
import org.nightswimming.screener.enums.VideoRecFormat;
import org.nightswimming.screener.util.FileGenerator;

public final class MediaRecorderFactory {
	
	public static ScreenFrameGrabber createWinScreenGrabber(){
		WinScreenFrameGrabber grabber = new WinScreenFrameGrabber(false);
		grabber.setFrameRate(30); //If 0, default is decided at codec level
	    //TODO:grabber.setVideoOption("video_size", "");
	    //TODO:grabber.setVideoOption("offset_x","");
	    //TODO:grabber.setVideoOption("offset_y","");
		
		//TODO: GDIGRAB does not support audio yet
	    grabber.setAudioChannels(0);
	    grabber.setAudioCodec(AV_CODEC_ID_NONE);
	    
		return grabber;
	}
	public static ScreenFrameGrabber createWinScreenshotGrabber(){
		WinScreenFrameGrabber grabber = new WinScreenFrameGrabber(true);
		grabber.setFrameRate(30); //If 0, default is decided at codec level
		return grabber;
	}
	
	/*public static FrameRecorder createAudioRecorder(String fileName, AudioRecFormat recCodec){
		recorder.setVideoCodec(AV_CODEC_ID_NONE);
	}*/
	
	public static RegenerableFrameRecorder createScreenshotRecorder(FileGenerator fileGenerator, VideoRecFormat recCodec){
		return new ScreenshotRecorder(fileGenerator, recCodec);
	}
	
	public static RegenerableFrameRecorder createVideoRecorder(FileGenerator fileGenerator, RecArea recArea, VideoRecFormat recCodec, boolean silent){
		
		VideoFrameRecorder recorder = new VideoFrameRecorder(fileGenerator, recArea, recCodec.getContainer());
	    recorder.setFormat(recCodec.getContainer().getFormatLabel());
		recorder.setVideoCodec(recCodec.getVideoCodec());
		recorder.setAudioCodec(recCodec.getAudioCodec());
																   
	    //Default Implicits
		//recorder.setFrameRate(30); 
	    //recorder.setInterleaved(true);
		
		/* Video Quality Options */
		// 2000 kb/s, reasonable "sane" area for 720
	    //recorder.setVideoBitrate(2000000);
		recorder.setGopSize(60); //Ojo amb raw on �s 1..Key Frame Interval every 2 secs at 30 fps (Default) [-1 to be managed by codec] (ajustar tb segons hz de la pantalla tb q es vegi millor)
		if(recCodec.getVideoCodec() == AV_CODEC_ID_H264){
			//https://trac.ffmpeg.org/wiki/Encode/H.264
			// tradeoff between size and cpu ultrafast,superfast, veryfast, faster, fast, medium, slow, slower, veryslow
			recorder.setVideoOption("preset", "ultrafast");
		    recorder.setVideoOption("crf", "28");
		    recorder.setVideoOption("tune", "zerolatency");//For streaming, but recording? film, animation, grain, stillimage, psnr, ssim, fastdecode, zerolatency
		    recorder.setVideoOption("profile","baseline"); //baseline, main, high, high10, high422, high444
		}
	    
		/* Audio Options */
		//Default Implicits
    	//recorder.setAudioChannels(2);
    	//recorder.setSampleRate(44100);	
		if (silent){
		    recorder.setAudioChannels(0);
		    recorder.setAudioCodec(AV_CODEC_ID_NONE);
		} else if (EnumSet.of(PRESERVATION,PRODUCTION,PRODUCTION2).contains(recCodec)){
			//recorder.setPixelFormat(AV_PIX_FMT_GBRP10LE);
			//recorder.setPixelFormat(AV_PIX_FMT_RGB32);
			//recorder.setPixelFormat(AV_PIX_FMT_YUV422P10LE); //What to use for RAWVIDEO?
			recorder.setSampleRate(96000);	        
		}
		//else if (EnumSet.of(APPLE,GOPRO,SONY).contains(recCodec)){ } 
		else {
			recorder.setAudioBitrate(64000);
	        recorder.setAudioOption("crf", "0"); //VBR
	        recorder.setAudioQuality(0); // Highest quality
	    }
	    
	    return recorder;
	}
}
