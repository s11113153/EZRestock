package tw.com.mobilogics.EZRestock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static tw.com.mobilogics.EZRestock.Utils.getFontFamily;
import static tw.com.mobilogics.EZRestock.Utils.promptMessage;
import static tw.com.mobilogics.EZRestock.Utils.strFilter;
import static tw.com.mobilogics.EZRestock.Utils.IsSmallerScreen;

public class RegisterActivity extends Activity {
  private Button mButtonStar = null;

  private EditText mEditTextCompanyName = null;

  private EditText mEditTextBranchNumber = null;

  private SharedPreferences mSharedPreferences = null;

  private TextView mTextViewRegisterTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    if (IsSmallerScreen(RegisterActivity.this)) {
      setContentView(R.layout.activity_register_smaller);
    }else {
      setContentView(R.layout.activity_register);
    }

    mSharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);

    // initial
    mButtonStar = (Button) findViewById(R.id.mButtonStar);
    mEditTextCompanyName = (EditText) findViewById(R.id.mEditTextCompanyName);
    mEditTextBranchNumber = (EditText) findViewById(R.id.mEditTextBranchNumber);
    mTextViewRegisterTitle = (TextView) findViewById(R.id.mTextViewRegisterTitle);

    mButtonStar.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String companyName = mEditTextCompanyName.getText().toString().trim();
        String branchNumber = mEditTextBranchNumber.getText().toString().trim();

        if (!companyName.equals("") && strFilter(companyName)) {
          editor.putString("COMPANYNAME", companyName);
          if (!branchNumber.equals("") && strFilter(branchNumber)) {
            editor.putString("BRANCHNUMBER", branchNumber);
          }
          editor.commit();
          Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
          startActivity(intent);
          finish();
        }else {
          promptMessage("format error", "No Blank and only English & Chinese & _", RegisterActivity.this);
        }
      }
    });

    mTextViewRegisterTitle.setTypeface(getFontFamily(RegisterActivity.this, "Quicksand-Bold.ttf"));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mSharedPreferences = null;
  }
}
