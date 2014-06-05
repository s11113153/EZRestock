package tw.com.mobilogics.EZRestock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class formatUtils {

    /* only uses Chinese && English && _ Character */
    public boolean strFilter(String str) throws PatternSyntaxException {
        String regex = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
