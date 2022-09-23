package com.missiveapp.openwith;

import android.content.ContentResolver;
import android.content.Intent;
import android.util.Log;
import java.util.Arrays;
import java.util.ArrayList;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
* This is the entry point of the openwith plugin
*
* @author Jean-Christophe Hoelt
*/
public class OpenWithPlugin extends CordovaPlugin {
  private final String PLUGIN_NAME = "OpenWithPlugin";


  private CallbackContext handlerContext; // Callback to the javascript onNewFile method */

  /** Intents added before the handler has been registered */
  private ArrayList pendingIntents = new ArrayList(); //NOPMD
    private Boolean withData = false;

    /**
   * Generic plugin command executor
   *
   * @param action
   * @param data
   * @param callbackContext
   * @return
   */
  @Override
  public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) {

    if ("init".equals(action)) {
      return init(data, callbackContext);
    }

    return false;
  }

  // Initialize the plugin
  public boolean init(final JSONArray data, final CallbackContext context) {

    if (data.length() != 1) {
      Log.w(PLUGIN_NAME, "init() -> invalidAction");
      return false;
    }
    try {
        withData = data.getBoolean(0);
    } catch (JSONException e) {
        e.printStackTrace();
    }

      handlerContext = context;
    Intent intent = cordova.getActivity().getIntent();
    onNewIntent(intent);
    if (intent.getAction() != "android.intent.action.MAIN") {
      intent.setAction("android.intent.action.MAIN");
      cordova.getActivity().setIntent(intent);
    }

    final PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    result.setKeepCallback(true);
    context.sendPluginResult(result);
    return true;
  }


  /**
   * This is called when a new intent is sent while the app is already opened.
   *
   * We also call it manually with the cordova application intent when the plugin
   * is initialized (so all intents will be managed by this method).
   */
  @Override
  public void onNewIntent(final Intent intent) {

    if (intent.getAction() != "android.intent.action.MAIN") {
      final JSONObject json = toJSONObject(intent);
      if (json != null) {
        pendingIntents.add(json);
      }
    }

    processPendingIntents();
  }

  /**
   * When the handler is defined, call it with all attached files.
   */
  private void processPendingIntents() {

    if (handlerContext == null) {
      return;
    }
    JSONObject item = new JSONObject();

    for (int i = 0; i < pendingIntents.size(); i++) {
      item = (JSONObject) pendingIntents.get(i);
    }

    pendingIntents.clear();
    if(item.length() != 0) {
      sendIntentToJavascript(item);
    }
  }

  /** Calls the javascript intent handlers. */
  private void sendIntentToJavascript(final JSONObject intent) {
    final PluginResult result = new PluginResult(PluginResult.Status.OK, intent);

    result.setKeepCallback(true);
    handlerContext.sendPluginResult(result);
  }

  /**
   * Converts an intent to JSON
   */
  private JSONObject toJSONObject(final Intent intent) {
    try {
      final ContentResolver contentResolver = this.cordova
        .getActivity().getApplicationContext().getContentResolver();

      return Serializer.toJSONObject(contentResolver, intent,withData);
    } catch (JSONException e) {
      Log.e(PLUGIN_NAME, "Error converting intent to JSON: " + e.getMessage());
      Log.e(PLUGIN_NAME, Arrays.toString(e.getStackTrace()));

      return null;
    }
  }
}
