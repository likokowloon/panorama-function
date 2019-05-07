package com.panorama.plugin;
import android.content.Context;
import android.content.Intent;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PanoramaFunction extends CordovaPlugin {

    private CallbackContext callback = null;
    public static final String EXTRA_IMAGE_PATH = "image_path";

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getActivity().getApplicationContext();
        if(action.equals("start")) {
            callback = callbackContext;

            cordova.setActivityResultCallback(this);
            Intent intent = new Intent(context, com.dermandar.panoramal.ShooterActivity.class);
            cordova.getActivity().startActivityForResult(intent, 0);

        }
        PluginResult result = new PluginResult(PluginResult.Status.OK, "before onActivityResult");
        result.setKeepCallback(true);

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, data.getStringExtra(EXTRA_IMAGE_PATH));
        result.setKeepCallback(true);
        callback.sendPluginResult(result);
        return;
    }
}