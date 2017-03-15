package com.tools;

import java.util.Random;

public class StructureSvmSGD {
	private Problem problem;
	private TreeMatrix tree;
	private Parameter params;
	private Feature[] w;
	
	private static Random random = new Random();
	
	public StructureSvmSGD(TreeMatrix tree, Problem problem, Parameter params) {
		this.tree = tree;
		this.params = params;
		this.problem = problem;
	}
	
	public Feature[] train(Problem problem, Parameter params) {
		
		String[] leaves = this.tree.getLeaves();
		
		Feature[] weight = null;
		
		long t = 1;                                                       //迭代次数
		
		double lr = 0;                                                     //学习速率
		
		int[]  idxs = new int[problem.l];
		for(int i = 0; i < params.getMaxItes(); i++) {                                              //在整个样本集上循环maxItes次
//			idxs = getSeq(problem.l);
System.out.println("    iteration " + i);
			
			int idx;
			Feature[] sample;
			String label;
			Node node;
			double scale;
			Feature[] add;
			
			double inp;
			double compare;
			
			long startTime = System.currentTimeMillis();
			long endTime;
			
			for(int j = 0; j < problem.l; j++) {
//System.out.println(t);
//				idx = idxs[j];
				sample = problem.x[j];
				label = problem.label[j];
				
				lr = 1.0 / (params.getC() * t);
				t = t + 1;
				
				node = getMaxOutput(sample, label, leaves, weight);
				
				scale = 1 - lr * params.getC();
				
				weight = SparseVector.scaleVec(weight, scale);
				if(node.loss > 0) {
					add = getFeatureSub(sample, label, node.label);
					add = SparseVector.scaleVec(add, lr);
					weight = SparseVector.addSparseVec(weight, add);
				}
				
				inp = SparseVector.innerProduct(weight, weight);
				compare = 1 / Math.sqrt(params.getC() * inp);
				
				scale = compare > 1 ? 1 : compare;
				
				weight = SparseVector.scaleVec(weight, scale);
			}
			
			endTime = System.currentTimeMillis();
System.out.println("one ephoch take time : " + (endTime - startTime) + " ms");	
//		
//			startTime = System.currentTimeMillis();
//			double loss = getSumLoss(problem.x, problem.label, leaves, weight);
//			endTime = System.currentTimeMillis();
//System.out.println("get loss take time : " + (endTime - startTime) + " ms");
//			
//			startTime = System.currentTimeMillis();
//			String[] predict = predict(problem.x, weight);
//			endTime = System.currentTimeMillis();
////System.out.println("predict take time : " + (endTime - startTime) + " ms");
			
//			int counter = 0;
//			for(int k = 0; k < predict.length; k++) {
//				if(predict[k].equals(problem.label[k])) {
//					counter++;
//				}
//			}
			
//			System.out.println("accuracy : " + ((double)counter/predict.length));
			
//			System.out.println("loss = " + loss);
		}
		return weight;
	}
	
	public void crossValidation(Problem problem, Parameter params, int nr_fold, String[] target) {
		int i;
		int l = problem.l;
		int[] perm = new int[l];
		
		if(nr_fold > l) {
			nr_fold = l;
			System.err.println("WARNING: #folds > #data. Will");
		}
		int[] fold_start = new int[nr_fold + 1];
		
		for( i = 0; i < l; i++) {
			perm[i] = i;
		}
		
		for( i = 0; i < l; i++) {
			int j = i + random.nextInt(l - i);
			int temp = perm[i];
			perm[i] = perm[j];
			perm[j] = temp;
		}
		
		for( i = 0; i <= nr_fold; i++) {
			fold_start[i] = i * l / nr_fold;
		}
		
		for( i = 0; i < nr_fold; i++) {
System.out.println("fold " + i);
			int begin = fold_start[i];
			int end = fold_start[i + 1];
			int j, k;
			
			Problem subprob = new Problem();
			
			subprob.bias = problem.bias;
			subprob.n = problem.n;
			subprob.l = l - (end - begin);
			subprob.x = new Feature[subprob.l][];
			subprob.y = new double[subprob.l];
			subprob.label = new String[subprob.l];
			
			k = 0;
			for( j = 0; j < begin; j++) {
				subprob.x[k] = problem.x[perm[j]];
				subprob.y[k] = problem.y[perm[j]];
				subprob.label[k] = problem.label[perm[j]];
				++k;
			}
			
			for( j = end; j < l; j++) {
				subprob.x[k] = problem.x[perm[j]];
				subprob.y[k] = problem.y[perm[j]];
				subprob.label[k] = problem.label[perm[j]];
				++k;
			}
			
			Feature[] w = train(subprob, params);			
			for( j = begin; j < end; j++) {
				target[perm[j]] = predictSample(problem.x[perm[j]], w); 
			}
		}
	}
	
