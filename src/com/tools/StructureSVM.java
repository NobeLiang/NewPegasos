package com.tools;

import java.util.ArrayList;

import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.functions.PDQuadraticMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;

public class StructureSVM {
	
	private TreeMatrix tree;
	private Problem problem;
	private Parameter params;
	private Feature[] weight;
	
	public StructureSVM(TreeMatrix tree, Problem problem, Parameter params) {
		this.tree = tree;
		this.problem = problem;
		this.params = params;
	}
	
	public Feature[] trainSS(Feature[][] samples, String[] labels) throws Exception {
		Feature[] weight = null;
		ArrayList<String[]> workSet = new ArrayList<String[]>();
		
		int counter = 0;
		
		double lastLoss = 0;
		while(true) {
			String[] mostViolateLabels = getViolateLabels(samples, labels, weight);
			double lossCurrent = this.getLoss(samples, labels, mostViolateLabels, weight);
			
			if(lossCurrent < lastLoss + this.params.getEps()) {
				break;
			}
			
			workSet.add(mostViolateLabels);
			
			System.out.print(counter + "  ");
			double[] alpha = solve(workSet, samples, labels, this.params);
			
			weight = calW(samples, labels, workSet, alpha);
			
			lastLoss = getMaxLoss(samples, labels, workSet, weight);
			System.out.println(lastLoss);
			this.weight = weight;
			counter++;
		}
		
		return weight;
	}
	
	public double getLoss(Feature[] sample, String label, String predictLabel, Feature[] weight) {
		int distance = this.tree.getTreeDistance(label, predictLabel);
		
		Feature[] a = SupportVector.featureMap(this.tree, sample, label, this.problem.n);
		Feature[] b = SupportVector.featureMap(this.tree, sample, predictLabel, this.problem.n);
		
		Feature[] sub = SparseVector.subSparseVec(a, b);
		
		double inp = SparseVector.innerProduct(weight, sub);
		
		return distance * (1 - inp);
	}
	
	public String[] getViolateLabels(Feature[][] samples, String[] labels, Feature[] weight) {
		String[] vioLabels = new String[samples.length];
		
		for(int i = 0; i < vioLabels.length; i++) {
			double loss;
			
			int idx = 0;
			double max = -Double.MAX_VALUE;
			for(int j = 0; j < this.tree.realCap; j++) {
				String label = labels[i];
				String preLabel = this.tree.labels[j];
				if(preLabel.equals("0")) {
					continue;
				}
				
				loss = getLoss(samples[i], label, preLabel, weight);
				
				if(loss > max) {
					max = loss;
					idx = j;
				}
			}
			
			vioLabels[i] = this.tree.labels[idx];
		}
		return vioLabels;
	}

	//(1 / n) sum(y, y)
	public double getSumDistance(String[] labels, String[] anotherLabels) {
		double n = labels.length;
		
		double sum = 0;
		for(int i = 0; i < labels.length; i++) {
			sum += this.tree.getTreeDistance(labels[i], anotherLabels[i]);
		}
		return sum / n;
	} 

	//sum(delta(y,y)(f(y, y) - f(y, y)))
	public Feature[] getSumFeature(Feature[][] samples, String[] labels, String[] another) {
		Feature[] result = null;
		
		double n = samples.length;
		
		for(int i = 0; i < samples.length; i++) {
			Feature[] sample = samples[i];
			int distance = this.tree.getTreeDistance(labels[i], another[i]);
			
			Feature[] a = SupportVector.featureMap(this.tree, sample, labels[i], this.problem.n);
			Feature[] b = SupportVector.featureMap(this.tree, sample, another[i], this.problem.n);
			
			Feature[] sub = SparseVector.subSparseVec(a, b);
			
			Feature[] fin = SparseVector.scaleVec(sub, distance / n);
			
			if(result == null) {
				result = fin;
			} else {
				result = SparseVector.addSparseVec(fin, result);
			}
		}
		return result;
		
	}
	
