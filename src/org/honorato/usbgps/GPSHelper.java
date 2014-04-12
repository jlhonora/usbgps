package org.honorato.usbgps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

public class GPSHelper {

	private static final String TAG = "GPSHelper";
	Context ctx;
	PendingIntent mPermissionIntent;
	UsbManager manager;

	private byte[] bytes = new byte[1024];
	private static int TIMEOUT = 0;
	private boolean forceClaim = true;

	public GPSHelper(Context ctx) {
		this.ctx = ctx;
		this.manager = (UsbManager) this.ctx.getSystemService(Context.USB_SERVICE);

		//mPermissionIntent = PendingIntent.getBroadcast(this.ctx, 0, new Intent(ACTION_USB_PERMISSION), 0);
		//IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		//this.ctx.registerReceiver(mUsbReceiver, filter);

	}

	public static boolean isListEmpty(List<UsbDevice> devices) {
		if (devices == null || devices.isEmpty()) {
			return true;
		}
		return false;
	}

	public List<UsbDevice> scan() {
		Log.d(TAG, "Scanning devices");

		HashMap<String, UsbDevice> deviceList = this.manager.getDeviceList();

		if (deviceList == null || deviceList.isEmpty()) {
			Log.d(TAG, "No devices");
			return null;
		}

		List<UsbDevice> devices = new ArrayList<UsbDevice>();
		for (Map.Entry<String, UsbDevice> e : deviceList.entrySet()) {
			Log.d(TAG, e.getKey() + "/" + e.getValue());
			devices.add(e.getValue());
		}
		return devices;
	}

	public void closeConnection(UsbDevice dev) {
		if (dev == null) {
			return;
		}
	}

	public void performIO() {
		Log.d(TAG, "Reading");
		// Find the first available driver.
		UsbSerialDriver driver = UsbSerialProber.acquire(manager);

		if (driver != null) {
			try {
				driver.open();
				driver.setBaudRate(115200);

				byte buffer[] = new byte[100];
				int numBytesRead = driver.read(buffer, 100);
				Log.d(TAG, "Read " + numBytesRead + " bytes.");
				Log.d(TAG, byte2str(buffer));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
				driver.close();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		} else {
			Log.d(TAG, "Driver is null");
		}
	}

	public String byte2str(byte[] bytes) {
		String res = "";
		for (byte c : bytes) {
			if (c < 32 || c > 126) {
				res += String.format("%02x ", c);
			} else {
				res += String.format("%c ", c);
			}
		}
		return res;
	}

	private static final String ACTION_USB_PERMISSION =
			"com.android.example.USB_PERMISSION";

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "Received action " + action);
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if(device != null){
							
						}
					} 
					else {
						Log.d(TAG, "permission denied for device " + device);
					}
				}
			}

			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (device != null) {
					closeConnection(device);
				}
			}
		}
	};

}
