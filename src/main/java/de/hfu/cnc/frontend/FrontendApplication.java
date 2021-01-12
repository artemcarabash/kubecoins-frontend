package de.hfu.cnc.frontend;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

// Rest Schnittstelle unserer Anwendung und Haupt Funktionen
@RestController
@SpringBootApplication
public class FrontendApplication {


    Counter requestCounter;
    private WebClient workerClient;
    private RabbitTemplate template;
    private DirectExchange direct;
    private String routingKey;
    // Log Objekt um informative Log Nachrichten in Console auszugeben
    private static final Logger LOG = LoggerFactory.getLogger(FrontendApplication.class);

    // Konstruktor
    public FrontendApplication(
            @Value("${service.backends.worker}") String workerBackend,
            @Value("${service.routingKey}") String routingKey,
            @Autowired RabbitTemplate template,
            @Autowired DirectExchange exchange,
            MeterRegistry registry
    ) {
        this.workerClient = WebClient.builder().baseUrl(workerBackend).build();
        this.routingKey = routingKey;
        this.template = template;
        this.direct = exchange;
        this.requestCounter = registry.counter("frontend_requests");
    }


    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }

    // Test Methode
    @RequestMapping("/home")
    public String home() {
        LOG.info("Called home() method");
        return "KubeCoin App. Carabas Artiom";
    }
    // Methode die erstellt 10 Nachrichten
    @RequestMapping("/work10")
    public String work10() {
        LOG.info("sent 10 requests");
        for (int i = 0; i < 10; i++) {
            sendMessage();
        }
        return "Sent 10 messages!";
    }
    // Methode die erstellt 100 Nachrichten
    @RequestMapping("/work100")
    public String work100() {
        LOG.info("sent 100 requests");
        for (int i = 0; i < 100; i++) {
            sendMessage();
        }
        return "Sent 100 messages!";
    }

    private void sendMessage() {
        template.convertAndSend(direct.getName(), routingKey, "1");
        requestCounter.increment();
    }


    @RequestMapping("/stats")
    public String stats() {
        LOG.info("Frontend stats request!");
        requestCounter.increment();

        String total = workerClient.get()
                .uri("/total")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        String last = workerClient.get()
                .uri("/last")
                .retrieve()
                .bodyToMono(String.class)
                .block();


        return "There are " + total + " hashes. The last hash - " + last;
    }


}
