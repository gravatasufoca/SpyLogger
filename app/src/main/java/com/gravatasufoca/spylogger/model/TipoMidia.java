package com.gravatasufoca.spylogger.model;

public enum TipoMidia{
        VIDEO("Video"),AUDIO("Audio"),IMAGEM("Images"),MAPA("Map"),TEXTO("");

        private String tipo;
        TipoMidia(String tipo) {
            this.tipo=tipo;
        }

        public String getTipo() {
            return tipo;
        }
    }