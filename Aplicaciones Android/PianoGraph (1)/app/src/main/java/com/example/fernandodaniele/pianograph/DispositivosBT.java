package com.example.fernandodaniele.pianograph;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;


public class DispositivosBT extends Activity {

    private static final String TAG = "DispositivosBT";

    TextView textView1;
    
    // EXTRA string to send on to mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_dispositivos);
    }
    
    @Override
    public void onResume() 
    {
    	super.onResume();
    	//*************** 
    	checkBTState();

    	textView1 = (TextView) findViewById(R.id.connecting);
    	textView1.setTextSize(40);
    	textView1.setText(" ");

    	// Initialize array adapter for paired devices
        ArrayAdapter<String> mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.nombre_dispositivo);

    	// Find and set up the ListView for paired devices
    	ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
    	pairedListView.setAdapter(mPairedDevicesArrayAdapter);
    	pairedListView.setOnItemClickListener(mDeviceClickListener);

    	// Get the local Bluetooth adapter
    	mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    	// Get a set of currently paired devices and append to 'pairedDevices'
    	Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

    	// Add previosuly paired devices to the array
    	if (pairedDevices.size() > 0) {
    		findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
    		for (BluetoothDevice device : pairedDevices) {
    			mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
    		}
    	} else {
    		String noDevices = getResources().getText(R.string.none_paired).toString();
    		mPairedDevicesArrayAdapter.add(noDevices);
    	}
  }

    // Set up on-click listener for the list (nicked this - unsure)
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

        	textView1.setText(R.string.conectando);
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity while taking an extra which is the MAC address.
			Intent i = new Intent(DispositivosBT.this, MainActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
			startActivity(i);
            finish();
        }
    };

    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
    	 mBtAdapter= BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if(mBtAdapter==null) { 
        	Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
          if (mBtAdapter.isEnabled()) {
            Log.d(TAG, "...Bluetooth Activado...");
          } else {
            //Prompt user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
 
            }
          }
        }
}