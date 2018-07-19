package com.github.fengleicn.dht.starter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class AppRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
            String ip = args[0];
            String port = args[1];
            new BtInfoFinder(ip, Integer.valueOf(port)).run();
    }
}
