package com.moduscreate.plugin;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.content.Context;
import android.widget.Toast;
public class ModusEcho extends CordovaPlugin {
  @Override
  public boolean execute(
    String action, JSONArray args, CallbackContext callbackContext
  ) throws JSONException {
    if ("echo".equals(action)) {
      echo(args.getString(0), callbackContext);
      return true;
    }
  return false;
  }

  private void start(CallbackContext callbackContext) {
    // test
    Toast.makeText(webView.getContext(), msg, Toast.LENGTH_LONG)
    .show();
    callbackContext.success('test android');
  }
}