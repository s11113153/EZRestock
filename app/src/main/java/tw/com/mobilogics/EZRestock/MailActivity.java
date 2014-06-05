package tw.com.mobilogics.EZRestock;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;


public class MailActivity extends ActionBarActivity implements View.OnClickListener{
    private static LinkedList<String> mLinkedList = null;
    private EditText mEditTextCompanyName = null;
    private EditText mEditTextBranchNumber = null;
    private Button mButtonSave = null;
    private Button mButtonMail = null;
    private TextView mTextViewVisitHome = null;
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
        return mEditTextCompanyName.getText().toString().trim();
    }
    private String getBranchNumber() {
        return mEditTextBranchNumber.getText().toString().trim();
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
                break;
            case R.id.mButtonMail :
                break;
            case R.id.mTextViewVisitHome :
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                startActivity(intent);
                break;
        }
    }
}
