package com.mediaserver.wmc;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cfs.io.FileTreeEnumeration;
import com.cfs.os.WindowsRegistry;
import com.cfs.os.WindowsRegistryRoot;
import com.cfs.progress.HistoricalTimedProgressMonitor;
import com.cfs.progress.ProgressableObjectMonitor;
import com.cfs.util.RegexFileFilter;
import com.cfs.util.RegexFileFilter.MatchType;
import com.cfs.util.WildcardFilenameFilter;

public class WindowsMediaCenterManager<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(WindowsMediaCenterManager.class);

	public static String MEDIA_CENTER_BASE = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\eHome\\";
	public static String MEDIA_CENTER_INFO_CACHE = MEDIA_CENTER_BASE + "DvdInfoCache\\";
	public static String MEDIA_CENTER_COVER_CACHE = MEDIA_CENTER_BASE + "DvdCoverCache\\";

	private static JAXBContext jaxbContext = null;
	private static final List<String> infoExcludedProperties = Arrays.asList(new String[]{"class", "thumbnailFile", "videoTSFile", "windowsMediaCenterMetaData", "dVDIdDisk"});
	private static final List<String> metaDataExcludedProperties = Arrays.asList(new String[]{"class"});

	private List<WindowsMediaCenterInfo> potentials;
	private Object lock = new Object();
	private Boolean isRefreshing = new Boolean(false);
	private CategoryTreeNode rootNode = getRootNode();
	private UPNPObject<T> nativeRootNode = null;
	
	public WindowsMediaCenterManager(UPNPObject<T> nativeRootNode) {
		this.nativeRootNode = nativeRootNode;
	}
	
	public void clearPotentials() {
		potentials = null;
	}
	
	public boolean isChildrenReady() {
		return potentials != null;
	}
	
	public void refreshCache(ProgressableObjectMonitor passedMonitor) {
		if (isRefreshing)
			return;
		
		synchronized (lock) {
			try {
				isRefreshing = true;
				if (potentials == null) {
					potentials = new ArrayList<WindowsMediaCenterInfo>();
					
					List<String> configuredFolders = getWindowsConfiguredFolders();
					for (String fileName : configuredFolders) {
						FileTreeEnumeration fileEnumeration = buildFileTreeEnumeration(fileName);
						ProgressableObjectMonitor newMonitor = new HistoricalTimedProgressMonitor(fileName, passedMonitor);
						newMonitor.startMonitor();
						fileEnumeration.setMonitor(newMonitor);
						int maxCount = 0;
						while (fileEnumeration.hasMoreElements() && maxCount < 20) {
							potentials.add(new WindowsMediaCenterInfo(fileEnumeration.nextElement(), fileName));
							maxCount++;
						}
						newMonitor.endMonitor();
						newMonitor.setNote("Complete for: " + fileName);
					}
				}
				
				nativeRootNode.setNode(rootNode, potentials);
			} finally {
				isRefreshing = false;
			}
		}
		//Somehow we need to reset the discovered maybe?
		//PMS.get().getRootFolder(null).
	}
	
	public static FileTreeEnumeration buildFileTreeEnumeration(String fileName) {
		return new FileTreeEnumeration(
				new File(fileName), 
				new WildcardFilenameFilter("*.dvdid.xml"), 
				new RegexFileFilter("(?i:VIDEO_TS)", MatchType.NEGATED_MATCH_NAME),
				true);
	}

	public static List<String> getWindowsConfiguredFolders() {
		WindowsRegistryRoot registryRoot = WindowsRegistryRoot.HKEY_CURRENT_USER;
		List<String> configuredFolders = new ArrayList<String>();
		String key = "Software\\Microsoft\\Windows\\CurrentVersion\\Media Center\\MediaFolders\\Movie";
		boolean folderFound = true;
		for (int t = 0; folderFound; t++) {
			String fileName = WindowsRegistry.readString(registryRoot, key, "Folder" + t);
			if (fileName == null)
				break;
			
			configuredFolders.add(fileName);
		}
		
		return configuredFolders;
	}
	
	public static JAXBContext getJAXBContext() {
		try {
			if (jaxbContext == null)
				jaxbContext = JAXBContext.newInstance(WindowsMediaCenterMetaData.class, DVDIdDisk.class);
			
			return jaxbContext;
		} catch (JAXBException e) {
			LOGGER.error("Couldn't get JAXB going.  Are you using Java 1.5 or later?");
			return null;
		}
	}
	
	public static Vector<String> getMediaCenterInfoProperties() {
		Vector<String> propertyList = new Vector<String>();
		BeanInfo info;
		try {
			info = Introspector.getBeanInfo(WindowsMediaCenterInfo.class);
			PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : descriptors) {
				if (!infoExcludedProperties.contains(descriptor.getName()))
					propertyList.add(descriptor.getDisplayName());
			}
			return propertyList;
		} catch (IntrospectionException e) {
			return propertyList;
		}
	}

	public static Vector<String> getMediaCenterMetaDataProperties() {
		Vector<String> propertyList = new Vector<String>();
		BeanInfo info;
		try {
			info = Introspector.getBeanInfo(WindowsMediaCenterMetaData.class);
			PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : descriptors) {
				if (!metaDataExcludedProperties.contains(descriptor.getName()))
					propertyList.add(descriptor.getDisplayName());
			}
			return propertyList;
		} catch (IntrospectionException e) {
			return propertyList;
		}
	}
	
	
	public CategoryTreeNode getRootNode() {
		if (rootNode != null)
			return rootNode;
		
		loadRootNode();
		if (rootNode == null) {
			rootNode = new CategoryTreeNode(null, null, false);
			rootNode.setChildren(Collections.singletonList(new CategoryTreeNode(rootNode, "genre", true)));
			saveRootNode();
		}
		return rootNode;
	}
	
	public void saveRootNode() {
		ObjectOutputStream stream = null;
		try {
			stream = new ObjectOutputStream(new FileOutputStream("MediaCenterCategories.ser"));
			stream.writeObject(getRootNode());
		} catch (FileNotFoundException e) {
			LOGGER.error("Couldn't save Media Center Categories", e);
		} catch (IOException e) {
			LOGGER.error("Couldn't save Media Center Categories", e);
		} finally {
			if (stream != null)
				try {stream.close();} catch (IOException e) {}
		}
	}
	
	public void loadRootNode() {
		ObjectInputStream stream = null;
		try {
			stream = new ObjectInputStream(new FileInputStream("MediaCenterCategories.ser"));
			rootNode = (CategoryTreeNode)stream.readObject();
		} catch (FileNotFoundException e) {
			LOGGER.error("Couldn't load Media Center Categories", e);
		} catch (IOException e) {
			LOGGER.error("Couldn't load Media Center Categories", e);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Couldn't load Media Center Categories", e);
		} finally {
			if (stream != null)
				try {stream.close();} catch (IOException e) {}
		}
	}
}
