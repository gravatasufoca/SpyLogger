package com.gravatasufoca.spylogger.services;

import android.os.Handler;

import com.gravatasufoca.spylogger.helpers.RequestInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by bruno on 01/12/16.
 */

public abstract class SendDataService<E> implements Callback<E>{

    private Retrofit retrofit;
    protected SendDataInterface sendApi;
    protected Handler handler;

    public SendDataService(Handler handler) {
        this.handler=handler;
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(new RequestInterceptor());

        retrofit=new Retrofit.Builder()
                .baseUrl(SendDataInterface.apiUrl)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        sendApi=retrofit.create(SendDataInterface.class);
    }

}
