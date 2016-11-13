package com.gravatasufoca.spylogger.vos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 12/11/16.
 */
@Getter
@Setter
public class ChatVO {

    private String nome;
    private String avatar;
    private List<MensagemVO> mensagens=new ArrayList<>();

    public Date getDate(){
        if(mensagens!=null && !mensagens.isEmpty()){
            return mensagens.get(0).getData();
        }
        return null;
    }

    public String getTexto(){
        if(mensagens!=null && !mensagens.isEmpty()){
            return mensagens.get(0).getTexto();
        }
        return null;
    }

    public void addMensagem(MensagemVO mensagemVO){
        if(mensagens!=null){
            mensagens.add(mensagemVO);
        }
    }

}
