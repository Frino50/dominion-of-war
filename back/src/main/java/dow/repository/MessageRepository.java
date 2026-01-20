package dow.repository;

import dow.model.dto.MessageDto;
import dow.model.entities.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT new dow.model.dto.MessageDto(m.id, p.pseudo, m.content, m.timestamp) " +
            "FROM Message m JOIN m.player p ORDER BY m.timestamp DESC")
    Page<MessageDto> findAllMessages(Pageable pageable);
}
