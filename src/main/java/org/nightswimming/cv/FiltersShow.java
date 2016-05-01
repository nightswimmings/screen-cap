package org.nightswimming.cv;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.Frame;
import org.jnativehook.GlobalScreen;
import org.nightswimming.cv.util.CVCanvas;
import org.nightswimming.cv.util.CVUtils;
import org.nightswimming.cv.util.CVCanvas.CVParam;
import org.nightswimming.screener.rec.MediaRecorderFactory;
import org.nightswimming.screener.rec.ScreenFrameGrabber;
import org.nightswimming.screener.util.tuple.NTuple;
import org.nightswimming.screener.util.tuple.Tuple;

public class FiltersShow {
	
	public static void showLocalImage(Path imageFile) throws InterruptedException{
		showLocalImageDynamicFiltering("Frame Display", imageFile, null, null);
	}
	
	public static void showLocalImageFixedFiltering(String filterName, Path imageFile, CVFilter<Tuple.Void> filter) throws InterruptedException{
		showLocalImageDynamicFiltering(filterName, imageFile, filter, Tuple.ofTyped());
	}
	
	public static <N extends NTuple.Size, T extends NTuple<?,N>> void showLocalImageDynamicFiltering(String filterName, Path imageFile, CVFilter<T> filter, NTuple<CVParam<?>,N> params) throws InterruptedException{
		Frame picture = CVUtils.toFrame(imageFile);
		new CVCanvas<>(filterName + " ShowCase", picture, filter, params);
	}	
	
	public static void showScreenImage() throws InterruptedException{
		showScreenImageDynamicFiltering("Frame Display", null, null);
	}
	
	public static void showScreenImageFixedFiltering(String filterName, CVFilter<Tuple.Void> filter) throws InterruptedException{
		showScreenImageDynamicFiltering(filterName, filter, Tuple.ofTyped());
	}
	
	public static <N extends NTuple.Size, T extends NTuple<?,N>> void showScreenImageDynamicFiltering(String filterName, CVFilter<T> filter, NTuple<CVParam<?>,N> params) throws InterruptedException{
		ScreenFrameGrabber grabber = MediaRecorderFactory.createWinScreenshotGrabber();
		grabber.start();
		Frame snapshot = grabber.grab();
		new CVCanvas<>(filterName + " ShowCase", snapshot, filter, params);
		grabber.stop();	
	}	
	
	//TODO: Convenient temporary main
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws InterruptedException{
		
		avutil.av_log_set_level(avutil.AV_LOG_QUIET);
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);	
		//Improve it with JParameter
		
		boolean printUsage = false;
		
		if (args.length < 1 || args.length > 2){
			printUsage = true;
		} else {
			@SuppressWarnings("rawtypes") NTuple params = null;
			@SuppressWarnings("rawtypes") CVFilter filter = null;
			
			switch (args[0]){
				case "sketching":
					filter = CVFilter.sketch();
					params = Tuple.ofTyped(
						  new CVParam<Float>("σS",0,200,60,x -> (float)x),
		    			  new CVParam<Float>("σR",0,100,7,x -> x/100f),
		    			  new CVParam<Float>("shade",0,10,2,x -> x/100f),
		    			  new CVParam<Boolean>("mono",0,1,1,x -> x==1)
		    			 );		    	
					break;
				case "stylization":
					filter = CVFilter.stylize();
					params = Tuple.ofTyped(
						new CVParam<Float>("σS",0,200,60,x -> (float)x),
				    	new CVParam<Float>("σR",0,100,45,x -> x/100f)
						);
					break;
				case "detailEnhancer":
					filter = CVFilter.detailEnhancer();
					params = Tuple.ofTyped(
						new CVParam<Float>("σS",0,200,10,x -> (float)x),
				    	new CVParam<Float>("σR",0,100,15,x -> x/100f)
						);
					break;
				case "edgePreserving":
					filter = CVFilter.edgePreserving();
					params = Tuple.ofTyped(
						  new CVParam<Boolean>("recursive",0,1,1,x -> x==1),
						  new CVParam<Float>("σS",0,200,60,x -> (float)x),
		    			  new CVParam<Float>("σR",0,100,40,x -> x/100f) //Though not sure it computes centessimals
		    			 );	
					break;
				default: {
					System.err.println("Unknown Filter");
					printUsage = true;
				}
			}
				
			if (printUsage){
				System.out.println(">>> Usage: FilterShow [stylization|sketching|detailEnhancer|edgePreserving] (file)");
			} else {
				if(args.length == 2) FiltersShow.showLocalImageDynamicFiltering(args[0],Paths.get(args[1]),filter,params);
				else  				 FiltersShow.showScreenImageDynamicFiltering(args[0],filter,params);
			}
		}
	}	
}
