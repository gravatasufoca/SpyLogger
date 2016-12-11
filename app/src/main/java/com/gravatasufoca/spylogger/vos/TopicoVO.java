package com.gravatasufoca.spylogger.vos;

import com.gravatasufoca.spylogger.model.Topico;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 10/12/16.
 */
@Getter
@Setter
@EqualsAndHashCode
public class TopicoVO {
    private Topico topico;
    private List<ContatoVO> contatos;
}
