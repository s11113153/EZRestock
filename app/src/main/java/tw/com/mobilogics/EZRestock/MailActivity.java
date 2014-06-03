package tw.com.mobilogics.EZRestock;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

public class MailActivity extends ActionBarActivity{
    private Button btnMail, btnSave;
    private EditText etCompanyName, etBranchNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail);
        initial();
    }
    private void initial() {
        setTitle("Mail");
        btnMail = (Button) findViewById(R.id.mail_btn_mail);
        btnSave = (Button) findViewById(R.id.mail_btn_save);
        etCompanyName = (EditText) findViewById(R.id.mail_et_company_name);
        etBranchNumber = (EditText) findViewById(R.id.mail_et_branch_number);
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
}
