package com.mediaserver.wmc;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WindowsMediaCenterFilter<T> implements Comparable<WindowsMediaCenterFilter<T>> {
	private static final Logger logger = LoggerFactory.getLogger(WindowsMediaCenterFilter.class);
	private Collection<WindowsMediaCenterInfo> potentials;
	private CategoryTreeNode filterNode;
	private String name;
	private String thumbnailIcon;
	private UPNPObject<T> nativeNode;
	
	private static BeanInfo BEAN_INFO;
	private static Map<String, PropertyDescriptor> PROPERTY_DESCRIPTORS = new HashMap<String, PropertyDescriptor>();
	static {
		try {
			BEAN_INFO = Introspector.getBeanInfo(WindowsMediaCenterInfo.class);
			for (PropertyDescriptor descriptor : BEAN_INFO.getPropertyDescriptors()) {
				PROPERTY_DESCRIPTORS.put(descriptor.getName(), descriptor);
			}
		} catch (IntrospectionException e) {
			logger.error("Chouldn't create bean info", e);
		}
	}
	
	private static String buildName(CategoryTreeNode filterNode) {
		if (filterNode.getNodeName() != null)
			return filterNode.getNodeName();
		
		if (filterNode.getProperty() != null)
			return filterNode.getProperty();
		
		return "Root";
	}
	
	public WindowsMediaCenterFilter(UPNPObject<T> nativeNode, String thumbnailIcon, CategoryTreeNode filterNode, Collection<WindowsMediaCenterInfo> potentials) {
		this.nativeNode = nativeNode;
		this.name = buildName(filterNode);
		this.thumbnailIcon = thumbnailIcon;
		this.filterNode = filterNode;
		this.potentials = potentials;
	}
	
	public String getThumbnailIconName() {
		return thumbnailIcon;
	}
	
	public String getName() {
		return name;
	}
	
	public void buildChildren() {
		nativeNode.clearChildren();
		
		if (filterNode.isUseHeading()) {
			//This builds a leaf
			nativeNode.addContainer(thumbnailIcon, filterNode.getInstance(false), potentials);
		} else if (filterNode.isLeaf() && (filterNode.getNodeName() != null ||
				(filterNode.getNodeName() == null && filterNode.getProperty() == null))) {
			//This builds the actual leaf movies
			//Collection<WindowsMediaCenterInfo> potentials = filterNode.getExpectedValue() != null?filterPotentials(this.potentials, filterNode):new TreeSet<WindowsMediaCenterInfo>(this.potentials);
			for (WindowsMediaCenterInfo info : this.potentials) {
				nativeNode.addVideoItem(info);
			}
		} else {
			//This builds category based children
			if (filterNode.getProperty() != null && filterNode.getNodeName() == null) {
				addNodeCategories(filterNode, potentials);
			} else {
				//Collection<WindowsMediaCenterInfo> potentials = filterNode.getNodeName() != null?filterPotentials(this.potentials, filterNode):this.potentials;
				for (CategoryTreeNode node : filterNode) {
					if (node.isUseHeading()) {
						nativeNode.addContainer(thumbnailIcon, node.getInstance(false), this.potentials);
					} else {
						addNodeCategories(node, potentials);
					}
				}
			}
		}
	}
	
	private Object replaceWithUnknown(Object value) {
		if (value == null || value.equals(""))
			return "(Unknown)";
		
		return value;
	}
	
	private void addNode(String key, WindowsMediaCenterInfo addElement, Map<String, Set<WindowsMediaCenterInfo>> addMap) {
		Set<WindowsMediaCenterInfo> value = addMap.get(key);
		if (value == null) {
			value = new TreeSet<WindowsMediaCenterInfo>();
			addMap.put(key, value);
		}
		
		value.add(addElement);
	}
	
	private void addNodeCategories(CategoryTreeNode targetNode, Collection<WindowsMediaCenterInfo> potentials) {
		Map<String, Set<WindowsMediaCenterInfo>> allNodes = new TreeMap<String, Set<WindowsMediaCenterInfo>>();
		Method readNodeValue = PROPERTY_DESCRIPTORS.get(targetNode.getProperty()).getReadMethod();
		for (WindowsMediaCenterInfo info : potentials) {
			Object value = null;
			try {
				value = readNodeValue.invoke(info, (Object[])null);
			} catch (Exception e) {
				logger.error("Error executing property accessor", e);
			}
			
			value = replaceWithUnknown(value);

			if (value instanceof List) {
				for (Object valueInstance : (List)value) {
					valueInstance = replaceWithUnknown(valueInstance);
					addNode(valueInstance + "", info, allNodes);
				}
			} else {
				addNode(value + "", info, allNodes);
			}
		}
		
		if (targetNode.getEntriesUnderHeading() > 0) {
			//If the targetNode is designed to have an entry count, then we need to summarize the entries now!
			Set<WindowsMediaCenterInfo> entriesInFolder = new TreeSet<WindowsMediaCenterInfo>();
			String currentString = null;
			int currentFolder = 0;
			String lastNodeName = null;
			
			for (Map.Entry<String, Set<WindowsMediaCenterInfo>> folder : allNodes.entrySet()) {
				currentFolder++;
				
				lastNodeName = folder.getKey();
				entriesInFolder.addAll(folder.getValue());
				if (currentString == null) {
					currentString = "(" + folder.getKey() + ") to (";
				}
				if (currentFolder % targetNode.getEntriesUnderHeading() == 0) {
					nativeNode.addContainer(thumbnailIcon, targetNode.getInstance(currentString + folder.getKey()), entriesInFolder);
					currentString = null;
					entriesInFolder = new TreeSet<WindowsMediaCenterInfo>();
				}
			}
			
			if (currentString != null) {
				if (currentFolder % targetNode.getEntriesUnderHeading() == 1) {
					nativeNode.addContainer(thumbnailIcon, targetNode.getInstance(lastNodeName), entriesInFolder);
				} else {
					nativeNode.addContainer(thumbnailIcon, targetNode.getInstance(currentString + lastNodeName + ")"), entriesInFolder);
				}
			}
		} else {
			//This is a normal entry...
			for (Map.Entry<String, Set<WindowsMediaCenterInfo>> folder : allNodes.entrySet()) {
				nativeNode.addContainer(thumbnailIcon, targetNode.getInstance(folder.getKey()), new TreeSet<WindowsMediaCenterInfo>(folder.getValue()));
			}
		}
	}
	
	/*private Set<WindowsMediaCenterInfo> filterPotentials(Collection<WindowsMediaCenterInfo> potentials, CategoryTreeNode filteringNode) {
		String property = filteringNode.getProperty();
		if (property == null)
			return new TreeSet<WindowsMediaCenterInfo>(potentials);
		
		Set<WindowsMediaCenterInfo> filteredChildren = new TreeSet<WindowsMediaCenterInfo>();
		for (WindowsMediaCenterInfo info : potentials) {
			Method readNodeValue = PROPERTY_DESCRIPTORS.get(property).getReadMethod();
			Object value = null;
			try {
				value = readNodeValue.invoke(info, (Object[])null);
			} catch (Exception e) {
				logger.error("Error executing property accessor", e);
			}
			
			value = replaceWithUnknown(value);

			if (value instanceof List) {
				if (((List)value).contains(filteringNode.getExpectedValue())) {
					filteredChildren.add(info);
				}
			} else if (value instanceof String) {
				if (((String)value).equalsIgnoreCase(filteringNode.getExpectedValue())) {
					filteredChildren.add(info);
				}
			}
		}
		
		return filteredChildren;
	}*/
	
	@Override
	public String toString() {
		return getName();
	}
	@Override
	public int compareTo(WindowsMediaCenterFilter o) {
		return name.compareToIgnoreCase(o.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filterNode == null) ? 0 : filterNode.hashCode());
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
		WindowsMediaCenterFilter other = (WindowsMediaCenterFilter) obj;
		if (filterNode == null) {
			if (other.filterNode != null)
				return false;
		} else if (!filterNode.equals(other.filterNode))
			return false;
		return true;
	}
}
