package com.gravatasufoca.spylogger.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 14/11/16.
 */

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@DatabaseTable(tableName = "topico")
public class Topico extends EntidadeAbstrata{
    private static final long serialVersionUID = -3806366120985930612L;

    private Topico() {
    }

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(canBeNull = false)
    private String idReferencia;
    @DatabaseField(canBeNull = true)
    private String nome;
    @DatabaseField(canBeNull = false)
    private Boolean enviado;
    @DatabaseField(canBeNull = false)
    private Boolean grupo;


    @ForeignCollectionField(eager=false,orderAscending=false,orderColumnName="data", foreignFieldName="topico")
    private ForeignCollection<Mensagem> mensagens;

    @Override
    public Serializable getId() {
        return id;
    }


    public static class TopicoBuilder{
        private Topico topico=new Topico();

        public TopicoBuilder setId(Integer id){
            topico.setId(id);
            return this;
        }
        public TopicoBuilder setIdReferencia(String idReferencia){
            topico.setIdReferencia(idReferencia);
            return this;
        }

        public TopicoBuilder setNome(String nome){
            topico.setNome(nome);
            return this;
        }

        public TopicoBuilder setGrupo(boolean grupo){
            topico.setGrupo(grupo);
            return this;
        }

        public Topico build(){
            topico.setEnviado(false);
            return topico;
        }
    }

}
