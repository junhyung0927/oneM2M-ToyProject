package kr.re.keti.service;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class M2MRetrofit {
    private static retrofit2.Retrofit ourInstance;
    private final static String URL = "https://wisoftlabs.ml:1883";

    private Context context;
    private M2MRetrofit(Context context) {
        this.context = context;
    }

    public static retrofit2.Retrofit getInstance() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

        OkHttpClient client = new OkHttpClient
                .Builder()
                .addInterceptor(interceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build();

        ourInstance = new retrofit2.Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();

        return ourInstance;
    }
}
