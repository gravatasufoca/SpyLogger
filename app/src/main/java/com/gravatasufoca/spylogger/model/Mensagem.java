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

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(canBeNull = false,dataType = DataType.STRING)
    private String idReferencia;
    @DatabaseField(canBeNull = false,defaultValue = "0",dataType = DataType.BOOLEAN_INTEGER)
    private Boolean remetente;
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
    @DatabaseField()
    private Long tamanhoArquivo;
    @DatabaseField(dataType = DataType.STRING)
    private String contato;
    @DatabaseField(dataType = DataType.STRING)
    private String numeroContato;

    @DatabaseField()
    private String raw_data = null;

    @DatabaseField()
    private String thumb_image;

    @DatabaseField(foreign = true,canBeNull = false,foreignColumnName = "id", columnName = "topico_id")
    private Topico topico;

    @DatabaseField(canBeNull = false,defaultValue = "0",dataType = DataType.BOOLEAN_INTEGER)
    private Boolean enviada;

    @DatabaseField(canBeNull = false,defaultValue = "0",dataType = DataType.BOOLEAN_INTEGER)
    private Boolean midiaEnviada;

    @DatabaseField()
    private Double latitude;

    @DatabaseField()
    private Double longitude;

    private Boolean temMedia;

    @Override
    public Serializable getId() {
        return id;
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

    public static Map<String, Map<Integer,DataType>> columns() {
        Map<String,  Map<Integer,DataType>> colunas = new HashMap<>();

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
                Map<Integer,DataType> m=new HashMap<>();
                m.put(i,getType(field));
                colunas.put(colName, m);
                i++;
            }
        }
        return colunas;
    }

    private static DataType getType(Field field){
        if(field.getType().isAssignableFrom(String.class))
            return DataType.STRING;

        if(field.getType().isAssignableFrom(Integer.class))
            return DataType.INTEGER;

        if(field.getType().isAssignableFrom(Double.class))
            return DataType.DOUBLE;

        if(field.getType().isAssignableFrom(Date.class))
            return DataType.DATE_LONG;

        if(field.getType().isAssignableFrom(Long.class))
            return DataType.LONG;

        if(field.getType().getSimpleName().equals("byte[]"))
            return DataType.BYTE_ARRAY;

        if(field.getType().isAssignableFrom(Boolean.class))
            return DataType.BOOLEAN_INTEGER;

        if(field.getType().isAssignableFrom(TipoMidia.class))
            return DataType.ENUM_STRING;

        if(field.getType().isAssignableFrom(Topico.class))
            return DataType.INTEGER;

        return DataType.UNKNOWN;
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

        public MensagemBuilder setLatitude(Double latitude) {
            mensagem.setLatitude(latitude);
            return this;
        }

        public MensagemBuilder setLongitude(Double longitude) {
            mensagem.setLongitude(longitude);
            return this;
        }

    }
}
