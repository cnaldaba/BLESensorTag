package com.example.blesensortag;

import android.R.bool;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.List;


public class MainActivity extends Activity {
	//Creates BLEWrapper instance
	private BleWrapper mBleWrapper = null; 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Initialize mBleWrapper object:
        mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null()
        {
        @Override
        public void uiDeviceFound(final BluetoothDevice device,final int rssi,final byte[] record)
        {
        	String msg = "uiDeviceFound: "+device.getName()+", "+rssi+", "+ String.valueOf(rssi);
        			Log.d("DEBUG", "uiDeviceFound: " + msg);
        			if (device.getName().equals("SensorTag") == true)
        			{
        			boolean status;
        			status = mBleWrapper.connect(device.getAddress().toString());
        			if (status == false)
        			{
        			Log.d("DEBUG", "uiDeviceFound: Connection problem");
        			}
        			}
        }
        
        @Override
        public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, List <BluetoothGattService> services)
        {
        for (BluetoothGattService service : services)
        {
        String serviceName = BleNamesResolver.resolveUuid
        (service.getUuid().toString());
        Log.d("DEBUG", serviceName);
        }
        }
        });
        
        if (mBleWrapper.checkBleHardwareAvailable() == false)
        {
        Toast.makeText(this, "No BLE-compatible hardware detected",
        Toast.LENGTH_SHORT).show();
        finish();
        }
    }

    @Override
    protected void onResume() {
    super.onResume();
    // check for Bluetooth enabled on each resume
    if (mBleWrapper.isBtEnabled() == false)
    {
    // Bluetooth is not enabled. Request to user to turn it on
    Intent enableBtIntent = new Intent(BluetoothAdapter.
    ACTION_REQUEST_ENABLE);
    startActivity(enableBtIntent);
    finish();
    }
    // init ble wrapper
    mBleWrapper.initialize();
    }
    
    @Override
    protected void onPause() {
    super.onPause();
    mBleWrapper.diconnect();
    mBleWrapper.close();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId())
    	{
    	case R.id.action_scan:
    		mBleWrapper.startScanning();
    		break;
    		case R.id.action_stop:
    		mBleWrapper.stopScanning();
    		break;
    		default:
    		break;
    		}
    		return super.onOptionsItemSelected(item);
    }
}
