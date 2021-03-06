package com.gravatasufoca.spylogger.vos;

import java.io.Serializable;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 04/12/16.
 */

@Getter
@Setter
@EqualsAndHashCode
public class RespostaRecebimentoVO implements Serializable {

    private static final long serialVersionUID = 3616277176061786990L;
    private String tipo;
    private List<Integer> ids;
}
