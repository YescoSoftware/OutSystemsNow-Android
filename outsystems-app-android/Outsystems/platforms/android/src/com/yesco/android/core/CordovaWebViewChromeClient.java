package com.yesco.android.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.engine.SystemWebChromeClient;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.util.ArrayList;
import java.util.StringTokenizer;


public class CordovaWebViewChromeClient extends SystemWebChromeClient{

    private static final int FILECHOOSER_RESULTCODE = 5173;
    private static final String LOG_TAG = "CordovaWebViewChromeClient";

    private static final String MIME_TYPE_AUDIO = "audio/*";
    private static final String MIME_TYPE_IMAGE = "image/*";
    private static final String MIME_TYPE_VIDEO = "video/*";

    private CordovaInterface cordovaInterface;

    public CordovaWebViewChromeClient(SystemWebViewEngine parentEngine,CordovaInterface cordovaInterface) {
        super(parentEngine);
        this.cordovaInterface = cordovaInterface;
    }

    @Override
    public void openFileChooser(final ValueCallback<Uri> uploadMsg, String acceptType, String capture)
    {

        boolean singleIntent = launchSingleIntent(uploadMsg, acceptType, capture);

        if(!singleIntent) {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            // Create file chooser intent
            Intent chooserIntent = Intent.createChooser(intent, "Choose an action");
            // Set camera intent to file chooser

            ArrayList<Intent> otherIntents = new ArrayList();

            otherIntents.add(this.getImageIntent());
            otherIntents.add(this.getVideoIntent());
            otherIntents.add(this.getSoundIntent());
            otherIntents.add(this.getMyFilesIntent());

            Parcelable[] parcelables = new Parcelable[otherIntents.size()];
            for (int i = 0; i < parcelables.length; i++) {
                parcelables[i] = otherIntents.get(i);
            }

            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, parcelables);

            this.cordovaInterface.startActivityForResult(new CordovaPlugin() {
                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                    Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
                    Log.d(LOG_TAG, "Receive file chooser URL: " + result);
                    uploadMsg.onReceiveValue(result);
                }
            }, chooserIntent, FILECHOOSER_RESULTCODE);

        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, final ValueCallback<Uri[]> filePathsCallback, final WebChromeClient.FileChooserParams fileChooserParams) {

        boolean singleIntent = launchSingleIntent(filePathsCallback, fileChooserParams);

        if(!singleIntent) {

            Intent intent = fileChooserParams.createIntent();

            // Create file chooser intent
            Intent chooserIntent = Intent.createChooser(intent, "Choose an action");
            // Set camera intent to file chooser

            ArrayList<Intent> otherIntents = new ArrayList();

            otherIntents.add(this.getImageIntent());
            otherIntents.add(this.getVideoIntent());
            otherIntents.add(this.getSoundIntent());
            otherIntents.add(this.getMyFilesIntent());

            Parcelable[] parcelables = new Parcelable[otherIntents.size()];
            for (int i = 0; i < parcelables.length; i++) {
                parcelables[i] = otherIntents.get(i);
            }

            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, parcelables);

            try {
                this.cordovaInterface.startActivityForResult(new CordovaPlugin() {
                    @Override
                    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                        Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, intent);
                        Log.d(LOG_TAG, "Receive file chooser URL: " + result);
                        filePathsCallback.onReceiveValue(result);
                    }
                }, chooserIntent, FILECHOOSER_RESULTCODE);
            } catch (ActivityNotFoundException e) {
                Log.w("No activity found to handle file chooser intent.", e);
                filePathsCallback.onReceiveValue(null);
            }
        }

        return true;
    }

    private boolean launchSingleIntent(final ValueCallback<Uri> uploadMsg, String acceptType, String capture){

        boolean single = false;

        if(acceptType != null && !acceptType.isEmpty()){
            StringTokenizer st = new StringTokenizer(acceptType,",");
            single = st.countTokens() == 1;
        }

        if(single){

            Intent intent = getIntentForType(acceptType);

            if (intent == null){
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
            }

            if(capture != null && !capture.isEmpty()){

                this.cordovaInterface.startActivityForResult(new CordovaPlugin() {
                    @Override
                    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                        Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
                        Log.d(LOG_TAG, "Receive file chooser URL: " + result);
                        uploadMsg.onReceiveValue(result);
                    }
                }, intent, FILECHOOSER_RESULTCODE);

            }
            else{

                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                fileIntent.setType("*/*");

                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(fileIntent, "Choose an action");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{intent});

                this.cordovaInterface.startActivityForResult(new CordovaPlugin() {
                    @Override
                    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                        Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
                        Log.d(LOG_TAG, "Receive file chooser URL: " + result);
                        uploadMsg.onReceiveValue(result);
                    }
                }, chooserIntent, FILECHOOSER_RESULTCODE);

            }

        }

        return single;
    }


    private boolean launchSingleIntent(final ValueCallback<Uri[]> filePathsCallback, final WebChromeClient.FileChooserParams fileChooserParams){

        String[] types = fileChooserParams.getAcceptTypes();
        boolean single = false;
        String contentType = null;

        if(types != null && types.length == 1){
            contentType = types[0];
            StringTokenizer st = new StringTokenizer(contentType,",");
            single = st.countTokens() == 1;
        }

        if(single){

            Intent intent = getIntentForType(contentType);

            if (intent == null){
                intent = fileChooserParams.createIntent();
            }

            if(fileChooserParams.isCaptureEnabled()){
                try {
                    this.cordovaInterface.startActivityForResult(new CordovaPlugin() {
                        @Override
                        public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, intent);
                            Log.d(LOG_TAG, "Receive file chooser URL: " + result);
                            filePathsCallback.onReceiveValue(result);
                        }
                    }, intent, FILECHOOSER_RESULTCODE);
                } catch (ActivityNotFoundException e) {
                    Log.w("No activity found to handle file chooser intent.", e);
                    filePathsCallback.onReceiveValue(null);
                }
            }
            else{

                Intent fileIntent = fileChooserParams.createIntent();

                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(fileIntent, "Choose an action");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{intent});

                try {
                    this.cordovaInterface.startActivityForResult(new CordovaPlugin() {
                        @Override
                        public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, intent);
                            Log.d(LOG_TAG, "Receive file chooser URL: " + result);
                            filePathsCallback.onReceiveValue(result);
                        }
                    }, chooserIntent, FILECHOOSER_RESULTCODE);
                } catch (ActivityNotFoundException e) {
                    Log.w("No activity found to handle file chooser intent.", e);
                    filePathsCallback.onReceiveValue(null);
                }

            }

        }

        return single;
    }

    private Intent getIntentForType(String type){
        Intent result = null;

        if(type.equalsIgnoreCase(MIME_TYPE_IMAGE)){
            result = getImageIntent();
        }
        else{
            if (type.equalsIgnoreCase(MIME_TYPE_VIDEO)){
                result = getVideoIntent();
            }
            else{
                if(type.equalsIgnoreCase(MIME_TYPE_AUDIO)){
                    result = getSoundIntent();
                }
            }
        }

        return result;
    }


    // Capture image intent
    private final Intent getImageIntent(){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        return intent;
    }

    // Capture video intent
    private final Intent getVideoIntent(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        return intent;
    }

    // Record audio intent
    private final Intent getSoundIntent(){
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        return intent;
    }

    private final Intent getMyFilesIntent(){
        Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        intent.putExtra("CONTENT_TYPE", "*/*");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }

}
