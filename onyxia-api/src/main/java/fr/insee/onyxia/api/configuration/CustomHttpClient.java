package fr.insee.onyxia.api.configuration;

import feign.Logger;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import okhttp3.OkHttpClient;

import java.io.IOException;

@Configuration
public class CustomHttpClient {

    @Value("${debug.http.log}")
    boolean LOG_HTTP;

    @Bean
    public OkHttpClient httpClient(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        enableDebugFeatureIfEnabled(builder);
        return builder.build();
    }

    @Bean
    @Qualifier("marathon")
    public OkHttpClient marathonHttpClient(final @Value("${marathon.url}") String MARATHON_URL, final @Value("${marathon.auth.token}") String MARATHON_AUTH_TOKEN) {


        OkHttpClient.Builder builder = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Request request = chain.request();
                Request newRequest = request;

                if (MARATHON_AUTH_TOKEN != null) {
                    newRequest = request.newBuilder()
                            .addHeader("Authorization", "token="+MARATHON_AUTH_TOKEN)
                            .build();
                }

                // TODO : add basic auth support

                return chain.proceed(newRequest);
            }
        });

        enableDebugFeatureIfEnabled(builder);


        return builder.build();
    }

    private void enableDebugFeatureIfEnabled(OkHttpClient.Builder builder) {
        if (LOG_HTTP) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
    }
}