	public double[][] getQ(Feature[][] samples, String[] labels, ArrayList<String[]> workSet) {
		double[][] result = new double[workSet.size()][workSet.size()];
		
		for(int i = 0; i < workSet.size(); i++) {
			String[] firstLabel = workSet.get(i);
			for(int j = 0; j < workSet.size(); j++) {
				String[] secondLabel = workSet.get(j);
				
				Feature[] a = getSumFeature(samples,  labels, firstLabel);
				Feature[] b = getSumFeature(samples, labels, secondLabel);
				
				result[i][j] = SparseVector.innerProduct(a, b);
			}
		}
		return result;
	}
	
	public double[] getP(String[] labels, ArrayList<String[]> workSet) {
		double[] p = new double[workSet.size()];
		for(int i = 0; i < p.length; i++) {
			String[] another = workSet.get(i);
			p[i] = -getSumDistance(labels, another);
		}
		return p;
	}
	
	public double[] solve(ArrayList<String[]> workSet, Feature[][] samples, String[] realLabels, Parameter params) throws Exception {
		double[] p = getP(realLabels, workSet);
		double[][] q = getQ(samples, realLabels, workSet);
		
		PDQuadraticMultivariateRealFunction objFunction = 
				new PDQuadraticMultivariateRealFunction(q, p, 0);
		int xn = p.length;
		ConvexMultivariateRealFunction[] inequalities = 
				new ConvexMultivariateRealFunction[xn+1];
		
		double[] i0 = new double[xn];
		for(int i = 0; i < i0.length; i++) {
			i0[i] = 1;
		}
		inequalities[0] = new LinearMultivariateRealFunction(i0, -params.getC());
		for(int j = 1; j < inequalities.length; j++) {
			double[] t = new double[xn];
			t[j - 1] = -1;
			inequalities[j] = new LinearMultivariateRealFunction(t, 0);
		}
		
		OptimizationRequest or = new OptimizationRequest();
		or.setF0(objFunction);
		or.setFi(inequalities);
		or.setTolerance(1.E-10);
		or.setToleranceFeas(1.E-10);
		
		JOptimizer opt = new JOptimizer();
		opt.setOptimizationRequest(or);
		int returnCode = opt.optimize();
		
		
		long solveStart = System.currentTimeMillis();
		double[] result = opt.getOptimizationResponse().getSolution();
		long solveEnd = System.currentTimeMillis();
		
//		System.out.println("solve problem take " + (solveEnd - solveStart) + " millis.");
		
		return result;
	}

	public Feature[] calW(Feature[][] samples, String[] labels, ArrayList<String[]> workSet, double[] alpha) {
		Feature[] result = null;
		
		for(int i = 0; i < workSet.size(); i++) {
			String[] another = workSet.get(i);
			
			Feature[] a = getSumFeature(samples, labels, another);
			Feature[] sa = SparseVector.scaleVec(a, alpha[i]);
			
			if(result == null) {
				result = sa;
			} else {
				result = SparseVector.addSparseVec(result, sa);
			}
		}
		return result;
	}

	public double getLoss(Feature[][] samples, String[] labels, String[] another, Feature[] weight) {
		double item1 = getSumDistance(labels, another);
		
		Feature[] a = getSumFeature(samples, labels, another);
		
		double item2 = SparseVector.innerProduct(weight, a);
		
		return (item1 - item2);
	}
	
	public double getMaxLoss(Feature[][] samples, String[] labels, ArrayList<String[]> workSet, Feature[] weight) {
		double[] losses = new double[workSet.size()];
		
		for(int i = 0; i < losses.length; i++) {
			String[] another = workSet.get(i);
			losses[i] = getLoss(samples, labels, another, weight);
		}
		
		double max = Double.MIN_VALUE;
		for(int i = 0; i < losses.length; i++) {
			if(losses[i] > max) {
				max = losses[i];
			}
		}
		return max;
	}

	public Feature[] getWeight() {
		return weight;
	}

	
	//
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
}
