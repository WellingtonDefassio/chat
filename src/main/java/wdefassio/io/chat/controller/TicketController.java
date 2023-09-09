package wdefassio.io.chat.controller;

import com.auth0.jwk.JwkException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import wdefassio.io.chat.services.TicketService;


import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("v1/ticket")
@RequiredArgsConstructor
@CrossOrigin()
public class TicketController {
    private final TicketService ticketService;

    @PostMapping
    public Map<String, String> buildTicket(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws JwkException {
        String token = Optional.ofNullable(authorization).map(it -> it.replace("Bearer ", ""))
                .orElse("");
        String ticket = ticketService.buildAndSaveTicket(token);
        return Map.of("ticket", ticket);
    }

}
