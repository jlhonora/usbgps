package org.honorato.usbgps;

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
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.UartConfig;

public class GPSHelper {

	private static final String TAG = "GPSHelper";
	Context ctx;
	PendingIntent mPermissionIntent;
	UsbManager manager;
	Physicaloid mSerial;

	private byte[] bytes = new byte[1024];
	private static int TIMEOUT = 0;
	private boolean forceClaim = true;

	private boolean mRunningMainLoop = false;
	private boolean mStop = false;

	// Default settings
	private int mBaudrate           = 9600;
	private int mDataBits           = UartConfig.DATA_BITS8;
	private int mParity             = UartConfig.PARITY_NONE;
	private int mStopBits           = UartConfig.STOP_BITS1;
	private int mFlowControl        = UartConfig.FLOW_CONTROL_OFF;

	public GPSHelper(Context ctx) {
		this.ctx = ctx;

		mSerial = new Physicaloid(this.ctx);

		// listen for new devices
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		this.ctx.registerReceiver(mUsbReceiver, filter);
		
		this.openUsbSerial();
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

	public UsbDevice setupAnyDevice(List<UsbDevice> devices) {
		if (GPSHelper.isListEmpty(devices)) {
			Log.d(TAG, "No devices");
			return null;
		}
		UsbDevice dev = devices.get(0);
		this.manager.requestPermission(dev, mPermissionIntent);
		Log.d(TAG, "N interfaces: " + dev.getInterfaceCount());
		return dev;
	}

	public void closeConnection(UsbDevice dev) {
		if (dev == null) {
			return;
		}
	}

	public void performIO(UsbDevice dev, int endpointIndex) {
		if (dev == null) {
			return;
		}
		try {
			UsbInterface intf = dev.getInterface(0);
			Log.d(TAG, "N endpoints: " + intf.getEndpointCount());
			UsbEndpoint endpoint = intf.getEndpoint(0);
			UsbDeviceConnection connection = this.manager.openDevice(dev); 
			connection.claimInterface(intf, forceClaim);
			connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT);
			Log.d(TAG, "Bytes: " + bytes);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void read() {
		Log.d(TAG, "Reading");
		if (mSerial.open()) {
			byte[] buf = new byte[256];

			int bytesRead = mSerial.read(buf, buf.length);
			String str = new String(buf);
			Log.d(TAG, "Read " + bytesRead + " bytes: " + str);

			mSerial.close();
		} else {
			Log.d(TAG, "Device is closed");
		}
	}

	private void openUsbSerial() {
		if(mSerial == null) {
			Log.d(TAG, "Serial is null, cannot open");
			return;
		}

		if (!mSerial.isOpened()) {
			Log.d(TAG, "onNewIntent begin");
			if (!mSerial.open()) {
				Log.d(TAG, "cannot open");
				return;
			} else {

				boolean dtrOn=false;
				boolean rtsOn=false;
				if(mFlowControl == UartConfig.FLOW_CONTROL_ON) {
					dtrOn = true;
					rtsOn = true;
				}
				mSerial.setConfig(new UartConfig(mBaudrate, mDataBits, mStopBits, mParity, dtrOn, rtsOn));

				Log.d(TAG, "setConfig : baud : "+mBaudrate+", DataBits : "+mDataBits+", StopBits : "+mStopBits+", Parity : "+mParity+", dtr : "+dtrOn+", rts : "+rtsOn);
				Log.d(TAG, "connected");
			}
		}

	}

	public void closeUsbSerial() {
		mSerial.close();
        mStop = true;
        this.ctx.unregisterReceiver(mUsbReceiver);
	}

	private static final String ACTION_USB_PERMISSION =
			"com.android.example.USB_PERMISSION";

	// BroadcastReceiver when insert/remove the device USB plug into/from a USB
	// port
	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				Log.d(TAG, "Device attached");
				if (!mSerial.isOpened()) {
					Log.d(TAG, "Device attached begin");
					openUsbSerial();
				}
				if (!mRunningMainLoop) {
					Log.d(TAG, "Device attached mainloop");
				}
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				Log.d(TAG, "Device detached");
				mStop = true;
				mSerial.close();
			} else if (ACTION_USB_PERMISSION.equals(action)) {
				Log.d(TAG, "Request permission");
				synchronized (this) {
					if (!mSerial.isOpened()) {
						Log.d(TAG, "Request permission begin");
						openUsbSerial();
					}
				}
				if (!mRunningMainLoop) {
					Log.d(TAG, "Request permission mainloop");
				}
			}
		}
	};

}