	public double getOutput(Feature[] sample, String firstLabel, String secondLabel, Feature[] weight) {
		int distance = this.tree.getTreeDistance(firstLabel, secondLabel);
		
		Feature[] a = SupportVector.featureMap(this.tree, sample, firstLabel, this.problem.n);
		Feature[] b = SupportVector.featureMap(this.tree, sample, secondLabel, this.problem.n);
		
		Feature[] sub = SparseVector.subSparseVec(a, b);
		
		Feature[] fin = SparseVector.scaleVec(sub, distance);
		
		double inp = SparseVector.innerProduct(weight, fin);
		
		return (distance - inp);
	}

	public Node getMaxOutput(Feature[] sample, String label, String[] allLabels, Feature[] weight) {
		
		double[] out = new double[allLabels.length];
		
		for(int i = 0; i < out.length; i++) {
			out[i] = getOutput(sample, label, allLabels[i], weight);
		}		
		
		double max = -Double.MAX_VALUE;
		int idx = 0;
		for(int i = 0; i < out.length; i++) {
			if(out[i] > max) {
				max = out[i];
				idx = i;
			}
		}
		
		return new Node(allLabels[idx], max);
	}
	
	public Feature[] getFeatureSub(Feature[] sample, String firstLabel, String secondLabel) {
		int distance = this.tree.getTreeDistance(firstLabel, secondLabel);
		
		Feature[] a = SupportVector.featureMap(this.tree, sample, firstLabel, this.problem.n);
		Feature[] b = SupportVector.featureMap(this.tree, sample, secondLabel, this.problem.n);
		
		Feature[] sub = SparseVector.subSparseVec(a, b);
		
		return SparseVector.scaleVec(sub, distance);
	}

	public String predictSample(Feature[] sample, Feature[] weight) {
		String[] leaves = this.tree.getLeaves();
		double[] outputs = new double[leaves.length];
		
		for(int i = 0; i < leaves.length; i++) {
			String label = leaves[i];
			Feature[] exts = SupportVector.featureMap(this.tree, sample, label, this.problem.n);
			outputs[i] = SparseVector.innerProduct(weight, exts);
		}
		
		double max = Double.MIN_VALUE;
		int index = 0;
		for(int i = 0; i < leaves.length; i++) {
			if(outputs[i] > max) {
				max = outputs[i];
				index = i;
			}
		}
		
		return leaves[index];
	}

	public String[] predict(Feature[][] samples, Feature[] weight) {
		String[] labels = new String[samples.length];
		for(int i = 0; i < labels.length; i++) {
			Feature[] sample = samples[i];
			labels[i] = predictSample(sample, weight);
		}
		return labels;
	}
	
	public int[] getSeq(int total) {
		int[] result = new int[total];
		for(int i = 0; i < result.length; i++) {
			result[i] = i;
		}
		
		for(int i = 0; i < result.length; i++) {
			int idx1 = new Random().nextInt(total);
			int idx2 = new Random().nextInt(total);
			int temp = result[idx1];
			result[idx1] = result[idx2];
			result[idx2] = temp;
		}
		return result;
	}
	
	public double getSumLoss(Feature[][] samples, String[] labels, String[] allLabels, Feature[] weight) {
		double sum = 0;
		
		for(int i = 0; i < samples.length; i++) {
			Feature[] sample = samples[i];
			String label = labels[i];
			Node node = getMaxOutput(sample, label, allLabels, weight);
			if(node.loss > 0) {
				sum += node.loss;
			}
		}
		
		return sum / samples.length;
	}

	public double getAccuracy(String[] labels, String[] predictLabels) {
		double n = labels.length;
		int counter = 0;
		for(int i = 0; i < labels.length; i++) {
			if(labels[i].equals(predictLabels[i])) {
				counter++;
			}
		}
		return (counter / n);
	}

	public Feature[] getW() {
		return this.w;
	}
}
