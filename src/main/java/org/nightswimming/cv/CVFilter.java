package org.nightswimming.cv;


import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_photo.detailEnhance;
import static org.bytedeco.javacpp.opencv_photo.edgePreservingFilter;
import static org.bytedeco.javacpp.opencv_photo.pencilSketch;
import static org.bytedeco.javacpp.opencv_photo.stylization;

import java.nio.file.Path;
import java.util.function.BiFunction;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.Frame;
import org.nightswimming.cv.util.CVUtils;
import org.nightswimming.screener.util.tuple.AbstractTuple;
import org.nightswimming.screener.util.tuple.NTuple;
import org.nightswimming.screener.util.tuple.Tuple;
import org.nightswimming.screener.util.tuple.Tuple.Pair;
import org.nightswimming.screener.util.tuple.Tuple.Quartet;
import org.nightswimming.screener.util.tuple.Tuple.Triplet;

@FunctionalInterface
public interface CVFilter<P extends NTuple<?,?>> extends BiFunction<Mat,P,Mat> {
	
	/******************************************
	 *          CV Algorithms                 *
	 ******************************************/
	
	//http://docs.opencv.org/3.0-beta/modules/photo/doc/npr.html	
	static CVFilter<Tuple.Void> stylize(float sigma_s, float sigma_r){
		return (input, pair) -> {
			Mat output = new Mat();
			stylization(input, output, sigma_s, sigma_r);
			return output;
		};
	}
	
	static CVFilter<Pair<Float,Float>> stylize(){
		return (input, pair) -> {
			Pair<Float,Float> saneParams = fillUpNulls(pair, Tuple.of(60f, 0.45f));
			float sigma_s = saneParams.getA();
			float sigma_r = saneParams.getB();
			
			Mat output = new Mat();
			stylization(input, output, sigma_s, sigma_r);
			return output;
		};
	}
	
	static CVFilter<Tuple.Void> sketch(float sigma_s, float sigma_r, float shade_factor, boolean monochrome){
		return (input, quartet) -> {
			Mat monoOutput = new Mat();
			Mat colorOutput = new Mat();
			pencilSketch(input, monoOutput, colorOutput, sigma_s, sigma_r, shade_factor);
			return monochrome ? monoOutput : colorOutput;
		};
	}
	
	static CVFilter<Quartet<Float,Float,Float,Boolean>> sketch(){
		return (input, quartet) -> {
			Mat monoOutput = new Mat();
			Mat colorOutput = new Mat();
			Quartet<Float,Float,Float,Boolean> saneParams = fillUpNulls(quartet, Tuple.of(60f, 0.07f,0.02f,true));
			float sigma_s = saneParams.getA();
			float sigma_r = saneParams.getB();
			float shade_factor = saneParams.getC();
			boolean monochrome = saneParams.getD();
			pencilSketch(input, monoOutput, colorOutput, sigma_s, sigma_r, shade_factor);
			return monochrome ? monoOutput : colorOutput;
		};
	}
	
	static CVFilter<Tuple.Void> detailEnhancer(float sigma_s, float sigma_r){
		return (input, pair) -> {
			Mat output = new Mat();
			detailEnhance(input, output, sigma_s, sigma_r);
			return output;
		};
	}
	
	static CVFilter<Pair<Float,Float>> detailEnhancer(){
		return (input, pair) -> {
			Pair<Float,Float> saneParams = fillUpNulls(pair, Tuple.of(10f, 0.15f));
			float sigma_s = saneParams.getA();
			float sigma_r = saneParams.getB();
			
			Mat output = new Mat();
			detailEnhance(input, output, sigma_s, sigma_r);
			return output;
		};
	}
	
	//Using RECURSIVE option is about 3.5x faster
	static CVFilter<Tuple.Void> edgePreserving(boolean recursive, float sigma_s, float sigma_r){
		return (input, triplet) -> {
			Mat output = new Mat();
			edgePreservingFilter(input, output, recursive?1:2, sigma_s, sigma_r);
			return output;
		};
	}
	
