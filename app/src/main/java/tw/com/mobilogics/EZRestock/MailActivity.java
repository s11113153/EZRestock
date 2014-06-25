package tw.com.mobilogics.EZRestock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static tw.com.mobilogics.EZRestock.Utils.IsSmallerScreen;
import static tw.com.mobilogics.EZRestock.Utils.checkInternetConnect;
import static tw.com.mobilogics.EZRestock.Utils.createEZRestockCachedFile;
import static tw.com.mobilogics.EZRestock.Utils.createEZRestockDir;
import static tw.com.mobilogics.EZRestock.Utils.getDateTime;
import static tw.com.mobilogics.EZRestock.Utils.promptMessage;
import static tw.com.mobilogics.EZRestock.Utils.setActionBarFontFamily;
import static tw.com.mobilogics.EZRestock.Utils.strFilter;
import static tw.com.mobilogics.EZRestock.Utils.createEZRestockFile;
import static tw.com.mobilogics.EZRestock.Utils.writeEZRestockFile;

public class MailActivity extends ActionBarActivity implements View.OnClickListener {

  private static LinkedList<String> mLinkedList = null;

  private EditText mEditTextCompanyName = null;

  private EditText mEditTextBranchNumber = null;

  private EditText mEditTextReceiveMail = null;

  private Button mButtonSave = null;

  private Button mButtonMail = null;

  private ImageView mImageViewVisitHome = null;

  private SharedPreferences mSharedPreferences = null;

  private final String TAG = ((Object) this).getClass().getSimpleName();

  private final String URL = "http://www.mobilogics.com.tw";

  private boolean mEZRestockDirIsExist = false;

  private final String EZRestockDirPath = Environment.getExternalStorageDirectory().getPath()+ "/EZRestock";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (IsSmallerScreen(MailActivity.this)) {
      setContentView(R.layout.activity_mail_smaller);
    }else {
      setContentView(R.layout.avtivity_mail);
    }
    initial();

