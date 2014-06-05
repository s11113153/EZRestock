package tw.com.mobilogics.EZRestock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import static tw.com.mobilogics.EZRestock.FilterUtils.strFilter;

public class RegisterActivity extends Activity {
    private String TAG = getClass().getName();
    // declare layout widget
    private Button mButtonStar = null;
    private EditText mEditTextCompanyName = null;
    private EditText mEditTextBranchNumber = null;

    private SharedPreferences mSharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        mSharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);

        // initial
        mButtonStar = (Button) findViewById(R.id.mButtonStar);
        mEditTextCompanyName = (EditText) findViewById(R.id.mEditTextCompanyName);
        mEditTextBranchNumber = (EditText) findViewById(R.id.mEditTextBranchNumber);

        mButtonStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String companyName = mEditTextCompanyName.getText().toString().trim();
                String branchNumber = mEditTextBranchNumber.getText().toString().trim();
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if (! companyName.equals("") && strFilter(companyName)) {
                    editor.putString("COMPANYNAME", companyName);
                    if (! branchNumber.equals("") && strFilter(branchNumber)) {
                        editor.putString("BRANCHNUMBER", branchNumber);
                    }
                    editor.commit();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    onDestroy();
                }else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("Format Error");
                    builder.setMessage("only English & Chinese & _");
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences = null;
        finish();
    }
}
