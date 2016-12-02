package com.gravatasufoca.spylogger.vos;

import java.io.Serializable;
import java.util.List;

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

    private static final long serialVersionUID = -5448792310590751572L;
    private String email;
    private String senha;

    private List<AparelhoVO> aparelhoVOList;
}
