package com.gravatasufoca.spylogger.helpers;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class RequestInterceptor implements Interceptor {
  @Override public Response intercept(Interceptor.Chain chain) throws IOException {
    Request originalRequest = chain.request();
    if (originalRequest.body() == null || originalRequest.header("AuthToken") != null) {
      return chain.proceed(originalRequest);
    }
    String token=FirebaseInstanceId.getInstance().getToken();
    if(token!=null && !token.isEmpty()) {
      Request compressedRequest = originalRequest.newBuilder()
              .header("AuthToken", token)
              .build();
      return chain.proceed(compressedRequest);
    }else{
      return chain.proceed(originalRequest);
    }
  }
}