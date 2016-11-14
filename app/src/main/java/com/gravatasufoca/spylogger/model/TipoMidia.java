package com.gravatasufoca.spylogger.model;

public enum TipoMidia {
    TEXTO("0",""),
    IMAGEM("1", "Images"),
    AUDIO("2", "Audio"),
    VIDEO("3", "Video"),
    MAPA("5", "Map"),
    TELA_CHAMADA("8", ""),
    ARQUIVO("9", "Docs"),
    CHAMADA("10", ""),
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