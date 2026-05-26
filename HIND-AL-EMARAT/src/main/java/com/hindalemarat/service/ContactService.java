package com.hindalemarat.service;

import com.hindalemarat.entity.ContactMessage;
import com.hindalemarat.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ContactService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    public ContactMessage save(ContactMessage message) {
        return contactMessageRepository.save(message);
    }

    public List<ContactMessage> findAll() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void markAsRead(Long id) {
        ContactMessage msg = contactMessageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        msg.setRead(true);
        contactMessageRepository.save(msg);
    }

    public long countUnread() {
        return contactMessageRepository.countByReadFalse();
    }
}
