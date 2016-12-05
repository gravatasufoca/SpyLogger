package com.gravatasufoca.spylogger.vos;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 02/12/16.
 */

@Getter
@Setter
@EqualsAndHashCode
public class AparelhoVO implements Serializable {

    private static final long serialVersionUID = 6664359917430951587L;

    private Integer id;
    @SerializedName("no_aparelho")
    private String nome;
    @SerializedName("ds_chave")
    private String chave;

}
