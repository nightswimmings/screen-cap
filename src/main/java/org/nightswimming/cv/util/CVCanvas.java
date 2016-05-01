package org.nightswimming.cv.util;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.nightswimming.cv.CVFilter;
import org.nightswimming.screener.util.tuple.NTuple;
import org.nightswimming.screener.util.tuple.Tuple;

public final class CVCanvas<P extends NTuple<?,?>> extends CanvasFrame {

	private static final long serialVersionUID = 1L;

	private final Mat imageMat;
	private final String title;
	private final CVFilter<P> filter;
	private final NTuple<CVParam<?>,?> params;
	
	@SuppressWarnings("unchecked")
	public <N extends NTuple.Size, T extends NTuple<?,N>> CVCanvas(String title, Frame imageFrame, CVFilter<T> filter, NTuple<CVParam<?>,N> params) throws InterruptedException{
		super(title, 1); //1=No gamma correction
		this.setCanvasSize(600,400);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.imageMat = CVUtils.toMat(imageFrame);
		this.filter =  (CVFilter<P>) filter;
		this.params = params;
		this.title = title;
		build();
	}
	
	private void renderOriginalPic(){
		this.showImage(CVUtils.toFrame(imageMat));
	}
	private void renderCurrentFilterSetting(final JComponent processingSign){
     	 processingSign.setVisible(true);
     	 
		 SwingWorker<Frame,Void> worker = new SwingWorker<Frame,Void>() {
             protected Frame doInBackground() throws Exception {
            	 String logMsg = "Executing "+title;
            	 if (params.size() > 0) logMsg +=  params.asList().stream().map(CVParam::toString).collect(Collectors.joining(","," with ","."));
            	 System.out.println(logMsg);	
            	 Frame resultFrame;
	         	 if (params.size() > 0){
	         		@SuppressWarnings("unchecked")
	         		P tupleParams = (P) CVCanvas.extractParams(params);
	         		resultFrame = filter.applyAsFrame(imageMat,tupleParams);
	         	 } 
	         	 else resultFrame = filter.applyAsFrame(imageMat); 
             	return resultFrame;
             }

             protected void done() {
            	try {
					showImage(get());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				} finally {
					processingSign.setVisible(false);
				}
             }
         };
         worker.execute();
	}
	
	private void build() throws InterruptedException{
		renderOriginalPic();
		
		if (!Objects.isNull(filter)){ 
			this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
			
			final JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(false);
            
            final JLabel processingSign = new JLabel("Processing...");
            processingSign.setForeground(Color.RED);
            processingSign.setFont(new Font("Serif", Font.BOLD, 14));
            processingSign.setVisible(false);
            
			final JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
			checkBoxPanel.add(Box.createHorizontalStrut(120));
			checkBoxPanel.add(checkBox);
			checkBoxPanel.add(new JLabel(title+" Processing"));
			checkBoxPanel.add(Box.createHorizontalStrut(90));
			checkBoxPanel.add(processingSign);
			this.add(checkBoxPanel);
			
			final List<JSlider> sliders = new ArrayList<>(params.size());
			
			for (CVParam<?> param : params.asList()){
				final JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
				sliderPanel.add(new JLabel(param.label));
				final JSlider slider = new JSlider(JSlider.HORIZONTAL, param.min, param.max, param.cur);
				slider.setEnabled(false);
				slider.addChangeListener(new ChangeListener() {
					@Override 
					public void stateChanged(ChangeEvent e){
						JSlider source = (JSlider) e.getSource();
				        if (!source.getValueIsAdjusting()) {
				            int val = (int)source.getValue();
				            param.setCurValue(val);
				            renderCurrentFilterSetting(processingSign);
				        }    			
					}
				});
				sliderPanel.add(slider);
				this.add(sliderPanel);
				sliders.add(slider);
			}
			
			checkBox.addActionListener(new ActionListener() {
                @Override 
                public void actionPerformed(ActionEvent e) {
                	boolean selected = checkBox.isSelected();
                    sliders.forEach(x -> x.setEnabled(selected));
                    if(selected)  renderCurrentFilterSetting(processingSign);
                    else 		  renderOriginalPic();
                }
            });
				
		}
		this.pack();
		this.setLocationRelativeTo(null);
		waitForCanvasToFinish(this);
	}
	
	public static class CVParam<R> {
		public final String label;
		public final int min, max;
		private int cur;
		private IntFunction<R> exporter;
		
		public CVParam (String label, int min, int max, int cur, IntFunction<R> exporter){
			this.label = label;
			this.min = min;
			this.max = max;
			this.cur = cur;
			this.exporter = exporter;
		}
		public void setCurValue(int cur){ this.cur = cur; }
		public R getCurValue(){
			return this.exporter.apply(cur);
		}
		public String toString(){ return label+"="+getCurValue(); }
	}
	
	private static <N extends NTuple.Size> NTuple<Object,N> extractParams(NTuple<CVParam<?>,N> cvParams){
		Object[] params = cvParams.asList().stream().map(param -> param.getCurValue()).toArray(); //Not spellable as CVParam::getCurValue due to a bug in some Java8 versions
		@SuppressWarnings("unchecked")
		NTuple<Object,N> tupleParams = (NTuple<Object, N>) Tuple.of(params);
		return tupleParams;
	}
	
	private static void waitForCanvasToFinish(CanvasFrame canvas) throws InterruptedException{
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	    service.scheduleWithFixedDelay(new Runnable(){
	        @Override public void run(){
	          if(!canvas.isVisible()) {
	        	  service.shutdownNow();
	          }
	        }
	     }, 0, 1, TimeUnit.SECONDS);
	    service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
	    System.out.println("Frame Showcase Terminated!");
	}
}
