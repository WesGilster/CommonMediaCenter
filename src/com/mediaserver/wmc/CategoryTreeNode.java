package com.mediaserver.wmc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeNode;

public class CategoryTreeNode implements TreeNode, Iterable<CategoryTreeNode>, Serializable {
	private static final long serialVersionUID = 648957819410650743L;
	private String property;
	private String nodeName;
	private boolean useHeading;
	private CategoryTreeNode parent;
	private List<CategoryTreeNode> children = new ArrayList<CategoryTreeNode>();
	private int entriesUnderHeading;
	
	private CategoryTreeNode(CategoryTreeNode parent, String property, String nodeName, int entriesUnderHeading, boolean useHeading, List<CategoryTreeNode> children) {
		this.property = property;
		this.useHeading = useHeading;
		this.parent = parent;
		this.nodeName = nodeName;
		this.children = children;
		this.entriesUnderHeading = entriesUnderHeading;
	}
	
	public CategoryTreeNode(CategoryTreeNode parent, String property, boolean useHeading) {
		this.property = property;
		this.useHeading = useHeading;
		this.parent = parent;
		this.nodeName = null;
		this.entriesUnderHeading = -1;
	}
	
	public CategoryTreeNode(CategoryTreeNode parent, String property, int entriesUnderHeading) {
		this.property = property;
		this.useHeading = false;
		this.parent = parent;
		this.nodeName = null;
		this.entriesUnderHeading = entriesUnderHeading;
	}
	
	@Override
	public TreeNode getChildAt(int childIndex) {
		return children.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return children.size();
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	@Override
	public boolean getAllowsChildren() {
		return !isLeaf();
	}

	@Override
	public boolean isLeaf() {
		return children.size() == 0;
	}

	@Override
	public Enumeration<CategoryTreeNode> children() {
		final Iterator<CategoryTreeNode> iter = children.iterator();
		return new Enumeration<CategoryTreeNode>() {
			@Override
			public boolean hasMoreElements() {
				return iter.hasNext();
			}

			@Override
			public CategoryTreeNode nextElement() {
				return iter.next();
			}
		};
	}

	public void setChildren(List<CategoryTreeNode> nodes) {
		children = new ArrayList<CategoryTreeNode>(nodes);
	}
	
	@Override
	public Iterator<CategoryTreeNode> iterator() {
		return children.iterator();
	}
	
	public void setProperty(String property) {
		this.property = property;
	}
	public String getProperty() {
		return property;
	}
	
	public boolean isUseHeading() {
		return useHeading;
	}
	public void setUseHeading(boolean useHeading) {
		this.useHeading = useHeading;
	}

	public String getNodeName() {
		return nodeName;
	}
	
	public CategoryTreeNode getInstance(boolean useHeading) {
		return new CategoryTreeNode(parent, property, nodeName, entriesUnderHeading, useHeading, children);
	}
	
	public CategoryTreeNode getInstance(String nodeName) {
		return new CategoryTreeNode(parent, property, nodeName, entriesUnderHeading, useHeading, children);
	}

	public void addChild(CategoryTreeNode node) {
		children.add(node);
	}

	public void removeChild(CategoryTreeNode node) {
		children.remove(node);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((nodeName == null) ? 0 : nodeName.hashCode());
		result = prime * result
				+ ((property == null) ? 0 : property.hashCode());
		result = prime * result + (useHeading ? 1231 : 1237);
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
		CategoryTreeNode other = (CategoryTreeNode) obj;
		if (nodeName == null) {
			if (other.nodeName != null)
				return false;
		} else if (!nodeName.equals(other.nodeName))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (useHeading != other.useHeading)
			return false;
		return true;
	}
	
	public int getEntriesUnderHeading() {
		return entriesUnderHeading;
	}
	
	@Override
	public String toString() {
		String name = getProperty();
		if (name == null)
			name = "Root";

		if (entriesUnderHeading > 0)
			name += " (" + entriesUnderHeading + " entry group)";
		else if (useHeading)
			name += " (Use Category Header)";
			
		return name;
	}
}
