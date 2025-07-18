package com.shopsphere.shopsphere_web.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * 1:1 문의 채팅 메시지 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "inquiry_chat")
public class InquiryChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_chat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_chat_room_id", nullable = false, referencedColumnName = "inquiry_chat_room_id")
    private InquiryChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id", nullable = false, referencedColumnName = "user_id")
    private User sender;

    @Lob
    @Column(nullable = false, length = 2000) // 메시지 길이 제한 (2000자)
    private String message;

    @Column(name = "inquiry_chat_sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }

    // Helper method to get sender ID as String
    public String getSenderId() {
        return sender != null ? sender.getId() : null;
    }

    // Helper method to get chat room ID as Long
    public Long getChatRoomId() {
        return chatRoom != null ? chatRoom.getId() : null;
    }

    // Helper method to check if the message is from a specific user
    public boolean isFromUser(String userId) {
        return userId != null && userId.equals(getSenderId());
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}