package com.example.tenbre_speaker;

import java.util.LinkedList;

public class MidWindow extends Window {
	
	LinkedList<Double> sorted = new LinkedList<Double>();

	public MidWindow(int width) {
		super(width);
	}

	@Override
	protected double processedValue(double nValue) {
		if (getWindow() == 0) {
			return 0;
		}

		// offer new value
		queue.offer(nValue);
		
		// remove old value
		if (queue.size() > getWindow()) {
			sorted.remove(queue.poll());
		}
		
		// add to sorted and get mid
		int midIdx = queue.size() / 2;
		int i = 0;
		for ( ; i < sorted.size(); i++) {
			if (sorted.get(i) > nValue) {
				sorted.add(i, nValue);
				return sorted.get(midIdx);
			}
		}
		// add to end
		sorted.add(nValue);
		return sorted.get(midIdx);
	}

	@Override
	protected void resetWidth() {
		while (queue.size() > getWindow()) {
			sorted.remove(queue.poll());
		}
	}

}
