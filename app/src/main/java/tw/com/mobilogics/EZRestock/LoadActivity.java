package tw.com.mobilogics.EZRestock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class LoadActivity extends Activity{
    private SharedPreferences sharedpreferences = null;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        intent = new Intent();

        sharedpreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        String str_CompanyName = sharedpreferences.getString("COMPANYNAME", null);

        if (null != str_CompanyName) {
            View  v = getLayoutInflater().inflate(R.layout.load,null,false);
            setContentView(v);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    intent.setClass(LoadActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            },2000);
        }else {
            intent.setClass(LoadActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
