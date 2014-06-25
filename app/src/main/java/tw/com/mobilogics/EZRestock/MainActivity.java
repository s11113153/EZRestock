package tw.com.mobilogics.EZRestock;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;


import static tw.com.mobilogics.EZRestock.Utils.checkInternetConnect;
import static tw.com.mobilogics.EZRestock.Utils.getDateTime;
import static tw.com.mobilogics.EZRestock.Utils.getFontFamily;
import static tw.com.mobilogics.EZRestock.Utils.getSQLiteDatabaseInsrance;
import static tw.com.mobilogics.EZRestock.Utils.promptMessage;
import static tw.com.mobilogics.EZRestock.Utils.IsSmallerScreen;
import static tw.com.mobilogics.EZRestock.Utils.searchOneOfProductData;
import static tw.com.mobilogics.EZRestock.Utils.searchProID;
import static tw.com.mobilogics.EZRestock.Utils.setSQLiteDatabaseInsrance;
import static tw.com.mobilogics.EZRestock.Utils.setActionBarFontFamily;

public class MainActivity extends ActionBarActivity implements View.OnFocusChangeListener {

  private DBHelper mDBHelper = null;

  private SharedPreferences mSharedPreferences = null;

  private LayoutInflater mInflater = null;

  private InputMethodManager mInputMethodManager = null;

  private ListAdapter mListAdapter = new ListAdapter();

  private EditText mEditTextQuantity = null;

  private EditText mEditTextInventory = null;

  private EditText mEditTextScanNumber = null;

  private Button mButtonScan = null;

  private ListView mListView = null;

  private TextView mTextViewProDesc = null;

  private ImageView mImageEditProduct = null;

  private long mId = -1;

  private static final String M_TABLE_MANAGEMENT = "Management";

  private static final String M_TABLE_PRODUCTCODE = "ProductCode";

  private static final String M_TABLE_PRODUCRS = "Products";

  private LinkedList<String> mLinkedList = new LinkedList<String>();

  private ArrayList<String> mArrayListProducts = new ArrayList<String>();

  private ArrayList<String> mArrayListProductCode = new ArrayList<String>();

  private SQLiteDatabase mSQLiteDatabaseWrite = null;

  private SQLiteDatabase mSQLiteDatabaseRead = null;

  private JSONObject mJSONObject = null;

  private static SlidingMainFragment mSlidingUpMainFragment = new SlidingMainFragment();

  private static FragmentManager mFragmentManager;

  private boolean M_UI_STATE = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (IsSmallerScreen(MainActivity.this)) {
      setContentView(R.layout.activity_main_smaller);
    }
    else {
      setContentView(R.layout.activity_main);
    }
    initial();
    mEditTextScanNumber.requestFocus(); // Default First Focus
    loadActivityTitle();

    mButtonScan.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String select = selectManagement(getScanNumber()); // if return "" , no Data exist

        if (!getScanNumber().equals("")) { setEditProductView(); };

