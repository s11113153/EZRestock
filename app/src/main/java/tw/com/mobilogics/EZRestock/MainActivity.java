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

import java.util.LinkedList;

import static tw.com.mobilogics.EZRestock.Utils.getDateTime;
import static tw.com.mobilogics.EZRestock.Utils.promptMessage;;

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
    private final String mTableName = "Management";

    private LinkedList<String> mLinkedList = new LinkedList<String>();

    private SQLiteDatabase mSQLiteDatabaseWrite = null;
    private SQLiteDatabase mSQLiteDatabaseRead = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initial();
        mEditTextScanNumber.requestFocus(); // Default First Focus
        loadActivityTitle();

        mButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( ! getScanNumber().equals("") && ! getQuantity().equals("") && !getInventory().equals("")) {
                    String select = dbSelect(getScanNumber()); // if return "" , no Data exist
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
                            refreshListData();
                            setListDataOrderBySelectId(getScanNumber());
                            mListView.setAdapter(mListAdapter);
                        }
                    }else {// Action for Insert OR Update
                        if (select.equals("")) {// Execution insert
                            dbInsert(getScanNumber(), getQuantity(), getInventory());
                            refreshListData();
                            mListView.setAdapter(mListAdapter);
                        }else {// Execution update
                            dbUpdate(getSelectId());
                            refreshListData();
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
        setTitle(mSharedPreferences.getString("COMPANYNAME", "")
                + " - " + mSharedPreferences.getString("BRANCHNUMBER","").trim());
    }

    private String getScanNumber() {
        return "" + mEditTextScanNumber.getText().toString().trim();
    }

    private void openDB(String CompanyName) {
        mDBHelper = new DBHelper(MainActivity.this, CompanyName + ".db", 1);
    }

    private void closeDB(){
        mDBHelper.close();
        mSQLiteDatabaseRead.close();
        mSQLiteDatabaseWrite.close();
    }

    private String getQuantity() {
        return "" + mEditTextQuantity.getText().toString().trim();
    }

    private String getInventory() {
        return "" + mEditTextInventory.getText().toString().trim();
    }

    /** According query result, store select Id */
    private void setSelectId(long Id) {
        mId = Id;
    }

    /** return Id  */
    private long getSelectId() {
        return mId;
    }

    private String dbSelect(String scanNumber) {
        String quantity = "" , inventory = "";
        Cursor cursor = mSQLiteDatabaseRead.rawQuery(
            "select Id, ScanNumber, Quantity, Inventory from Management Where ScanNumber=?",
            new String[]{scanNumber}
        );
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

    private void dbInsert(String scanNumber, String quantity, String inventory) {
        ContentValues values = new ContentValues();
        values.put("ScanNumber", scanNumber);
        values.put("Quantity"  , quantity);
        values.put("Inventory" , inventory);
        values.put("createTime" , getDateTime());
        if (-1 == mSQLiteDatabaseWrite.insert(mTableName, null, values)) {
            promptMessage("Notice", "Insert Into Fail", MainActivity.this);
        }
    }

    /** if Update return -1 , then show Message */
    private void dbUpdate(long id) {
        ContentValues values = new ContentValues();
        values.put("Quantity"  , getQuantity());
        values.put("Inventory" , getInventory());
        values.put("createTime" , getDateTime());
        if (-1 == mSQLiteDatabaseWrite.update(mTableName, values, "Id=" + id, null)) {
            promptMessage("Notice", "Update Fail", MainActivity.this);
        }
        setSelectId(-1);
    }

    /**
     *  According select id, display data on the first line ,
     *  Other datas are order by DateTime
     */
    private void setListDataOrderBySelectId(String ScanNumber) {
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

    private void refreshListData() {
        mLinkedList.clear();

        Cursor cursor = mSQLiteDatabaseRead.rawQuery("SELECT ScanNumber, Quantity, Inventory, createTime" +
                " FROM " + mTableName + " ORDER BY datetime(createTime) DESC", null);
        int count = 0;
        while (cursor.moveToNext()) {
            String rowQuery = cursor.getString(0) + "_" + cursor.getString(1) + "_" + cursor.getString(2) + "_" +cursor.getString(3);
            mLinkedList.add(count++, rowQuery);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (! hasFocus) {
            switch (v.getId()) {
                case R.id.mEditTextQuantity :
                    if (getQuantity().equals(""))  { mEditTextQuantity.setText("0"); }
                    break;
                case R.id.mEditTextInventory :
                    if (getInventory().equals("")) { mEditTextInventory.setText("0"); }
                    break;
            }
        }else {
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
                refreshListData();
                Intent intent = new Intent(MainActivity.this, MailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("ListData", mLinkedList);
                intent.putExtras(bundle);
                startActivity(intent);
                break;

            default:
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
}
