import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OkHttpClientTest {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(OkHttpClientTest.class);
    public static void main(String[] args) throws IOException {
        //String url = "https://fonts.gstatic.com/s/googlesans/v29/4UaGrENHsxJlGDuGo1OIlL3Owp4.woff2";
        String url = "https://fonts.gstatic.com/";
        //String url = "http://localhost/";
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new CustomHttpLogger());
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .addInterceptor(interceptor)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            try (ResponseBody body = response.body()) {
                logger.info("response length: {}", body == null ? "no body" : body.contentLength());
            }
        }
    }
}


class CustomHttpLogger implements HttpLoggingInterceptor.Logger {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CustomHttpLogger.class);
    @Override
    public void log(@NotNull String s) {
        logger.info("{}", s);
    }
}