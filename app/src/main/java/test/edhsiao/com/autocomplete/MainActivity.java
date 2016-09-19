package test.edhsiao.com.autocomplete;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import test.edhsiao.com.autocomplete.widget.DatePickerFragment;

public class MainActivity extends AppCompatActivity implements DatePickerFragment.DateSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PICKER_FRAGMENT = "datePicker";

    @BindView(R.id.origin) AutoCompleteTextView mOrigin;
    @BindView(R.id.destination) AutoCompleteTextView mDestination;
    @BindView(R.id.picker) TextView mPicker;
    @BindView(R.id.search) Button mSearch;
    private DatePickerFragment mDateFragment;

    @OnClick(R.id.search)
    public void onSearchClick() {
        Log.v(TAG, "on search clicked");
        Toast.makeText(this, "Search function has not been implemented", Toast.LENGTH_SHORT).show();
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

    private TextWatcher mWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public synchronized void onTextChanged(CharSequence s, int start, int before, int count) {
            // Kick off query
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

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
    }

    @Override
    public void onDateSelected(int year, int month, int day) {
        Log.v(TAG, "on date selected");
        // Year parameter in date begins with 1900:
        Date date = new Date(year-1900, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        mPicker.setText( sdf.format(date).toString());
        mDateFragment.dismiss();
    }
}
