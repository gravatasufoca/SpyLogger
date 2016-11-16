package com.gravatasufoca.spylogger.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 14/11/16.
 */

@Getter
@Setter
@DatabaseTable(tableName = "mensagem")
public class Mensagem extends EntidadeAbstrata{
    private static final long serialVersionUID = -8813262353266302208L;

    private Mensagem() {
    }

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(canBeNull = false)
    private String idReferencia;
    @DatabaseField(canBeNull = false)
    private boolean remetente;
    @DatabaseField(canBeNull = true)
    private String texto;
    @DatabaseField(canBeNull = false,dataType=DataType.DATE_LONG)
    private Date data;
    @DatabaseField(canBeNull = false,dataType= DataType.DATE_LONG)
    private Date dataRecebida;
    @DatabaseField(dataType = DataType.ENUM_STRING)
    private TipoMidia tipoMidia;
    @DatabaseField
    private String midiaMime;
    @DatabaseField
    private long tamanhoArquivo;
    @DatabaseField
    private String contato;

    @DatabaseField( dataType = DataType.BYTE_ARRAY)
    private byte[] raw_data=null;

    @DatabaseField(foreign=true, foreignColumnName="id",columnName="topico_id")
    private Topico topico;

    @DatabaseField(dataType = DataType.ENUM_INTEGER)
    private TipoMensagem tipoMensagem;

    @DatabaseField(canBeNull = false)
    private boolean enviada;

    private boolean temMedia;

    @Override
    public Serializable getId() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Mensagem mensagem = (Mensagem) o;

        if (id != null ? !id.equals(mensagem.id) : mensagem.id != null) return false;
        if (idReferencia != null ? !idReferencia.equals(mensagem.idReferencia) : mensagem.idReferencia != null)
            return false;
        if (data != null ? !data.equals(mensagem.data) : mensagem.data != null) return false;
        return tipoMidia == mensagem.tipoMidia;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (idReferencia != null ? idReferencia.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (tipoMidia != null ? tipoMidia.hashCode() : 0);
        return result;
    }

    public static class MensagemBuilder {

        private Mensagem mensagem=new Mensagem();

        public MensagemBuilder() {
        }

        public Mensagem build(TipoMensagem tipoMensagem){
            mensagem.setTipoMensagem(tipoMensagem);
            mensagem.setEnviada(false);
            return mensagem;
        }

        public MensagemBuilder setIdReferencia(String idReferencia) {
            mensagem.setIdReferencia(idReferencia);
            return this;
        }

        public MensagemBuilder setRemetente(boolean remetente) {
            mensagem.setRemetente(remetente);
            return this;
        }

        public MensagemBuilder setTexto(String texto) {
            mensagem.setTexto(texto);
            return this;
        }

        public MensagemBuilder setData(Date data) {
            mensagem.setData(data);
            return this;
        }

        public MensagemBuilder setDataRecebida(Date dataRecebida) {
            mensagem.setDataRecebida(dataRecebida);
            return this;
        }

        public MensagemBuilder setTipoMidia(TipoMidia tipoMidia) {
            mensagem.setTipoMidia(tipoMidia);
            return this;
        }

        public MensagemBuilder setTamanhoArquivo(long tamanhoArquivo) {
            mensagem.setTamanhoArquivo(tamanhoArquivo);
            return this;
        }

        public MensagemBuilder setTopico(Topico topico) {
            mensagem.setTopico(topico);
            return this;
        }

        public MensagemBuilder setContato(String contato) {
            mensagem.setContato(contato);
            return this;
        }
        public MensagemBuilder setTemMedia(boolean temMedia) {
            mensagem.setTemMedia(temMedia);
            return this;
        }
        public MensagemBuilder setMediaMime(String mime) {
            mensagem.setMidiaMime(mime);
            return this;
        }

    }
}
