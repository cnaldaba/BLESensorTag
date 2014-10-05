package com.example.blesensortag;

import static java.util.UUID.fromString;
import android.R.bool;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.UUID;




public class MainActivity extends Activity {
	private final String LOGTAG = "BLETEST";
	//Creates BLEWrapper instance
	private BleWrapper mBleWrapper = null; 
    private final String TARGET = "SensorTag";
    private mSensorState mState;
    private String gattList = "";
    private TextView mTv;

    private enum mSensorState {IDLE, ACC_ENABLE, ACC_READ};
    
	public final static UUID 
	    UUID_IRT_SERV = fromString("f000aa00-0451-4000-b000-000000000000"),
	    UUID_IRT_DATA = fromString("f000aa01-0451-4000-b000-000000000000"),
	    UUID_IRT_CONF = fromString("f000aa02-0451-4000-b000-000000000000"), // 0: disable, 1: enable

	    UUID_ACC_SERV = fromString("f000aa10-0451-4000-b000-000000000000"),
	    UUID_ACC_DATA = fromString("f000aa11-0451-4000-b000-000000000000"),
	    UUID_ACC_CONF = fromString("f000aa12-0451-4000-b000-000000000000"), // 0: disable, 1: enable
	    UUID_ACC_PERI = fromString("f000aa13-0451-4000-b000-000000000000"), // Period in tens of milliseconds

	    UUID_HUM_SERV = fromString("f000aa20-0451-4000-b000-000000000000"),
	    UUID_HUM_DATA = fromString("f000aa21-0451-4000-b000-000000000000"),
	    UUID_HUM_CONF = fromString("f000aa22-0451-4000-b000-000000000000"), // 0: disable, 1: enable

	    UUID_MAG_SERV = fromString("f000aa30-0451-4000-b000-000000000000"),
	    UUID_MAG_DATA = fromString("f000aa31-0451-4000-b000-000000000000"),
	    UUID_MAG_CONF = fromString("f000aa32-0451-4000-b000-000000000000"), // 0: disable, 1: enable
	    UUID_MAG_PERI = fromString("f000aa33-0451-4000-b000-000000000000"), // Period in tens of milliseconds

	    UUID_BAR_SERV = fromString("f000aa40-0451-4000-b000-000000000000"), 
	    UUID_BAR_DATA = fromString("f000aa41-0451-4000-b000-000000000000"),
	    UUID_BAR_CONF = fromString("f000aa42-0451-4000-b000-000000000000"), // 0: disable, 1: enable
	    UUID_BAR_CALI = fromString("f000aa43-0451-4000-b000-000000000000"), // Calibration characteristic

	    UUID_GYR_SERV = fromString("f000aa50-0451-4000-b000-000000000000"), 
	    UUID_GYR_DATA = fromString("f000aa51-0451-4000-b000-000000000000"),
	    UUID_GYR_CONF = fromString("f000aa52-0451-4000-b000-000000000000"), // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z

