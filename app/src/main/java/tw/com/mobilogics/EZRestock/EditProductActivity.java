package tw.com.mobilogics.EZRestock;

import org.json.JSONException;
import org.json.JSONObject;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import static tw.com.mobilogics.EZRestock.Utils.IsSmallerScreen;
import static tw.com.mobilogics.EZRestock.Utils.setActionBarFontFamily;

public class EditProductActivity extends ActionBarActivity {
  private EditText mEditTextProCode = null;
  private EditText mEditTextBarcode = null;
  private EditText mEditTextProDesc = null;
  private EditText mEditTextProUnitS = null;
  private EditText mEditTextProUnitL = null;
  private EditText mEditTextPackageQ = null;
  private EditText mEditTextSupCope = null;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (IsSmallerScreen(EditProductActivity.this)) {
      setContentView(R.layout.activity_edit_product_smaller);
    }else {
      setContentView(R.layout.activity_edit_product);
    }
    initial();

    try {
      JSONObject jsonObject = new JSONObject(getIntent().getStringExtra("EDIT_PRODUCT_JSON_DATA"));
      // 商品代碼, 國際條碼, 商品描述, 小單位,    大單位,    包裝量,    廠商
      // ProCode, Barcode, ProDesc, ProUnitS, ProUnitL, PackageQ, SupCode
      mEditTextProCode.setText(jsonObject.get("ProCode").toString().trim());
      mEditTextBarcode.setText(jsonObject.get("Barcode").toString().trim());
      mEditTextProDesc.setText(jsonObject.get("ProDesc").toString().trim());
      mEditTextProUnitS.setText(jsonObject.get("ProUnitS").toString().trim());
      mEditTextProUnitL.setText(jsonObject.get("ProUnitL").toString().trim());
      mEditTextPackageQ.setText(jsonObject.get("PackageQ").toString().trim());
      mEditTextSupCope.setText(jsonObject.get("SupCode").toString().trim());
    }catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void initial() {
    mEditTextProCode = (EditText) findViewById(R.id.mEditTextProCode);
    mEditTextBarcode = (EditText) findViewById(R.id.mEditTextBarcode);
    mEditTextProDesc = (EditText) findViewById(R.id.mEditTextProDesc);
    mEditTextProUnitS = (EditText) findViewById(R.id.mEditTextProUnitS);
    mEditTextProUnitL = (EditText) findViewById(R.id.mEditTextProUnitL);
    mEditTextPackageQ = (EditText) findViewById(R.id.mEditTextPackageQ);
    mEditTextSupCope = (EditText) findViewById(R.id.mEditTextSupCope);

    setActionBarFontFamily(EditProductActivity.this, "Quicksand-Bold.ttf");
    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e6e6e6")));
    getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>Product</font>"));
    getSupportActionBar().setIcon(R.drawable.ic_unconnect);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.edit_product, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_save :
        Toast.makeText(EditProductActivity.this, "No", Toast.LENGTH_LONG).show();
        break;
      case R.id.action_revert:
        finish();
        break;
    }
    return super.onOptionsItemSelected(item);
  }
}
