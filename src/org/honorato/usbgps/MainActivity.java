package org.honorato.usbgps;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	GPSHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		initGPS();
		initLayout();
	}
	
	protected void initLayout() {
		Button scanButton = (Button) this.findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (helper != null) {
					renderUSBDevices(helper.scan());
				}
			}
		});
	}
	
	protected void initGPS() {
		this.helper = new GPSHelper(this);
		List<UsbDevice> devices = helper.scan();
		renderUSBDevices(devices);
		UsbDevice dev = this.helper.setupAnyDevice(devices);
	}
	
	protected void renderUSBDevices(List<UsbDevice> devices) {
		if (devices == null || devices.isEmpty()) {
			return;
		}
		
		TextView txt = (TextView) this.findViewById(R.id.main_text_view);
		
		List<String> usbNames = new ArrayList<String>();
		for (UsbDevice d : devices) {
			usbNames.add(d.getDeviceName());
		}
		
		txt.setText(TextUtils.join(", ", usbNames));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
