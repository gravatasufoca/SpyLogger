package com.gravatasufoca.spylogger.model;

import com.google.gson.annotations.SerializedName;

public enum TipoMidia {
    @SerializedName("0")
    TEXTO("0",""),
    @SerializedName("1")
    IMAGEM("1", "Images"),
    @SerializedName("2")
    AUDIO("2", "Audio"),
    @SerializedName("3")
    VIDEO("3", "Video"),
    @SerializedName("4")
    CONTATO("4","Contact"),
    @SerializedName("5")
    MAPA("5", "Map"),
    @SerializedName("8")
    TELA_CHAMADA("8", ""),
    @SerializedName("9")
    ARQUIVO("9", "Docs"),
    @SerializedName("10")
    CHAMADA("10", ""),
    @SerializedName("13")
    GIF("13", "Images");

    private String tipo;
    private String id;

    TipoMidia(String id,String tipo) {
        this.id=id;
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getId() {
        return id;
    }

    public static TipoMidia getTipoMidia(String id){
        for(TipoMidia tipoMidia:values()){
            if(tipoMidia.getId().equals(id))
                return tipoMidia;
        }
        return null;
    }
}