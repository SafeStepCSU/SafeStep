package com.columbus.ssep;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.columbus.ssep.databinding.ActivityLoginScreenBinding;
import com.columbus.ssep.ui.login.LoggedInUserView;
import com.columbus.ssep.ui.login.LoginFormState;
import com.columbus.ssep.ui.login.LoginResult;
import com.columbus.ssep.ui.login.LoginViewModel;
import com.columbus.ssep.ui.login.LoginViewModelFactory;

public class MainActivity extends AppCompatActivity {
    boolean toggle = false;

    private LoginViewModel loginViewModel;
    private ActivityLoginScreenBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });
/*
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());


            }
        });*/
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
    public void Login(View v){setContentView(R.layout.activity_main);}
    public void Update(View view){
        TextView lbl_s_1 = (TextView) findViewById(R.id.lbl_sensor_1);
        TextView lbl_s_2 = (TextView) findViewById(R.id.lbl_sensor_2);
        TextView lbl_s_3 = (TextView) findViewById(R.id.lbl_sensor_3);
        TextView lbl_s_4 = (TextView) findViewById(R.id.lbl_sensor_4);
        TextView lbl_s_5 = (TextView) findViewById(R.id.lbl_sensor_5);
        if(toggle) {
            lbl_s_1.setText(String.format("%s %s", getResources().getString(R.string.sensor_1), getResources().getString(R.string.normal)));
            lbl_s_2.setText(String.format("%s %s", getResources().getString(R.string.sensor_2), getResources().getString(R.string.normal)));
            lbl_s_3.setText(String.format("%s %s", getResources().getString(R.string.sensor_3), getResources().getString(R.string.abnormal)));
            lbl_s_4.setText(String.format("%s %s", getResources().getString(R.string.sensor_4), getResources().getString(R.string.normal)));
            lbl_s_5.setText(String.format("%s %s", getResources().getString(R.string.sensor_5), getResources().getString(R.string.abnormal)));
            toggle = false;
        }
        else {
            lbl_s_1.setText(String.format("%s %s", getResources().getString(R.string.sensor_1), getResources().getString(R.string.abnormal)));
            lbl_s_2.setText(String.format("%s %s", getResources().getString(R.string.sensor_2), getResources().getString(R.string.normal)));
            lbl_s_3.setText(String.format("%s %s", getResources().getString(R.string.sensor_3), getResources().getString(R.string.normal)));
            lbl_s_4.setText(String.format("%s %s", getResources().getString(R.string.sensor_4), getResources().getString(R.string.abnormal)));
            lbl_s_5.setText(String.format("%s %s", getResources().getString(R.string.sensor_5), getResources().getString(R.string.normal)));
            toggle = true;
        }
    }
}