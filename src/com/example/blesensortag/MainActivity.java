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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.UUID;


public class MainActivity extends Activity {
	private final String LOGTAG = "BLETEST";
	private static final long SCAN_PERIOD = 10000; // Used to scan for devices for only 10 secs
	//Creates BLEWrapper instance
	private BleWrapper mBleWrapper = null; 
	private BleWrapper mBleWrapper2 = null; 
	//This only connects the desired target
    private final String TARGET = "SensorTag";
    private enum mSensorState {IDLE, ACC_ENABLE, ACC_READ, IRT_ENABLE};
    private mSensorState mState;
    private mSensorState mState2;
    private String gattList = "";
    private TextView mTv;
    
    
    
    //BLE device list
    private ArrayAdapter<String> BTArrayAdapter;
    private ListView myListView;
    
    //For toast messages:
    Context context;
    private Handler handler = new Handler();
    
    //--------------------------------------------------------------------
    // TI SensorTag UUIDs
    //--------------------------------------------------------------------
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
	
    //--------------------------------------------------------------------
    // ON CREATE function
    //--------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@	
        // INITIALIZE mBleWrapper OBJECT
        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        
        
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //*********************************************************	
        // MBLEWRAPPER FOR DEVICE 1
        //********************************************************* 
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        
        mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null()
        {
        //*******************	
        // If a device is found
        //*******************
        @Override
        public void uiDeviceFound(final BluetoothDevice device,final int rssi,final byte[] record)
        {
        	String msg = "uiDeviceFound: "+device.getName()+", "+rssi+", "+ String.valueOf(rssi);
        			Log.d("DEBUG", "uiDeviceFound: " + msg);
        			
 
        		      handler.post(new Runnable(){
        					@Override
        					public void run() {
        						
        						if (BTArrayAdapter.isEmpty()){
        							BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
        						}
        						else{
        						for(int i = 0; i<=10; i++){
        							String item = (String) BTArrayAdapter.getItem(i);
        							String[] separated = item.split("\n");
        							//String device  = separated[0];
        							String address  = separated[1];
        							if (!address.equals(device.getAddress())){
        								BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
        							}
        							else{
        								return;
        							}
        						}
        						}
        						
        						
        					}
        		        	
        		         });

        		      
        		    
      			

        }
        //*******************	
        // Stores BLE device's services and enables them
        //*******************
        @Override
        public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, List <BluetoothGattService> services)
        {
            BluetoothGattCharacteristic c;
            BluetoothGattDescriptor d;
            //Retrieves Services:
        	for (BluetoothGattService service : services)
        		{
        			String serviceName = BleNamesResolver.resolveUuid(service.getUuid().toString());
        			Log.d(LOGTAG, serviceName);
                    gattList += serviceName + "\n";

                    mBleWrapper.getCharacteristicsForService(service);
        		}
            //Enable services:
            Log.d(LOGTAG, "DEVICE 1 uiAvailableServices: Enabling services");
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
        //*******************	
        // Successful Write function
        //******************* 
        @Override
        public void uiSuccessfulWrite(	BluetoothGatt gatt,
                                        BluetoothDevice device, 
                                        BluetoothGattService service,
                                        BluetoothGattCharacteristic ch, 
                                        String description) 
        {
            BluetoothGattCharacteristic c;

            super.uiSuccessfulWrite(gatt, device, service, ch, description);
            Log.d(LOGTAG, "DEVICE 1  uiSuccessfulWrite");

            switch (mState)
            {
            case ACC_ENABLE:
            	Log.d(LOGTAG, "DEVICE 1 uiSuccessfulWrite: Successfully enabled accelerometer");
            	  MainActivity.this.runOnUiThread(new Runnable() {
    		          public void run() {
    		              Toast.makeText(MainActivity.this, "Successfully enabled DEVICE 1 accelerometers", Toast.LENGTH_SHORT).show();

    		          }
    		      });
                break;
            case ACC_READ:
                Log.d(LOGTAG, "DEVICE 1 uiSuccessfulWrite: state = ACC_READ");					
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
            Log.d(LOGTAG, "DEVICE 1 uiFailedWrite");
        }
        
        @Override
        public void uiNewValueForCharacteristic(BluetoothGatt gatt,
                                                BluetoothDevice device, 
                                                BluetoothGattService service,
                                                BluetoothGattCharacteristic ch, 
                                                String strValue,
                                                int intValue, 
                                                byte[] rawValue, 
                                                String timestamp,
                                                final float[] vector) 
        {
            
        	super.uiNewValueForCharacteristic(gatt, device, service, ch, strValue, intValue, rawValue, timestamp,vector);
            
            Log.d(LOGTAG, "DEVICE 1 uiNewValueForCharacteristic");
            // decode current read operation
            switch (mState)
            {
            	case ACC_READ:
            		Log.d(LOGTAG, "DEVICE 1 uiNewValueForCharacteristic: Accelerometer data:" + vector[0] +  "," + vector[1] +  "," + vector[2] );
            		//Sends data to main UI thread
            		
            	      handler.post(new Runnable(){
            				@Override
            				public void run() {
            					TextView t;
                				t = (TextView)findViewById(R.id.accelText1);
                				t.setText("Accelerometer data:" + vector[0] +  "," + vector[1] +  "," + vector[2]);
            				}
            	        	
            	         });
            		
            	break;
            }
            for (byte b:rawValue)
            {
            Log.d(LOGTAG, "DEVICE 1 Val: " + b);
            }
            
            
            //Request Device 2 data
            if (mBleWrapper2.isConnected()) {
            	// requests an accelerometer read
            	Log.d(LOGTAG, "Request Device 2 acc read");
                readDevice2();
            }
          
        }
        
        @Override
        public void uiDeviceConnected(BluetoothGatt gatt, final BluetoothDevice device) 
        {
            Log.d(LOGTAG, "DEVICE 1 uiDeviceConnected: State = " + mBleWrapper.getAdapter().getState());
            MainActivity.this.runOnUiThread(new Runnable() {
		          public void run() {
		              Toast.makeText(MainActivity.this, "Device 1 connected" + device.getName(), Toast.LENGTH_SHORT).show();

		          }
		      });
        }

        @Override
        public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
            Log.d(LOGTAG, "DEVICE 1 uiDeviceDisconnected: State = " + mBleWrapper.getAdapter().getState());	
            gatt.disconnect();
        }
        
        
        @Override
        public void uiGotNotification(	BluetoothGatt gatt,
                BluetoothDevice device, 
                BluetoothGattService service,
                BluetoothGattCharacteristic characteristic) 
        {
            super.uiGotNotification(gatt, device, service, characteristic);
            String ch = BleNamesResolver.resolveCharacteristicName(characteristic.getUuid().toString());

            Log.d(LOGTAG,  "DEVICE 1 uiGotNotification: " + ch);
        }
        });
        
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //*********************************************************	
        // MBLEWRAPPER FOR DEVICE 2
        //********************************************************* 
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        
        
        mBleWrapper2 = new BleWrapper(this, new BleWrapperUiCallbacks.Null()
        {
        //*******************	
        // If a device is found
        //*******************
        	  @Override
              public void uiDeviceFound(final BluetoothDevice device,final int rssi,final byte[] record)
              {
              	String msg = "uiDeviceFound: "+device.getName()+", "+rssi+", "+ String.valueOf(rssi);
              			Log.d("DEBUG", "uiDeviceFound: " + msg);
              			
       
              		      handler.post(new Runnable(){
              					@Override
              					public void run() {
              						
              						if (BTArrayAdapter.isEmpty()){
              							BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
              						}
              						else{
              						for(int i = 0; i<=10; i++){
              							String item = (String) BTArrayAdapter.getItem(i);
              							String[] separated = item.split("\n");
              							//String device  = separated[0];
              							String address  = separated[1];
              							if (!address.equals(device.getAddress())){
              								BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
              							}
              							else{
              								return;
              							}
              						}
              						}
              						
              						
              					}
              		        	
              		         });

              		      
              		    
            			

              }
              //*******************	
              // Stores BLE device's services and enables them
              //*******************
              @Override
              public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, List <BluetoothGattService> services)
              {
                  BluetoothGattCharacteristic c;
                  BluetoothGattDescriptor d;
                  //Retrieves Services:
              	for (BluetoothGattService service : services)
              		{
              			String serviceName = BleNamesResolver.resolveUuid(service.getUuid().toString());
              			Log.d(LOGTAG, serviceName);
                          gattList += serviceName + "\n";

                          mBleWrapper2.getCharacteristicsForService(service);
              		}
                  //Enable services:
                  Log.d(LOGTAG, "DEVICE 2 uiAvailableServices: Enabling services");
                  c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_CONF);
                  mBleWrapper2.writeDataToCharacteristic(c, new byte[] {0x01});
                  mState2 = mSensorState.ACC_ENABLE;
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
              //*******************	
              // Successful Write function
              //******************* 
              @Override
              public void uiSuccessfulWrite(	BluetoothGatt gatt,
                                              BluetoothDevice device, 
                                              BluetoothGattService service,
                                              BluetoothGattCharacteristic ch, 
                                              String description) 
              {
                  BluetoothGattCharacteristic c;

                  super.uiSuccessfulWrite(gatt, device, service, ch, description);
                  Log.d(LOGTAG, "DEVICE 2  uiSuccessfulWrite");

                  switch (mState2)
                  {
                  case ACC_ENABLE:
                  	Log.d(LOGTAG, "DEVICE 2 uiSuccessfulWrite: Successfully enabled accelerometer");
                  	  MainActivity.this.runOnUiThread(new Runnable() {
          		          public void run() {
          		              Toast.makeText(MainActivity.this, "Successfully enabled DEVICE 2 accelerometers", Toast.LENGTH_SHORT).show();

          		          }
          		      });
                      break;
                  case ACC_READ:
                      Log.d(LOGTAG, "DEVICE 2 uiSuccessfulWrite: state = ACC_READ");					
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
                  Log.d(LOGTAG, "DEVICE 2 uiFailedWrite");
              }
              
              @Override
              public void uiNewValueForCharacteristic(BluetoothGatt gatt,
                                                      BluetoothDevice device, 
                                                      BluetoothGattService service,
                                                      BluetoothGattCharacteristic ch, 
                                                      String strValue,
                                                      int intValue, 
                                                      byte[] rawValue, 
                                                      String timestamp,
                                                      final float[] vector) 
              {
                  
              	super.uiNewValueForCharacteristic(gatt, device, service, ch, strValue, intValue, rawValue, timestamp,vector);
                  
                  Log.d(LOGTAG, "DEVICE 2 uiNewValueForCharacteristic");
                  // decode current read operation
                  switch (mState2)
                  {
                  	case ACC_READ:
                  		Log.d(LOGTAG, "DEVICE 2 uiNewValueForCharacteristic: Accelerometer data:" + vector[0] +  "," + vector[1] +  "," + vector[2] );
                  		//Sends data to main UI thread
                  		
                  	      handler.post(new Runnable(){
                  				@Override
                  				public void run() {
                  					TextView t;
                      				t = (TextView)findViewById(R.id.accelText2);
                      				t.setText("Accelerometer data:" + vector[0] +  "," + vector[1] +  "," + vector[2]);
                  				}
                  	        	
                  	         });
                  		
                  	break;
                  }
                  for (byte b:rawValue)
                  {
                  Log.d(LOGTAG, "DEVICE 2 Val: " + b);
                  }
                  
                  
                  
                
              }
              
              @Override
              public void uiDeviceConnected(BluetoothGatt gatt, final BluetoothDevice device) 
              {
                  Log.d(LOGTAG, "DEVICE 2 uiDeviceConnected: State = " + mBleWrapper2.getAdapter().getState());
                  MainActivity.this.runOnUiThread(new Runnable() {
      		          public void run() {
      		              Toast.makeText(MainActivity.this, "Device 2 connected" + device.getName(), Toast.LENGTH_SHORT).show();

      		          }
      		      });
              }

              @Override
              public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
                  Log.d(LOGTAG, "DEVICE 2 uiDeviceDisconnected: State = " + mBleWrapper2.getAdapter().getState());	
                  gatt.disconnect();
              }
              
              
              @Override
              public void uiGotNotification(	BluetoothGatt gatt,
                      BluetoothDevice device, 
                      BluetoothGattService service,
                      BluetoothGattCharacteristic characteristic) 
              {
                  super.uiGotNotification(gatt, device, service, characteristic);
                  String ch = BleNamesResolver.resolveCharacteristicName(characteristic.getUuid().toString());

                  Log.d(LOGTAG,  "DEVICE 2 uiGotNotification: " + ch);
              }
              });
        
        //*******************	
        // Checks for BLE
        //******************* 
        if (mBleWrapper.checkBleHardwareAvailable() == false)
        {
        	Toast.makeText(this, "No BLE-compatible hardware detected",Toast.LENGTH_SHORT).show();
        	finish();
        }
        
        //*******************	
        // Initializes buttons
        //*******************        

       initButtons();
       
       //*******************	
       // Initializes list for discovered devices
       //******************* 
       BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
       myListView = (ListView)findViewById(R.id.listView1);
       myListView.setAdapter(BTArrayAdapter);
       myListView.setOnItemClickListener(new OnItemClickListener(){
 
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String item = (String) parent.getItemAtPosition(position);
			String[] separated = item.split("\n");
			//String device  = separated[0];
			String address  = separated[1];
			
			boolean status = true;
			
			if (!mBleWrapper.isConnected()){
			status = mBleWrapper.connect(address);
			}
			else if(!mBleWrapper2.isConnected()){
				status = mBleWrapper2.connect(address);
			}
			
			if (status == false)
			{
			Log.d("DEBUG", "uiDeviceFound: Connection problem");
			}
			else{
				Log.d("DEBUG", "Connection successful");
			}
		}
    	 }); 
    
    }

    //--------------------------------------------------------------------
    // ON RESUME function
    //--------------------------------------------------------------------
    @Override
    protected void onResume() {
    	super.onResume();
    	// Check for Bluetooth enabled on each resume
    	if (mBleWrapper.isBtEnabled() == false)
    		{
    		// Bluetooth is not enabled. Request to user to turn it on
    			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    			startActivity(enableBtIntent);
    			finish();
    		}
    	// init ble wrapper
    	mBleWrapper.initialize();
    	mBleWrapper2.initialize();
    }
    
    //--------------------------------------------------------------------
    // ON PAUSE function
    //--------------------------------------------------------------------
    @Override
    protected void onPause() {
    super.onPause();
    mBleWrapper.diconnect();
    mBleWrapper.close();
    
    mBleWrapper2.diconnect();
    mBleWrapper2.close();
    }
    
    //--------------------------------------------------------------------
    // OPTIONS MENU
    //--------------------------------------------------------------------
    
    //*******
    // Creates options menu:
    //*******
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    //*******
    // Options menu functionality:
    //*******
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId())
    	{
    		case R.id.action_scan:
    			Log.d(LOGTAG, "startScan");
    			mBleWrapper.startScanning();
    			
    			//Stops scanning after 10 seconds
    			handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    	mBleWrapper.stopScanning();
                    	Log.d(LOGTAG, "Stop Scanning");
                    }
                }, SCAN_PERIOD);
    			
    			break;
    		case R.id.action_stop:
    			Log.d(LOGTAG, "StopScan");
    			mBleWrapper.stopScanning();
    			break;
    		case R.id.action_test:
    			//testButton();
    			break;
    		case R.id.action_scan2:
    			/*Log.d(LOGTAG, "startScan");
    			mBleWrapper2.startScanning();
    			
    			//Stops scanning after 10 seconds
    			handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    	mBleWrapper2.stopScanning();
                    	Log.d(LOGTAG, "Stop Scanning");
                    }
                }, SCAN_PERIOD); */
    			break;
    			
    		case R.id.action_stop2:
    			/*Log.d(LOGTAG, "StopScan");
    			mBleWrapper2.stopScanning();*/
    			break;
    			
    		default:
    		break;
    		}
    		return super.onOptionsItemSelected(item);
    }
    
    public void scan(){
    	mBleWrapper.startScanning();
    }
    //--------------------------------------------------------------------
    // MISC.
    //--------------------------------------------------------------------
 
    
    private void initButtons(){
    	//*******************	
        // Read 1 button onClick init
        //*******************
        final Button button = (Button) findViewById(R.id.readButton);
        button.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
            	handler.postDelayed(runnable, 100);
            	Log.d(LOGTAG, "Start polling TI sensorTag DEVICE 1");
            }
        });
       	//*******************	
        // Stop 1 button init
        //*******************
        final Button button2 = (Button) findViewById(R.id.stopButton);
        button2.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
            	handler.removeCallbacks(runnable);
            	Log.d(LOGTAG, "Stop polling TI sensorTag DEVICE 1");
            }
        });
        
        
    	
    }
    
    
    
   	//*******************	
    // Used for automated polling
    //*******************
    private Runnable runnable = new Runnable() {
    	   @Override
    	   public void run() {
    	       

                if (!mBleWrapper.isConnected()) {
                    return;
                }
                // requests an accelerometer read
                readDevice1();
    	        handler.postDelayed(this, 100);
    	        
    	        
    	   }
    	};
    	
        private Runnable runnable2 = new Runnable() {
     	   @Override
     	   public void run() {
     	       

                 if (!mBleWrapper2.isConnected()) {
                     return;
                 }
                 // requests an accelerometer read
                readDevice2();
     	        handler.postDelayed(this, 100);
     	        
     	        
     	   }
     	};
    	private void readDevice1(){
    		BluetoothGatt gatt;
            BluetoothGattCharacteristic c;
    		 Log.d(LOGTAG, "DEVICE 1 testButton: Reading acc");
             gatt = mBleWrapper.getGatt();
             c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
             mBleWrapper.requestCharacteristicValue(c);
             mState = mSensorState.ACC_READ;
    	}
    	
      	private void readDevice2(){
    		BluetoothGatt gatt;
            BluetoothGattCharacteristic c;
    		 Log.d(LOGTAG, "DEVICE 2 testButton: Reading acc");
             gatt = mBleWrapper2.getGatt();
             c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
             mBleWrapper2.requestCharacteristicValue(c);
             mState2 = mSensorState.ACC_READ;
    	}

    
}
