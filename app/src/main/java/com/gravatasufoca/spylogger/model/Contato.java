package com.gravatasufoca.spylogger.model;

/**
 * Created by bruno on 10/12/16.
 */

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "contatos")
public class Contato extends EntidadeAbstrata{

    private static final long serialVersionUID = 3994144507162110568L;

    public Contato() {
    }

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(canBeNull = true)
    private String nome;
    @DatabaseField(canBeNull = false,unique = true)
    private String numero;
    @DatabaseField(canBeNull = true,dataType = DataType.BYTE_ARRAY)
    private byte[] raw_data = null;

    @ForeignCollectionField(eager=false,orderAscending=false,orderColumnName="data", foreignFieldName="contato")
    private ForeignCollection<Mensagem> mensagens;

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Contato contato = (Contato) o;

        return numero != null ? numero.equals(contato.numero) : contato.numero == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (numero != null ? numero.hashCode() : 0);
        return result;
    }
}
