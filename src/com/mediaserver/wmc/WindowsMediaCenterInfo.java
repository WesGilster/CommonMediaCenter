package com.mediaserver.wmc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cfs.io.IOUtilities;

public class WindowsMediaCenterInfo implements Comparable<WindowsMediaCenterInfo> {
	private static final Logger logger = LoggerFactory.getLogger(WindowsMediaCenterInfo.class);
	private static final String FILE_SEPARATOR_REGEX = File.separator.replaceAll("\\\\", "\\\\\\\\");
	private DVDIdDisk disk;
	private File diskFile;
	private WindowsMediaCenterMetaData metaData;
	private File metaDataFile;
	private File videoTSFile;
	private transient String flatFolders;
	private transient String firstFolder;
	private transient String basePath;
	private transient String releaseYear = null;
	
	public WindowsMediaCenterInfo(File dvdDisk, String basePath) {
		this.diskFile = dvdDisk;
		this.basePath = basePath;
		
		if (!diskFile.exists())
			logger.error("DVDDisk file must exist.", new FileNotFoundException(dvdDisk.toString()));
		
		this.videoTSFile = new File(diskFile.getParentFile(), "VIDEO_TS");
		InputStreamReader reader = null;
		try {
			Unmarshaller unmarshaller = WindowsMediaCenterManager.getJAXBContext().createUnmarshaller();
			unmarshaller.setSchema(null);
			disk = (DVDIdDisk)unmarshaller.unmarshal(diskFile);
			
			metaDataFile = new File(WindowsMediaCenterManager.MEDIA_CENTER_INFO_CACHE + "\\" + disk.getId().replaceAll("[|]", "-") + ".xml");
			if (!metaDataFile.exists())
				return;
			
			reader = new InputStreamReader(new FileInputStream(metaDataFile), "ISO-8859-1");
			metaData = (WindowsMediaCenterMetaData)unmarshaller.unmarshal(reader);
		} catch (Exception e) {
			logger.error("JAXB Not available, are you running Java 1.5 or later?", e);
		} finally {
			if (reader != null)
				try {reader.close();} catch (Exception ex) {}
		}
	}
	
	public File getVideoTSFile() {
		return videoTSFile;
	}
	
	public File getThumbnailFile() {
		if (metaData == null)
			return null;
		
		return metaData.getThumbnailFile();
	}
	
	public String getTitle() {
		return metaData != null && metaData.getTitle() != null?metaData.getTitle():disk.getName();
	}
	
	public List<String> getLeadPerformer() {
		return metaData == null?null:metaData.getLeadPerformer();
	}
	
	public List<String> getGenre() {
		return metaData == null?null:metaData.getGenre();
	}
	
	public String getStudio() {
		return metaData == null?null:metaData.getStudio();
	}
	
	public List<String> getDirector() {
		return metaData == null?null:metaData.getDirector();
	}
	
	public String getMPAARating() {
		return metaData == null?null:metaData.getMPAARating();
	}
	
	public String getRating() {
		return metaData == null?null:metaData.getRating();
	}
	
	public String getReleaseYear() {
		if (releaseYear != null || metaData == null || metaData.getReleaseDate() == null)
			return releaseYear;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(metaData.getReleaseDate());
		return cal.get(Calendar.YEAR) + "";
	}
	
	public String getFlatFolders() {
		if (flatFolders != null)
			return flatFolders;
		
		flatFolders = IOUtilities.getRelativePath(basePath, diskFile.getParentFile().getParent()).substring(1);
		return flatFolders;
	}
	
	public String getFirstFolder() {
		if (firstFolder != null)
			return firstFolder;
		
		String[] paths = IOUtilities.getRelativePath(basePath, diskFile.getParentFile().getParent()).substring(1).split(FILE_SEPARATOR_REGEX);
		if (paths.length < 2)
			throw new IllegalArgumentException(diskFile.getAbsolutePath() + " is busted");
		
		firstFolder = paths[1];
		return firstFolder;
	}
	
	public WindowsMediaCenterMetaData getWindowsMediaCenterMetaData() {
		if (metaData == null) {
			metaData = new WindowsMediaCenterMetaData();
		}
		
		return metaData;
	}
	
	public DVDIdDisk getDVDIdDisk() {
		return disk;
	}
	
	public void saveWindowsMediaCenterMetaData() {
		FileWriter writer = null;
		try {
			Marshaller marshaller = WindowsMediaCenterManager.getJAXBContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			writer = new FileWriter(metaDataFile);
			writer.write("<?xml version=\"1.0\"?>\n");
			marshaller.marshal(metaData, writer);
		} catch (Exception e) {
			logger.error("JAXB Not available, are you running Java 1.5 or later?", e);
		} finally {
			if (writer != null)
				try { writer.close(); } catch (Exception e) {}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((disk == null) ? 0 : disk.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WindowsMediaCenterInfo other = (WindowsMediaCenterInfo) obj;
		if (disk == null) {
			if (other.disk != null)
				return false;
		} else if (!disk.equals(other.disk))
			return false;
		return true;
	}

	@Override
	public int compareTo(WindowsMediaCenterInfo o) {
		int compareValue = getTitle().compareTo(o.getTitle());
		if (compareValue != 0)
			return compareValue;
		
		return hashCode() - o.hashCode();
	}

	@Override
	public String toString() {
		return getTitle();
	}
}
