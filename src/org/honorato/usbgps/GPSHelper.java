package org.honorato.usbgps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class GPSHelper {
	
	private static final String TAG = "GPSHelper";
	Context ctx;
	
	public GPSHelper(Context ctx) {
		this.ctx = ctx;
	}
	
	public List<UsbDevice> scan() {
		Log.d(TAG, "Scanning devices");
		UsbManager manager = (UsbManager) this.ctx.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		
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

}
