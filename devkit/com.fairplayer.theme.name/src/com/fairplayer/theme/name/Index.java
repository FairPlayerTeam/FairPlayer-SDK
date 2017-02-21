/**
 * Copyright (c) 2016-2017 FairPlayer Team
 * https://fairplayerteam.github.io/FairPlayer-SDK/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @version 1.0
 */
package com.fairplayer.theme.name;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.widget.Toast;

import com.fairplayer.theme.name.R;
import com.google.android.gms.plus.PlusOneButton;

@SuppressLint({ "SetJavaScriptEnabled" })
public class Index extends Activity {
	
    // Logging tag
    protected static final String TAG = "FairPlayerTheme";
    
    /**
     * Main WebKit file
     */
    protected static final String PAGE = "file:///android_asset/index.html";

    /**
     * First time use flag
     */
    protected boolean resumeFromIntent = false;

    /**
     * Browser
     */
    protected WebView browser;

    /**
     * Custom alert dialog
     */
    private CustomAlertDialog mDialog;

    /**
     * Alert dialog
     */
    class CustomAlertDialog extends AlertDialog {
        public CustomAlertDialog(Context context) {
            super(context);
        }
    }
    
    /**
     * +1 Button
     */
    protected PlusOneButton mPlusOneButton;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        // Create
        super.onCreate(savedInstanceState);

        // Load the index
        this.loadTheIndex();

        // Set the +1 button
        try {
            mPlusOneButton = (PlusOneButton) getWindow().getDecorView().findViewWithTag("plusOneButton");
        } catch (Exception exc) {
            Log.e(TAG, exc.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    protected void loadTheIndex() {
        // Set the layout
        setContentView(R.layout.layoutindex);

        // Destroy
        if (null != browser) {
            browser.destroy();
        }
        
        // Get the webview ID
        browser = (WebView) getWindow().getDecorView().findViewWithTag("webkitp3");
        
        // Get the Settings
        browser.getSettings().setJavaScriptEnabled(true);

        // Set the render priority
        browser.getSettings().setRenderPriority(RenderPriority.HIGH);
        browser.getSettings().setPluginState(android.webkit.WebSettings.PluginState.OFF);

        // No Cache
        browser.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        browser.getSettings().setAppCacheEnabled(false);

        // Add the JS interface
        browser.addJavascriptInterface(new WebAppInterface(this), "Android");

        // Set the webview client
        browser.setWebChromeClient(new CustomClient());

        // Set the background color
        if (android.os.Build.VERSION.SDK_INT < 16) {
            browser.setBackgroundColor(0x00000000);
        } else {
            browser.setBackgroundColor(Color.argb(1, 0, 0, 0));
        }

        // Set a long click listener
        browser.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        
        // Disable the long click
        browser.setLongClickable(false);

        // Load the page
        browser.loadUrl(PAGE);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make visible
        if (this.resumeFromIntent) {
            Log.d(TAG, "Resume from intent...");
            this.loadTheIndex();

            // Reset the flag
            this.resumeFromIntent = false;
        }
        
        // Refresh the state of the +1 button each time the activity receives
        try {
            mPlusOneButton = (PlusOneButton) getWindow().getDecorView().findViewWithTag("plusOneButton");
            mPlusOneButton.initialize("https://market.android.com/details?id=" + this.getPackageName(), 9000);
        } catch (Exception exc) {
            Log.e(TAG, exc.getMessage());
        }
    }

    private class CustomClient extends WebChromeClient {

        public boolean onConsoleMessage(ConsoleMessage cm) {
            // Log the message
            Log.d(TAG, cm.messageLevel().toString() + " " + cm.lineNumber() + ": " + cm.message() + " (" + cm.sourceId() + ")");

            // On error, reload
            if (cm.messageLevel().toString().equals("ERROR")) {
                browser.reload();
            }

            // All done
            return super.onConsoleMessage(cm);
        }

        public boolean onJsTimeout() {
            Log.e(TAG, "JS timeout");
            return true;
        }
    }

    /**
     * Web App interface
     */
    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void showInterstitial() {
            // TODO
        }

        @JavascriptInterface
        public void startApply() {
            // Set the flag
            Index.this.resumeFromIntent = true;

            // Apply the theme
            Index.this.applyFairPlayer();
        }

        @JavascriptInterface
        public String getString(String name) {
            // Get the string
            return getResources().getString(getResources().getIdentifier(name, "string", getPackageName()));
        }

        @JavascriptInterface
        public void toast(String text) {
            // Show the message
            Toast.makeText(Index.this, text, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check if the package is installed
     */
    public boolean isPackageInstalled(String packageName) {
        try {
            this.createPackageContext(packageName, 2);
        } catch (NameNotFoundException exception) {
            Log.i(TAG, exception.getMessage());
            return false;
        }

        return true;
    }

    // Apply the FairPlayer theme
    public void applyFairPlayer() {
        // FairPlayer is available
        if (this.isPackageInstalled("com.fairplayer")) {
            // Start the player
        	Intent intent = new Intent(Intent.ACTION_MAIN).setClassName("com.fairplayer", "com.fairplayer.ActivityNowplaying");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);

            // Inform the user
            Toast.makeText(getApplicationContext(), getString(R.string.fp_toastInfo), Toast.LENGTH_LONG).show();

            // Get the handler
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Start the jump mechanism
                    Intent localIntent = new Intent("android.intent.action.MAIN");
                    localIntent.setComponent(ComponentName.unflattenFromString("com.fairplayer/com.fairplayer.ActivityNowplaying"));                   
                    localIntent.addCategory("android.intent.category.LAUNCHER");
                    localIntent.putExtra("packageName", Index.this.getPackageName());

                    // Start the intent
                    try {
                        startActivity(localIntent);
                    } catch (Exception exc) {
                        Log.e(TAG, "Apply error: " + exc.getMessage());
                    }
                }
            }, 5000);
        } else {
            // Need to download FairPlayer
            this.mDialog = new CustomAlertDialog(this);
            this.mDialog.setTitle(getResources().getString(R.string.fp_downloadTitle));
            this.mDialog.setMessage(getResources().getString(R.string.fp_downloadContent) + (Index.this.hasFairPlayerAnimations() ? (" " + getResources().getString(R.string.fp_animationSupport)) : ""));
            this.mDialog.setButton(
                -1,
                getResources().getString(R.string.fp_downloadTitle),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                        try {
                            Index.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.fairplayer")));
                        } catch (Exception localException) {
                            // Nothing to do
                        }
                    }
                }
            );

            // Show the dialog box
            this.mDialog.show();
        }
    }

    /**
     * Check animations enabled
     */
    public boolean hasFairPlayerAnimations() {
        try {
            // Found a local ID
            int localId = Index.this.getResources().getIdentifier(
                "fp_frame_animation_0",
                "drawable",
                Index.this.getPackageName()
            );

            // Found the first frame
            if (localId != 0) {
                return true;
            }
        } catch (Exception e) {}

        // No animation starting frame
        return false;
    }
}

/* EOF */