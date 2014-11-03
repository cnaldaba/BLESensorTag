package com.example.blesensortag;

//Source: http://www.learn2crack.com/2014/04/android-read-write-file.html

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

public class FileOperations {
   
   private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CANADA);
   public FileOperations() {
      }
   
   public void write(String fname, String fcontent, String fPath, int recordState){
	   //
	   // recordState:
	   // 0 - Only Device 1
	   // 1 - Connected to Device 1 and Device 2, received Device 1's data
	   // 2 - Connected to Device 1 and Device 2, received Device's 2 data
      try {
        //String fpath = "/sdcard/"+fname+".txt";
        File file = new File(fPath, fname);
        // If file does not exists, then create it
        if (!file.exists()) {
          file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsolutePath(),true);
        BufferedWriter bw = new BufferedWriter(fw);
        
        String time;
        //Time now = new Time();
		 //now.setToNow();
		 //long millis = System.currentTimeMillis();
		 //time =  now.format("%H:%M:%S");
		 Calendar c = Calendar.getInstance();
		 
		 time = sdf.format(c.getTime());
        
        String writeThis;
        switch (recordState){
        case 0:
        	writeThis = time + ',' + fcontent;
        	Log.d("FileOperations", writeThis);
        	fw.append(writeThis);
        	fw.append("\r\n");
        	break;
        case 1:
        	writeThis = time + ',' + fcontent + ',';
        	fw.append(writeThis);
        	break;
        case 2:
        	writeThis = time + ',' + fcontent;
        	fw.append(writeThis);
        	fw.append("\r\n");
        	break;
        	
        default:
        	break;
        }
        
        fw.close();
        Log.d("Suceess","Sucess");
        return ;
      } catch (IOException e) {
        e.printStackTrace();
        return ;
      }
   }
   
   //
   
   public void write2(String fName, String fcontent, String fPath, int recordState){
	   File myFile = new File(fPath, fName);
	   
	  
   }
   //
   
   
   public void getTimeStamp(String time){
	   Time now = new Time();
		 now.setToNow();
		 time =  now.format("%H:%M:%S");
		 //time = getDate(System.currentTimeMillis(), "dd/MM/yyyy hh:mm:ss.SSS");
		 
	   
   }
   
   /**
    * Return date in specified format.
    * @param milliSeconds Date in milliseconds
    * @param dateFormat Date format 
    * @return String representing date in specified format
    */
   public static String getDate(long milliSeconds, String dateFormat)
   {
       // Create a DateFormatter object for displaying date in specified format.
       SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

       // Create a calendar object that will convert the date and time value in milliseconds to date. 
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
   }


}
