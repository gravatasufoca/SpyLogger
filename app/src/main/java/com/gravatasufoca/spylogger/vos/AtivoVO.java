package com.gravatasufoca.spylogger.vos;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 28/03/17.
 */

@Getter
@Setter
public class AtivoVO implements Serializable {

    private static final long serialVersionUID = 5651079597485844898L;
    private Boolean ativo;
    private Boolean wifi;
}
