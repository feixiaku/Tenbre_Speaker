package com.example.tenbre_speaker;

public class AvgWindow extends Window {
	
	private double sum = 0;

	public AvgWindow(int width) {
		super(width);
	}

	@Override
	protected double processedValue(double nValue) {
		
		if (getWindow() == 0) {
			return 0;
		}
		
		queue.offer(nValue);
		sum += nValue;
		
		if (queue.size() > getWindow()) {
			sum -= queue.poll();
		}
		
		return sum / queue.size();
	}

	@Override
	protected void resetWidth() {
		while (queue.size() > getWindow()) {
			sum -= queue.poll();
		}
	}

}
