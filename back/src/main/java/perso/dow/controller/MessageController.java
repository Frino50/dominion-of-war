package perso.dow.controller;

import org.springframework.web.bind.annotation.*;
import perso.dow.model.PageResponse;
import perso.dow.model.dto.MessageDto;
import perso.dow.service.MessageService;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping()
    public void sendMessage(@RequestBody Map<String, String> data) {
        messageService.sendMessage(data.get("message"));
    }

    @GetMapping("/{page}/{size}")
    public PageResponse<MessageDto> getMessages(@PathVariable int page, @PathVariable int size) {
        return new PageResponse<>(messageService.getMessages(page, size));
    }
}