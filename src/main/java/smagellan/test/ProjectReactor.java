package smagellan.test;

import reactor.core.publisher.Flux;

import java.time.Duration;

public class ProjectReactor {
    public static void main(String[] args) {
        Flux.range(0, 100)
                .windowTimeout(10, Duration.ofMinutes(1))
                .flatMap(Flux::collectList)
                .subscribe(System.err::println);
    }
}
