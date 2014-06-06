package tw.com.mobilogics.EZRestock;

import android.app.Activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /* only uses Chinese && English && _ Character */
    public static boolean strFilter(String str) {
        String regex = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
/*
    public static boolean checkInternetConnect(Activity activity) {

    }
    */
}
