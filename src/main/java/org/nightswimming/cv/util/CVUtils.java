package org.nightswimming.cv.util;

import java.nio.file.Path;
import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class CVUtils {

	final static OpenCVFrameConverter<Mat> converter
			= new OpenCVFrameConverter.ToMat();
	
	public static Mat toMat(Frame inputFrame){
		return converter.convert(inputFrame);
	}
	public static Mat toMat(Path inputImage){
		return imread(inputImage.toAbsolutePath().toString(), IMREAD_COLOR);
	}
	public static Frame toFrame(Mat mat){
		return converter.convert(mat);
	}
	public static Frame toFrame(Path inputImage){
		return toFrame(toMat(inputImage)); //? Improve?
	}
}
