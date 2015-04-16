package com.eugene.googlemaps;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.eugene.googlemaps.Mapping.Globals;
import com.eugene.googlemaps.Mapping.HistoryProvider;

import java.text.DecimalFormat;


public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    String totalList;
    public int mActivityIndex;
    private static final int LOADER_ID = 0;
    public ActivityEntriesCursorAdapter mAdapter;
    public Cursor mActivityEntryCursor;
    public int mDistanceIndex;
    public int mInputTypeIndex;
    private ListView listView;
    public static final String DISTANCE_FORMAT = "#.##";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewsById();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                Bundle extras = new Bundle();
                mActivityEntryCursor = mAdapter.getCursor();
                int to = mAdapter.getCount();
                totalList = Integer.toString(to);
                mActivityEntryCursor.moveToPosition(position);
                extras.putInt(Globals.KEY_TASK_TYPE, Globals.TASK_TYPE_HISTORY);
                extras.putLong(Globals.KEY_ROWID, id);
                mActivityIndex = mActivityEntryCursor.getColumnIndex(Globals.KEY_ACTIVITY_TYPE);
                extras.putInt(Globals.KEY_ACTIVITY_TYPE, mActivityEntryCursor.getInt(mActivityIndex));
                intent.setClass(MainActivity.this, MapsActivity.class);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }

    Toolbar toolbar_main;

    private void findViewsById() {
        mActivityEntryCursor = getContentResolver().query(HistoryProvider.CONTENT_URI, null, null, null, null);
        mDistanceIndex = mActivityEntryCursor.getColumnIndex(Globals.KEY_DISTANCE);
        mInputTypeIndex = mActivityEntryCursor.getColumnIndex(Globals.KEY_ROWID);
        android.app.LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_ID, null, this);
        mAdapter = new ActivityEntriesCursorAdapter(this, mActivityEntryCursor);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mAdapter);
        toolbar_main = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar_main.inflateMenu(R.menu.menu_main);
        toolbar_main.setTitle(R.string.title_activity_maps);
        toolbar_main.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int menuId = menuItem.getItemId();
                if (menuId == R.id.action_new) {
                    Intent i = new Intent(MainActivity.this, MapsActivity.class);
                    Bundle extras = new Bundle();
                    extras.putInt(Globals.KEY_TASK_TYPE, Globals.TASK_TYPE_NEW);
                    i.putExtras(extras);
                    startActivity(i);
                }
                return false;
            }
        });
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, HistoryProvider.CONTENT_URI,
            null, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private class ActivityEntriesCursorAdapter extends CursorAdapter {
        private LayoutInflater mInflater;

        public ActivityEntriesCursorAdapter(Context context, Cursor c) {
            super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView titleView = (TextView) view.findViewById(android.R.id.text1);
            String distanceString = parseDistance(cursor.getDouble(mDistanceIndex));
            titleView.setText(distanceString);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(android.R.layout.simple_list_item_1, null);
        }
    }

    private String parseDistance(double distInMeters) {
        double distInMiles = distInMeters / 1000.0 / Globals.KM2MILE_RATIO;
        String unit = getString(R.string.miles);
        DecimalFormat decimalFormat = new DecimalFormat(DISTANCE_FORMAT);
        return decimalFormat.format(distInMiles) + " " + unit;
    }
}
