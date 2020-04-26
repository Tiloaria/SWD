package ru.ifmo.ctd.turnstile;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import ru.ifmo.ctd.model.PassType;

import java.io.IOException;

public class StatisticsNotifier {
    private final String myHostname;
    private final int myPort;
    private final String myEndpoint;

    public StatisticsNotifier(String hostname, int port, String endpoint) {
        myHostname = hostname;
        myPort = port;
        myEndpoint = endpoint;
    }

    public void sendNotification(Integer userId, long passTime, PassType type) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        String path = String.format("%s?user_id=%s&pass_time=%s&pass_type=%s",
                myEndpoint, userId, passTime, type.name().toLowerCase());
        HttpGet request = new HttpGet(path);
        HttpHost target = new HttpHost(myHostname, myPort);
        CloseableHttpResponse response = client.execute(target, request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException(response.getStatusLine().getReasonPhrase());
        }
        response.close();
        client.close();
    }
}
