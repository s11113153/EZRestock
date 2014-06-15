package tw.com.mobilogics.EZRestock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class Utils {
    private Utils() {}

    private final static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);

    private final static String mState = Environment.getExternalStorageState();
    private final static DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    /**
     * only uses Chinese && English && _ Character
     */
    public static boolean strFilter(String str) {
        String regex = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static boolean checkInternetConnect(Activity activity) {
        ConnectivityManager connectivityManager =
            (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);// get instance
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || ! networkInfo.isConnected()) {
            return false;
        }
        else if (! networkInfo.isAvailable()){
            return false;
        }
        return true;
    }

    /**
     *  According title && message && activity , notice User current operation error message
     */
    public static void promptMessage(String title, String msg, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("Ok", null);
        builder.show();
    }

    public static String getDateTime() {
        return mDateFormat.format(System.currentTimeMillis());
    }

    /**
     * create Dir to store file.txt && return create result
     */
    public static boolean createEZRestockDir(String path, String dir) {
      boolean exist = false;
      if (Environment.MEDIA_MOUNTED.equals(mState)) {
        File file = new File(path, dir);
        if (!file.exists()) {
          if (file.mkdir()) {
            exist = true;
          }
        } else {
          exist = true;
        }
      }
      return exist;
    }

    /**
     * File has been store in EZRestock DIR (Externel Stroage)。
     * File Name is according time to create , So only createNewFile && return file, otherwise status return null
     */
    public static File createEZRestockFile(String path, String fileName) throws IOException {
      if (Environment.MEDIA_MOUNTED.equals(mState)) {
        File file = new File(path, fileName);
        if (!file.exists()) {
          try {
            file.createNewFile();
            return file;
          } catch (IOException e) {
              throw new IOException();
          }
        }
      }
      return null;
    }

    /**
    * if no SD-Card, Then Will use this function
    * @return File :  has been store in data/data/files/(Internal Storage), otherwise status return null
    */
    public static File createEZRestockCachedFile(Activity activity, String fileName) throws IOException {
      File cacheFile = new File(activity.getCacheDir() + "/" + fileName);
      if (! cacheFile.exists()) {
        try {
          cacheFile.createNewFile();
          return cacheFile;
        }catch (IOException e) {
          e.printStackTrace();
        }
      }
      return null;
    }

    /**
    * mLinkedList aleardy != null
    * write mLinkedList data to CachedFile(Internal Stroage) or File(External Stroage)
    */
    public static void writeEZRestockFile(File file, LinkedList<String> mLinkedList)
        throws FileNotFoundException, UnsupportedEncodingException {
      try {
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter streamWriter = new OutputStreamWriter(fos, "UTF8");
        PrintWriter printWriter = new PrintWriter(streamWriter);
        printWriter.println("ProCode,Quantity,Inventory,Date,Time\r");
        for (int i = 0; i < mLinkedList.size(); i++) {
          //originFormat 4710063022883_5_0_2014-06-07 08:56:13
          //makeFormat   4710063022883,5,0,20130604,1659
          String s = mLinkedList.get(i).replace("_", ",").replace("-", "").replace(" ", ",")
              .replace(":", "");
          printWriter.println(s + "\r");
        }
        printWriter.flush();
        printWriter.close();
      }catch (FileNotFoundException e) {
        throw new FileNotFoundException();
      }catch (UnsupportedEncodingException e) {
        throw new UnsupportedEncodingException();
      }
    }

    /**
    * According to current Activity && Screen, judge whether smaller screen
    * @return true : smaller screen
    */
    public static boolean IsSmallerScreen(Activity activity) {
      activity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
      if (mDisplayMetrics.widthPixels <= 240 && mDisplayMetrics.heightPixels <= 320) {
        return true;
      }
      return false;
    }
}
