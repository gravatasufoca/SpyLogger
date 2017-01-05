package com.gravatasufoca.spylogger.model;

import com.google.gson.annotations.SerializedName;

public enum TipoMidia {
    @SerializedName("0")
    TEXTO("0","",null),
    @SerializedName("1")
    IMAGEM("1", "Images","img"),
    @SerializedName("2")
    AUDIO("2", "Audio","aud"),
    @SerializedName("3")
    VIDEO("3", "Video","vid"),
    @SerializedName("4")
    CONTATO("4","Contact",null),
    @SerializedName("5")
    MAPA("5", "Map",null),
    @SerializedName("8")
    TELA_CHAMADA("8", "",null),
    @SerializedName("9")
    ARQUIVO("9", "Docs",null),
    @SerializedName("10")
    CHAMADA("10", "",null),
    @SerializedName("13")
    GIF("13", "Images","img");

    private String tipo;
    private String id;
    private String prefixo;

    TipoMidia(String id,String tipo,String prefixo) {
        this.id=id;
        this.tipo = tipo;
        this.prefixo=prefixo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getPrefixo() {
        return prefixo;
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