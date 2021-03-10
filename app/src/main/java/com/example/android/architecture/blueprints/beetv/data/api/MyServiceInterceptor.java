package com.example.android.architecture.blueprints.beetv.data.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.android.architecture.blueprints.beetv.BeeTVApplication;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class MyServiceInterceptor implements Interceptor {

  private String sessionToken;
  private static MyServiceInterceptor instance;
  public static MyServiceInterceptor getInstance() {
    if (instance == null) instance = new MyServiceInterceptor();
    return instance;
  }

  public MyServiceInterceptor() {
  }

  public void loadSessionTokenFromCache() {
    SharedPreferences sharedPref = BeeTVApplication.context.getSharedPreferences(
            "BeeTV", Context.MODE_PRIVATE);
    sessionToken = sharedPref.getString("token", "");
  }

  public void setSessionToken(String sessionToken) {
    this.sessionToken = sessionToken;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Request.Builder requestBuilder = request.newBuilder();
    if (sessionToken != null) {
      requestBuilder.addHeader("Authorization", "Bearer " + sessionToken);
    }
//    if (request.header(NO_AUTH_HEADER_KEY) == null) {
//      // needs credentials
//      if (sessionToken == null) {
//        throw new RuntimeException("Session token should be defined for auth apis");
//      } else {
//        requestBuilder.addHeader("Cookie", sessionToken);
//      }
//    }
    return chain.proceed(requestBuilder.build());
  }
}