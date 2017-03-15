package com.tools;
import java.io.File;

import java.io.IOException;

public class Problem {
	/**  the number of training data  */
	public int l;
	
	/**  the number of features (including the bias feature if bias >= 0)*/
	public int n;
	
	/**  an array containing the target value  */
	public double[] y;
	
	/** an array containing the string labels*/
	public String[] label;
	
	/**  array of sparse feature nodes  */
	public Feature[][] x;
	
	/**
	 * 	  If bias >= 0, we assume that one additional feature is added
	 *   to the end of each data instance
	 * 
	 * */
	public double bias;
	
	public static Problem readFromFile(File file, double bias) throws IOException  {
		return LoadData.readProblem(file, bias);
	}
	
	public  void resortFeature() {
		int n = this.x.length;
		for(int i = 0; i < n; i++) {
			int a = (int)(Math.random() * n);
			int b = (int)(Math.random() * n);
			Feature[] temp = x[a];
			x[a] = x[b];
			x[b] = temp;
			
			double lal = y[a];
			y[a] = y[b];
			y[b] =lal;
			
			String str = label[a];
			label[a] = label[b];
			label[b] = label[a];
		}
	}
	
}

