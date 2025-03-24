package io.github.studiousxiaoyu.hunyuan.api.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;


public class ApiAuthHttpRequestInterceptor implements ClientHttpRequestInterceptor {
        private final Log log = LogFactory.getLog(ApiAuthHttpRequestInterceptor.class);

		private final HunYuanAuthApi hunYuanAuthApi;

		public ApiAuthHttpRequestInterceptor(String secretId, String secretKey) {
            this.hunYuanAuthApi = new HunYuanAuthApi(secretId, secretKey, HunYuanConstants.DEFAULT_CHAT_HOST, HunYuanConstants.DEFAULT_SERVICE);
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            String action = request.getHeaders().getFirst("X-TC-Action");
            MultiValueMap<String, String> httpHeadersConsumer = hunYuanAuthApi.getHttpHeadersConsumer(action, body);
			request.getHeaders().putAll(httpHeadersConsumer);
            ClientHttpResponse response = execution.execute(request, body);
//            logResponse(response);
            return response;
        }

        private void logRequest(HttpRequest request, byte[] body) throws UnsupportedEncodingException {
            log.info("URI: " + request.getURI());
            log.info("HTTP Method: " + request.getMethod());
            log.info("HTTP Headers: " + headersToString(request.getHeaders()));
            log.info("Request Body: " + new String(body, StandardCharsets.UTF_8));
        }

        private void logResponse(ClientHttpResponse response) throws IOException {
            log.info(MessageFormat.format("HTTP Status Code: {0}", response.getStatusCode()));
            log.info("Status Text: " + response.getStatusText());
            log.info("HTTP Headers: " + headersToString(response.getHeaders()));
            log.info("Response Body: " + bodyToString(response.getBody()));
        }

        private String headersToString(HttpHeaders headers) {
            StringBuilder builder = new StringBuilder();
            for(Map.Entry<String, List<String>> entry : headers.entrySet()) {
                builder.append(entry.getKey()).append("=[");
                for(String value : entry.getValue()) {
                    builder.append(value).append(",");
                }
                builder.setLength(builder.length() - 1); // Get rid of trailing comma
                builder.append("],");
            }
            builder.setLength(builder.length() - 1); // Get rid of trailing comma
            return builder.toString();
        }
        
        private String bodyToString(InputStream body) throws IOException {
            StringBuilder builder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8));
            String line = bufferedReader.readLine();
            while (line != null) {
                builder.append(line).append(System.lineSeparator());
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            return builder.toString();
        }
    }
