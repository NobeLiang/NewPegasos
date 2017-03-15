package com.testTools;

import java.io.File;

import com.tools.Feature;
import com.tools.LoadData;
import com.tools.Parameter;
import com.tools.Pegasos;
import com.tools.PegasosCP;
import com.tools.Problem;
import com.tools.StructureSVM;
import com.tools.StructureSvmSGD;
import com.tools.TreeMatrix;

public class TestTools {
	public static void main(String[] args) throws Exception{
		String filename = "F:/Java源程序/SVMSMO/convertedCircle1.txt";
		String treeStructFile = "F:/Java源程序/SVMSMO/hierarchical.txt";
		
		filename = "F:/DataSets/RCV1RCV2/vectors/lyrl2004_vectors_train_filtered.dat";
		treeStructFile = "F:/DataSets/RCV1RCV2/rcv1.topics.hierorig.txt";
		
		Problem problem = LoadData.readProblem(new File(filename), 1);
		TreeMatrix tree = LoadData.loadTreeStruct(treeStructFile);
		Parameter params = new Parameter(0.00001, 15, 0.001);
		
		
//		PegasosCP pcp = new PegasosCP(problem, tree);
//		Feature[] w = pcp.trainSS(problem.x, problem.label, 0.001, 10000, 10, problem.n);
//		Feature[] w = pcp.getW();
//		
//		
//		for(int i = 0; i < w.length; i++) {
//			System.out.println(w[i].getIndex() + " : " + w[i].getValue());
//		}
		
//		StructureSVM sss = new StructureSVM(tree, problem, params);
//		sss.trainSS(problem.x, problem.label);
//		Feature[] w= sss.getWeight();
		
		
		StructureSvmSGD sss = new StructureSvmSGD(tree, problem, params);
		String[] target = new String[problem.l];
		sss.crossValidation(problem, params, 3, target);
		double accuracy = sss.getAccuracy(problem.label, target);	
		System.out.println("final accuracy : " + accuracy);
		
		
	}
}
