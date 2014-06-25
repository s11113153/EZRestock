package tw.com.mobilogics.EZRestock;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import static tw.com.mobilogics.EZRestock.Utils.getSQLiteDatabaseInsrance;
import static tw.com.mobilogics.EZRestock.Utils.searchOneOfProductData;
import static tw.com.mobilogics.EZRestock.Utils.searchProID;

public class SlidingMainFragment extends android.support.v4.app.Fragment {

  private SlidingDrawer mSlidingDrawer;

  private ImageView mImageViewSlidingUp;

  private ListView mListView;

  private LinkedList<String> mLinkedList;

  private JSONObject mJSONObject;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_sliding_main, container, false);
    mListView = (ListView) v.findViewById(R.id.mListView);
    mSlidingDrawer = (SlidingDrawer) v.findViewById(R.id.mSlidingDrawer);
    mImageViewSlidingUp = (ImageView)v.findViewById(R.id.mImageViewSlidingUp);
    return v;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mLinkedList = new LinkedList<String>((List) getArguments().getSerializable("ListData"));

    mListView.setAdapter(new BaseAdapter() {
      @Override
      public int getCount() {
        return mLinkedList.size();
      }

      @Override
      public Object getItem(int position) {
        return null;
      }

      @Override
      public long getItemId(int position) {
        return 0;
      }

      @Override
      public View getView(final int position, View convertView, ViewGroup parent) {
        convertView =getActivity().getLayoutInflater().inflate(R.layout.row, null);
        TextView rowScanNumber = (TextView) convertView.findViewById(R.id.rowScanNumber);
        TextView rowQuantity = (TextView) convertView.findViewById(R.id.rowQuantity);
        TextView rowInventory = (TextView) convertView.findViewById(R.id.rowInventory);
        final String row[] = mLinkedList.get(position).trim().split("_");
        rowScanNumber.setText(row[0]);
        rowQuantity.setText(row[1]);
        rowInventory.setText(row[2]);

        convertView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            int selectIndex = searchProID(row[0], "ProductCode", getSQLiteDatabaseInsrance().getReadableDatabase());
            if ( -1 != selectIndex) {
              mJSONObject = searchOneOfProductData(selectIndex, "Products", getSQLiteDatabaseInsrance().getReadableDatabase());
              Intent intent = new Intent(getActivity(), EditProductActivity.class);
              intent.putExtra("EDIT_PRODUCT_JSON_DATA", mJSONObject.toString());
              startActivity(intent);
            }else {
              Toast.makeText(getActivity(), "Not Found!!", Toast.LENGTH_LONG).show();
            }
          }
        });

        if (position %2 == 1) {
          convertView.setBackgroundColor(Color.parseColor("#e6e6e6"));
        }

        return convertView;
      }
    });

    mSlidingDrawer.post(new Runnable() {
      @Override
      public void run() {
        mSlidingDrawer.animateOpen();
      }
    });

    mSlidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
      @Override
      public void onDrawerOpened() {
        mImageViewSlidingUp.setImageResource(android.R.drawable.arrow_down_float);
      }
    });

    mSlidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
      @Override
      public void onDrawerClosed() {
        getActivity().onBackPressed();
      }
    });
  }
}