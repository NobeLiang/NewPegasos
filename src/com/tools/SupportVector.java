package com.tools;

import java.util.ArrayList;
import java.util.Arrays;

public class SupportVector {
	
	public static Feature[] featureMap(TreeMatrix tree, Feature[] sample, String label, int n) {
		String[] path = tree.getPathToRoot(label);
		int[] intPath = new int[path.length - 1];
		
		int index = 0;
		for(int i = 0; i < path.length; i++) {
			if(!path[i].equals(tree.getRoot())) {
				intPath[index++] = Integer.parseInt(path[i]);
			}
		}
		Arrays.sort(intPath);
		
		Feature[] result = new Feature[sample.length * intPath.length];
		index = 0;
		for(int j = 0; j < intPath.length; j++) {
			int iPath = intPath[j];
			int base = (iPath - 1) * n;
			
			for(int k = 0; k < sample.length; k++) {
				 result[index++]= new Feature(sample[k].getIndex()+base, sample[k].getValue());
			}
		}
		
		return result;
	}
}
