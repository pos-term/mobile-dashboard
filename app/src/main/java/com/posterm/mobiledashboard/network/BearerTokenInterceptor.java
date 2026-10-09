package com.posterm.mobiledashboard.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

final class BearerTokenInterceptor implements Interceptor {
    private final String bearerToken;

    BearerTokenInterceptor(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request authorizedRequest = chain.request()
                .newBuilder()
                .header("Authorization", "Bearer " + bearerToken)
                .build();
        return chain.proceed(authorizedRequest);
    }
}
