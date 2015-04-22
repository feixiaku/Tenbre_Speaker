package com.example.tenbre_speaker;

import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;

public abstract class Window {
	
	private static final String TAG = Window.class.getName();
	private int mWidth;
	private double lastValue;
	
	// queue for average
	Queue<Double> queue = new LinkedList<Double>();
	
	public Window(int width) {
		this.mWidth = width;
	}
	
	/**
	 * 滑动窗口宽度
	 * @param width
	 */
	public void setWindowWidth(int width) {
		this.mWidth = width;
		resetWidth();
	}
	
	public int getWindow() {
		return mWidth;
	}
	
	/**
	 * 返回上次计算的结果
	 * @return
	 */
	public double getLastValue() {
		return lastValue;
	}

	/**
	 * 塞入一个新数据，并获得当前窗口的计算结果
	 * @param nValue
	 * @return
	 */
	public double getProcessedValue(double nValue) {
		lastValue = processedValue(nValue);
		Log.d(TAG, "in: " + nValue + ", out: " + lastValue);
		return lastValue;
	}
	
	/**
	 * 塞入一个新数据，并获得当前窗口的计算结果
	 * @param nValue
	 * @return
	 */
	protected abstract double processedValue(double nValue);
	
	protected abstract void resetWidth();
}