	    UUID_KEY_SERV = fromString("0000ffe0-0000-1000-8000-00805f9b34fb"), 
	    UUID_KEY_DATA = fromString("0000ffe1-0000-1000-8000-00805f9b34fb"),
	    UUID_CCC_DESC = fromString("00002902-0000-1000-8000-00805f9b34fb");
	
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
        			else{
        				Log.d("DEBUG", "Connection successful");
        			}
        			}
        }
        
        @Override
        public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, List <BluetoothGattService> services)
        {
            BluetoothGattCharacteristic c;
            BluetoothGattDescriptor d;
            
        	for (BluetoothGattService service : services)
        		{
        			String serviceName = BleNamesResolver.resolveUuid(service.getUuid().toString());
        			Log.d(LOGTAG, serviceName);
                    gattList += serviceName + "\n";

                    mBleWrapper.getCharacteristicsForService(service);
        		}
            // enable services
            Log.d(LOGTAG, "uiAvailableServices: Enabling services");
            c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_CONF);
            mBleWrapper.writeDataToCharacteristic(c, new byte[] {0x01});
            mState = mSensorState.ACC_ENABLE;
        }
        
        @Override
        public void uiCharacteristicForService(	BluetoothGatt gatt, 
                BluetoothDevice device, 
                BluetoothGattService service,
                List<BluetoothGattCharacteristic> chars) 
        {
            super.uiCharacteristicForService(gatt, device, service, chars);
            for (BluetoothGattCharacteristic c : chars)
            {
                String charName = BleNamesResolver.resolveCharacteristicName(c.getUuid().toString());
                Log.d(LOGTAG, charName);
                gattList += "Characteristic: " + charName + "\n";
            }
        }
        
        @Override
        public void uiSuccessfulWrite(	BluetoothGatt gatt,
                                        BluetoothDevice device, 
                                        BluetoothGattService service,
                                        BluetoothGattCharacteristic ch, 
                                        String description) 
        {
            BluetoothGattCharacteristic c;

            super.uiSuccessfulWrite(gatt, device, service, ch, description);
            Log.d(LOGTAG, "uiSuccessfulWrite");

            switch (mState)
            {
            case ACC_ENABLE:
                Log.d(LOGTAG, "uiSuccessfulWrite: Reading acc");
                c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
                mBleWrapper.requestCharacteristicValue(c);
                mState = mSensorState.ACC_READ;
                break;

            case ACC_READ:
                Log.d(LOGTAG, "uiSuccessfulWrite: state = ACC_READ");					
                break;

            default:
                break;
            }
        }
        
        @Override
        public void uiFailedWrite(	BluetoothGatt gatt,
                                    BluetoothDevice device, 
                                    BluetoothGattService service,
                                    BluetoothGattCharacteristic ch, 
                                    String description) 
        {
            super.uiFailedWrite(gatt, device, service, ch, description);
            Log.d(LOGTAG, "uiFailedWrite");
        }
        
        @Override
        public void uiNewValueForCharacteristic(BluetoothGatt gatt,
                                                BluetoothDevice device, 
                                                BluetoothGattService service,
                                                BluetoothGattCharacteristic ch, 
                                                String strValue,
                                                int intValue, 
                                                byte[] rawValue, 
                                                String timestamp) 
        {
            super.uiNewValueForCharacteristic(gatt, device, service, ch, strValue, intValue, rawValue, timestamp);
            Log.d(LOGTAG, "uiNewValueForCharacteristic");
            for (byte b:rawValue)
            {
                Log.d(LOGTAG, "Val: " + b);
            }				
        }
        
        @Override
        public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) 
        {
            Log.d(LOGTAG, "uiDeviceConnected: State = " + mBleWrapper.getAdapter().getState());
        }

        @Override
        public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
            Log.d(LOGTAG, "uiDeviceDisconnected: State = " + mBleWrapper.getAdapter().getState());	
            gatt.disconnect();
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
    			Log.d(LOGTAG, "startScan");
    			mBleWrapper.startScanning();
    			break;
    		case R.id.action_stop:
    			Log.d(LOGTAG, "StopScan");
    			mBleWrapper.stopScanning();
    			break;
    		case R.id.action_settings:
    			testButton();
    			break;
    		default:
    		break;
    		}
    		return super.onOptionsItemSelected(item);
    }
    
    public void scan(){
    	mBleWrapper.startScanning();
    }
    
    private void testButton()
    {
        BluetoothGatt gatt;
        BluetoothGattCharacteristic c;

        if (!mBleWrapper.isConnected()) {
            return;
        }

        //mTv = (TextView)findViewById(R.id.textView1);
        //mTv.setText(gattList);

        //Log.d(LOGTAG, "testButton: Reading acc");
        //gatt = mBleWrapper.getGatt();
        //c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
        //mBleWrapper.requestCharacteristicValue(c);

        //Log.d(LOGTAG, "uiAvailableServices: Setting notification");
        gatt = mBleWrapper.getGatt();
        c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
        mBleWrapper.setNotificationForCharacteristic(c, true);
        
        //c = gatt.getService(UUID_IRT_SERV).getCharacteristic(UUID_IRT_DATA);
        //mBleWrapper.requestCharacteristicValue(c);



        //		if (c.getValue()!= null) {
        //			Log.d(LOGTAG, "testButton: " + c.getValue()[0]);
        //		}
    }
    
}
