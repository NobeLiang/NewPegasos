package com.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TreeMatrix {
	
	//连接矩阵，行表示父节点
	public byte[][] treeMatrix;
	public int ininCap = 1000;
	public int increase = 1000;
	
	public String[] labels;
	
	public String root;
	
	public int realCap; // 实际节点数目
	
	public TreeMatrix() {
		this.treeMatrix = new byte[ininCap][ininCap];
		this.labels = new String[ininCap];
		this.realCap = 0;
	}
	
	public void addChild(String parent, String child) {
		if(parent == null || child == null) {
			return;
		}
		
		this.realCap = addLabel(this.labels, parent, this.realCap, this.increase);
		this.realCap = addLabel(this.labels, child, this.realCap, this.increase);
		
		//类别数目超过当前可容纳的数目
		if(this.realCap > this.treeMatrix.length) {
			int newlength = this.treeMatrix.length + this.increase;
			
			byte[][] newTreeMatrix = new byte[newlength][newlength];
			
			for(int i = 0; i < this.treeMatrix.length; i++) {
				for(int j = 0; j < this.treeMatrix.length; j++) {
					newTreeMatrix[i][j] = this.treeMatrix[i][j];
				}
			}
			this.treeMatrix = newTreeMatrix;
		} 
		
		int parentIndex = this.getStringIndex(this.labels, parent);
		int childIndex = this.getStringIndex(this.labels, child);
		
		this.treeMatrix[parentIndex][childIndex] = 1;
	}
	
	public int addLabel(String[] labels, String label, int cap, int increase) {
		
		boolean flag = false;
		if(cap >= labels.length) {
			String[] newLabels = new String[labels.length + increase];
			for(int i = 0; i < labels.length; i++) {
				newLabels[i] = labels[i];
			}
			labels = newLabels;
		}
		
		for(int i = 0; i < cap; i++) {
			if(label.equals(labels[i])) {
				flag = true;
			}
		}
		
		if(flag) {
			this.labels = labels;
		} else {
			labels[cap] = label;
			cap++;
			this.labels = labels;
		}
		return cap;
	}
	
	//获得字符串str在strs中的位置索引
	public int getStringIndex(String[] strs, String str) {
		int index = -1;
		for(int i = 0; i < strs.length; i++) {
			if(str.equals(strs[i])) {
				index = i;
			}
		}
		return index;
	}
	
	//树的根节点
	public void setRoot(String root) {
		this.root = root;
	}
	
	
	//获得该标签到根节点的路径
	public String[] getPathToRoot(String label) {
		if(label == null) {
			return null;
		}
		
		if(label.equals(this.root)) {
			return new String[] {this.root};
		}
		
		ArrayList<String> path = new ArrayList<String>();
		path.add(label);
		String parent = this.getParent(label);
		while(parent != null && !parent.equals(root)) {
			path.add(parent);
			parent = this.getParent(parent);
		}
		path.add(this.getRoot());
		
		int length = path.size();
		String[] result = new String[length];
		for(int i = 0; i < result.length; i++) {
			result[i] = path.get(length - i - 1);
		}
		return result;
	}
	
	public String getParent(String label) {
		int index = this.getStringIndex(this.labels, label);
		for(int i = 0; i < this.labels.length; i++) {
			if(this.treeMatrix[i][index] == 1) {
				return this.labels[i];
			}
		}
		return null;
	}
	
	
	//获得节点的子节点
	public String[] getChildren(String label) {
		int index = this.getStringIndex(this.labels, label);
		ArrayList<String> list = new ArrayList<String>();
		
		for(int i = 0; i < this.labels.length; i++) {
			if(this.treeMatrix[index][i] == 1) {
				list.add(this.labels[i]);
			}
		}
		
		String[] result = new String[list.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}
		return result;
	}
	
	public String getRoot() {
		return root;
	}
	
	public void showPath(String[] path) {
		if(path == null) {
			return;
		} 
		for(int i = 0; i < path.length; i++) {
			System.out.print(path[i]);
			if(i < path.length - 1) {
				System.out.print("--");
			} else {
				System.out.println();
			}
		}
	}
	
	//获得两类标之间的距离
	public int getTreeDistance(String label1, String label2) {
		String[] p1 = getPathToRoot(label1);
		String[] p2 = getPathToRoot(label2);
		
		int commonIndex = 0;
		while(commonIndex < p1.length && commonIndex < p2.length) {
			if(p1[commonIndex].equals(p2[commonIndex])) {
				commonIndex++;
			} else {
				break;
			}
		}
		
		int result = (p1.length - commonIndex) + (p2.length - commonIndex);
//		return result;
		return result / 2;
	}

	//
	public String[] findDifference(String label_a, String label_b) {
		String path_a[] = this.getPathToRoot(label_a);
		String path_b[] = this.getPathToRoot(label_b);
		
		Set<String> set = new HashSet<String>();
		for(int i = 0; i < path_b.length; i++) {
			set.add(path_b[i]);
		}
		
		ArrayList<String> list = new ArrayList<String>();
		for(int j = 0; j < path_a.length; j++) {
			if(!set.contains(path_a[j])) {
				list.add(path_a[j]);
			}
		}
		
		String[] result = new String[list.size()];
		for(int k = 0; k < result.length; k++) {
			result[k] = list.get(k);
		}
		return result;
	}
	
	//取得树的叶子节点类别
	public String[] getLeaves() {
		ArrayList<String> list = new ArrayList<String>();
		
		for(int i = 0; i < realCap; i++) {
			int counter = 0;
			for(int j = 0; j< realCap; j++) {
				if(this.treeMatrix[i][j] == 1) {
					counter++;
				}
			}
			if(counter == 0) {
				list.add(this.labels[i]);
			}
		}
		
		String[] leaves = new String[list.size()];
		for(int i = 0; i < leaves.length; i++) {
			leaves[i] = list.get(i);
		}
		return leaves;
	}
}
