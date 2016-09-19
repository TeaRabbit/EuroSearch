package test.edhsiao.com.autocomplete.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import test.edhsiao.com.autocomplete.QueryManager;

/**
 * Created by edhsiao on 19/09/2016.
 */
public class QueryTextWatcher implements TextWatcher {
    private WeakReference<AutoCompleteTextView> mView;
    private WeakReference<Context> mContext;
    private WeakReference<SearchCompletedListener> mListener;

    public interface SearchCompletedListener {
        void onSearchDone();
    }

    public QueryTextWatcher(Context context, AutoCompleteTextView view, SearchCompletedListener listener) {
        mView = new WeakReference<>( view );
        mContext = new WeakReference<>( context);
        mListener = new WeakReference<>(listener);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // not used
    }

    @Override
    public synchronized void onTextChanged(final CharSequence s, int start, int before, int count) {
        // Kick off query
        if (mContext.get() != null && mView.get() != null && mListener.get() != null) {
            ArrayList<String> result = QueryManager.getInstance().searchLocation(s.toString());
            mView.get().setAdapter(new ArrayAdapter<>(mContext.get(),
                    android.R.layout.simple_dropdown_item_1line,
                    result.toArray(new String[result.size()])));
            mListener.get().onSearchDone();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        // not used
    }

}
