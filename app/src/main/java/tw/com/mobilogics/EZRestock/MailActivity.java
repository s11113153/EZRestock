package tw.com.mobilogics.EZRestock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static tw.com.mobilogics.EZRestock.Utils.checkInternetConnect;
import static tw.com.mobilogics.EZRestock.Utils.createEZRestockDir;
import static tw.com.mobilogics.EZRestock.Utils.getDateTime;
import static tw.com.mobilogics.EZRestock.Utils.promptMessage;
import static tw.com.mobilogics.EZRestock.Utils.strFilter;
import static tw.com.mobilogics.EZRestock.Utils.createEZRestockFile;


public class MailActivity extends ActionBarActivity implements View.OnClickListener{
    private static LinkedList<String> mLinkedList = null;
    private EditText mEditTextCompanyName = null;
    private EditText mEditTextBranchNumber = null;
    private Button mButtonSave = null;
    private Button mButtonMail = null;
    private TextView mTextViewVisitHome = null;

    private SharedPreferences mSharedPreferences = null;

    private final String TAG = ((Object)this).getClass().getSimpleName();
    private final String URL = "http://www.mobilogics.com.tw";

    private boolean mEZRestockDirIsExist = false;
    private final String EZRestockDirPath = Environment.getExternalStorageDirectory().getPath() + "/EZRestock";

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


        mEZRestockDirIsExist = createEZRestockDir(Environment.getExternalStorageDirectory().getPath(), "EZRestock");
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
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                boolean editResult = false;

                if (! getCompanyName().equals("") && strFilter(getCompanyName())) {
                    editor.putString("COMPANYNAME", getCompanyName());
                    editResult = true;
                }
                if (! getBranchNumber().equals("") && strFilter(getBranchNumber())) {
                    editor.putString("BRANCHNUMBER", getBranchNumber());
                    editResult = true;
                }
                if (editResult) {
                    editor.commit();
                    mEditTextCompanyName.setText("");
                    mEditTextBranchNumber.setText("");
                    promptMessage("Notice", "已經修改完成", MailActivity.this);
                }
                break;

            case R.id.mButtonMail :
                if (checkInternetConnect(MailActivity.this)) {
                    if (null != mLinkedList  && mLinkedList.size() > 0) {
                        String dateTime = getDateTime();
                        String date = dateTime.split(" ")[0].replace("-", "");
                        String time = dateTime.split(" ")[1].replace(":", "");
                        String companyName  = mSharedPreferences.getString("COMPANYNAME", "");
                        String branchNumber = mSharedPreferences.getString("BRANCHNUMBER", "");

                        String mailSubject = "ezRestock" + "_" + companyName + "_" + branchNumber + "_" + date + "_" + time;
                        String fileName =  companyName + "_" + branchNumber + "_" + date + "_" + time + ".txt";

                        if (mEZRestockDirIsExist) {
                            File file;
                            try {
                                if ((file = createEZRestockFile(EZRestockDirPath, fileName)) != null) {
                                    FileWriter fileWriter = new FileWriter(file);
                                    fileWriter.write("ProCode,Quantity,Inventory,Date,Time\n");
                                    for (int i=0;i<mLinkedList.size();i++) {
                                        //originFormat 4710063022883_5_0_2014-06-07 08:56:13
                                        //makeFormat   4710063022883,5,0,20130604,1659
                                        String s = mLinkedList.get(i).replace("_" , ",").replace("-", "")
                                                .replace(" ",",").replace(":", "");
                                        fileWriter.write(s + "\n");
                                    }
                                    fileWriter.close();

                                    // open Gmail && send mail
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
                                    intent.putExtra(Intent.EXTRA_SUBJECT, mailSubject);
                                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                                    startActivity(intent);
                                } else {
                                    promptMessage("Fail", "create EZRestockFile is Fail", MailActivity.this);
                                }
                            } catch(IOException e) {
                                Log.e(TAG, "EZRestock file is throw IOException , maybe be created or write file");
                              e.printStackTrace();
                            }
                        } else {
                            promptMessage("Fail", "EZRestock Directory is no exist", MailActivity.this);
                        }
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
        // clean objects references
        mSharedPreferences = null;
        mLinkedList = null;
        finish();
    }
}