	static CVFilter<Triplet<Boolean,Float,Float>> edgePreserving(){
		return (input, triplet) -> {
			Mat output = new Mat();
			Triplet<Boolean,Float,Float> saneParams = fillUpNulls(triplet, Tuple.of(true,60f,0.4f));
			boolean recursive  = saneParams.getA();
			float sigma_s = saneParams.getB();
			float sigma_r = saneParams.getC();
			edgePreservingFilter(input, output, recursive?1:2, sigma_s, sigma_r);
			return output;
		};
	}
	
	static CVFilter<Tuple.Void> grey(){
		return (input, ignore) -> {
			Mat output = new Mat();
			cvtColor(input, output, CV_BGR2GRAY);
			return output;
		};
	}
	
	
	/******************************************
	 *        I/O convenience methods         *
	 ******************************************/
	
	default Frame applyAsFrame(Path inputImage){
		return applyAsFrame(inputImage, null);
	}
	default Frame applyAsFrame(Frame inputFrame){
		return applyAsFrame(inputFrame, null);
	}
	default Frame applyAsFrame(Mat inputMat){
		return applyAsFrame(inputMat, null);
	}
	
	default Frame applyAsFrame(Path inputImage, P paramsTuple){
		Mat output = this.apply(inputImage, paramsTuple);
		return CVUtils.toFrame(output);
	}
	default Frame applyAsFrame(Frame inputFrame, P paramsTuple){
		Mat output = this.apply(inputFrame, paramsTuple);
		return CVUtils.toFrame(output);
	}
	default Frame applyAsFrame(Mat inputMat, P paramsTuple){
		Mat output = this.apply(inputMat, paramsTuple);
		return CVUtils.toFrame(output);
	}
	
	default Mat apply(Path inputImage){
		return this.apply(inputImage, null);
	}
	default Mat apply(Frame inputFrame){
		return this.apply(inputFrame, null);
	}
	default Mat apply(Mat inputMat){
		return this.apply(inputMat, null);
	}
	default Mat apply(Path inputImage, P paramsTuple){
		Mat inputMat = CVUtils.toMat(inputImage);
		return this.apply(inputMat, paramsTuple);
	}
	default Mat apply(Frame inputFrame, P paramsTuple){
		Mat inputMat = CVUtils.toMat(inputFrame);
		return this.apply(inputMat, paramsTuple);
	}
	@Override Mat apply(Mat mat, P paramsTuple);
	
	//UNDERSTAND CODE FOR SOBEL
	/*public static Mat sobel(Mat input){
		
		//Sobel requires working in 32f bits so we need to convert them after to 8U, the std to show and display
		Mat greyedInput = new Mat();
		cvtColor(input, greyedInput, CV_BGR2GRAY);
		
		Mat sobelX = new Mat();
		Sobel(greyedInput, sobelX, CV_32F, 1, 0);
	    Mat sobelY = new Mat();
		Sobel(greyedInput, sobelY, CV_32F, 0, 1);
		Mat sobel = sobelX.clone();
		magnitude(sobelX, sobelY, sobel);
		
		DoublePointer min = new DoublePointer(1);
		DoublePointer max = new DoublePointer(1);
		minMaxLoc(sobel, min, max, null, null, new Mat());
		  
		 // Threshold edges: extract foreground
		Mat output = new Mat();
	    threshold(sobel, output, 100, 255, THRESH_BINARY_INV);
	    
	    //Sense scaling:  scale=1d, offset=0d
	    double scale = 255.0/(max.get() - min.get());
	    double offset = -min.get() * scale;
	    output.convertTo(output, CV_8U, scale, offset);
		return  output;
	}*/
	
	@SuppressWarnings("unchecked")
	static <T,N extends NTuple.Size,P extends NTuple<T,N>> P fillUpNulls(P tuple, P defaults){
		P result = (P) defaults.clone();
		if (tuple != null){
			for (int i=0; i<tuple.size();i++){
				if (tuple.getAt(i)!=null) 
					((AbstractTuple<T,N,P>)result).setUnsafeAt(i, (T) tuple.getAt(i)); 
			}
		}
		return result;
	}
}


