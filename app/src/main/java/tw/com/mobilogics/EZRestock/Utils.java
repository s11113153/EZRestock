package tw.com.mobilogics.EZRestock;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

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

  private static DBHelper mDBHelper;

  /**
  * only uses Chinese && English && _ Character
  */
  public static boolean strFilter(String str) {
    String regex = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(str);
    Log.v("matcher result = ", "" + matcher.matches());
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

  /** Uses scanNumber to query ProID From ProductCode table */
  public static int searchProID(String scanNumber, String table, SQLiteDatabase mSQLiteDatabaseRead) {
    Cursor cursor = mSQLiteDatabaseRead.rawQuery(
        "select ProID from " + table + " Where ProCode=?", new String[]{scanNumber});
    if (cursor.moveToFirst()) {
      int index = Integer.parseInt(cursor.getString(0));
      cursor.close();
      return index;
    }
    return -1;
  }

  /** According ProID to query ProCode, Barcode, ProDesc, ProUnitS, ProUnitL, PackageQ, SupCode  From Products table && return JSON data */
  public static JSONObject searchOneOfProductData(int index, String table, SQLiteDatabase mSQLiteDatabaseRead) {
    Cursor cursor = mSQLiteDatabaseRead.rawQuery("select ProCode, Barcode, ProDesc, ProUnitS, ProUnitL, PackageQ, SupCode from " + table + " Where ProID=?", new String[]{"" + index});
    if (cursor.moveToFirst()) {
      try {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ProCode",  cursor.getString(0));
        jsonObject.put("Barcode", cursor.getString(1));
        jsonObject.put("ProDesc",  cursor.getString(2));
        jsonObject.put("ProUnitS", cursor.getString(3));
        jsonObject.put("ProUnitL", cursor.getString(4));
        jsonObject.put("PackageQ", cursor.getString(5));
        jsonObject.put("SupCode",  cursor.getString(6));
        cursor.close();
        return jsonObject;
      }catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public static void setSQLiteDatabaseInsrance(DBHelper dbHelper) {
    mDBHelper = dbHelper;
  }

  public static DBHelper getSQLiteDatabaseInsrance() {
    return mDBHelper;
  }

  public static void setActionBarFontFamily(Activity activity, String ttf) {
    int actionBarTitle = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
    TextView actionBarTitleView = (TextView) activity.getWindow().findViewById(actionBarTitle);
    actionBarTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40);
    Typeface Quicksand = getFontFamily(activity, ttf);
    if (Quicksand != null && actionBarTitleView != null) {
      actionBarTitleView.setTypeface(Quicksand);
    }
  }

  public static Typeface getFontFamily(Activity activity, String ttf) {
    Typeface typeface = Typeface.createFromAsset(activity.getAssets(), ttf);
    return typeface != null ? typeface : null;
  }
}
