package org.nexo.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(DiscoveryServerApplication.class, args);

        System.out.println("Discovery Server is running...");
        System.out.println("Access the Discovery Server at: http://localhost:8761/");

    }

}
