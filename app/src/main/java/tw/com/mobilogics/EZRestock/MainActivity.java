package tw.com.mobilogics.EZRestock;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import static tw.com.mobilogics.EZRestock.Utils.promptMessage;
import static tw.com.mobilogics.EZRestock.Utils.IsSmallerScreen;

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
  private long mId = -1;
  private final String TABLE_MANAGEMENT = "Management";
  private LinkedList<String> mLinkedList = new LinkedList<String>();
  private ArrayList<String> mArrayListProducts = new ArrayList<String>();
  private ArrayList<String> mArrayListProductCode = new ArrayList<String>();
  private SQLiteDatabase mSQLiteDatabaseWrite = null;
  private SQLiteDatabase mSQLiteDatabaseRead = null;

  @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (IsSmallerScreen(MainActivity.this)) {
      setContentView(R.layout.activity_main_smaller);
    }else {
      setContentView(R.layout.activity_main);
    }
    initial();
    mEditTextScanNumber.requestFocus(); // Default First Focus
    loadActivityTitle();

    mButtonScan.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if ( ! getScanNumber().equals("") && ! getQuantity().equals("") && !getInventory().equals("")) {
          String select = selectManagement(getScanNumber()); // if return "" , no Data exist
          // Execution select
          if (0 == Double.parseDouble(getQuantity()) && 0 == Double.parseDouble(getInventory())) {
            if (select.equals("")) {
              // Cancel Action
            } else {
              // According to select Result , set EditTex{Quantity && Inventory} values。
              // on the first line is display select result && other lines are sort order by DateTime
              String [] mResult = select.split("_");
              mEditTextQuantity.setText(mResult[1]); // Quantity
              mEditTextInventory.setText(mResult[2]);// Inventory
              mEditTextQuantity.requestFocus();
              refreshListManagementData();
              setListDataOrderByManagementSelectId(getScanNumber());
              mListView.setAdapter(mListAdapter);
            }
          }else {
            // Action for Insert OR Update
            if (select.equals("")) {// Execution insert
              InsertManagement(getScanNumber(), getQuantity(), getInventory());
              refreshListManagementData();
              mListView.setAdapter(mListAdapter);
            }else {// Execution update
              updateManagement(getSelectId());
              refreshListManagementData();
              mListView.setAdapter(mListAdapter);
            }
          }
        }else {
          Toast.makeText(MainActivity.this, "Scan Number is null", Toast.LENGTH_LONG).show();
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

    mEditTextQuantity.setOnFocusChangeListener(this);
    mEditTextInventory.setOnFocusChangeListener(this);
    mButtonScan.setOnFocusChangeListener(this);
    openDB("Database");
    mSQLiteDatabaseWrite = mDBHelper.getWritableDatabase();
    mSQLiteDatabaseRead = mDBHelper.getReadableDatabase();

    mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
  }

  private void loadActivityTitle() {
    String companyName = mSharedPreferences.getString("COMPANYNAME", "");
    String branchNumber = mSharedPreferences.getString("BRANCHNUMBER", "");
    getSupportActionBar().setTitle(companyName + " - " + branchNumber);
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
        "select Id, ScanNumber, Quantity, Inventory from " + TABLE_MANAGEMENT + " Where ScanNumber=?", new String[]{scanNumber});
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
  private void InsertManagement(String scanNumber, String quantity, String inventory) {
    ContentValues values = new ContentValues();
    values.put("ScanNumber", scanNumber);
    values.put("Quantity"  , quantity);
    values.put("Inventory" , inventory);
    values.put("createTime" , getDateTime());
    if (-1 == mSQLiteDatabaseWrite.insert(TABLE_MANAGEMENT, null, values)) {
      promptMessage("Notice", "Insert Into Fail", MainActivity.this);
    }
  }

  /** if Update return -1 , then show Message */
  private void updateManagement(long id) {
    ContentValues values = new ContentValues();
    values.put("Quantity"  , getQuantity());
    values.put("Inventory" , getInventory());
    values.put("createTime", getDateTime());
    if (-1 == mSQLiteDatabaseWrite.update(TABLE_MANAGEMENT, values, "Id=" + id, null)) {
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
        " FROM " + TABLE_MANAGEMENT + " ORDER BY datetime(createTime) DESC", null);
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
        case R.id.mEditTextQuantity:
          if (getQuantity().equals("")) {
            mEditTextQuantity.setText("0");
          }
        break;

        case R.id.mEditTextInventory:
          if (getInventory().equals("")) {
            mEditTextInventory.setText("0");
          }
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
      return mLinkedList.size();
    }

    @Override
    public Object getItem(int position) {
      return mLinkedList.get(position);
    }

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
        if (! checkInternetConnect(MainActivity.this)) {
          promptMessage("NetWork", "Not Found NetWork", MainActivity.this);
        }else {
          final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
          builder.setTitle("下載及跟新");
          builder.setMessage("這步驟可能會花上幾分鐘, 確定嗎？");
          builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              new DownloadDB().execute();
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


  /** open new Thread, handle download a&& update DB */
  private class DownloadDB extends AsyncTask<Void, Integer, Boolean > {
    private ProgressDialog mProgressDialog;
    private final String mDownloadProducts     = "http://cdn.aps.pos.tw/Products.txt";
    private final String mDownloadProductCode  = "http://cdn.aps.pos.tw/ProductCode.txt";
    private final String SQL_FOR_INSERT_ProductCode = "INSERT INTO ProductCode (PCoID, ProID, ProCode, SupID) VALUES (?, ?, ?, ?)";
    private final String SQL_FOR_UPDATE_ProductCode = "UPDATE ProductCode SET ProID=?, ProCode=?, SupID=? WHERE PCoID=?";
    private final String SQL_FOR_INSERT_Products = "INSERT INTO Products (ProID, ProCode, BarCode, ProDesc, ProUnitS, ProUnitL, PackageQ, SupCode) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private final String SQL_FOR_UPDATE_Products= "UPDATE Products SET ProCode=?, BarCode=?, ProDesc=?, ProUnitS=?, ProUnitL=?, PackageQ=?, SupCode=? WHERE ProID=?";
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
        httpURLConnection = getURLConnectInstance(mDownloadProductCode);
        httpURLConnection.connect();
        if (HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()) {
          inputStream = httpURLConnection.getInputStream();
          bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
          bufferedReader.readLine();
          while ((line = bufferedReader.readLine()) != null) { mArrayListProductCode.add(line); }
        }

        //Download Products
        httpURLConnection = getURLConnectInstance(mDownloadProducts);
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
        mInsertStatement = mSQLiteDatabaseWrite.compileStatement(SQL_FOR_INSERT_ProductCode);
        mUpdateStatement = mSQLiteDatabaseWrite.compileStatement(SQL_FOR_UPDATE_ProductCode);
        for (int i=0; i<mArrayListProductCode.size(); i++) {
          String srr[] = mArrayListProductCode.get(i).split(",");
          Cursor cursor = mSQLiteDatabaseWrite.rawQuery("SELECT PCoID FROM ProductCode WHERE PCoID=?", new String[]{srr[0]});
          count = handleDownloadDataToDB(srr, cursor, mInsertStatement, mUpdateStatement, count);
          cursor.close();
          publishProgress(Integer.valueOf((int) (count++ * rate)));
        }

        mInsertStatement = mSQLiteDatabaseWrite.compileStatement(SQL_FOR_INSERT_Products);
        mUpdateStatement = mSQLiteDatabaseWrite.compileStatement(SQL_FOR_UPDATE_Products);
        for (int i=0; i<mArrayListProducts.size(); i++) {
          String srr[] = mArrayListProducts.get(i).split(",");
          Cursor cursor = mSQLiteDatabaseWrite.rawQuery("SELECT ProID FROM Products WHERE ProID=?", new String[]{srr[0]});
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
}
