package org.nightswimming.cv;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bytedeco.javacpp.avutil;
import org.jnativehook.GlobalScreen;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.nightswimming.cv.CVFilter;
import org.nightswimming.cv.FiltersShow;
import org.nightswimming.cv.util.CVCanvas.CVParam;
import org.nightswimming.screener.util.tuple.Tuple;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FiltersTest {
	@Rule public final TemporaryFolder tmpFolder = new TemporaryFolder();
	@Rule public final ExpectedException exception = ExpectedException.none();
	private static Path testPicture;
	//private final Timer timer = new Timer();
	
	@BeforeClass
	public static void setUp() throws IOException, URISyntaxException{
		avutil.av_log_set_level(avutil.AV_LOG_QUIET);
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);	
		testPicture = Paths.get(FiltersTest.class.getClassLoader().getResource("MessiNeymar.jpg").toURI());
	}
	
	@Test
	public void simpleLocalImageShouldShow() throws InterruptedException, URISyntaxException{
		FiltersShow.showLocalImage(testPicture);
	}
	@Test
	public void simpleScreenshotShouldShow() throws InterruptedException{
		FiltersShow.showScreenImage();
	}
	
	@Test
	public void localImageShouldShowWithFixedFiltering() throws InterruptedException, URISyntaxException{
		FiltersShow.showLocalImageFixedFiltering("Stylization", testPicture, CVFilter.stylize(60f,0.45f));
	}
	@Test
	public void screenshotShouldShowWithFixedFiltering() throws InterruptedException, URISyntaxException{
		FiltersShow.showScreenImageFixedFiltering("Sketching", CVFilter.sketch(60f, 0.07f, 0.02f, false));
	}
	
	@Test
	public void localImageShouldShowWithDynFiltering() throws InterruptedException, URISyntaxException{
		FiltersShow.showLocalImageDynamicFiltering("Sketching", testPicture,
				CVFilter.sketch(),
			    Tuple.ofTyped(new CVParam<Float>("σS",0,200,60,x -> (float)x),
			    			  new CVParam<Float>("σR",0,100,7,x -> x/100f),
			    			  new CVParam<Float>("shade",0,10,2,x -> x/100f),
			    			  new CVParam<Boolean>("mono",0,1,1,x -> x==1)
			    			 )
		);
	}

	@Test
	public void screenshotShouldShowWithDynFiltering() throws InterruptedException, URISyntaxException{
		FiltersShow.showScreenImageDynamicFiltering("Stylization", 
				CVFilter.stylize(),
			    Tuple.ofTyped(new CVParam<Float>("σS",0,200,60,x -> (float)x),
			    			  new CVParam<Float>("σR",0,100,45,x -> x/100f)
			    			 )
				);
	}

	//test the other 2 new filters, edgePreserving and DetailEnhancer
	//Test it by comparing to a golden master transformed file -> Frame sketched = CVFilter.sketch(60f, 0.07f, 0.02f, false).applyToFile(testPicture);
}
