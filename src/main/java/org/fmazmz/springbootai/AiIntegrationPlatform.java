package org.fmazmz.springbootai;

import org.fmazmz.springbootai.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiIntegrationPlatform {

    public static void main(String[] args) {
        DotenvLoader.loadFromProjectRoot();
        SpringApplication.run(AiIntegrationPlatform.class, args);
    }

}
