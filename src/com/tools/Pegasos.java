package com.tools;

import java.util.ArrayList;

public class Pegasos {
	
	private Problem problem;
	private Feature[] weight;
	
	public Pegasos(Problem problem) {
		this.problem = problem;
	}
	
	public Feature[] trainSamples(Feature[][] samples, double[] labels, double nalmda, int iteration, int k) {
		
		int numOfSamples = samples.length;
		int n = samples[0].length;
		Feature[] weight = new Feature[n];
		
		for(int i = 0; i< iteration; i++) {
			
			double lr = 1.0 /  (nalmda * (i + 1));
			int[] index = getKindex(k, numOfSamples);
			int[] lossIndex = selectLossIndex(samples, labels, weight, index);
			
			Feature[] deltaF = addSamples(samples, labels, lossIndex);
			deltaF = SparseVector.scaleVec(deltaF, lr / k);
			
			weight = SparseVector.scaleVec(weight, 1 - lr * nalmda);
			weight = SparseVector.addSparseVec(weight, deltaF);
			
			double normw = SparseVector.innerProduct(weight, weight);
			
			double temp = (1 / Math.sqrt(nalmda)) / normw;
			
			double min = 1 > temp ? temp : 1;
			
			weight = SparseVector.scaleVec(weight, min);
			
			double norm = (nalmda / 2) * SparseVector.innerProduct(weight, weight);
			double hinge = getMeanLoss(samples, labels, weight);
			double obj = (nalmda / 2) * SparseVector.innerProduct(weight, weight) + getMeanLoss(samples, labels, weight);
			
			System.out.println("norm = " + norm + " , hinge = " + hinge);
//			System.out.println("obj = " + obj);
		}
		
		this.weight = weight;
		return weight;
	}
	
	//
	public double predictSample(Feature[] sample, Feature[] weight) {
		double inp = SparseVector.innerProduct(sample, weight);
		if(inp >= 0) {
			return 1;
		} else {
			return -1;
		}
	}
	
	public double[] predict(Feature[][] samples, Feature[] weight) {
		double[] labels = new double[samples.length];
		for(int i = 0; i < labels.length; i++) {
			Feature[] sample = samples[i];
			labels[i] = predictSample(sample, weight);
		}
		return labels;
	}
	
	public Feature[] addSamples(Feature[][] samples, double[] labels, int[] lossIndex) {
		Feature[] result = null;
		for(int i = 0; i < lossIndex.length; i++) {
			Feature[] sample = samples[lossIndex[i]];
			double label = labels[lossIndex[i]];
			Feature[] ss = SparseVector.scaleVec(sample, label);
			
			if(result == null) {
				result = ss;
			} else {
				result = SparseVector.addSparseVec(result, ss);
			}
		}
		return result;
	} 
	
	//计算所有样本的平均损失
	public double getMeanLoss(Feature[][] samples, double[] labels, Feature[] weight) {
		
		int nSamples = samples.length;
		double totleLoss = 0;
		
		for(int i = 0; i < samples.length; i++) {
			Feature[] sample = samples[i];
			double label = labels[i];
			
			double loss = 1 - label * SparseVector.innerProduct(sample, weight);
			
			loss = loss > 0 ? loss : 0;
			
			totleLoss += loss;			
		}
		
		return totleLoss / nSamples;
//		return totleLoss;
	}
	
	//选出有损失的样本
	public int[] selectLossIndex(Feature[][] samples, double[] labels, Feature[] weight, int[] index) {
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < index.length; i++) {
			Feature[] sample = samples[index[i]];
			double label = labels[index[i]];
			double ip = SparseVector.innerProduct(sample, weight);
			if(label * ip < 1) {
				list.add(index[i]);
			}
		}
		
		int[] result = new int[list.size()];
		for(int j = 0; j < result.length; j++) {
			result[j] = list.get(j);
		}
		return result;
	}
	//
	public int[] getKindex(int k, int numOfSamples) {
		int[] index = new int[k];
		for(int i = 0; i< index.length; i++) {
			int temp = (int)(Math.random() * numOfSamples);
			index[i] = temp;
		}
		return index;
	}

	public Problem getProblem() {
		return problem;
	}
	

	public void setProblem(Problem problem) {
		this.problem = problem;
	}
	

	public Feature[] getWeight() {
		return weight;
	}



	
}
