package com.github.fengleicn.dht;


import com.alibaba.fastjson.JSON;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {
    public static void main(String[] args) throws Exception {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://pv.sohu.com/cityjson?ie=utf-8");
        HttpResponse httpResponse = closeableHttpClient.execute(httpPost);
        String post = EntityUtils.toString(httpResponse.getEntity());
        String json = post.split(" = ")[1];
        json = json.substring(0, json.length() - 1);

        String ip = JSON.parseObject(json).getString("cip");
        String port = "6883";
        LoggerFactory.getLogger(MainApplication.class).info("My ip address is " + ip + " port is " + port);

        SpringApplication.run(MainApplication.class, ip, port);
    }
}
