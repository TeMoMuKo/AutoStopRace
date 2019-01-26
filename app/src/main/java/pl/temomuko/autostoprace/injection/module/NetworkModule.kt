package pl.temomuko.autostoprace.injection.module

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import pl.temomuko.autostoprace.BuildConfig
import pl.temomuko.autostoprace.Constants
import pl.temomuko.autostoprace.data.model.ErrorResponse
import pl.temomuko.autostoprace.data.remote.GmtDateDeserializer
import pl.temomuko.autostoprace.data.remote.PostProcessingEnabler
import pl.temomuko.autostoprace.data.remote.api.Asr2019Service
import pl.temomuko.autostoprace.data.remote.api.TokenAuthenticationInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    fun provideAsr2019Service(retrofit: Retrofit) = retrofit.create<Asr2019Service>()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
            .registerTypeAdapter(Date::class.java, GmtDateDeserializer())
            .registerTypeAdapterFactory(PostProcessingEnabler())
            .create()
        return Retrofit.Builder()
            .baseUrl("http://192.168.1.101:8080")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()
    }

    @Provides
    fun provideOkHttp(
        loggingInterceptor: HttpLoggingInterceptor,
        tokenAuthenticationInterceptor: TokenAuthenticationInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Constants.HTTP_CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(Constants.HTTP_READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(Constants.HTTP_WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(tokenAuthenticationInterceptor)
            .addNetworkInterceptor(StethoInterceptor())
            .addNetworkInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", Constants.HEADER_VALUE_APPLICATION_JSON)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .setLevel(
                if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            )
    }

    private inline fun <reified T> Retrofit.create(): T {
        return create(T::class.java)
    }
}