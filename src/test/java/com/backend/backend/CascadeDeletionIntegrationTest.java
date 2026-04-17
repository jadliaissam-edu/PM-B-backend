package com.backend.backend;

import com.backend.backend.dao.entities.Attachment;
import com.backend.backend.dao.entities.Comment;
import com.backend.backend.dao.entities.Folder;
import com.backend.backend.dao.entities.Liste;
import com.backend.backend.dao.entities.Space;
import com.backend.backend.dao.entities.Subtask;
import com.backend.backend.dao.entities.Task;
import com.backend.backend.dao.entities.User;
import com.backend.backend.dao.entities.Workspace;
import com.backend.backend.dao.entities.WorkspaceMember;
import com.backend.backend.dao.enums.ListType;
import com.backend.backend.dao.enums.Priority;
import com.backend.backend.dao.enums.TaskStatus;
import com.backend.backend.dao.enums.WorkspaceRole;
import com.backend.backend.dao.repositories.AttachmentRepository;
import com.backend.backend.dao.repositories.CommentRepository;
import com.backend.backend.dao.repositories.FolderRepository;
import com.backend.backend.dao.repositories.ListeRepository;
import com.backend.backend.dao.repositories.SpaceRepository;
import com.backend.backend.dao.repositories.SubtaskRepository;
import com.backend.backend.dao.repositories.TaskRepository;
import com.backend.backend.dao.repositories.UserRepository;
import com.backend.backend.dao.repositories.WorkspaceMemberRepository;
import com.backend.backend.dao.repositories.WorkspaceRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class CascadeDeletionIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private ListeRepository listeRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @TestConfiguration
    static class PasswordEncoderTestConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Test
    void deletingSpaceShouldCascadeToFoldersAndNestedTree() {
        User owner = createUser("owner-space@test.local");
        User assignee = createUser("assignee-space@test.local");
        Workspace workspace = createWorkspace(owner, "Workspace Space Delete", "workspace-space-delete");
        Space space = createSpace(workspace, "Space A");
        Folder folder = createFolder(space, "Folder A");
        Liste liste = createListe(folder, "Liste A");
        Task task = createTask(liste, assignee, "Task A");

        Subtask subtask = createSubtask(task, assignee, "Subtask A");
        Comment comment = createComment(task, owner, "Comment A");
        Attachment attachment = createAttachment(task, "attachment-a.pdf");

        entityManager.flush();
        entityManager.clear();

        spaceRepository.deleteById(space.getId());
        entityManager.flush();

        assertEquals(false, folderRepository.existsById(folder.getId()), "Folders should be deleted when space is deleted");
        assertEquals(false, listeRepository.existsById(liste.getId()), "Listes should be deleted when space is deleted");
        assertEquals(false, taskRepository.existsById(task.getId()), "Tasks should be deleted when space is deleted");
        assertEquals(false, subtaskRepository.existsById(subtask.getId()), "Subtasks should be deleted when space is deleted");
        assertEquals(false, commentRepository.existsById(comment.getId()), "Comments should be deleted when space is deleted");
        assertEquals(false, attachmentRepository.existsById(attachment.getId()), "Attachments should be deleted when space is deleted");
        assertEquals(true, workspaceRepository.existsById(workspace.getId()), "Workspace should remain after deleting one space");
    }

    @Test
    void deletingWorkspaceShouldCascadeToWholeWorkspaceTree() {
        User owner = createUser("owner-workspace@test.local");
        User memberUser = createUser("member-workspace@test.local");
        Workspace workspace = createWorkspace(owner, "Workspace Cascade", "workspace-cascade");

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUser(memberUser);
        member.setRole(WorkspaceRole.MEMBER);
        member.setJoinedAt(LocalDateTime.now());
        member = workspaceMemberRepository.save(member);

        Space space = createSpace(workspace, "Space B");
        Folder folder = createFolder(space, "Folder B");
        Liste liste = createListe(folder, "Liste B");
        Task task = createTask(liste, memberUser, "Task B");

        Subtask subtask = createSubtask(task, memberUser, "Subtask B");
        Comment comment = createComment(task, owner, "Comment B");
        Attachment attachment = createAttachment(task, "attachment-b.pdf");

        entityManager.flush();
        entityManager.clear();

        workspaceRepository.deleteById(workspace.getId());
        entityManager.flush();

        assertEquals(false, workspaceRepository.existsById(workspace.getId()), "Workspace should be deleted");
        assertEquals(false, workspaceMemberRepository.existsById(member.getId()), "Workspace members should be deleted with workspace");
        assertEquals(false, spaceRepository.existsById(space.getId()), "Spaces should be deleted with workspace");
        assertEquals(false, folderRepository.existsById(folder.getId()), "Folders should be deleted with workspace");
        assertEquals(false, listeRepository.existsById(liste.getId()), "Listes should be deleted with workspace");
        assertEquals(false, taskRepository.existsById(task.getId()), "Tasks should be deleted with workspace");
        assertEquals(false, subtaskRepository.existsById(subtask.getId()), "Subtasks should be deleted with workspace");
        assertEquals(false, commentRepository.existsById(comment.getId()), "Comments should be deleted with workspace");
        assertEquals(false, attachmentRepository.existsById(attachment.getId()), "Attachments should be deleted with workspace");
        assertEquals(true, userRepository.existsById(owner.getId()), "Owner should remain and not be cascade-deleted");
        assertEquals(true, userRepository.existsById(memberUser.getId()), "Member user should remain and not be cascade-deleted");
    }

    private User createUser(String email) {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("hashed-password");
        user.setRole("USER");
        user.setMfaEnabled(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setWorkspaceMembers(new ArrayList<>());
        user.setAssignedTasks(new ArrayList<>());
        user.setComments(new ArrayList<>());
        user.setNotifications(new ArrayList<>());
        user.setAssignedSubtasks(new ArrayList<>());
        return userRepository.save(user);
    }

    private Workspace createWorkspace(User owner, String name, String slug) {
        Workspace workspace = new Workspace();
        workspace.setName(name);
        workspace.setSlug(slug);
        workspace.setUser(owner);
        workspace.setCreatedAt(LocalDateTime.now());
        return workspaceRepository.save(workspace);
    }

    private Space createSpace(Workspace workspace, String name) {
        Space space = new Space();
        space.setName(name);
        space.setColor("#123456");
        space.setPrivate(false);
        space.setWorkspace(workspace);
        return spaceRepository.save(space);
    }

    private Folder createFolder(Space space, String name) {
        Folder folder = new Folder();
        folder.setName(name);
        folder.setHidden(false);
        folder.setCreatedAt(LocalDateTime.now());
        folder.setSpace(space);
        return folderRepository.save(folder);
    }

    private Liste createListe(Folder folder, String name) {
        Liste liste = new Liste();
        liste.setName(name);
        liste.setType(ListType.PHASE);
        liste.setOrder(1);
        liste.setCreatedAt(LocalDateTime.now());
        liste.setFolder(folder);
        return listeRepository.save(liste);
    }

    private Task createTask(Liste liste, User assignee, String title) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("desc");
        task.setStatus(TaskStatus.TO_DO);
        task.setPriority(Priority.MEDIUM);
        task.setDueDate(LocalDateTime.now().plusDays(3));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setListe(liste);
        task.setAssignee(assignee);
        task.setTags(new ArrayList<>());
        return taskRepository.save(task);
    }

    private Subtask createSubtask(Task task, User assignee, String title) {
        Subtask subtask = new Subtask();
        subtask.setTitle(title);
        subtask.setDone(false);
        subtask.setCreatedAt(LocalDateTime.now());
        subtask.setTask(task);
        subtask.setAssignee(assignee);
        return subtaskRepository.save(subtask);
    }

    private Comment createComment(Task task, User author, String content) {
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    private Attachment createAttachment(Task task, String filename) {
        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setFilename(filename);
        attachment.setUrl("https://example.local/" + filename);
        attachment.setMimeType("application/pdf");
        attachment.setSizeBytes(1024);
        attachment.setUploadedAt(LocalDateTime.now());
        return attachmentRepository.save(attachment);
    }
}
