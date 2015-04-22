package com.example.tenbre_speaker;

import android.util.Log;

public class JudgeSide {
	
	private static final String TAG = JudgeSide.class.getName();
	private SideComparator comp;
	private Window left, right;
	private RssiToDistance rtd;

	public JudgeSide() {
		comp = new SideComparator();
		left = new AvgWindow(3);
		right = new AvgWindow(3);
		rtd = new RssiToDistanceHyl();
	}
	
	/**
	 * 输入一个rssi数据，获得当前比较结果
	 * @param side  输入数据属于哪边，小于0左边，大于0右边
	 * @param rssi
	 * @return
	 */
	public int compare(int side, int rssi) {
		Log.d(TAG, "Compare Side: " + side + ", rssi: " + rssi);
		if (side == 0) {
			return 0;
		} else if (side < 0) {
			return comp.compare(left.getProcessedValue(rtd.rssiToDist(rssi)), right.getLastValue());
		} else {
			return comp.compare(left.getLastValue(), right.getProcessedValue(rtd.rssiToDist(rssi)));
		}
	}

	public SideComparator getComp() {
		return comp;
	}

	public Window getLeft() {
		return left;
	}

	public Window getRight() {
		return right;
	}

	public RssiToDistance getRtd() {
		return rtd;
	}

	public void setLeft(Window left) {
		this.left = left;
	}

	public void setRight(Window right) {
		this.right = right;
	}

}

abstract class RssiToDistance {
	
	int txPower = -62;
	
	public void setTxPower(int txPower) {
		this.txPower = txPower;
	}
	
	/**
	 * 根据rssi计算距离
	 * @param rssi
	 * @return 距离
	 */
	abstract double rssiToDist(int rssi);
}

class RssiToDistanceWl extends RssiToDistance {

	@Override
	public double rssiToDist(int rssi) {
		double dist = 0;

		if (rssi == 0) {
			return -1.0;
		}

		double ratio = (rssi * 1.0) / txPower;
		
		if (ratio < 1.0) {
			return dist = Math.pow(ratio, 10) + 0.2;
		} else {
			dist = (0.89976) * Math.pow(ratio, 7.7095) + 0.2;
		}

		return dist;
	}
	
}

class RssiToDistanceHyl extends RssiToDistance {

	private static final String TAG = RssiToDistanceHyl.class.getName();

	@Override
	public double rssiToDist(int rssi) {
		double dist = 0;

		if (rssi == 0) {
			return -1.0;
		}

		int rate = 30;
		double pow = ((txPower * 1.0) - rssi) / rate;
		dist = Math.pow(10, pow);

		return dist;
	}
	
}