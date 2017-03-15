package com.tools;

public class Feature {
	private int index;
	private double value;
	
	public Feature(int index, double value) {
		if(index < 0) {
			System.out.println("index can't below 0.");
			return;
		}
		
		this.index = index;
		this.value = value;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