        try {
          double quantity  = Double.parseDouble(getQuantity());
          double inventory = Double.parseDouble(getInventory());

          if (!getScanNumber().equals("") && !select.equals("")) {
            if (0 == quantity && 0 == inventory) { // query
              // According to select Result , set EditTex{Quantity && Inventory} values。
              // on the first line is display select result && other lines are sort order by DateTime
              String [] mResult = select.split("_");
              mEditTextQuantity.setText(mResult[1]); // Quantity
              mEditTextInventory.setText(mResult[2]);// Inventory
              mEditTextQuantity.requestFocus();
              refreshListManagementData();
              setListDataOrderByManagementSelectId(getScanNumber());
              mListView.setAdapter(mListAdapter);
            }else { // update , display result
              updateManagement(getSelectId());
              refreshListManagementData();
              mListView.setAdapter(mListAdapter);
            }
          }else if (!getScanNumber().equals("") && select.equals("")
              && (Double.parseDouble(getQuantity()) != 0 || Double.parseDouble(getInventory()) != 0)) { // insert into
            InsertManagement(getScanNumber(), Integer.parseInt(getQuantity()), Integer.parseInt(getInventory()));
            refreshListManagementData();
            mListView.setAdapter(mListAdapter);
          }
        }catch (NumberFormatException e) {
          e.printStackTrace();
        }
      }
    });

    mImageEditProduct.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mJSONObject) {
          Intent intent = new Intent(MainActivity.this, EditProductActivity.class);
          intent.putExtra("EDIT_PRODUCT_JSON_DATA", mJSONObject.toString());
          startActivity(intent);
        }
      }
    });

    mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
      @Override
      public void onBackStackChanged() {
        if (!(M_UI_STATE = ! M_UI_STATE)) {
          setUIWidgetEnable(true);
          mListView.setVisibility(View.VISIBLE);
        }
        else {
          setUIWidgetEnable(false);
          mListView.setVisibility(View.INVISIBLE);
        }
      }
    });

  }
  private void initial() {
    mInflater =  (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
    mSharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);

    mEditTextQuantity  =  (EditText) findViewById(R.id.mEditTextQuantity);
    mEditTextInventory =  (EditText) findViewById(R.id.mEditTextInventory);
    mEditTextScanNumber = (EditText) findViewById(R.id.mEditTextScanNumber);

    mButtonScan = (Button) findViewById(R.id.mButtonScan);
    mButtonScan.setFocusableInTouchMode(true);
    mButtonScan.setInputType(InputType.TYPE_NULL);

    mListView = (ListView) findViewById(R.id.mListView);

    mTextViewProDesc = (TextView) findViewById(R.id.mTextViewProDesc);
    mTextViewProDesc.setTypeface(getFontFamily(MainActivity.this, "Arial-Bold.ttf"));

    mImageEditProduct = (ImageView) findViewById(R.id.mImageEditProduct);

    mEditTextQuantity.setOnFocusChangeListener(this);
    mEditTextQuantity.setTypeface(getFontFamily(MainActivity.this, "Quicksand-Regular.ttf"));

    mEditTextInventory.setOnFocusChangeListener(this);
    mEditTextInventory.setTypeface(getFontFamily(MainActivity.this, "Quicksand-Regular.ttf"));

    mEditTextScanNumber.setOnFocusChangeListener(this);
    mEditTextScanNumber.setTypeface(getFontFamily(MainActivity.this, "Arial-Regular.ttf"));

    mButtonScan.setOnFocusChangeListener(this);

    openDB("Database");
    setSQLiteDatabaseInsrance(mDBHelper);
    mSQLiteDatabaseWrite = getSQLiteDatabaseInsrance().getWritableDatabase();
    mSQLiteDatabaseRead = getSQLiteDatabaseInsrance().getReadableDatabase();

    mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    mFragmentManager = getSupportFragmentManager();

    // Setting ActionBar Theme
    setActionBarFontFamily(MainActivity.this, "Quicksand-Bold.ttf");
    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e6e6e6")));

    // Setting Icon Of UnConnect
    getSupportActionBar().setIcon(R.drawable.ic_unconnect);
  }

  private void loadActivityTitle() {
    String companyName = mSharedPreferences.getString("COMPANYNAME", "");
    String branchNumber = mSharedPreferences.getString("BRANCHNUMBER", "");
    getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>" + companyName + " - " + branchNumber + "</font>"));
  }

  private String getScanNumber() { return "" + mEditTextScanNumber.getText().toString().trim(); }

  private void openDB(String CompanyName) { mDBHelper = new DBHelper(MainActivity.this, CompanyName + ".db", 1); }

  private void closeDB() {
    mDBHelper.close();
    mSQLiteDatabaseRead.close();
    mSQLiteDatabaseWrite.close();
  }

  private String getQuantity() { return "" + mEditTextQuantity.getText().toString().trim(); }

  private String getInventory() { return "" + mEditTextInventory.getText().toString().trim(); }

  /** According query result, store select Id */
  private void setSelectId(long Id) { mId = Id; }

  /** return Id  */
  private long getSelectId() { return mId; }


  /** select ScanNumber From Management Table has exists ?
  *  true :  return result
  *  false : return ""
  * */
  private String selectManagement(String scanNumber) {
    String quantity = "" , inventory = "";
    Cursor cursor = mSQLiteDatabaseRead.rawQuery(
        "select Id, ScanNumber, Quantity, Inventory from " + M_TABLE_MANAGEMENT + " Where ScanNumber=?", new String[]{scanNumber});
    if (cursor.moveToFirst()) {
      setSelectId(Integer.parseInt(cursor.getString(0)));
      quantity = "" + cursor.getString(2);
      inventory ="" + cursor.getString(3);
    }
    cursor.close();
    if (quantity.equals("") && inventory.equals("")) {
      return  "";
    }else {
      return scanNumber + "_" + quantity + "_" + inventory;
    }
  }

  /** insert table From Management */
  private void InsertManagement(String scanNumber, int quantity, int inventory) {
    ContentValues values = new ContentValues();
    values.put("ScanNumber", scanNumber);
    values.put("Quantity"  , quantity);
    values.put("Inventory" , inventory);
    values.put("createTime" , getDateTime());
    if (-1 == mSQLiteDatabaseWrite.insert(M_TABLE_MANAGEMENT, null, values)) {
      promptMessage("Notice", "Insert Into Fail", MainActivity.this);
    }
  }

  /** if Update return -1 , then show Message */
  private void updateManagement(long id) {
    ContentValues values = new ContentValues();
    values.put("Quantity"  , getQuantity());
    values.put("Inventory" , getInventory());
    values.put("createTime", getDateTime());
    if (-1 == mSQLiteDatabaseWrite.update(M_TABLE_MANAGEMENT, values, "Id=" + id, null)) {
      promptMessage("Notice", "Update Fail", MainActivity.this);
    }
    setSelectId(-1);
  }

  /**
   *  Table : Management
   *  According select id, display data on the first line ,
   *  Other datas are order by DateTime
   */
  private void setListDataOrderByManagementSelectId(String ScanNumber) {
    String tmpContent = "";
    for (int i=0; i<mLinkedList.size(); i++) {
      String compare = mLinkedList.get(i).split("_")[0];
      if (compare.equals(ScanNumber)) {
        tmpContent = mLinkedList.get(i);
        mLinkedList.remove(i);
        break;
      }
    }
    mLinkedList.addFirst(tmpContent);
  }

  private void refreshListManagementData() {
    mLinkedList.clear();
    Cursor cursor = mSQLiteDatabaseRead.rawQuery("SELECT ScanNumber, Quantity, Inventory, createTime" +
        " FROM " + M_TABLE_MANAGEMENT + " ORDER BY datetime(createTime) DESC", null);
    int count = 0;
    while (cursor.moveToNext()) {
      String rowQuery = cursor.getString(0) + "_" + cursor.getString(1) + "_" + cursor.getString(2) + "_" +cursor.getString(3);
      mLinkedList.add(count++, rowQuery);
    }
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (!hasFocus) {
      switch (v.getId()) {
        case R.id.mEditTextQuantity :
          if (getQuantity().equals("")) {
            mEditTextQuantity.setText("0");
          }
        break;

        case R.id.mEditTextInventory :
          if (getInventory().equals("")) {
            mEditTextInventory.setText("0");
          }
        break;

        case R.id.mEditTextScanNumber :
          setEditProductView();
        break;
      }
    } else {
      if (R.id.mButtonScan == v.getId()) {
        mInputMethodManager.hideSoftInputFromWindow(this.mButtonScan.getWindowToken(), 0);
        this.mButtonScan.performClick();
      }
    }
  }

  class ListAdapter extends BaseAdapter {
    @Override
    public int getCount() {
      return mLinkedList.size() >= 4 ? 4 : mLinkedList.size();
    }

    @Override
    public Object getItem(int position) { return mLinkedList.get(position); }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      convertView = mInflater.inflate(R.layout.row, null);
      TextView rowScanNumber = (TextView) convertView.findViewById(R.id.rowScanNumber);
      TextView rowQuantity = (TextView) convertView.findViewById(R.id.rowQuantity);
      TextView rowInventory = (TextView) convertView.findViewById(R.id.rowInventory);
      String row[]= mLinkedList.get(position).trim().split("_");
      rowScanNumber.setText(row[0]);
      rowQuantity.setText(row[1]);
      rowInventory.setText(row[2]);
      convertView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Bundle bundle = new Bundle();
          bundle.putSerializable("ListData", mLinkedList);
          mSlidingUpMainFragment.setArguments(bundle);
          FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
          fragmentTransaction.replace(R.id.contentFrame, mSlidingUpMainFragment);
          fragmentTransaction.addToBackStack(null);
          fragmentTransaction.commit();
        }
      });

      if (position %2 == 1) {
       convertView.setBackgroundColor(Color.parseColor("#e6e6e6"));
      }
      return convertView;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_email :
        refreshListManagementData();
        Intent intent = new Intent(MainActivity.this, MailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("ListData", mLinkedList);
        intent.putExtras(bundle);
        startActivity(intent);
      break;

      case R.id.action_download :
        if (!checkInternetConnect(MainActivity.this)) {
          promptMessage("NetWork", "Not Found NetWork", MainActivity.this);
        }else {
          final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
          builder.setTitle("下載及跟新");
          builder.setMessage("這步驟可能會花上幾分鐘, 確定嗎？");
          builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              new DownloadAsync().execute();
            }
          });
          builder.setNegativeButton("No", null);
          builder.setCancelable(false);
          builder.show();
        }
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // clean objects references
    closeDB();
    mSharedPreferences = null;
    mInflater = null;
    mInputMethodManager = null;
    finish();
  }

  /** Let Title reset, No matter MailActivity has been modified {CompanyName or BranchNumber} 。*/
  @Override
  protected void onResume() {
    super.onResume();
    loadActivityTitle();
  }


  /** open new Thread, handle download a&& update data to DB */
  private class DownloadAsync extends AsyncTask<Void, Integer, Boolean > {
    private ProgressDialog mProgressDialog;
    private static final String M_DOWNLOAD_PRODUCTS = "http://cdn.aps.pos.tw/Products.txt";
    private static final String M_DOWNLOAD_PRODUCTCODE = "http://cdn.aps.pos.tw/ProductCode.txt";
    private SQLiteStatement mInsertStatement, mUpdateStatement;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mProgressDialog = new ProgressDialog(MainActivity.this);
      mProgressDialog.setTitle("更新");
      mProgressDialog.setMessage("更新資料庫中 ... , 請稍後");
      mProgressDialog.setCancelable(false);
      mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      mProgressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      HttpURLConnection httpURLConnection;
      InputStream inputStream;
      BufferedReader bufferedReader;
      String line;
      double rate;
      int count = 0;

      try {
        // Download ProductCode
        httpURLConnection = getURLConnectInstance(M_DOWNLOAD_PRODUCTCODE);
        httpURLConnection.connect();
        if (HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()) {
          inputStream = httpURLConnection.getInputStream();
          bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
          bufferedReader.readLine();
          while ((line = bufferedReader.readLine()) != null) { mArrayListProductCode.add(line); }
        }

        //Download Products
        httpURLConnection = getURLConnectInstance(M_DOWNLOAD_PRODUCTS);
        httpURLConnection.connect();
        if (HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()) {
          inputStream = httpURLConnection.getInputStream();
          bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
          bufferedReader.readLine();
          while ((line = bufferedReader.readLine()) != null) { mArrayListProducts.add(line); }
        }

        // download rate
        rate = 100.0 / (mArrayListProductCode.size()+ mArrayListProducts.size());
        mSQLiteDatabaseWrite.beginTransaction();

        // judge uses Update or Insert for ProductCode Table
        mInsertStatement = mSQLiteDatabaseWrite.compileStatement("INSERT INTO ProductCode (PCoID, ProID, ProCode, SupID) VALUES (?, ?, ?, ?)");
        mUpdateStatement = mSQLiteDatabaseWrite.compileStatement("UPDATE ProductCode SET ProID=?, ProCode=?, SupID=? WHERE PCoID=?");
        for (int i = 0; i < mArrayListProductCode.size(); i++) {
          String srr[] = mArrayListProductCode.get(i).split(",");
          Cursor cursor = mSQLiteDatabaseRead.rawQuery("SELECT PCoID FROM ProductCode WHERE PCoID=?", new String[]{srr[0]});
          count = handleDownloadDataToDB(srr, cursor, mInsertStatement, mUpdateStatement, count);
          cursor.close();
          publishProgress(Integer.valueOf((int) (count++ * rate)));
        }

        // judge uses Update or Insert for Products Table
        mInsertStatement = mSQLiteDatabaseWrite.compileStatement(
            "INSERT INTO Products (ProID, ProCode, BarCode, ProDesc, ProUnitS, ProUnitL, PackageQ, SupCode) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        mUpdateStatement = mSQLiteDatabaseWrite.compileStatement(
            "UPDATE Products SET ProCode=?, BarCode=?, ProDesc=?, ProUnitS=?, ProUnitL=?, PackageQ=?, SupCode=? WHERE ProID=?");
        for (int i = 0; i < mArrayListProducts.size(); i++) {
          String srr[] = mArrayListProducts.get(i).split(",");
          Cursor cursor = mSQLiteDatabaseRead.rawQuery("SELECT ProID FROM Products WHERE ProID=?", new String[]{srr[0]});
          count = handleDownloadDataToDB(srr, cursor, mInsertStatement, mUpdateStatement, count);
          cursor.close();
          publishProgress(Integer.valueOf((int) (count++ * rate)));
        }

        mSQLiteDatabaseWrite.setTransactionSuccessful();
        mSQLiteDatabaseWrite.endTransaction();
        mArrayListProducts.clear();
        mArrayListProductCode.clear();
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return true;
    }

    /** return Url Connect Instance */
    private HttpURLConnection getURLConnectInstance(String DownloadPath) throws IOException {
      return (HttpURLConnection) new URL(DownloadPath).openConnection();
    }

    private int handleDownloadDataToDB(String srr[], Cursor cursor, SQLiteStatement insertStatement, SQLiteStatement updateStatement, int count) {
      if (srr.length == 4) { // ProductCode Table
        if (cursor.getCount() != 0) {
          updateStatement.bindString(1, srr[1]);
          updateStatement.bindString(2, srr[2]);
          updateStatement.bindString(3, srr[3]);
          updateStatement.bindString(4, srr[0]);
          updateStatement.execute();
          updateStatement.clearBindings();
        } else {
          insertStatement.bindString(1, srr[0]);
          insertStatement.bindString(2, srr[1]);
          insertStatement.bindString(3, srr[2]);
          insertStatement.bindString(4, srr[3]);
          insertStatement.execute();
          insertStatement.clearBindings();
        }
      } else if (srr.length == 8) {// Products Table
        if (cursor.getCount() != 0) {
          updateStatement.bindString(1, srr[1]);
          updateStatement.bindString(2, srr[2]);
          updateStatement.bindString(3, srr[3]);
          updateStatement.bindString(4, srr[4]);
          updateStatement.bindString(5, srr[5]);
          updateStatement.bindString(6, srr[6]);
          updateStatement.bindString(7, srr[7]);
          updateStatement.bindString(8, srr[0]);
          updateStatement.execute();
          updateStatement.clearBindings();
        } else {
          insertStatement.bindString(1, srr[0]);
          insertStatement.bindString(2, srr[1]);
          insertStatement.bindString(3, srr[2]);
          insertStatement.bindString(4, srr[3]);
          insertStatement.bindString(5, srr[4]);
          insertStatement.bindString(6, srr[5]);
          insertStatement.bindString(7, srr[6]);
          insertStatement.bindString(8, srr[7]);
          insertStatement.execute();
          insertStatement.clearBindings();
        }
      }
      return count++;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
      super.onProgressUpdate(values);
      mProgressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
      super.onPostExecute(aBoolean);
      if (aBoolean) {
        mProgressDialog.dismiss();
        Toast.makeText(MainActivity.this, "更新完畢", Toast.LENGTH_LONG).show();
      }
    }
  }

  private void setUIWidgetEnable(boolean enable) {
    mEditTextScanNumber.setEnabled(enable);
    mImageEditProduct.setEnabled(enable);
    mEditTextQuantity.setEnabled(enable);
    mEditTextInventory.setEnabled(enable);
    mButtonScan.setEnabled(enable);
    mListView.setEnabled(enable);
  }

  /**
   * query scanNumber from Product table && according result to display View
   * result == "" , please Scan to get ProDesc ,
   * result != -1 , ProductName
   * result == -1 , new Product
   */
  private void setEditProductView() {
    mTextViewProDesc.setText("New Products");
    mImageEditProduct.setVisibility(View.INVISIBLE);
    if (!getScanNumber().equals("")) {
      int selectIndex = searchProID(getScanNumber(), M_TABLE_PRODUCTCODE, mSQLiteDatabaseRead);
      if (-1 != selectIndex) {
        try {
          mJSONObject = searchOneOfProductData(selectIndex, M_TABLE_PRODUCRS, mSQLiteDatabaseRead);
          String ProDesc = mJSONObject.get("ProDesc").toString().trim();
          mTextViewProDesc.setText(ProDesc);
          mImageEditProduct.setVisibility(View.VISIBLE);
        }catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }else if (getScanNumber().equals("")){
      mTextViewProDesc.setText("please Scan to get ProDesc");
    }
  }
}
