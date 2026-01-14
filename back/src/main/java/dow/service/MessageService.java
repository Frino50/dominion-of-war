package dow.service;

import dow.model.dto.MessageDto;
import dow.model.entities.Message;
import dow.model.entities.Player;
import dow.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UtilsService utilsService;

    public MessageService(MessageRepository messageRepository, SimpMessagingTemplate messagingTemplate, UtilsService utilsService) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
        this.utilsService = utilsService;
    }

    public void sendMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Le message ne peut pas être vide ou ne contenir que des espaces.");
        }

        Player player = utilsService.getPlayer();

        Message msg = new Message();
        msg.setPlayer(player);
        msg.setContent(message);
        ZoneId parisZone = ZoneId.of("Europe/Paris");
        msg.setTimestamp(LocalDateTime.now(parisZone));
        msg = messageRepository.save(msg);

        messagingTemplate.convertAndSend("/topic/chat",
                new MessageDto(msg.getId(), player.getPseudo(), msg.getContent(), msg.getTimestamp()));
    }

    public Page<MessageDto> getMessages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return messageRepository.findAllMessages(pageable);
    }
}