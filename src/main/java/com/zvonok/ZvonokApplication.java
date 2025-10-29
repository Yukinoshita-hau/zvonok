package com.zvonok;

import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Getter
@SpringBootApplication
public class ZvonokApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZvonokApplication.class, args);
    }

}
