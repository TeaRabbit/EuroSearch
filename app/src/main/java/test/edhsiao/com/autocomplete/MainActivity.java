package test.edhsiao.com.autocomplete;

import android.Manifest;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import test.edhsiao.com.autocomplete.widget.DatePickerFragment;
import test.edhsiao.com.autocomplete.widget.QueryTextWatcher;
import test.edhsiao.com.autocomplete.widget.QueryTextWatcher.SearchCompletedListener;

public class MainActivity extends AppCompatActivity implements DatePickerFragment.DateSelectedListener, SearchCompletedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PICKER_FRAGMENT = "datePicker";
    private static final int PERMISSION_LOCATION = 1;
    private  String[] mPermission = { Manifest.permission.ACCESS_COARSE_LOCATION };

    @BindView(R.id.origin) AutoCompleteTextView mOrigin;
    @BindView(R.id.destination) AutoCompleteTextView mDestination;
    @BindView(R.id.picker) TextView mPicker;
    @BindView(R.id.search) Button mSearch;
    private DatePickerFragment mDateFragment;
    private LocationManager mLocationManager;
    private Location mLocation;

    @OnClick(R.id.search)
    public void onSearchClick() {
        Log.v(TAG, "on search clicked");
        Toast.makeText(this, "Search is not yet implemented", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.picker)
    public void onPickerClicked() {
        Log.v(TAG, "onPickerClicked");

        FragmentManager manager = getSupportFragmentManager();

        Fragment prev = manager.findFragmentByTag(PICKER_FRAGMENT);
        if (prev != null) {
            manager.beginTransaction().remove(prev).commit();
        }
        mDateFragment.show(manager, PICKER_FRAGMENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initialize();
    }

    private void initialize() {
        mDateFragment = new DatePickerFragment();
        mDateFragment.setListener(this);
        mOrigin.addTextChangedListener(new QueryTextWatcher( this, mOrigin, this));
        mDestination.addTextChangedListener(new QueryTextWatcher( this, mDestination, this));

        mLocationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        if( Build.VERSION.SDK_INT < Build.VERSION_CODES.M || EasyPermissions.hasPermissions( this, mPermission) ) {
            mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            QueryManager.getInstance().setLocation(mLocation);
        } else {
            EasyPermissions.requestPermissions(this, "Need to have access to Location permission", PERMISSION_LOCATION, mPermission);
        }

    }

    @AfterPermissionGranted(PERMISSION_LOCATION)
    private void onLocationPermission() {
        mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        QueryManager.getInstance().setLocation(mLocation);
    }

    @Override
    public void onDateSelected(int year, int month, int day) {
        Log.v(TAG, "on date selected");
        // Year parameter in date begins with 1900:
        Date date = new Date(year-1900, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        mPicker.setText( sdf.format(date).toString());
        mDateFragment.dismiss();
        checkAllField();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void checkAllField() {
        mSearch.setEnabled( !TextUtils.isEmpty(mOrigin.getText()) &&
                !TextUtils.isEmpty(mDestination.getText()) &&
                !TextUtils.isEmpty(mPicker.getText()));
    }

    @Override
    public void onSearchDone() {
        checkAllField();
    }
}
