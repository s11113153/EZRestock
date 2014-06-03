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
    private SharedPreferences sharedpreferences = null;
    private TextView tvTitle = null;
    private EditText etQuantity = null;
    private EditText etInventory = null;
    private EditText etScanNumber = null;
    private Button btnScan = null;
    private ListView listView = null;
    private FocusChange focusChange = new FocusChange();
    private tw.com.mobilogics.EZRestock.dbHelper dbHelper = null;

    private LayoutInflater mInflater;
    private ListAdapter listAdapter = new ListAdapter();
    private LinkedList linkedList = new LinkedList();

    private long mId = -1;
    private final String TableName = "Management";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initial();
        loadTitle();

        btnScan.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(btnScan.getWindowToken(), 0);
                if (hasFocus) {
                    btnScan.performClick();
                }
            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( ! getScanNumber().equals("") && ! getQuantity().equals("") && !getInventory().equals("")) {
                    Double mQuantity = Double.parseDouble(etQuantity.getText().toString().trim());
                    Double mInventory = Double.parseDouble(etInventory.getText().toString().trim());
                    String mSelect = dbSelect(getScanNumber());

                    if (0 == mQuantity && 0 == mInventory) {// According to etScanNumber value , Select etQuantity && etInventory
                        if (mSelect.equals("")) { // Cancel Action
                            return;
                        } else {// According to etQuantity && etInventory , show values of Existing database
                            String [] mResult = mSelect.split("_");
                            etQuantity.setText(mResult[1]);
                            etInventory.setText(mResult[2]);
                            etQuantity.requestFocus();
                            setListData();
                            showListOrderBySelect(getScanNumber());
                            listView.setAdapter(listAdapter);
                        }
                    }else {// Maybe Action Insert OR Update
                        if (mSelect.equals("")) {//Execution insert
                            dbInsert(getScanNumber(),mQuantity, mInventory);
                            setListData();
                            listView.setAdapter(listAdapter);
                        }else {//Execution update
                            dbUpdate(getSelectId());
                            setListData();
                            listView.setAdapter(listAdapter);
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
        sharedpreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        openDB(getCompanyName());

        tvTitle = (TextView)findViewById(R.id.tv_title);
        etQuantity = (EditText) findViewById(R.id.et_quantity);
        etInventory = (EditText) findViewById(R.id.et_inventory);
        etScanNumber = (EditText) findViewById(R.id.et_scan_number);
        btnScan = (Button) findViewById(R.id.btn_scan);
        btnScan.setFocusableInTouchMode(true);
        btnScan.setInputType(InputType.TYPE_NULL);
        listView = (ListView) findViewById(R.id.listview);
        etScanNumber.requestFocus();
        etQuantity.setOnFocusChangeListener(focusChange);
        etInventory.setOnFocusChangeListener(focusChange);
    }
    private void loadTitle() {
            String strTitle = getCompanyName() + "-" + getBranchNumber().trim();
            tvTitle.setText(strTitle);
    }
    private String getBranchNumber() {
        if (sharedpreferences.contains("BRANCHNUMBER")) {
            return sharedpreferences.getString("BRANCHNUMBER", "");
        }
        return "";
    }
    private String getCompanyName() {
        if (sharedpreferences.contains("COMPANYNAME")) {
            return sharedpreferences.getString("COMPANYNAME","");
        }
        return "";
    }
    private String getScanNumber() {
        return "" + etScanNumber.getText().toString().trim();
    }
    private void openDB(String CompanyName) {
        dbHelper = new dbHelper(MainActivity.this, CompanyName + ".db", 1);
    }
    private String getQuantity() {
        return "" + etQuantity.getText().toString().trim();
    }
    private String getInventory() {
        return  "" + etInventory.getText().toString().trim();
    }
    private void setSelectId(long Id) {
        mId = Id;
    }
    private long getSelectId() {
        return mId;
    }
    private String dbSelect(String ScanNumber) {
        String mQuantity = "" , mInventory = "";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "select Id, ScanNumber, Quantity, Inventory from Management Where ScanNumber=?",
            new String[]{ScanNumber}
        );
        while (cursor.moveToNext()) {
            setSelectId(Integer.parseInt(cursor.getString(0)));
            mQuantity = cursor.getString(2);
            mInventory = cursor.getString(3);
        }
        cursor.close();
        db.close();
        if (mQuantity.equals("") && mInventory.equals("")) {
            return  "";
        }else {
            return ScanNumber + "_" + mQuantity + "_" + mInventory;
        }
    }
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
        Date date = new Date();
        return dateFormat.format(date);
    }
    private void dbInsert(String mScanNumber, Double mQuantity, Double mInventory) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ScanNumber", mScanNumber);
        values.put("Quantity"  , mQuantity);
        values.put("Inventory" , mInventory);
        values.put("createTime" , getDateTime());
        long index = db.insert(TableName, null, values);
        if (index <= -1) {
            Toast.makeText(MainActivity.this, "Insert Into Fail", Toast.LENGTH_LONG).show();
        }
        db.close();
    }
    private void dbUpdate(long Id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Quantity"  , getQuantity());
        values.put("Inventory" , getInventory());
        values.put("createTime" , getDateTime());
        int updateCode = db.update(TableName, values, "Id=" + Id, null);
        setSelectId(-1);
        if (updateCode <= -1) {
            Toast.makeText(MainActivity.this, "Update Fail", Toast.LENGTH_LONG).show();
        }
    }
    private void showListOrderBySelect(String ScanNumber) {
        String tmpContent = "";
        for (int i=0; i<linkedList.size(); i++) {
            String compare = linkedList.get(i).toString().split("_")[0];
            if (compare.equals(ScanNumber)) {
                tmpContent = linkedList.get(i).toString();
                linkedList.remove(i);
            }
        }
        linkedList.addFirst(tmpContent);
    }
    private void setListData() {
        linkedList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ScanNumber, Quantity, Inventory, createTime" +
            " FROM " + TableName + " ORDER BY datetime(createTime) DESC", null);
        int count = 0;
        while (cursor.moveToNext()) {
            String mQuery = cursor.getString(0) + "_" + cursor.getString(1) + "_" + cursor.getString(2) + "_" +
                    cursor.getString(3);
            linkedList.add(count++, mQuery);
        }
    }
    private class FocusChange implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (R.id.et_quantity == v.getId()) {
                if (!hasFocus) {
                    if (etQuantity.getText().toString().trim().equals("")) {
                        etQuantity.setText("0.0");
                    }
                }
            } else if (R.id.et_inventory == v.getId()) {
                if (!hasFocus) {
                    if (etInventory.getText().toString().trim().equals("")) {
                        etInventory.setText("0.0");
                        //btn request
                    }
                }
            }
        }
    }
    class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return linkedList.size();
        }

        @Override
        public Object getItem(int position) {
            return linkedList.get(position);
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
            String []mQuery = linkedList.get(position).toString().trim().split("_");
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
        if (R.id.action_email == item.getItemId()) {
            Intent intent = new Intent(MainActivity.this, MailActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
