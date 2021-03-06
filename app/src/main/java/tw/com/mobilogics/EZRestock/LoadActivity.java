package tw.com.mobilogics.EZRestock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.view.Window;


public class LoadActivity extends Activity{

  private SharedPreferences mSharedPreferences = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    mSharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
    String companyName = mSharedPreferences.getString("COMPANYNAME", null);
    final Intent intent = new Intent();
    if (null != companyName) {
      View  v = getLayoutInflater().inflate(R.layout.load, null, false);
      setContentView(v);
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          intent.setClass(LoadActivity.this, MainActivity.class);
          startActivity(intent);
          finish();
        }
      }, 2000);
    }else {
      intent.setClass(LoadActivity.this, RegisterActivity.class);
      startActivity(intent);
      finish();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mSharedPreferences = null;
  }
}
