package tw.com.mobilogics.EZRestock;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class MainActivity extends ActionBarActivity {
    private SharedPreferences mSharedPreferences = null;
    private EditText mEditTextQuantity = null;
    private EditText mEditTextInventory = null;
    private EditText mEditTextScanNumber = null;
    private Button mButtonScan = null;
    private ListView mListView = null;
    private FocusChange focusChange = new FocusChange(); // will edit
    private tw.com.mobilogics.EZRestock.dbHelper dbHelper = null;

    private LayoutInflater mInflater;
    private ListAdapter listAdapter = new ListAdapter();
    private static LinkedList mLinkedList = new LinkedList();

    private long mId = -1;
    private final String mTableName = "Management";

    private SQLiteDatabase mSQLiteDatabaseWrite = null;
    private SQLiteDatabase mSQLiteDatabaseRead = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initial();
        openDB(getCompanyName());
        mSQLiteDatabaseWrite = dbHelper.getWritableDatabase();
        mSQLiteDatabaseRead = dbHelper.getReadableDatabase();
        mEditTextScanNumber.requestFocus(); // Default First Focus
        loadActivityTitle();

        mButtonScan.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mButtonScan.getWindowToken(), 0);
                if (hasFocus) {
                    mButtonScan.performClick();
                }
            }
        });
        mButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( ! getScanNumber().equals("") && ! getQuantity().equals("") && !getInventory().equals("")) {
                    Double mQuantity = Double.parseDouble(
                            mEditTextQuantity.getText().toString().trim());
                    Double mInventory = Double.parseDouble(
                            mEditTextInventory.getText().toString().trim());
                    String mSelect = dbSelect(getScanNumber());

                    if (0 == mQuantity && 0 == mInventory) {// According to etScanNumber value , Select etQuantity && etInventory
                        if (mSelect.equals("")) {
                            // Cancel Action
                        } else {// According to etQuantity && etInventory , show values of Existing database
                            String [] mResult = mSelect.split("_");
                            mEditTextQuantity.setText(mResult[1]);
                            mEditTextInventory.setText(mResult[2]);
                            mEditTextQuantity.requestFocus();
                            refreshData();
                            showListOrderBySelect(getScanNumber());
                            mListView.setAdapter(listAdapter);
                        }
                    }else {// Maybe Action Insert OR Update
                        if (mSelect.equals("")) {//Execution insert
                            dbInsert(getScanNumber(),mQuantity, mInventory);
                            refreshData();
                            mListView.setAdapter(listAdapter);
                        }else {//Execution update
                            dbUpdate(getSelectId());
                            refreshData();
                            mListView.setAdapter(listAdapter);
                        }
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Scan Number is Null", Toast.LENGTH_LONG).show();
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
        mEditTextQuantity.setOnFocusChangeListener(focusChange);
        mEditTextInventory.setOnFocusChangeListener(focusChange);
    }
    private void loadActivityTitle() {
        setTitle(getCompanyName() + " - " + getBranchNumber().trim());
    }
    private String getBranchNumber() {
        if (mSharedPreferences.contains("BRANCHNUMBER")) {
            return mSharedPreferences.getString("BRANCHNUMBER", "");
        }
        return "";
    }
    private String getCompanyName() {
        if (mSharedPreferences.contains("COMPANYNAME")) {
            return mSharedPreferences.getString("COMPANYNAME","");
        }
        return "";
    }
    private String getScanNumber() {
        return "" + mEditTextScanNumber.getText().toString().trim();
    }
    private void openDB(String CompanyName) {
        dbHelper = new dbHelper(MainActivity.this, CompanyName + ".db", 1);
    }
    private void closeDB(){}
    private String getQuantity() {
        return "" + mEditTextQuantity.getText().toString().trim();
    }
    private String getInventory() {
        return  "" + mEditTextInventory.getText().toString().trim();
    }
    private void setSelectId(long Id) {
        mId = Id;
    }
    private long getSelectId() {
        return mId;
    }
    private String dbSelect(String ScanNumber) {
        String mQuantity = "" , mInventory = "";
        Cursor cursor = mSQLiteDatabaseRead.rawQuery(
            "select Id, ScanNumber, Quantity, Inventory from Management Where ScanNumber=?",
            new String[]{ScanNumber}
        );
        while (cursor.moveToNext()) {
            setSelectId(Integer.parseInt(cursor.getString(0)));
            mQuantity = cursor.getString(2);
            mInventory = cursor.getString(3);
        }
        cursor.close();
        mSQLiteDatabaseRead.close();
        if (mQuantity.equals("") && mInventory.equals("")) {
            return  "";
        }else {
            return ScanNumber + "_" + mQuantity + "_" + mInventory;
        }
    }
    private String getDateTime() {
        Date date = new Date();
        return dateFormat.format(date);
    }
    private void dbInsert(String mScanNumber, Double mQuantity, Double mInventory) {
        ContentValues values = new ContentValues();
        values.put("ScanNumber", mScanNumber);
        values.put("Quantity"  , mQuantity);
        values.put("Inventory" , mInventory);
        values.put("createTime" , getDateTime());
        long index = mSQLiteDatabaseWrite.insert(mTableName, null, values);
        if (index <= -1) {
            Toast.makeText(MainActivity.this, "Insert Into Fail", Toast.LENGTH_LONG).show();
        }
        mSQLiteDatabaseWrite.close();
    }
    private void dbUpdate(long Id) {
        ContentValues values = new ContentValues();
        values.put("Quantity"  , getQuantity());
        values.put("Inventory" , getInventory());
        values.put("createTime" , getDateTime());
        int updateCode = mSQLiteDatabaseWrite.update(mTableName, values, "Id=" + Id, null);
        setSelectId(-1);
        if (updateCode <= -1) {
            Toast.makeText(MainActivity.this, "Update Fail", Toast.LENGTH_LONG).show();
        }
        mSQLiteDatabaseWrite.close();
    }
    private void showListOrderBySelect(String ScanNumber) {
        String tmpContent = "";
        for (int i=0; i<mLinkedList.size(); i++) {
            String compare = mLinkedList.get(i).toString().split("_")[0];
            if (compare.equals(ScanNumber)) {
                tmpContent = mLinkedList.get(i).toString();
                mLinkedList.remove(i);
            }
        }
        mLinkedList.addFirst(tmpContent);
    }

    private void refreshData() {
        mLinkedList.clear();
        Cursor cursor = mSQLiteDatabaseRead.rawQuery("SELECT ScanNumber, Quantity, Inventory, createTime" +
            " FROM " + mTableName + " ORDER BY datetime(createTime) DESC", null);
        int count = 0;
        while (cursor.moveToNext()) {
            String mQuery = cursor.getString(0) + "_" + cursor.getString(1) + "_" + cursor.getString(2) + "_" +
                    cursor.getString(3);
            mLinkedList.add(count++, mQuery);
        }
        mSQLiteDatabaseRead.close();
    }
    private class FocusChange implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (R.id.mEditTextQuantity == v.getId()) {
                if (!hasFocus) {
                    if (mEditTextQuantity.getText().toString().trim().equals("")) {
                        mEditTextQuantity.setText("0.0");
                    }
                }
            } else if (R.id.mEditTextInventory == v.getId()) {
                if (!hasFocus) {
                    if (mEditTextInventory.getText().toString().trim().equals("")) {
                        mEditTextInventory.setText("0.0");
                        //btn request
                    }
                }
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
            TextView tvScanNumber = (TextView) convertView.findViewById(R.id.row_scan_number);
            TextView tvQuantity = (TextView) convertView.findViewById(R.id.row_quantity);
            TextView tvInventory = (TextView) convertView.findViewById(R.id.row_inventory);
            String []mQuery = mLinkedList.get(position).toString().trim().split("_");
            tvScanNumber.setText(mQuery[0]);
            tvQuantity.setText(mQuery[1]);
            tvInventory.setText(mQuery[2]);
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
                refreshData();
                Intent intent = new Intent(MainActivity.this, MailActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static LinkedList getSelectAllData() {
        return mLinkedList;
    }
}
