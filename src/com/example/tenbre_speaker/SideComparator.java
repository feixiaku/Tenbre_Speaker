package com.example.tenbre_speaker;

import android.util.Log;

public class SideComparator {
	
	protected static final String TAG = SideComparator.class.getName();

	private int lastState = 0;
	
	// default comparator
	private Comparator comparatorRatio = new Comparator() {
		
		private double ratio_B = 1.3;
		private double ratio_A = 0.8;
		
		@Override
		public int compare(double l, double r, int lastState) {
			Log.d(TAG, "comparator: " + l + ", " + r + ", " + lastState);
			if (lastState == 0) {
				return l < r ? -1 : 1;
			}
			if (lastState > 0 && ratio_B * l <= r) {
				return -1;
			}
			if ((((ratio_A*r) < l) && ((ratio_B*r) > l)) || (((ratio_A*l) < r) && ((ratio_B*l) > r)))
			{
				return 0;
			}
			if (lastState < 0 && l >= r * ratio_B) {
				return 1;
			}
			return lastState;
		}
	};
	
	// fixed comparator
	private Comparator comparatorFixed = new Comparator() {
		
		private double diff = 1.0;
		
		@Override
		public int compare(double l, double r, int lastState) {
			Log.d(TAG, "comparator: " + l + ", " + r + ", " + lastState);
			if (lastState == 0) {
				return l < r ? -1 : 1;
			}
			if (lastState > 0 && l + diff <= r) {
				return -1;
			}
			if (lastState < 0 && l >= r + diff) {
				return 1;
			}
			return lastState;
		}
	};
	
	private Comparator comparator = comparatorRatio;
//	private Comparator comparator = comparatorFixed;
	
	public void setComparator(Comparator comparator) {
		this.comparator = comparator;
	}
	
	public int compare(double l, double r) {
		lastState = comparator.compare(l, r, lastState);
		return lastState;
	}

}

interface Comparator {
	/**
	 * 比较两个输入数据，判断哪边音响该响
	 * @param l  左边数据
	 * @param r  右边数据
	 * @param lastState  当前播放状态
	 * @return  -1 左边放，1 右边放
	 */
	int compare(double l, double r, int lastState);
}
