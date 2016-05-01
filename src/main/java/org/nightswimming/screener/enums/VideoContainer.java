package org.nightswimming.screener.enums;

public enum VideoContainer {
	/* LEGACY */
	FLV ("flv",".flv"), //Adobe   
	_3GP ("3gp",".3gp"), //3GPP, (and .3G2 v2) based on MPEG
	AVI ("avi", ".avi"), //Microsoft (Limitations)
	
	/* LOSSY HQ */
	MOV ("mov", ".mov"), //Apple
	MP4 ("mp4",".mp4"), //MPEG (standard)      
	MKV ("matroska",".mkv"), //Open Standard (most complete but not widely supported)
	
	/* Intermediate, VFX */
	MXF("mxf",".mfx"),
	
	/* Pro-VFX, Single Image */
	DPX ("dpx",".dpx"), //Legacy Standard
	EXR ("exr", ".exr"),
	
	/* STILL IMAGE */
	PNG ("png",".png"),
	JPG ("jpeg",".jpg"),
	BMP ("bmp",".bmp");
	
	private final String formatLabel, extension;
	
	private VideoContainer(String formatLabel, String extension){
		this.formatLabel = formatLabel;
		this.extension = extension;
	}	
	public String getFormatLabel() { return this.formatLabel; }
	public String getExttension()  { return this.extension; }
}