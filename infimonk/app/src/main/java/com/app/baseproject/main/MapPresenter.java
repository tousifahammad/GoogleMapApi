package com.app.baseproject.main;

import android.util.Log;

import com.app.baseproject.R;
import com.app.baseproject.baseclasses.BasePresenter;
import com.app.baseproject.baseclasses.SharedMethods;
import com.app.baseproject.baseclasses.WebServices;
import com.app.baseproject.loaders.JSONFunctions;
import com.app.baseproject.utils.Alert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

public class MapPresenter extends BasePresenter {
    private MapsActivity activity;

    MapPresenter(MapsActivity activity) {
        super(activity);
        this.activity = activity;
    }


    @Override
    public void getJSONResponseResult(String result, int url_no) {
        if (getpDialog().isShowing()) {
            getpDialog().dismiss();
        }

        switch (url_no) {
            case WebServices.request_url_no_1:
                if (SharedMethods.isSuccess(result, activity)) {
                    responseGetGeofence(result);
                }
                break;
            case WebServices.request_url_no_2:
                if (SharedMethods.isSuccess(result, activity)) {
                    responsePostGeofence(result);
                }
                break;
        }
    }

    void requestGetGeofence() {
        if (JSONFunctions.isInternetOn(activity)) {
            getpDialog().setMessage("Getting Geofence. Please wait...");
            getpDialog().show();

            getJfns().makeHttpRequest(WebServices.geofence, "GET", WebServices.request_url_no_1);
        } else {
            Alert.showError(activity, activity.getString(R.string.no_internet));
        }
    }

    private void responseGetGeofence(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray data_ja = jsonObject.getJSONArray(WebServices.data);

            if (data_ja.length() == 0) return;

            activity.geofence_list.clear();
            for (int i = 0; i < data_ja.length(); i++) {
                JSONObject location_jo = data_ja.getJSONObject(i);
                Geofence geofence = new Geofence(location_jo.getString("geofenceId"),
                        location_jo.getString("latitude"),
                        location_jo.getString("longitude"));
                activity.geofence_list.add(geofence);
            }

            Log.d("1111", "geofence_list: " + activity.geofence_list);
            activity.setMarker();

        } catch (JSONException ex) {
            Alert.showError(activity, ex.getMessage());
        }
    }


    void requestPostGeofence(double distance) {
        if (JSONFunctions.isInternetOn(activity)) {
            getpDialog().setMessage("Submitting result. Please wait...");
            getpDialog().show();

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("userId", String.valueOf(new Random().nextInt(90)));
            hashMap.put("distance", String.valueOf(distance));

            getJfns().makeHttpRequest(WebServices.geofence, "POST", hashMap, WebServices.request_url_no_2);

        } else {
            Alert.showError(activity, activity.getString(R.string.no_internet));
        }
    }

    private void responsePostGeofence(String response) {
        try {
            Alert.showMessage(activity, new JSONObject(response).getString("msg"));
        } catch (JSONException ex) {
            Alert.showError(activity, ex.getMessage());
        }
    }

}
