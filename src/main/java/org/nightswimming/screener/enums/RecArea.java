package org.nightswimming.screener.enums;

import java.awt.Dimension;
import java.awt.Toolkit;

public enum RecArea {
	FULL(Toolkit.getDefaultToolkit().getScreenSize());
	
	private final int width, height;
	
	private RecArea(Dimension rectangle){
		this((int) rectangle.getWidth(), (int) rectangle.getHeight());
	}
	private RecArea(int width, int height) { 
		this.width = width;
		this.height = height;
	}
	
	public Integer getWidth() { return this.width; }	
	public Integer getHeight(){ return this.height; }
}