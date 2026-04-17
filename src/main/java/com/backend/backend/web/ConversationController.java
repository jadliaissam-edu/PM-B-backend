package com.backend.backend.web;

import com.backend.backend.dto.conversation.ConversationMessageRequestDto;
import com.backend.backend.dto.conversation.ConversationMessageResponseDto;
import com.backend.backend.dto.conversation.ConversationRequestDto;
import com.backend.backend.dto.conversation.ConversationResponseDto;
import com.backend.backend.service.serviceInterface.IConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final IConversationService conversationService;

    @PostMapping
    public ResponseEntity<ConversationResponseDto> createConversation(@RequestBody(required = false) ConversationRequestDto requestDto) {
        ConversationResponseDto created = conversationService.createConversation(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponseDto>> getMyConversations() {
        return ResponseEntity.ok(conversationService.getConversationsByCurrentUser());
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<ConversationMessageResponseDto>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(conversationService.getMessagesByConversation(conversationId, limit));
    }

    @PutMapping("/{conversationId}/title")
    public ResponseEntity<ConversationResponseDto> updateConversationTitle(
            @PathVariable String conversationId,
            @RequestBody(required = false) ConversationRequestDto requestDto
    ) {
        return ResponseEntity.ok(conversationService.updateConversationTitle(conversationId, requestDto));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ConversationMessageResponseDto> addMessage(
            @PathVariable String conversationId,
            @RequestBody ConversationMessageRequestDto requestDto
    ) {
        ConversationMessageResponseDto created = conversationService.addMessage(conversationId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        conversationService.deleteConversation(conversationId);
        return ResponseEntity.noContent().build();
    }
}