    // EZRestock Directory Whether exists
    mEZRestockDirIsExist = createEZRestockDir(Environment.getExternalStorageDirectory().getPath(), "EZRestock");
    // IF Setting MailAddress Then Display MailAddress
    mEditTextReceiveMail.setText("" + mSharedPreferences.getString("RECEIVEMAIL", ""));
  }

  private void initial() {
    setTitle("Mail");
    mLinkedList = new LinkedList<String>((List) getIntent().getSerializableExtra("ListData"));
    mSharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
    mEditTextCompanyName  = (EditText) findViewById(R.id.mEditTextCompanyName);
    mEditTextBranchNumber = (EditText) findViewById(R.id.mEditTextBranchNumber);
    mEditTextReceiveMail  = (EditText) findViewById(R.id.mEditTextReceiveMail);
    mButtonSave = (Button) findViewById(R.id.mButtonSave);
    mButtonMail = (Button) findViewById(R.id.mButtonMail);
    mImageViewVisitHome = (ImageView) findViewById(R.id.mImageViewVisitHome);

    mImageViewVisitHome.setOnClickListener(this);
    mButtonSave.setOnClickListener(this);
    mButtonMail.setOnClickListener(this);

    // Setting ActionBar Theme
    setActionBarFontFamily(MailActivity.this, "Quicksand-Bold.ttf");
    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e6e6e6")));
    getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>Mail</font>"));

    // Setting Icon Of UnConnect
    getSupportActionBar().setIcon(R.drawable.ic_unconnect);
  }

  private String getCompanyName()  { return "" + mEditTextCompanyName.getText().toString().trim();  }
  private String getBranchNumber() { return "" + mEditTextBranchNumber.getText().toString().trim(); }
  private String getReceiveMail()  { return "" + mEditTextReceiveMail.getText().toString().trim();  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.mail, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (R.id.action_revert == item.getItemId()) { finish(); }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.mButtonSave:
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        boolean editResult = false;
        // handle companyName
        if (! getCompanyName().equals("") && strFilter(getCompanyName())) {
          editor.putString("COMPANYNAME", getCompanyName());
          editResult = true;
        }
        // handle branchNumber
        if (! getBranchNumber().equals("") && strFilter(getBranchNumber())) {
          editor.putString("BRANCHNUMBER", getBranchNumber());
          editResult = true;
        }
        // handle receiveMail , and judge valid format of email
        if (! getReceiveMail().equals("") && ! getReceiveMail().equals(mSharedPreferences.getString("RECEIVEMAIL",""))) {
          if (! Linkify.addLinks(mEditTextReceiveMail.getText(), Linkify.EMAIL_ADDRESSES)) {
            editResult = false;
            promptMessage("Error", "Auto-Receive-Mail format error", MailActivity.this);
          }else {
            mEditTextReceiveMail.setText(getReceiveMail());// cancel Linkify Auto under Line && text of blue color
            editor.putString("RECEIVEMAIL", getReceiveMail());
            editResult = true;
          }
        }
        if (editResult) {
          editor.commit();
          mEditTextCompanyName.setText("");
          mEditTextBranchNumber.setText("");
          promptMessage("Notice", "已經修改完成", MailActivity.this);
        }
        break;

      case R.id.mButtonMail:
        File file = null;
        if (! checkInternetConnect(MailActivity.this)) {
          // notification Netwoek is no connect
          promptMessage("網路", "搜尋不到網路", MailActivity.this);
        } else {
          if (null == mLinkedList || mLinkedList.size() <= 0) {
            // notification database is no data exist
            promptMessage("資料庫", "目前沒有資料存在!", MailActivity.this);
          }else {
            // handle Mail of fileName && mailSubject
            String dateTime = getDateTime();
            String date = dateTime.split(" ")[0].replace("-", "");
            String time = dateTime.split(" ")[1].replace(":", "");
            String companyName = mSharedPreferences.getString("COMPANYNAME", "");
            String branchNumber = mSharedPreferences.getString("BRANCHNUMBER", "");

            String mailSubject = "ezRestock" + "_" + companyName + "_" + branchNumber + "_" + date + "_" + time;
            String fileName = companyName + "_" + branchNumber + "_" + date + "_" + time + ".txt";

            if (! mEZRestockDirIsExist) { // use Internal Storage
              try {
                if ((file = createEZRestockCachedFile(MailActivity.this, fileName)) != null) {
                  writeEZRestockFile(file, mLinkedList);
                }
                // open Gmail && send mail
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{
                    mSharedPreferences.getString("RECEIVEMAIL", "")});
                intent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_SUBJECT, mailSubject);
                intent.putExtra(Intent.EXTRA_STREAM,
                    Uri.parse("content://" + CachedFileProvider.getAuthority() + "/" + fileName));
                startActivity(intent);
              }catch (IOException e) {
                e.printStackTrace();
              }
            } else { // use External Stroage of EZRestock DIR
              try {
                if ( (file = createEZRestockFile(EZRestockDirPath, fileName)) != null) {
                  writeEZRestockFile(file, mLinkedList);
                  // open Gmail && send mail
                  Intent intent = new Intent(Intent.ACTION_VIEW);
                  intent.putExtra(Intent.EXTRA_EMAIL, new String[]{
                      mSharedPreferences.getString("RECEIVEMAIL", "")});
                  intent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
                  intent.putExtra(Intent.EXTRA_SUBJECT, mailSubject);
                  intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                  startActivity(intent);
                } else {
                  promptMessage("Fail", "create EZRestockFile is Fail", MailActivity.this);
                }
              } catch (IOException e) {
                Log.e(TAG, "EZRestock file is throw IOException , maybe be created or write file");
                e.printStackTrace();
              }
            }
          }
        }
        break;

      case R.id.mImageViewVisitHome:
        if (checkInternetConnect(MailActivity.this)) {
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
          startActivity(intent);
        } else {
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
