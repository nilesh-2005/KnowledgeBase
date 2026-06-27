package com.nilesh.knowledgebase.service;

import com.nilesh.knowledgebase.entity.ChatCitation;
import com.nilesh.knowledgebase.entity.ChatMessage;
import com.nilesh.knowledgebase.entity.Conversation;
import com.nilesh.knowledgebase.entity.User;
import com.nilesh.knowledgebase.repository.ChatCitationRepository;
import com.nilesh.knowledgebase.repository.ChatMessageRepository;
import com.nilesh.knowledgebase.repository.ConversationRepository;
import com.nilesh.knowledgebase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatCitationRepository chatCitationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Conversation createConversation(UUID userId, String firstMessage) {
        User user = userRepository.findById(userId).orElseThrow();
        Conversation conversation = new Conversation();
        conversation.setUser(user);
        
        // Generate title from first message
        String title = firstMessage.length() > 40 ? firstMessage.substring(0, 40) + "..." : firstMessage;
        conversation.setTitle(title);
        
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public Page<Conversation> getUserConversations(UUID userId, Pageable pageable) {
        return conversationRepository.findByUserIdAndArchivedFalseAndDeletedAtIsNullOrderByUpdatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Conversation getConversation(UUID id, UUID userId) {
        Conversation conversation = conversationRepository.findById(id).orElseThrow();
        if (!conversation.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to conversation");
        }
        return conversation;
    }
    
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(UUID conversationId) {
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        // Force initialization of lazy collections while still inside the transaction
        messages.forEach(msg -> {
            msg.getCitations().size();
            msg.getCitations().forEach(citation -> {
                // Initialize document proxy to ensure title is available for frontend
                if (citation.getDocument() != null) {
                    citation.getDocument().getTitle();
                }
            });
        });
        return messages;
    }

    @Transactional
    public void deleteConversation(UUID id, UUID userId) {
        Conversation conversation = getConversation(id, userId);
        conversation.setDeletedAt(Instant.now());
        conversationRepository.save(conversation);
    }
    
    @Transactional
    public Conversation updateConversation(UUID id, UUID userId, String title, Boolean archived) {
        Conversation conversation = getConversation(id, userId);
        if (title != null) {
            conversation.setTitle(title);
        }
        if (archived != null) {
            conversation.setArchived(archived);
        }
        return conversationRepository.save(conversation);
    }

    @Transactional
    public void saveMessageWithCitations(ChatMessage message, List<ChatCitation> citations) {
        ChatMessage savedMessage = chatMessageRepository.save(message);
        if (citations != null && !citations.isEmpty()) {
            citations.forEach(c -> c.setMessage(savedMessage));
            chatCitationRepository.saveAll(citations);
        }
        
        // Update conversation updated_at
        Conversation conversation = savedMessage.getConversation();
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);
    }
}
