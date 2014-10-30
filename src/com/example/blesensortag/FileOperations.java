package com.example.blesensortag;

//Source: http://www.learn2crack.com/2014/04/android-read-write-file.html

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.util.Log;

public class FileOperations {
   public FileOperations() {
      }
   public Boolean write(String fname, String fcontent, String fPath){
      try {
        //String fpath = "/sdcard/"+fname+".txt";
        File file = new File(fPath);
        // If file does not exists, then create it
        if (!file.exists()) {
          file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(fcontent);
        bw.close();
        Log.d("Suceess","Sucess");
        return true;
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
   }


}
