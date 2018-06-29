package pricing.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public AsyncRestTemplate getAsyncRestTemplate() {
        AsyncRestTemplate template = new AsyncRestTemplate();
        template.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false; // to handle the errors self in a more user-friendly way
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });
        return template;
    }
}
