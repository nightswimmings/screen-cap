package org.nightswimming.screener.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.nightswimming.screener.enums.VideoContainer;

public class FileGenerator {
	
	private final Path folder;
	private DateFormat timeFormatter = new SimpleDateFormat("yy-MM-dd HH.mm.ss");
	private AtomicInteger index = new AtomicInteger(0);
	
	public FileGenerator(Path baseFolder) throws IOException{
		String timestamp = timeFormatter.format(new Date()); 
		this.folder = baseFolder.resolve(timestamp+"/");
	}
	
	public String generateFile(boolean screenshot, VideoContainer container) throws IOException{
		Files.createDirectories(folder);
		return folder.resolve(""+index.incrementAndGet()+container.getExttension()).toAbsolutePath().toString();
	}
}
