package com.tools;

import java.util.ArrayList;

public class PegasosCP {
	private Problem problem;
	private TreeMatrix tree;
	private Feature[] w;
	
	public PegasosCP(Problem problem, TreeMatrix tree) {
		this.problem = problem;
		this.tree = tree;
	}

	//training Structural SVMs via the 1-slack formulation
	public Feature[] trainSS(Feature[][] samples, String[] labels, double nalmda, int iteration, int k, int n) {
		ArrayList<String[]> workSet = new ArrayList<String[]>();
		
		Feature[] weight = null;
		
		int counter = 0;
		while(counter++ < 4) {
		String[] mostViolate = this.getMostViolateLabels(samples, labels, weight, n);
		
		workSet.add(mostViolate);
		
		weight = trainSamples(samples, labels, workSet, nalmda, iteration, k, n);
		
		
		int[] index = this.getSeq(samples.length);
		int idx = this.getMaxLossIndex(samples, labels, workSet, weight, index, n);
		
		double loss = this.getLoss(samples, labels, workSet.get(idx), weight, index, n);
		
		double currentLoss = this.getLoss(samples, labels, mostViolate, weight, index, n);
		}
		return weight;
	}
	
	public Feature[] trainSamples(Feature[][] samples, String[] labels, ArrayList<String[]> workSet, double nalmda, int iteration, int k, int n) {
		Feature[] w = null;
		 
		for(int i = 0; i < iteration; i++) {
			
			int[] index = getRandIndex(n, k);
			
			double lr = 1.0 / (nalmda * (i + 1));
			
			int maxLoss = getMaxLossIndex(samples, labels, workSet, w, index, n);
			
			String[] mostViolate = workSet.get(maxLoss);
			
			double loss = getLoss(samples, labels, mostViolate, w, index, n);
			
			if(loss > 0) {
				Feature[] term = SparseVector.scaleVec(w, 1 - lr * nalmda);
				
				Feature[] term1 = getSumLoss(samples, labels, mostViolate, index, n);
				
				term1 = SparseVector.scaleVec(term1, lr / k);
				
				w = SparseVector.addSparseVec(term, term1);
				
				double inp = SparseVector.innerProduct(w, w);
				
				double min = (1.0 / Math.sqrt(nalmda)) / inp;
				
				double scale = 1 > min ? min : 1;
				
				w = SparseVector.scaleVec(w, scale);
			}
		}
		return w;
	}
	
	// (1 / n) * sum(delta(y, yy))
	public double getSumTreeDistance(String[] realLabels, String[] predictLabels, int[] index) {
		int k = index.length;
		
		double sumDistance = 0;
		for(int i = 0; i < index.length; i++) {
			sumDistance += this.tree.getTreeDistance(realLabels[index[i]], predictLabels[index[i]]);
		}
		
		return sumDistance / k;
	}

	public int getMaxLossIndex(Feature[][] samples, String[] realLabels, ArrayList<String[]> workSet, Feature[] w, int[] index, int n) {
		double[] loss = new double[workSet.size()];
		
		for(int i = 0; i < loss.length; i++) {
			String[] predictLabels = workSet.get(i);
			loss[i] = getLoss(samples, realLabels, predictLabels, w, index, n);
		}
		
		double max = -Double.MAX_VALUE;
		
		int idx = 0;
		for(int m = 0; m < loss.length; m++) {
			if(loss[m] > max) {
				idx = m;
				max = loss[m];
			}
		}
		return idx;
	}
	
	//
	public Feature[] getSumLoss(Feature[][] samples, String[] realLabels, String[] predictLabels, int[] index, int n) {
		Feature[] result = null;
		
		for(int i = 0; i < index.length; i++) {
			int idx = index[i];
			Feature[] sample = samples[idx];
			String realLabel = realLabels[idx];
			String predictLabel = predictLabels[idx];
			
			Feature[] a = SupportVector.featureMap(this.tree, sample, realLabel, n);
			Feature[] b = SupportVector.featureMap(this.tree, sample, predictLabel, n);
			
			Feature[] sub = SparseVector.subSparseVec(a, b);
			
			int distance = this.tree.getTreeDistance(realLabel, predictLabel);
			
			Feature[] tempResult = SparseVector.scaleVec(sub, distance);
			
			if(result == null) {
				result = tempResult;
			} else {
				result = SparseVector.addSparseVec(result, tempResult);
			}
		}
		return result;
	}

	//
	public double getLoss(Feature[][] samples, String[] realLabels, String[] predictLabels, Feature[] w, int[] index, int n) {
		
		double term1 = getSumTreeDistance(realLabels, predictLabels, index);
		
		Feature[] sumWeight = getSumLoss(samples, realLabels, predictLabels, index, n);
		
		double inp = SparseVector.innerProduct(w, sumWeight);
		
		double term2 = inp / index.length;
		
		return (term1 - term2);
	}
	
	//
	public int[] getRandIndex(int n, int k) {
		int[] result = new int[k];
		for(int i = 0; i < k; i++) {
			int temp = (int)(Math.random() * n);
			result[i] = temp;
		}
		return result;
	}

	//
	public double getOutput(Feature[] sample, String realLabel, String predictLabel, Feature[] weight, int n) {
		int distance = this.tree.getTreeDistance(realLabel, predictLabel);
		
		Feature[] a = SupportVector.featureMap(this.tree, sample, realLabel, n);
		
		Feature[] b = SupportVector.featureMap(this.tree, sample, predictLabel, n);
		
		Feature[] sub = SparseVector.subSparseVec(a, b);
		
		double inp = SparseVector.innerProduct(weight, sub);
		
		return (distance * (1 - inp));
	}
	
	public String[] getMostViolateLabels(Feature[][] samples, String[] realLabels, Feature[] weight, int n) {
		String[] mostViolateLabels = new String[samples.length];
		
		for(int i = 0; i < mostViolateLabels.length; i++) {
			
			int counter = 0;
			double max = -Double.MAX_VALUE;
			for(int j = 0; j < this.tree.realCap; j++) {
				if(this.tree.labels[j].equals("0")) {
					continue;
				}
				
				double loss = getOutput(samples[i], realLabels[i], this.tree.labels[j], weight, n);
				if(loss > max) {
					max = loss;
					counter = j;
				}
			}
			
			mostViolateLabels[i] = this.tree.labels[counter];
		}
		return mostViolateLabels;
	}
	
	public Problem getProblem() {
		return problem;
	}

	public void setProblem(Problem problem) {
		this.problem = problem;
	}

	public TreeMatrix getTree() {
		return tree;
	}

	public void setTree(TreeMatrix tree) {
		this.tree = tree;
	}

	public Feature[] getW() {
		return w;
	}

	public void setW(Feature[] w) {
		this.w = w;
	}
	
	public int[] getSeq(int n) {
		int[] result = new int[n];
		for(int i = 0; i < n; i++) {
			result[i] = i;
		}
		return result;
	}
	
}
