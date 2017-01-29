package com.gravatasufoca.spylogger.vos;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 28/01/17.
 */

@Getter
@Setter
public class LocalizacaoVO {
    private EnvioArquivoVO envioArquivoVO;
    private Double latitude;
    private Double longitude;
    private Double precisao;
    private Boolean gpsLigado;
}
