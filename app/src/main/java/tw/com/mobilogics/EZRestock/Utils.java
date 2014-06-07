package tw.com.mobilogics.EZRestock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private final static  SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
    private final static  String state = Environment.getExternalStorageState();


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
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);// get instance
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
        Date date = new Date();
        return mDateFormat.format(date);
    }

    public static String getSPofBranchNumber(SharedPreferences sharedPreferences) {
        if (sharedPreferences.contains("BRANCHNUMBER")) {
            return sharedPreferences.getString("BRANCHNUMBER", "");
        }
        return "";
    }

    public static  String getSPofCompanyName(SharedPreferences sharedPreferences) {
        if (sharedPreferences.contains("COMPANYNAME")) {
            return sharedPreferences.getString("COMPANYNAME","");
        }
        return "";
    }


    /**
     * create Dir to store  file.txt && return create result
     */
    public static boolean createEZRestockDir(String path, String dir) {
        boolean exist = false;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File file = new File(path, dir);
            if (!file.exists()) {
                if (file.mkdir()) {
                    exist = true;
                }
            }else {
                exist = true;
            }
        }
        return exist;
    }

    /**
     * File is store in EZRestock DIR。
     * File Name is according time to create , So only createNewFile && return file ,   otherwise status return null
     */
    public static File createEZRestockFile(String path, String filePath) throws IOException{
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            File file = new File(path, filePath);
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
}
