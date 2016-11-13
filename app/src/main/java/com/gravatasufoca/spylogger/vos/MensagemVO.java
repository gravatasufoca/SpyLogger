package com.gravatasufoca.spylogger.vos;

import com.gravatasufoca.spylogger.model.TipoMidia;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MensagemVO {
    private boolean remetente;
    private ContatoVO contato;
    private String texto;
    private Date data;
    private Date dataRecebida;
    private TipoMidia tipoMidia;
    private long tamanhoArquivo;

}