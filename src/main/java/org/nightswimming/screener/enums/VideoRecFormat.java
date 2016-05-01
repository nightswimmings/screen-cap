package org.nightswimming.screener.enums;

import static org.bytedeco.javacpp.avcodec.*;

public enum VideoRecFormat {
	
	/* STILL IMAGE */
	STILL_STD     (AV_CODEC_ID_JPEGLS,  AV_CODEC_ID_NONE, VideoContainer.JPG),
	STILL_OPEN    (AV_CODEC_ID_PNG,  AV_CODEC_ID_NONE, VideoContainer.PNG),
	STILL_RAW     (AV_CODEC_ID_BMP, AV_CODEC_ID_NONE, VideoContainer.BMP),

	/* LEGACY */
	FLASH         (AV_CODEC_ID_FLV1,  AV_CODEC_ID_AAC, VideoContainer.FLV),
	MOBILE3G      (AV_CODEC_ID_H263,  AV_CODEC_ID_AAC, VideoContainer._3GP),
	COMPATIBILITY (AV_CODEC_ID_MPEG4, AV_CODEC_ID_AC3, VideoContainer.AVI),
	
	/* LOSSY HQ */
	STD          (AV_CODEC_ID_H264,  AV_CODEC_ID_AAC, VideoContainer.MP4), //MKV not widely supported	
	EXPERIMENTAL (AV_CODEC_ID_HEVC,  AV_CODEC_ID_OPUS, VideoContainer.MP4),

	/* VFX */
	//APPLE        (AV_CODEC_ID_PRORES,  AV_CODEC_ID_PCM_S16LE, VideoContainer.MOV),
	//GOPRO      (AV_CODEC_ID_CINEFORM(VC5), AV_CODEC_ID_PCM_S16LE, VideoContainer.MXF),
	//SONY       (AV_CODEC_ID_XAVC, AV_CODEC_ID_PCM_S16LE, VideoContainer.MXF) //XAVC H.264
	
	/* LOSSLESS */
	PRESERVATION (AV_CODEC_ID_FFV1,  AV_CODEC_ID_PCM_S24LE, VideoContainer.MKV), //Waiting for FFV1 catch MagicYUV perfoamce
	PRODUCTION   (AV_CODEC_ID_RAWVIDEO, AV_CODEC_ID_PCM_S24LE, VideoContainer.DPX),
	PRODUCTION2  (AV_CODEC_ID_RAWVIDEO, AV_CODEC_ID_PCM_S24LE, VideoContainer.EXR);
			
		
	private final int videoCodec, audioCodec;
	private final VideoContainer container;
	
	private VideoRecFormat(int videoCodec, int audioCodec, VideoContainer container){
		this.videoCodec = videoCodec;
		this.audioCodec = audioCodec;
		this.container = container;		
	}
	
	public int getVideoCodec()   { return this.videoCodec; }
	public int getAudioCodec()   { return this.audioCodec; }
	public VideoContainer getContainer() { return this.container; }
}