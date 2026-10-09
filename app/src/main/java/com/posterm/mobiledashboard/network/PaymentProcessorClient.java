package com.posterm.mobiledashboard.network;

import com.posterm.mobiledashboard.model.ProblemDetails;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class PaymentProcessorClient {
    private final PaymentProcessorApi api;
    private final Converter<ResponseBody, ProblemDetails> problemConverter;

    private PaymentProcessorClient(Retrofit retrofit) {
        api = retrofit.create(PaymentProcessorApi.class);
        problemConverter = retrofit.responseBodyConverter(
                ProblemDetails.class,
                new Annotation[0]
        );
    }

    public static PaymentProcessorClient create(ApiConfiguration configuration) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.redactHeader("Authorization");
        loggingInterceptor.setLevel(configuration.isNetworkLoggingEnabled()
                ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new BearerTokenInterceptor(configuration.getBearerToken()))
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(configuration.getBaseUrl())
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return new PaymentProcessorClient(retrofit);
    }

    public PaymentProcessorApi getApi() {
        return api;
    }

    public ProblemDetails parseError(Response<?> response) throws IOException {
        if (response.isSuccessful() || response.errorBody() == null) {
            return null;
        }
        return problemConverter.convert(response.errorBody());
    }
}
