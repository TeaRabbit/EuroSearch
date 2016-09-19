package test.edhsiao.com.autocomplete;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by edhsiao on 19/09/2016.
 */
public class QueryManager {

    private static final String TAG = QueryManager.class.getSimpleName();
    private static final String REQUEST_URL = "http://api.goeuro.com/api/v2/position/suggest/%s/%s";

    private static final String JSON_NAME_FIELD = "name";
    private static final String JSON_GEO_POSITION_FIELD = "geo_position";
    private static final String JSON_GEO_LATITUDE_FIELD = "latitude";
    private static final String JSON_GEO_LONGITUDE_FIELD = "longitude";

    private static QueryManager sIntance;

    private OkHttpClient mOkHttpClient;
    private HttpAsyncTask mQueryTask;
    private Location mLocation;

    private class HttpAsyncTask extends AsyncTask<String, Void, Response> {
        @Override
        protected Response doInBackground(String... params) {
            String keyword = params[0];
            String request = String.format(REQUEST_URL, Locale.getDefault().getLanguage(), keyword);
            Request.Builder requestBuild = new Request.Builder().url(request);
            requestBuild.method("GET", null);
            Call call = mOkHttpClient.newCall(requestBuild.build());
            Response response = null;
            try {
                response = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    private class SortByLocation implements Comparator<JSONObject> {
        @Override
        public int compare(JSONObject lhs, JSONObject rhs) {
            try {
                JSONObject lgeo = lhs.getJSONObject(JSON_GEO_POSITION_FIELD);
                double leftLat = lgeo.getDouble(JSON_GEO_LATITUDE_FIELD);
                double leftLon = lgeo.getDouble(JSON_GEO_LONGITUDE_FIELD);

                JSONObject rgeo = rhs.getJSONObject(JSON_GEO_POSITION_FIELD);
                double rightLat = rgeo.getDouble(JSON_GEO_LATITUDE_FIELD);
                double rightLon = rgeo.getDouble(JSON_GEO_LONGITUDE_FIELD);
                LatLng currentLatLon = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

                double leftDist = SphericalUtil.computeDistanceBetween(currentLatLon, new LatLng(leftLat, leftLon));
                double rightDist = SphericalUtil.computeDistanceBetween(currentLatLon, new LatLng(rightLat, rightLon));

                return leftDist > rightDist ? 1 : leftDist < rightDist ? -1 : 0;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    private QueryManager() {
        mOkHttpClient = new OkHttpClient();
    }

    public static QueryManager getInstance() {
        if (sIntance == null) {
            sIntance = new QueryManager();
        }
        return sIntance;
    }
    public void setLocation( Location location ) {
        mLocation = location;
    }

    public synchronized ArrayList<String> searchLocation(String keyword) {
        try {
            if( mQueryTask != null && mQueryTask.getStatus() != AsyncTask.Status.FINISHED) {
                mQueryTask.cancel(true);
                mQueryTask = null;
            }
            mQueryTask = new HttpAsyncTask();
            Response response = mQueryTask.execute(keyword).get();
            if (response != null) {
                return convertToResult(response);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


    private ArrayList<String> convertToResult( Response response) {
        ArrayList<String> output = new ArrayList<>();
        JSONArray results = null;
        try {
            results = new JSONArray(response.body().string());

            List<JSONObject> sortedResults = new ArrayList<JSONObject>();
            for (int i = 0; i < results.length(); i++) {
                sortedResults.add(results.getJSONObject(i));
            }

            Collections.sort(sortedResults, new SortByLocation());

            for ( int i = 0; i < sortedResults.size();i++ ) {
                JSONObject result = sortedResults.get(i);
                String name = result.getString(JSON_NAME_FIELD);
                // debug
                //JSONObject geo = result.getJSONObject(JSON_GEO_POSITION_FIELD);
                //double lon = geo.getDouble(JSON_GEO_LONGITUDE_FIELD);
                //double lat = geo.getDouble(JSON_GEO_LATITUDE_FIELD);
                //LatLng currentLatLon = new LatLng(sLocation.getLatitude(), sLocation.getLongitude());
                //double distance = SphericalUtil.computeDistanceBetween(currentLatLon, new LatLng(lat, lon));
                //Log.v(TAG, "result: " + name + ", and location to current is : " + distance);
                output.add(name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "output size: " + output.size());
        return output;
    }
}
