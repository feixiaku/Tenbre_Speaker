package com.example.tenbre_speaker;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.altbeacon.beacon.Region;


import android.util.Log;

public class MainActivity extends ActionBarActivity
{
	JudgeSide judger = new JudgeSide();
	private String SERVER_IP = "192.168.168.241";
//	private String SERVER_IP = "192.168.168.24";
	private int SERVER_PORT = 7890;
	private int volume;
	private Boolean Auto = true;
	private Boolean ConnetState = false;

	private SocketChannel SocketInfo;
	private ByteBuffer BufTemp;

	// BLE
	private BluetoothAdapter mBluetoothAdapter;
	private Boolean mScanning;
	private int SCAN_PERIOD = 1 * 1000;
	
	private Region region;
	
	private SeekBar volumeControl;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
		{
			Toast.makeText(this, "Not support BLE", Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null)
		{
			Toast.makeText(this, "Not support BLE", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		BufTemp = ByteBuffer.allocateDirect(12);
		BufTemp.order(ByteOrder.LITTLE_ENDIAN);

		volume = 80;
		volumeControl = (SeekBar) findViewById(R.id.seekBar1);
		// volumeControl.setProgress(volume);

		final ListView listview = (ListView) findViewById(R.id.listView1);
		String[] values = new String[]
		{ "Viva La Vida", "Clint Eastwood", "Bubbly", "My Daddy", "Bluebell Railway", "Symphony in C Major", "My Darling Lime", "Danse Espagnole", "Secret Garden", "Rondo", "Bewitched" };

		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i)
		{
			list.add(values[i]);
		}
		final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, list);
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, final long id)
			{

				Thread ConThread = new Thread()
				{
					public void run()
					{

						BufTemp.rewind();
						BufTemp.limit(12);
						byte command[] = new byte[4];
						command[0] = 0x61;
						command[1] = 0x05;

						BufTemp.putInt(4);
						BufTemp.put(command, 0, 4);
						BufTemp.putInt((int) id + 1);

						try
						{
							BufTemp.rewind();
							SocketInfo.write(BufTemp);
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				ConThread.start();

			}
		});

		OnConnect(SERVER_IP);
		
//		applyMode();
	}

