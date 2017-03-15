package com.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadData {
	
	//从文本文件读取训练数据
	public static Problem readProblem(File file, double bias) throws IOException {
		BufferedReader fp = new BufferedReader(new FileReader(file));
		List<Double> vy = new ArrayList<Double>();
		List<Feature[]> vx = new ArrayList<Feature[]>();
		
		int max_index = 0;
		int lineNr = 0;
		
		try {
			while(true) {
				String line = fp.readLine();
				if(line == null) break;
				lineNr++;
				
				String[] tokens = line.split("\\s+|:|\t|\n|\r|\f");
				vy.add(Double.parseDouble(tokens[0]));
				
				int m = tokens.length / 2;
				Feature[] x;
				if(bias >= 0) {
					x = new Feature[m + 1];
				} else {
					x = new Feature[m];
				}
				
				int indexBefore = 0;
				for(int j = 0; j < m; j++) {
					String token = tokens[2 * j+1];
					int index;
					index = Integer.parseInt(token);
					
					token = tokens[2*j + 2];
					double value = Double.parseDouble(token);
					x[j] = new Feature(index, value);
				}
				
				if(m > 0) {
					max_index = Math.max(max_index, x[m-1].getIndex());
				}
				
				vx.add(x);
			}
			
			return constructProblem(vy, vx, max_index, bias);
		} finally {
			fp.close();
		}
		
	}

	
	//与具体问题相关联
		public static TreeMatrix loadTreeStruct(String filename) throws Exception {
			TreeMatrix treeMatrix = new TreeMatrix();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(filename)));
			
			Map<String, String> map = LoadData.loadLabelMapID("F:/java/java1/NewPegasos/strMapInt.txt");
			
			String line = null;
			String[] splitString = null;
			
			treeMatrix.setRoot("0");
			while((line = br.readLine()) != null) {
				splitString = line.split(":\\s|\\s");
				if(splitString[1].equals("None")) {
					continue;
				}
				
				String parent = splitString[1];
				String child = splitString[3];
				
				parent = map.get(parent);
				child = map.get(child);
				
				treeMatrix.addChild(parent, child);
			}
		
			if(br != null) {
				br.close();
			}
			
			return treeMatrix;
		}
		
	//
	private static Problem constructProblem(List<Double> vy, List<Feature[]> vx, int max_index, double bias) {
		Problem prob = new Problem();
		prob.bias = bias;
		prob.l = vy.size();
		prob.n = max_index;
		
		if(bias >= 0) {
			prob.n++;
		}
		
		prob.x = new Feature[prob.l][];
		for(int i = 0; i < prob.l; i++) {
			prob.x[i] = vx.get(i);
			
			if(bias >= 0) {
				assert prob.x[i][prob.x[i].length - 1] == null;
				prob.x[i][prob.x[i].length - 1] = new Feature(max_index + 1, bias);
			}
		}
		
		prob.y = new double[prob.l];
		for(int i = 0; i < prob.l; i++) {
			prob.y[i] = vy.get(i).doubleValue();
		}
		
		prob.label = new String[prob.l];
		for(int i = 0; i < prob.l; i++) {
			prob.label[i] = ((int)prob.y[i]) + "";
		}
		
		
		return prob;
	}
	
	
	public static Map<String, String> loadLabelMapID(String filename) throws Exception {
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(filename)));
		
		Map<String, String> map = new HashMap<String, String>();
		String line = null;
		
		while((line = in.readLine()) != null) {
			String[] strs = line.split("\\s");
			map.put(strs[0], strs[1]);
		}
		return map;
	}
}
