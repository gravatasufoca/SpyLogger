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
public class UsuarioVO implements Serializable{

    private static final long serialVersionUID = -1488733798528397201L;
    private Integer id;
    @SerializedName("ds_email")
    private String email;
    @SerializedName("ds_senha")
    private String senha;
    @SerializedName("perfil")
    private AparelhoVO aparelho;
}
