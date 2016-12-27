package com.gravatasufoca.spylogger.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 14/11/16.
 */

@Getter
@Setter
@DatabaseTable(tableName = "mensagem")
public class Mensagem extends EntidadeAbstrata {
    private static final long serialVersionUID = -8813262353266302208L;

    private Mensagem() {
    }

    @DatabaseField(generatedId = true,dataType = DataType.INTEGER)
    private Integer id;
    @DatabaseField(canBeNull = false,dataType = DataType.STRING)
    private String idReferencia;
    @DatabaseField(canBeNull = false,defaultValue = "0",dataType = DataType.BOOLEAN_INTEGER)
    private boolean remetente;
    @DatabaseField(canBeNull = true,dataType = DataType.STRING)
    private String texto;
    @DatabaseField(canBeNull = false, dataType = DataType.DATE_LONG)
    private Date data;
    @DatabaseField(canBeNull = false, dataType = DataType.DATE_LONG)
    private Date dataRecebida;
    @DatabaseField(dataType = DataType.ENUM_STRING)
    private TipoMidia tipoMidia;
    @DatabaseField(dataType = DataType.STRING)
    private String midiaMime;
    @DatabaseField(dataType = DataType.LONG)
    private long tamanhoArquivo;
    @DatabaseField(dataType = DataType.STRING)
    private String contato;
    @DatabaseField(dataType = DataType.STRING)
    private String numeroContato;

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] raw_data = null;

    @DatabaseField(dataType=DataType.BYTE_ARRAY)
    private byte[] thumb_image;

    @DatabaseField(foreign = true,canBeNull = false,foreignColumnName = "id", columnName = "topico_id",dataType = DataType.INTEGER)
    private Topico topico;

    @DatabaseField(canBeNull = false,defaultValue = "0",dataType = DataType.BOOLEAN_INTEGER)
    private boolean enviada;

    @DatabaseField(canBeNull = false,defaultValue = "0",dataType = DataType.BOOLEAN_INTEGER)
    private boolean midiaEnviada;

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

    public static Map<String, Integer> columns() {
        Map<String, Integer> colunas = new HashMap<>();

        Field[] fields = Mensagem.class.getDeclaredFields();
        int i = 0;
        for (Field field : fields) {
            if (field.isAnnotationPresent(DatabaseField.class)) {
                DatabaseField anotation = field.getAnnotation(DatabaseField.class);
                String colName;
                if (anotation.columnName() != null && !anotation.columnName().isEmpty()) {
                    colName = anotation.columnName();
                } else {
                    colName=field.getName();
                }
                colunas.put(colName, i);
                i++;
            }
        }
        return colunas;
    }

    public static class MensagemBuilder {

        private Mensagem mensagem = new Mensagem();

        public MensagemBuilder() {
        }

        public Mensagem build() {
            mensagem.setEnviada(false);
            mensagem.setMidiaEnviada(false);
            return mensagem;
        }

        public MensagemBuilder setId(Integer id) {
            mensagem.setId(id);
            return this;
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

        public MensagemBuilder setNumeroContato(String numeroContato) {
            mensagem.setNumeroContato(numeroContato);
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
