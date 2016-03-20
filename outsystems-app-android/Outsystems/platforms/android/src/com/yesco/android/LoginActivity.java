/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.yesco.android;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yesco.android.R;
import com.yesco.android.core.DatabaseHandler;
import com.yesco.android.core.EventLogger;
import com.yesco.android.core.WSRequestHandler;
import com.yesco.android.core.WebServicesClient;
import com.yesco.android.helpers.ApplicationSettingsController;
import com.yesco.android.helpers.DeepLinkController;
import com.yesco.android.helpers.HubManagerHelper;
import com.yesco.android.helpers.OfflineSupport;
import com.yesco.android.model.AppSettings;
import com.yesco.android.model.Application;
import com.yesco.android.model.HubApplicationModel;
import com.yesco.android.model.Login;

/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class LoginActivity extends BaseActivity {

    public static String KEY_INFRASTRUCTURE_NAME = "infrastructure";
    public static String KEY_AUTOMATICALLY_LOGIN = "key_login_automatically";

    public boolean doLogin = false;

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            hideKeyboard();

            String userName = ((EditText) findViewById(R.id.edit_text_user_mail)).getText().toString();
            String password = ((EditText) findViewById(R.id.edit_text_passwod)).getText().toString();

            if (!"".equals(userName) && !"".equals(password)) {
                callLoginService(v, userName, password);
            } else {
                ((EditText) findViewById(R.id.edit_text_user_mail)).setError(getResources().getString(
                        R.string.label_error_login));
                ((EditText) findViewById(R.id.edit_text_passwod)).setError(getResources().getString(
                        R.string.label_error_login));
                showError(findViewById(R.id.root_view));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean hideActionBar = ApplicationSettingsController.getInstance().hideActionBar(this);

        if(hideActionBar) {
            // Hide action bar
            getSupportActionBar().hide();
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String infrastructure = bundle.getString(KEY_INFRASTRUCTURE_NAME);
            doLogin = bundle.getBoolean(KEY_AUTOMATICALLY_LOGIN);

            ((TextView) findViewById(R.id.text_view_label_application_value)).setText(infrastructure);
        }

        final Button buttonLogin = (Button) findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(onClickListener);

        DatabaseHandler database = new DatabaseHandler(getApplicationContext());
        HubApplicationModel hub = database.getHubApplication(HubManagerHelper.getInstance().getApplicationHosted());
        database.close();

        // Check if deep link has valid settings                
        if(DeepLinkController.getInstance().hasValidSettings()){

        	Object[] params = new Object[1];
        	params[0] = hub;
        	
        	DeepLinkController.getInstance().resolveOperation(this, params);

        }
        
        if (hub != null && (hub.getUserName() != null || !"".equals(hub.getUserName()))
                && (hub.getPassword() != null || !"".equals(hub.getPassword()))) {
            ((EditText) findViewById(R.id.edit_text_user_mail)).setText(hub.getUserName());
            ((EditText) findViewById(R.id.edit_text_passwod)).setText(hub.getPassword());
            if (doLogin) {
                callLoginService(buttonLogin, hub.getUserName(), hub.getPassword());
                getIntent().removeExtra(KEY_AUTOMATICALLY_LOGIN);
            }
        }

        // Add a custom Action Bar
        setupActionBar();

        final EditText editText = (EditText) findViewById(R.id.edit_text_user_mail);
        editText.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                int width = editText.getWidth();
                int height = editText.getHeight();

                ViewGroup.LayoutParams params = buttonLogin.getLayoutParams();
                params.height = height;
                params.width = width;
                buttonLogin.requestLayout();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    editText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    editText.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        // Application Settings

        boolean hasValidSettings = ApplicationSettingsController.getInstance().hasValidSettings();
        if(hasValidSettings){

            // Show application logo
            View applicationLabel = findViewById(R.id.text_view_label_application);
            if(applicationLabel != null)
                applicationLabel.setVisibility(View.GONE);

            View environmentLabel = findViewById(R.id.text_view_label_application_value);
            if(environmentLabel != null)
                environmentLabel.setVisibility(View.GONE);

            View logoImage = findViewById(R.id.image_view_logo);
            if(logoImage != null)
                logoImage.setVisibility(View.VISIBLE);

            // Change colors
            AppSettings appSettings =  ApplicationSettingsController.getInstance().getSettings();

            boolean customBgColor = appSettings.getBackgroundColor() != null && !appSettings.getBackgroundColor().isEmpty();

            if(customBgColor){
                View root = findViewById(R.id.root_view);
                root.setBackgroundColor(Color.parseColor(appSettings.getBackgroundColor()));
            }

            boolean customFgColor = appSettings.getForegroundColor() != null && !appSettings.getForegroundColor().isEmpty();
            if(customFgColor){
                int newColor = Color.parseColor(appSettings.getForegroundColor());
                Drawable drawable = buttonLogin.getBackground();
                drawable.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);

                buttonLogin.setTextColor(newColor);

                ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
                drawable = progressBar.getIndeterminateDrawable();
                drawable.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);

            }

        }


    }

    public void callLoginService(final View v, final String userName, final String password) {
        showLoading(v);
        
		final DisplayMetrics displaymetrics = new DisplayMetrics();		
		getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
		
        WebServicesClient.getInstance().loginPlattform(getApplicationContext(), userName, password,
                HubManagerHelper.getInstance().getDeviceId(), (int)(displaymetrics.widthPixels / displaymetrics.density), (int)(displaymetrics.heightPixels / displaymetrics.density), new WSRequestHandler() {
                    @Override
                    public void requestFinish(Object result, boolean error, int statusCode) {
                        stopLoading(v);
                        ((EditText) findViewById(R.id.edit_text_user_mail)).setError(null);
                        ((EditText) findViewById(R.id.edit_text_passwod)).setError(null);
                        EventLogger.logMessage(getClass(), "Status Code: " + statusCode);

                        // Offline Support
                        OfflineSupport.getInstance(getApplicationContext()).prepareForLogin();

                        if (!error) {
                            Login login = (Login) result;

                            if (login == null || !login.isSuccess()) {
                                ((EditText) findViewById(R.id.edit_text_user_mail)).setError(getResources().getString(
                                        R.string.label_error_login));
                                ((EditText) findViewById(R.id.edit_text_passwod)).setError(getResources().getString(
                                        R.string.label_error_login));
                                showError(findViewById(R.id.root_view));
                            } else if (login.getVersion() == null || !login.getVersion().startsWith(getString(R.string.required_module_version))) {
                            	
                            	// invalid OutSystems Now modules in the server   
                            	 ((EditText) findViewById(R.id.edit_text_user_mail)).setError(getResources().getString(
                                         R.string.label_invalid_version));
                                 ((EditText) findViewById(R.id.edit_text_passwod)).setError(getResources().getString(
                                         R.string.label_invalid_version));
                            	showError(findViewById(R.id.root_view));
                            } else {

                                DatabaseHandler database = new DatabaseHandler(getApplicationContext());
                                database.updateHubApplicationCredentials(HubManagerHelper.getInstance()
                                        .getApplicationHosted(), userName, password);
                                database.addLoginApplications(HubManagerHelper.getInstance()
                                        .getApplicationHosted(), userName, login.getApplications());

                                database.close();

                                // Offline Support
                                OfflineSupport.getInstance(getApplicationContext()).checkCurrentSession(HubManagerHelper.getInstance()
                                        .getApplicationHosted(), userName);

                                boolean singleApp = login.getApplications() != null && login.getApplications().size() == 1;
                                openNextActivity(singleApp, login);

                            }
                        } else {
                            ((EditText) findViewById(R.id.edit_text_user_mail)).setError(WebServicesClient.PrettyErrorMessage(statusCode)); // getResources().getString(R.string.label_error_login)                            
                            showError(findViewById(R.id.root_view));
                        }
                    }
                });
               
    }


    private void openNextActivity(boolean singleApp, Login login){
        if (singleApp) {
            openWebApplicationActivity(login);
        } else {

            Intent nextIntent = ApplicationSettingsController.getInstance().getNextActivity(this);

            if(nextIntent == null ){
                nextIntent = new Intent(getApplicationContext(), ApplicationsActivity.class);
            }

            if(nextIntent.getComponent() != null && nextIntent.getComponent().getClassName().equals(ApplicationsActivity.class))
            {
                ArrayList arrayList = (ArrayList)login.getApplications();
                nextIntent.putParcelableArrayListExtra(ApplicationsActivity.KEY_CONTENT_APPLICATIONS,
                        (ArrayList<? extends Parcelable>)arrayList);
                nextIntent.putExtra(ApplicationsActivity.KEY_TITLE_ACTION_BAR, getResources().getString(R.string.label_logout));

            }
            startActivity(nextIntent);
        }
    }


    /**
     * Open web application activity.
     * 
     * @param login the login
     */
    private void openWebApplicationActivity(Login login) {
        Intent intent = new Intent(getApplicationContext(), WebApplicationActivity.class);
        Application application = login.getApplications().get(0);
        if (application != null) {
            intent.putExtra(WebApplicationActivity.KEY_APPLICATION, application);
            intent.putExtra(WebApplicationActivity.KEY_SINGLE_APPLICATION, true);
        }
        startActivity(intent);
    }

    private void hideKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if (this.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        WebServicesClient.getInstance().resetLoginHeaders();
    }
}
