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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegisterActivity extends Activity {
    private String TAG = getClass().getName();

    // declare layout widget
    private Button btnStar = null;
    private EditText etCompanyName = null;
    private EditText etBranchNumber = null;

    private SharedPreferences sharedpreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.register);
        sharedpreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);

        // initial widget
        btnStar = (Button) findViewById(R.id.btn_register_star);
        etCompanyName = (EditText) findViewById(R.id.et_register_company_name);
        etBranchNumber = (EditText) findViewById(R.id.et_register_branch_number);

        btnStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean mFormatCurrent = true;
                String str_CompanyName = etCompanyName.getText().toString().trim();
                String str_BranchNumber = etBranchNumber.getText().toString().trim();

                try {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    if (null != str_CompanyName && !str_CompanyName.equals("")) {
                        if (strFilter(str_CompanyName)) {
                            editor.putString("COMPANYNAME", str_CompanyName);
                        }else {
                            mFormatCurrent = false;
                        }
                        if (null != str_BranchNumber && ! str_BranchNumber.equals("")) {
                            if (strFilter(str_BranchNumber)) {
                                editor.putString("BRANCHNUMBER", str_BranchNumber);
                            }else {
                                mFormatCurrent = false;
                            }
                        }

                        if (!mFormatCurrent) { // show message for error
                            final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            builder.setTitle("Format Error");
                            builder.setMessage("only English & Chinese & _");
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK", null);
                            builder.show();
                        }else {
                            editor.commit();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }else {
                        // no input
                    }

                }catch (PatternSyntaxException e) {
                    Log.d(TAG, "PatternSyntaxException ..");
                }
            }
        });
    }

    /* Use only Chinese && English && _ Character */
    private boolean strFilter(String str) throws PatternSyntaxException {
        String regex = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
