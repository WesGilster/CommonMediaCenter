package com.mediaserver.wmc;

import java.util.Collection;
import java.util.List;



public interface UPNPObject<T> {
	public List<T> getChildren();
	public void setNode(CategoryTreeNode node, List<WindowsMediaCenterInfo> potentials);
	public void addVideoItem(WindowsMediaCenterInfo info);
	public void addContainer(String thumbnailName, CategoryTreeNode node, Collection<WindowsMediaCenterInfo> potentials);
	public void clearChildren();
}
