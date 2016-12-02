package com.gravatasufoca.spylogger.vos;

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

    private static final long serialVersionUID = 99413416902472295L;
    private String nome;
    private String chave;
    private UsuarioVO usuarioVO;
}
