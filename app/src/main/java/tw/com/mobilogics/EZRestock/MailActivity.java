package tw.com.mobilogics.EZRestock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.LinkedList;
import java.util.List;

import static tw.com.mobilogics.EZRestock.Utils.checkInternetConnect;
import static tw.com.mobilogics.EZRestock.Utils.getDateTime;
import static tw.com.mobilogics.EZRestock.Utils.getSPofBranchNumber;
import static tw.com.mobilogics.EZRestock.Utils.getSPofCompanyName;
import static tw.com.mobilogics.EZRestock.Utils.promptMessage;
import static tw.com.mobilogics.EZRestock.Utils.strFilter;


public class MailActivity extends ActionBarActivity implements View.OnClickListener{
    private static LinkedList<String> mLinkedList = null;
    private EditText mEditTextCompanyName = null;
    private EditText mEditTextBranchNumber = null;
    private Button mButtonSave = null;
    private Button mButtonMail = null;
    private TextView mTextViewVisitHome = null;

    private SharedPreferences mSharedPreferences = null;

    private final String URL = "http://www.mobilogics.com.tw";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_mail);
        initial();
    }
    private void initial() {
        setTitle("Mail");
        mLinkedList = new LinkedList<String>((List)getIntent().getSerializableExtra("ListData"));
        mSharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        mEditTextCompanyName =  (EditText) findViewById(R.id.mEditTextCompanyName);
        mEditTextBranchNumber = (EditText) findViewById(R.id.mEditTextBranchNumber);
        mButtonSave = (Button) findViewById(R.id.mButtonSave);
        mButtonMail = (Button) findViewById(R.id.mButtonMail);
        mTextViewVisitHome = (TextView) findViewById(R.id.mTextViewVisitHome);

        mTextViewVisitHome.setOnClickListener(this);
        mButtonSave.setOnClickListener(this);
        mButtonMail.setOnClickListener(this);
    }

    private String getCompanyName() {
        return "" + mEditTextCompanyName.getText().toString().trim();
    }
    private String getBranchNumber() {
        return "" + mEditTextBranchNumber.getText().toString().trim();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mail, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_revert == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mButtonSave :
                if (! getCompanyName().equals("") && strFilter(getCompanyName())) {
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("COMPANYNAME", getCompanyName());
                    if (! getBranchNumber().equals("") && strFilter(getBranchNumber())) {
                        editor.putString("BRANCHNUMBER", getBranchNumber());
                    }
                    editor.commit();
                    mEditTextCompanyName.setText("");
                    mEditTextBranchNumber.setText("");
                    promptMessage("Notice", "已經修改完成", MailActivity.this);
                }
                break;

            case R.id.mButtonMail :
                if (checkInternetConnect(MailActivity.this)) {
                    if (null != mLinkedList  && mLinkedList.size() > 0) {
                        // .txt附檔名, 格式為cvs
                        String dateTime = getDateTime();
                        String date = dateTime.split(" ")[0].replace("-", "");
                        String time = dateTime.split(" ")[1].replace(":", "");
                        String mailTitle = "ezRestock" + "_" + getSPofCompanyName(mSharedPreferences) + "_" + getSPofBranchNumber(mSharedPreferences)
                                + "_" + date + "_" + time;
                        Log.d("mailTitle : ", mailTitle);

                    }else {
                        // notification database is no data exist
                        promptMessage("資料庫", "目前沒有資料存在!", MailActivity.this);
                    }
                }else {
                    // notification Netwoek is no connect
                    promptMessage("網路", "搜尋不到網路", MailActivity.this);
                }
                break;

            case R.id.mTextViewVisitHome :
                if (checkInternetConnect(MailActivity.this)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                    startActivity(intent);
                }else {
                    // notification Netwoek is no connect
                    promptMessage("網路", "搜尋不到網路", MailActivity.this);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences = null;
        finish();
    }
}
