package com.tools;

import java.util.ArrayList;

public class SparseVector {
	
	//œ° ËœÚ¡øœ‡≥À
	public static double innerProduct(Feature[] a, Feature[] b) {
		if(a == null || b == null || a.length == 0 || b.length == 0 || a[0] == null || b[0] == null) {
			return 0.0;
		}
		
		int pa  = 0;
		int  pb = 0;
		
		double result = 0;
		while(pa < a.length && pb < b.length) {
			int indexa = a[pa].getIndex();
			int indexb = b[pb].getIndex();
			
			if(indexa == indexb) {
				result += a[pa].getValue() * b[pb].getValue();
				pa++;
				pb++;
			} else if(indexa > indexb) {
				pb++;
			} else {
				pa++;
			}
		}
		
		return result;
	}

	public static Feature[] addSparseVec(Feature[] a, Feature[] b) {
		if(a == null && b == null) {
			return null;
		} 
		
		if(a == null || a.length == 0) {
			return b;
		} 
		
		if(b == null || b.length == 0) {
			return a;
		}
		
		ArrayList<Feature> list = new ArrayList<Feature>();
		
		int pa = 0; 
		int pb = 0;
		
		while(pa < a.length && pb < b.length) {
			int indexa = a[pa].getIndex();
			int indexb = b[pb].getIndex();
			
			if(indexa == indexb) {
				double sum = a[pa].getValue() + b[pb].getValue();
				if(sum != 0.0) {
					Feature f = new Feature(indexa, sum);
					list.add(f);
				}
				pa++;
				pb++;
				
			} else if(indexa > indexb) {
				list.add(b[pb]);
				pb++;
			} else {
				list.add(a[pa]);
				pa++;
			}
		}
		
		while(pa < a.length) {
			list.add(a[pa]);
			pa++;
		}
		
		while(pb < b.length) {
			list.add(b[pb]);
			pb++;
		}
		
		Feature[] result = new Feature[list.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}
		return result;
	}

	public static Feature[] subSparseVec(Feature[] a, Feature[] b) {
		if(a == null || b == null) {
			return null;
		} 
		
		Feature[] sub = new Feature[b.length];
		for(int i = 0; i < b.length; i++) {
			sub[i] = new Feature(b[i].getIndex(), -b[i].getValue());
		}
		
		return addSparseVec(a,  sub);
		
	}
	
	public static Feature[] scaleVec(Feature[] a, double scale) {
		if(a == null ) {
			return null;
		}
		
		Feature[] result = new Feature[a.length];
		for(int i = 0; i < result.length; i++) {
			result[i] = new Feature(a[i].getIndex(), scale * a[i].getValue());
		}
		return result;
	}
	
	public static void showVector(Feature[] a) {
		for(int i = 0; i < a.length; i++) {
			System.out.print(a[i].getIndex() + ":" + a[i].getValue());
			if(i < a.length - 1) {
				System.out.print("  ");
			} else {
				System.out.println();
			}
		}
	}
}