	private void showDialog()
	{
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog, null);
		final EditText text = (EditText) view.findViewById(R.id.ip);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view).setPositiveButton("OK", new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String ip = text.getText().toString();
				// TODO
				OnConnect(ip);
				dialog.dismiss();
			}
		}).setNegativeButton("Cancel", new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		}).create().show();
	}

	public void OnConnect(final String ip)
	{
		Thread ConThread = new Thread()
		{
			public void run()
			{
				Message msg = new Message();
				try
				{
//					SocketInfo = SocketChannel.open(new InetSocketAddress(SERVER_IP, SERVER_PORT));
					SocketInfo = SocketChannel.open(new InetSocketAddress(ip, SERVER_PORT));
					if (!SocketInfo.isConnected())
					{
						msg.what = 2;
						mHandler.sendMessage(msg);
						Log.i("Tenbre_Speaker", "Socket Connection Fail!");
						// showDialog();
					} else
					{
						msg.what = 1;
						mHandler.sendMessage(msg);
						Log.i("Tenbre_Speaker", "Socket Connection Success!");
						SocketInfo.configureBlocking(true);

						BufTemp.rewind();
						BufTemp.limit(8);
						byte command[] = new byte[8];
						command[4] = 0x61;
						command[5] = 0x03;
						BufTemp.put(command, 0, 8);

						try
						{
							BufTemp.rewind();
							SocketInfo.write(BufTemp);
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						BufTemp.rewind();
						BufTemp.limit(8);

						try
						{
							// Read header and data
							SocketInfo.read(BufTemp);
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if (BufTemp.get(5) == 0x01)
						{
							// connect successfully
							// dlgAlert.create().show();
							Log.i("Tenbre_Speaker", "Connection Successfully!");

						} else if (BufTemp.get(5) == 0x02)
						{
							// connect failed
							Log.i("Tenbre_Speaker", "Connection Fail!");
						}
					}
				} catch (IOException e)
				{
					e.printStackTrace();
				}

			}
		};
		ConThread.start();

	}

	public void OnPlayAMuteB(View v)
	{
		if (Auto == false)
		{
			PlayAMuteB();
		} else
		{
			Toast.makeText(getApplicationContext(), "Please Change Manual Mode!", Toast.LENGTH_SHORT).show();
		}
	}

	protected void PlayAMuteB()
	{
		Thread ConThread = new Thread()
		{
			public void run()
			{

				BufTemp.rewind();
				BufTemp.limit(12);
				byte command[] = new byte[4];
				command[0] = 0x61;
				command[1] = 0x06;

				BufTemp.putInt(4);
				BufTemp.put(command, 0, 4);
				BufTemp.putInt(1);

				try
				{
					BufTemp.rewind();
					SocketInfo.write(BufTemp);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BufTemp.rewind();
				BufTemp.limit(12);
				command[1] = 0x07;

				BufTemp.putInt(4);
				BufTemp.put(command, 0, 4);
				BufTemp.putInt(2);

				try
				{
					BufTemp.rewind();
					SocketInfo.write(BufTemp);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		ConThread.start();
	}

	public void OnPlayBMuteA(View v)
	{
		if (Auto == false)
		{
			PlayBMuteA();
		} else
		{
			Toast.makeText(getApplicationContext(), "Please Change Manual Mode!", Toast.LENGTH_SHORT).show();
		}
	}

	protected void PlayBMuteA()
	{
		Thread ConThread = new Thread()
		{
			public void run()
			{

				BufTemp.rewind();
				BufTemp.limit(12);
				byte command[] = new byte[4];
				command[0] = 0x61;
				command[1] = 0x06;

				BufTemp.putInt(4);
				BufTemp.put(command, 0, 4);
				BufTemp.putInt(2);

				try
				{
					BufTemp.rewind();
					SocketInfo.write(BufTemp);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BufTemp.rewind();
				BufTemp.limit(12);
				command[1] = 0x07;

				BufTemp.putInt(4);
				BufTemp.put(command, 0, 4);
				BufTemp.putInt(1);

				try
				{
					BufTemp.rewind();
					SocketInfo.write(BufTemp);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		ConThread.start();
	}

	public void OnAllPlay(View v)
	{
		if (Auto == false)
		{
			AllPlay();
		} else
		{
			Toast.makeText(getApplicationContext(), "Please Change Manual Mode!", Toast.LENGTH_SHORT).show();
		}
	}

	protected void AllPlay()
	{
		Thread ConThread = new Thread()
		{
			public void run()
			{

				BufTemp.rewind();
				BufTemp.limit(12);
				byte command[] = new byte[4];
				command[0] = 0x61;
				command[1] = 0x06;

				BufTemp.putInt(4);
				BufTemp.put(command, 0, 4);
				BufTemp.putInt(1);

				try
				{
					BufTemp.rewind();
					SocketInfo.write(BufTemp);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BufTemp.rewind();
				BufTemp.limit(12);

				BufTemp.putInt(4);
				BufTemp.put(command, 0, 4);
				BufTemp.putInt(2);

				try
				{
					BufTemp.rewind();
					SocketInfo.write(BufTemp);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		ConThread.start();
	}

	public void OnAllMute(View v)
	{
		if (Auto == false)
		{
			AllMute();
		} else
		{
			Toast.makeText(getApplicationContext(), "Please Change Manual Mode!", Toast.LENGTH_SHORT).show();
		}
	}

	protected void AllMute()
	{
		Thread ConThread = new Thread()
		{
			public void run()
			{

				BufTemp.rewind();
				BufTemp.limit(12);
				byte command[] = new byte[4];
				command[0] = 0x61;
				command[1] = 0x07;

				BufTemp.putInt(4);
				BufTemp.put(command, 0, 4);
				BufTemp.putInt(1);

				try
				{
					BufTemp.rewind();
					SocketInfo.write(BufTemp);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BufTemp.rewind();
				BufTemp.limit(12);

				BufTemp.putInt(4);
				BufTemp.put(command, 0, 4);
				BufTemp.putInt(2);

				try
				{
					BufTemp.rewind();
					SocketInfo.write(BufTemp);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		ConThread.start();
	}

	public void OnVolumeUp(View v)
	{
		if (ConnetState)
		{
			volume += 5;
			if (volume > 100)
				volume = 99;
			volumeControl.setProgress(volume);

			Thread ConThread = new Thread()
			{
				public void run()
				{

					BufTemp.rewind();
					BufTemp.limit(8);
					byte command[] = new byte[8];
					command[4] = 0x61;
					command[5] = 0x08;
					BufTemp.put(command, 0, 8);

					try
					{
						BufTemp.rewind();
						SocketInfo.write(BufTemp);
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			ConThread.start();
		}else {
			showDialog();
		}
	}

	public void OnVolumeDown(View v)
	{
		if (ConnetState)
		{
			volume -= 5;
			if (volume < 0)
				volume = 1;
			volumeControl.setProgress(volume);

			Thread ConThread = new Thread()
			{
				public void run()
				{

					BufTemp.rewind();
					BufTemp.limit(8);
					byte command[] = new byte[8];
					command[4] = 0x61;
					command[5] = 0x09;
					BufTemp.put(command, 0, 8);

					try
					{
						BufTemp.rewind();
						SocketInfo.write(BufTemp);
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			ConThread.start();
		}else {
			showDialog();
		}
	}

	public void OnManual(View v)
	{
		if (ConnetState)
		{
			Auto = false;

			scanLeDevice(Auto);

			View view = findViewById(R.id.manual_pad);
			view.setVisibility(view.isShown() ? View.GONE : View.VISIBLE);
		}else {
			showDialog();
		}

	}

	public void OnAuto(View v)
	{
		Auto = Auto ? false : true;
		applyMode();
	}

	private void applyMode()
	{
		if (ConnetState)
		{
			scanLeDevice(Auto);

			findViewById(R.id.manual_pad).setVisibility(Auto ? View.GONE : View.VISIBLE);

			String mode = Auto ? "Auto" : "Manual";
			Toast.makeText(this, "Current mode is " + mode, Toast.LENGTH_SHORT).show();
		}else {
			showDialog();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment
	{

		public PlaceholderFragment()
		{
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	}

	private class StableArrayAdapter extends ArrayAdapter<String>
	{

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects)
		{
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i)
			{
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position)
		{
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds()
		{
			return true;
		}

	}

	// BLE
	// Hander
	public final Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 1:
				ConnetState = true;
				applyMode();
				Toast.makeText(getApplicationContext(), "Connect Ok!", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				ConnetState = false;
				showDialog();
				Toast.makeText(getApplicationContext(), "Connect Error!", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private void scanLeDevice(final boolean enable)
	{
		if (enable)
		{
			Thread scanThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (mScanning)
					{
						mScanning = false;
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
					}
				}
			});

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else
		{
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
	{
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord)
		{

			// String le = "34:ce", re = "2f:4a";
			String le = "32:77", re = "2f:4a";
			String mac = device.getAddress().toLowerCase();
			Log.d("mac", "device mac: " + mac);

			if (mac.endsWith(le))  
			{
				applyResult(judger.compare(-1, rssi), judger.getLeft().getLastValue(), judger.getRight().getLastValue());
			} else if (mac.endsWith(re))
			{
				applyResult(judger.compare(1, rssi), judger.getLeft().getLastValue(), judger.getRight().getLastValue());
			}
		}
	};

	private void applyResult(final int side, final double l, final double r)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (ConnetState)
				{
					// 判断选择那边的音响启动
					if (side > 0)
					{
						PlayBMuteA();
						Log.d("wl", "Left Start!");
					} else if (side < 0)
					{
						PlayAMuteB();
						Log.d("wl", "Right Start!");
					} else if (side == 0)
					{
						AllPlay();
						Log.d("wl", "All Play");
					}
				} else
				{
					showDialog();
				}
			}
		});
	}
}
