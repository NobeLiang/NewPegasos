package com.tools;

public class Parameter {
	private double C;
	private int maxItes;
	private double eps;
	
	public Parameter(double C, int maxItes, double eps) {
		this.C = C;
		this.maxItes = maxItes;
		this.eps = eps;
	}

	public double getC() {
		return C;
	}

	public void setC(double c) {
		C = c;
	}

	public int getMaxItes() {
		return maxItes;
	}

	public void setMaxItes(int maxItes) {
		this.maxItes = maxItes;
	}

	public double getEps() {
		return eps;
	}

	public void setEps(double eps) {
		this.eps = eps;
	}
	
	
}
