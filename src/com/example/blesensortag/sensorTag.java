package com.example.blesensortag;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SINT8;

public class sensorTag {
	private BleWrapper mBleWrapper = null; 
	private Handler handler = new Handler();
	
	
	
	
	public static float[] getAccelerometerValue(final BluetoothGattCharacteristic c){
		Integer x = c.getIntValue(FORMAT_SINT8, 0);
		Integer y = c.getIntValue(FORMAT_SINT8, 1);
		Integer z = c.getIntValue(FORMAT_SINT8, 2) * -1;
		
		double scaledX = x / 16.0;
        double scaledY = y / 16.0;
        double scaledZ = z / 16.0;

        return new float[]{(float)scaledX, (float)scaledY, (float)scaledZ};
		
	}

}
