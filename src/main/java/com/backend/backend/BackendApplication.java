package com.backend.backend;

import com.backend.backend.dao.entities.*;
import com.backend.backend.dao.enums.ListType;
import com.backend.backend.dao.enums.Priority;
import com.backend.backend.dao.enums.TaskStatus;
import com.backend.backend.dao.enums.WorkspaceRole;
import com.backend.backend.dao.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	CommandLineRunner start(UserRepository userRepository,
	                        WorkspaceRepository workspaceRepository,
	                        WorkspaceMemberRepository workspaceMemberRepository,
	                        SpaceRepository spaceRepository,
	                        FolderRepository folderRepository,
	                        SprintRepository sprintRepository,
	                        ListeRepository listeRepository,
	                        TaskRepository taskRepository,
	                        TagRepository tagRepository,
	                        SubtaskRepository subtaskRepository,
	                        CommentRepository commentRepository,
	                        AttachmentRepository attachmentRepository,
	                        ConversationRepository conversationRepository,
	                        ConversationMessageRepository conversationMessageRepository,
	                        NotificationRepository notificationRepository,
	                        PasswordEncoder passwordEncoder) {
		return args -> {
			SeedContext ctx = new SeedContext(
					userRepository,
					workspaceRepository,
					workspaceMemberRepository,
					spaceRepository,
					folderRepository,
					sprintRepository,
					listeRepository,
					taskRepository,
					tagRepository,
					subtaskRepository,
					commentRepository,
					attachmentRepository,
					conversationRepository,
					conversationMessageRepository,
					notificationRepository,
					passwordEncoder
			);

			LocalDateTime now = LocalDateTime.now();

			User marouane = createUserIfMissing(ctx.userRepository(), ctx.passwordEncoder(),
					"marouanequaisse@gmail.com", "Marouane", "Quaisse", "ADMIN", "123");
			User prof = createUserIfMissing(ctx.userRepository(), ctx.passwordEncoder(),
					"prof@ensam-casa.ma", "Prof", "Demo", "USER", "prof1234");
			User amine = createUserIfMissing(ctx.userRepository(), ctx.passwordEncoder(),
					"amine.bennani@ensam-casa.ma", "Amine", "Bennani", "USER", "amine2026");
			User salma = createUserIfMissing(ctx.userRepository(), ctx.passwordEncoder(),
					"salma.elmansouri@ensam-casa.ma", "Salma", "Elmansouri", "USER", "salma2026");
			User yassine = createUserIfMissing(ctx.userRepository(), ctx.passwordEncoder(),
					"yassine.ouahbi@ensam-casa.ma", "Yassine", "Ouahbi", "USER", "yassine2026");

			List<User> coreTeam = List.of(prof, amine, salma, yassine);

			for (WorkspaceBlueprint blueprint : buildWorkspaceBlueprints()) {
				Workspace workspace = createWorkspaceIfMissing(ctx.workspaceRepository(), marouane,
						blueprint.name(), blueprint.slug());

				ensureWorkspaceMember(ctx.workspaceMemberRepository(), workspace, marouane,
						WorkspaceRole.OWNER, now.minusMonths(4));

				for (int i = 0; i < coreTeam.size(); i++) {
					WorkspaceRole role;
					if (i == 0) {
						role = WorkspaceRole.ADMIN;
					} else if (i <= 2) {
						role = WorkspaceRole.MEMBER;
					} else {
						role = WorkspaceRole.GUEST;
					}

					ensureWorkspaceMember(ctx.workspaceMemberRepository(), workspace, coreTeam.get(i),
							role, now.minusMonths(3).plusDays(i));
				}

				List<Sprint> workspaceSprints = new ArrayList<>();
				for (SprintBlueprint sprintBlueprint : blueprint.sprints()) {
					LocalDateTime startDate = now.plusDays(sprintBlueprint.startOffsetDays());
					LocalDateTime endDate = startDate.plusDays(sprintBlueprint.durationDays());
					String sprintName = blueprint.slug().toUpperCase(Locale.ROOT) + " - " + sprintBlueprint.name();

					Sprint sprint = createSprintIfMissing(
							ctx.sprintRepository(),
							sprintName,
							startDate,
							endDate,
							sprintBlueprint.goal(),
							sprintBlueprint.active());
					workspaceSprints.add(sprint);
				}

				for (int i = 0; i < blueprint.spaces().size(); i++) {
					SpaceBlueprint spaceBlueprint = blueprint.spaces().get(i);
					Space space = createSpaceIfMissing(
							ctx.spaceRepository(),
							workspace,
							spaceBlueprint.name(),
							spaceBlueprint.color(),
							spaceBlueprint.privateSpace());

					seedSpaceTree(ctx, workspace, space, marouane, coreTeam, workspaceSprints, spaceBlueprint, i);
				}

				seedConversation(ctx, marouane, workspace);
				seedNotifications(ctx, marouane, coreTeam, workspace);
			}

			System.out.println(">>> [DataSeed] Injection complete terminee pour marouanequaisse@gmail.com : 3 workspaces, 4 spaces chacun, et donnees metier A a Z.");
		};
	}

	// --- HELPER METHODS ---

	private User createUserIfMissing(UserRepository repo, PasswordEncoder encoder, String email,
	                                 String fName, String lName, String role, String pass) {
		return repo.findByEmail(email).orElseGet(() -> {
			User user = new User();
			user.setEmail(email);
			user.setFirstName(fName);
			user.setLastName(lName);
			user.setName(fName + " " + lName);
			user.setRole(role);
			user.setPassword(encoder.encode(pass));
			user.setCreatedAt(LocalDateTime.now());
			System.out.println("Seed: Utilisateur créé -> " + email);
			return repo.save(user);
		});
	}

	private Workspace createWorkspaceIfMissing(WorkspaceRepository repo, User owner, String name, String slug) {
		return repo.findAllByUser(owner).stream()
				.filter(existing -> existing.getName().equalsIgnoreCase(name))
				.findFirst()
				.map(existing -> {
					existing.setSlug(slug);
					return repo.save(existing);
				})
				.orElseGet(() -> {
			Workspace ws = new Workspace();
			ws.setName(name);
			ws.setSlug(slug);
			ws.setUser(owner);
			ws.setCreatedAt(LocalDateTime.now());
			System.out.println("Seed: Workspace créé -> " + name);
			return repo.save(ws);
		});
	}

	private Space createSpaceIfMissing(SpaceRepository repo, Workspace ws, String name, String color, boolean isPrivate) {
		return repo.findByWorkspace(ws).stream()
				.filter(existing -> existing.getName().equalsIgnoreCase(name))
				.findFirst()
				.map(existing -> {
					existing.setColor(color);
					existing.setPrivate(isPrivate);
					return repo.save(existing);
				})
				.orElseGet(() -> {
			Space space = new Space();
			space.setName(name);
			space.setColor(color);
			space.setPrivate(isPrivate);
			space.setWorkspace(ws);
			System.out.println("Seed: Space créé -> " + name);
			return repo.save(space);
		});
	}

	private Folder createFolderIfMissing(FolderRepository repo, Space space, String name, boolean isHidden) {
		return repo.findAll().stream()
				.filter(existing -> existing.getSpace() != null
						&& existing.getSpace().getId().equals(space.getId())
						&& existing.getName().equalsIgnoreCase(name))
				.findFirst()
				.map(existing -> {
					existing.setHidden(isHidden);
					return repo.save(existing);
				})
				.orElseGet(() -> {
					Folder folder = new Folder();
					folder.setName(name);
					folder.setHidden(isHidden);
					folder.setSpace(space);
					folder.setCreatedAt(LocalDateTime.now());
					System.out.println("Seed: Folder cree -> " + name + " (" + space.getName() + ")");
					return repo.save(folder);
				});
	}

	private Sprint createSprintIfMissing(SprintRepository repo,
	                                   String name,
	                                   LocalDateTime startDate,
	                                   LocalDateTime endDate,
	                                   String goal,
	                                   boolean active) {
		return repo.findAll().stream()
				.filter(existing -> existing.getName().equalsIgnoreCase(name))
				.findFirst()
				.map(existing -> {
					existing.setStartDate(startDate);
					existing.setEndDate(endDate);
					existing.setGoal(goal);
					existing.setActive(active);
					return repo.save(existing);
				})
				.orElseGet(() -> {
					Sprint sprint = new Sprint();
					sprint.setName(name);
					sprint.setStartDate(startDate);
					sprint.setEndDate(endDate);
					sprint.setGoal(goal);
					sprint.setActive(active);
					System.out.println("Seed: Sprint cree -> " + name);
					return repo.save(sprint);
				});
	}

	private Liste createListeIfMissing(ListeRepository repo,
	                                Folder folder,
	                                String name,
	                                ListType type,
	                                int order,
	                                Sprint sprint) {
		return repo.findByFolder(folder).stream()
				.filter(existing -> existing.getName().equalsIgnoreCase(name))
				.findFirst()
				.map(existing -> {
					existing.setType(type);
					existing.setOrder(order);
					existing.setSprint(sprint);
					return repo.save(existing);
				})
				.orElseGet(() -> {
					Liste liste = new Liste();
					liste.setName(name);
					liste.setType(type);
					liste.setOrder(order);
					liste.setCreatedAt(LocalDateTime.now());
					liste.setFolder(folder);
					liste.setSprint(sprint);
					System.out.println("Seed: Liste cree -> " + name + " (" + folder.getName() + ")");
					return repo.save(liste);
				});
	}

	private Task createTaskIfMissing(TaskRepository repo,
	                               Liste liste,
	                               String title,
	                               String description,
	                               TaskStatus status,
	                               Priority priority,
	                               LocalDateTime dueDate,
	                               Sprint sprint,
	                               User assignee) {
		Optional<Task> existingTask = repo.findByListeId(liste.getId()).stream()
				.filter(task -> task.getTitle().equalsIgnoreCase(title))
				.findFirst();

		if (existingTask.isPresent()) {
			Task task = existingTask.get();
			task.setDescription(description);
			task.setStatus(status);
			task.setPriority(priority);
			task.setDueDate(dueDate);
			task.setSprint(sprint);
			task.setAssignee(assignee);
			task.setUpdatedAt(LocalDateTime.now());
			if (task.getCreatedAt() == null) {
				task.setCreatedAt(LocalDateTime.now());
			}
			return repo.save(task);
		}

		Task task = new Task();
		task.setTitle(title);
		task.setDescription(description);
		task.setStatus(status);
		task.setPriority(priority);
		task.setDueDate(dueDate);
		task.setCreatedAt(LocalDateTime.now());
		task.setUpdatedAt(LocalDateTime.now());
		task.setListe(liste);
		task.setSprint(sprint);
		task.setAssignee(assignee);
		task.setTags(new ArrayList<>());
		System.out.println("Seed: Task cree -> " + title);
		return repo.save(task);
	}

	private Tag createTagIfMissing(TagRepository repo, String name, String color) {
		return repo.findAll().stream()
				.filter(existing -> existing.getName().equalsIgnoreCase(name))
				.findFirst()
				.map(existing -> {
					if (existing.getColor() == null) {
						existing.setColor(color);
						return repo.save(existing);
					}
					return existing;
				})
				.orElseGet(() -> {
					Tag tag = new Tag();
					tag.setName(name);
					tag.setColor(color);
					return repo.save(tag);
				});
	}

	private void ensureTaskTag(TaskRepository taskRepository, Task task, Tag tag) {
		Task managedTask = taskRepository.findWithTagsById(task.getId()).orElse(task);
		List<Tag> tags = managedTask.getTags();
		if (tags == null) {
			tags = new ArrayList<>();
			managedTask.setTags(tags);
		}

		boolean alreadyLinked = tags.stream()
				.anyMatch(existing -> existing.getId() != null && existing.getId().equals(tag.getId()));

		if (!alreadyLinked) {
			tags.add(tag);
			taskRepository.save(managedTask);
		}
	}

	private Subtask createSubtaskIfMissing(SubtaskRepository repo,
	                                     Task task,
	                                     User assignee,
	                                     String title,
	                                     boolean done) {
		return repo.findAll().stream()
				.filter(existing -> existing.getTask() != null
						&& existing.getTask().getId().equals(task.getId())
						&& existing.getTitle().equalsIgnoreCase(title))
				.findFirst()
				.map(existing -> {
					existing.setDone(done);
					existing.setAssignee(assignee);
					return repo.save(existing);
				})
				.orElseGet(() -> {
					Subtask subtask = new Subtask();
					subtask.setTitle(title);
					subtask.setDone(done);
					subtask.setCreatedAt(LocalDateTime.now());
					subtask.setTask(task);
					subtask.setAssignee(assignee);
					return repo.save(subtask);
				});
	}

	private Comment createCommentIfMissing(CommentRepository repo,
	                                     Task task,
	                                     User author,
	                                     String content) {
		return repo.findAll().stream()
				.filter(existing -> existing.getTask() != null
						&& existing.getTask().getId().equals(task.getId())
						&& existing.getAuthor() != null
						&& existing.getAuthor().getId().equals(author.getId())
						&& existing.getContent().equals(content))
				.findFirst()
				.orElseGet(() -> {
					Comment comment = new Comment();
					comment.setTask(task);
					comment.setAuthor(author);
					comment.setContent(content);
					comment.setCreatedAt(LocalDateTime.now());
					comment.setUpdatedAt(LocalDateTime.now());
					return repo.save(comment);
				});
	}

	private Attachment createAttachmentIfMissing(AttachmentRepository repo,
	                                           Task task,
	                                           String filename,
	                                           String url,
	                                           String mimeType,
	                                           int sizeBytes) {
		return repo.findAll().stream()
				.filter(existing -> existing.getTask() != null
						&& existing.getTask().getId().equals(task.getId())
						&& existing.getFilename().equalsIgnoreCase(filename))
				.findFirst()
				.map(existing -> {
					existing.setUrl(url);
					existing.setMimeType(mimeType);
					existing.setSizeBytes(sizeBytes);
					return repo.save(existing);
				})
				.orElseGet(() -> {
					Attachment attachment = new Attachment();
					attachment.setTask(task);
					attachment.setFilename(filename);
					attachment.setUrl(url);
					attachment.setMimeType(mimeType);
					attachment.setSizeBytes(sizeBytes);
					attachment.setUploadedAt(LocalDateTime.now());
					return repo.save(attachment);
				});
	}

	private Conversation createConversationIfMissing(ConversationRepository repo,
	                                               User user,
	                                               String title) {
		return repo.findAllByUserOrderByUpdatedAtDesc(user).stream()
				.filter(existing -> existing.getTitle().equalsIgnoreCase(title))
				.findFirst()
				.map(existing -> {
					existing.setUpdatedAt(LocalDateTime.now());
					return repo.save(existing);
				})
				.orElseGet(() -> {
					Conversation conversation = new Conversation();
					conversation.setTitle(title);
					conversation.setCreatedAt(LocalDateTime.now());
					conversation.setUpdatedAt(LocalDateTime.now());
					conversation.setUser(user);
					return repo.save(conversation);
				});
	}

	private ConversationMessage createMessageIfMissing(ConversationMessageRepository repo,
	                                                 Conversation conversation,
	                                                 String role,
	                                                 String content) {
		return repo.findAllByConversationOrderByCreatedAtAsc(conversation).stream()
				.filter(existing -> existing.getRole().equals(role) && existing.getContent().equals(content))
				.findFirst()
				.orElseGet(() -> {
					ConversationMessage message = new ConversationMessage();
					message.setConversation(conversation);
					message.setRole(role);
					message.setContent(content);
					message.setCreatedAt(LocalDateTime.now());
					return repo.save(message);
				});
	}

	private Notification createNotificationIfMissing(NotificationRepository repo,
	                                               User user,
	                                               String message,
	                                               boolean read) {
		return repo.findAll().stream()
				.filter(existing -> existing.getUser() != null
						&& existing.getUser().getId().equals(user.getId())
						&& existing.getMessage().equals(message))
				.findFirst()
				.map(existing -> {
					existing.setRead(read);
					return repo.save(existing);
				})
				.orElseGet(() -> {
					Notification notification = new Notification();
					notification.setUser(user);
					notification.setMessage(message);
					notification.setRead(read);
					notification.setCreatedAt(LocalDateTime.now());
					return repo.save(notification);
				});
	}

	private WorkspaceMember ensureWorkspaceMember(WorkspaceMemberRepository repo,
	                                             Workspace workspace,
	                                             User user,
	                                             WorkspaceRole role,
	                                             LocalDateTime joinedAt) {
		return repo.findByWorkspace(workspace).stream()
				.filter(existing -> existing.getUser() != null && existing.getUser().getId().equals(user.getId()))
				.findFirst()
				.map(existing -> {
					existing.setRole(role);
					if (existing.getJoinedAt() == null) {
						existing.setJoinedAt(joinedAt);
					}
					return repo.save(existing);
				})
				.orElseGet(() -> {
					WorkspaceMember member = new WorkspaceMember();
					member.setWorkspace(workspace);
					member.setUser(user);
					member.setRole(role);
					member.setJoinedAt(joinedAt);
					return repo.save(member);
				});
	}

	private void seedSpaceTree(SeedContext ctx,
	                         Workspace workspace,
	                         Space space,
	                         User owner,
	                         List<User> team,
	                         List<Sprint> sprints,
	                         SpaceBlueprint plan,
	                         int index) {
		Sprint sprintA = sprints.get(0);
		Sprint sprintB = sprints.get(1);

		Folder cadrage = createFolderIfMissing(ctx.folderRepository(), space, "Cadrage et priorisation", false);
		Folder execution = createFolderIfMissing(ctx.folderRepository(), space, "Execution operationnelle", false);

		Liste backlog = createListeIfMissing(ctx.listeRepository(), cadrage, "Backlog priorise", ListType.PHASE, 1, null);
		Liste prepSprint = createListeIfMissing(ctx.listeRepository(), cadrage,
				"Preparation " + sprintA.getName(), ListType.SPRINT, 2, sprintA);
		Liste inProgress = createListeIfMissing(ctx.listeRepository(), execution,
				"Execution " + sprintA.getName(), ListType.SPRINT, 1, sprintA);
		Liste recette = createListeIfMissing(ctx.listeRepository(), execution, "Recette et UAT", ListType.PHASE, 2, null);
		Liste nextSprint = createListeIfMissing(ctx.listeRepository(), execution,
				"Plan sprint suivant", ListType.SPRINT, 3, sprintB);

		List<User> assigneePool = new ArrayList<>();
		assigneePool.add(owner);
		assigneePool.addAll(team);

		String i0 = pickInitiative(plan.initiatives(), 0);
		String i1 = pickInitiative(plan.initiatives(), 1);
		String i2 = pickInitiative(plan.initiatives(), 2);
		String i3 = pickInitiative(plan.initiatives(), 3);
		String i4 = pickInitiative(plan.initiatives(), 4);
		String i5 = pickInitiative(plan.initiatives(), 5);

		seedTaskScenario(ctx, workspace, space, backlog, null, owner, pickUser(assigneePool, index),
				"Qualifier les besoins - " + i0,
				"Atelier de cadrage metier, estimation initiale et criteres d'acceptation pour " + i0 + ".",
				TaskStatus.TO_DO,
				Priority.HIGH,
				7,
				List.of("analyse", "priorisation", workspace.getSlug()),
				List.of("Consolider le contexte", "Definir la valeur metier"));

		seedTaskScenario(ctx, workspace, space, backlog, null, owner, pickUser(assigneePool, index + 1),
				"Structurer le backlog - " + i1,
				"Regrouper les demandes, clarifier les dependances et preparer la priorisation trimestrielle.",
				TaskStatus.IN_DEV,
				Priority.MEDIUM,
				10,
				List.of("backlog", "gouvernance", workspace.getSlug()),
				List.of("Ranger par impact", "Taguer les risques"));

		seedTaskScenario(ctx, workspace, space, prepSprint, sprintA, owner, pickUser(assigneePool, index + 2),
				"Preparer le sprint actif - " + i2,
				"Finaliser les pre-requis techniques et verrouiller le scope de sprint pour " + space.getName() + ".",
				TaskStatus.IN_TEST,
				Priority.HIGH,
				5,
				List.of("sprint-planning", "coordination", "delivery"),
				List.of("Valider les prerequis", "Partager le plan de charge"));

		seedTaskScenario(ctx, workspace, space, inProgress, sprintA, owner, pickUser(assigneePool, index + 3),
				"Implementer le flux principal - " + i3,
				"Realiser l'implementation principale, brancher les controles qualite et documenter les decisions.",
				TaskStatus.IN_DEV,
				Priority.URGENT,
				4,
				List.of("implementation", "sprint", "critical"),
				List.of("Coder le flux principal", "Ajouter les validations"));

		seedTaskScenario(ctx, workspace, space, inProgress, sprintA, owner, pickUser(assigneePool, index + 4),
				"Synchroniser les integrations - " + i4,
				"Verifier les APIs, stabiliser les interfaces et monitorer les incidents d'integration.",
				TaskStatus.IN_REVIEW,
				Priority.HIGH,
				6,
				List.of("integration", "api", workspace.getSlug()),
				List.of("Verifier les contrats API", "Tracer les ecarts"));

		seedTaskScenario(ctx, workspace, space, recette, null, owner, pickUser(assigneePool, index + 1),
				"Mener la recette utilisateur - " + i5,
				"Executer les scenarios de recette, consolider les retours et preparer la livraison.",
				TaskStatus.DONE,
				Priority.MEDIUM,
				3,
				List.of("recette", "validation", "uat"),
				List.of("Executer le plan de tests", "Publier le bilan de recette"));

		seedTaskScenario(ctx, workspace, space, nextSprint, sprintB, owner, pickUser(assigneePool, index + 2),
				"Planifier la prochaine iteration - " + i0,
				"Construire le prochain lot de valeur et preparer les points de securisation.",
				TaskStatus.TO_DO,
				Priority.LOW,
				14,
				List.of("roadmap", "next-sprint", "projection"),
				List.of("Proposer le perimetre", "Valider la capacite equipe"));
	}

	private void seedTaskScenario(SeedContext ctx,
	                            Workspace workspace,
	                            Space space,
	                            Liste liste,
	                            Sprint sprint,
	                            User owner,
	                            User assignee,
	                            String title,
	                            String description,
	                            TaskStatus status,
	                            Priority priority,
	                            int dueInDays,
	                            List<String> tagNames,
	                            List<String> subtaskTitles) {
		Task task = createTaskIfMissing(
				ctx.taskRepository(),
				liste,
				title,
				description,
				status,
				priority,
				LocalDateTime.now().plusDays(dueInDays),
				sprint,
				assignee
		);

		for (String tagName : tagNames) {
			Tag tag = createTagIfMissing(ctx.tagRepository(), tagName, resolveTagColor(tagName));
			ensureTaskTag(ctx.taskRepository(), task, tag);
		}

		for (int i = 0; i < subtaskTitles.size(); i++) {
			boolean done = status == TaskStatus.DONE && i == 0;
			createSubtaskIfMissing(ctx.subtaskRepository(), task, assignee, subtaskTitles.get(i), done);
		}

		createCommentIfMissing(ctx.commentRepository(), task, owner,
				"Suivi owner: priorite confirmee pour " + title + ".");
		createCommentIfMissing(ctx.commentRepository(), task, assignee,
				"Mise a jour equipe: avancement partage sur " + title + ".");

		String fileSlug = slugify(workspace.getSlug() + "-" + space.getName() + "-" + title);
		createAttachmentIfMissing(
				ctx.attachmentRepository(),
				task,
				fileSlug + ".pdf",
				"https://cdn.pm-b.local/seeds/" + fileSlug + ".pdf",
				"application/pdf",
				256000
		);
	}

	private void seedConversation(SeedContext ctx, User owner, Workspace workspace) {
		Conversation conversation = createConversationIfMissing(
				ctx.conversationRepository(),
				owner,
				"Pilotage hebdo - " + workspace.getName()
		);

		createMessageIfMissing(
				ctx.conversationMessageRepository(),
				conversation,
				"system",
				"Contexte charge: suivi de livraison et risques du workspace " + workspace.getName() + "."
		);

		createMessageIfMissing(
				ctx.conversationMessageRepository(),
				conversation,
				"user",
				"Donne-moi le top 3 des actions critiques a boucler avant la revue de vendredi."
		);

		createMessageIfMissing(
				ctx.conversationMessageRepository(),
				conversation,
				"assistant",
				"Priorite 1: fermer les tickets urgents. Priorite 2: finaliser la recette. Priorite 3: publier le reporting de capacite."
		);
	}

	private void seedNotifications(SeedContext ctx,
	                             User owner,
	                             List<User> team,
	                             Workspace workspace) {
		createNotificationIfMissing(
				ctx.notificationRepository(),
				owner,
				"Workspace " + workspace.getName() + " initialise avec succes.",
				false
		);

		createNotificationIfMissing(
				ctx.notificationRepository(),
				owner,
				"Rappel: revue de sprint planifiee pour " + workspace.getName() + ".",
				false
		);

		for (User teammate : team) {
			createNotificationIfMissing(
					ctx.notificationRepository(),
					teammate,
					"Vous etes membre du workspace " + workspace.getName() + ".",
					false
			);
		}
	}

	private User pickUser(List<User> users, int index) {
		return users.get(Math.floorMod(index, users.size()));
	}

	private String pickInitiative(List<String> initiatives, int index) {
		return initiatives.get(Math.floorMod(index, initiatives.size()));
	}

	private String resolveTagColor(String tagName) {
		String normalized = tagName.toLowerCase(Locale.ROOT);
		if (normalized.contains("urgent") || normalized.contains("critical")) {
			return "#C1121F";
		}
		if (normalized.contains("recette") || normalized.contains("validation") || normalized.contains("uat")) {
			return "#2A9D8F";
		}
		if (normalized.contains("integration") || normalized.contains("api")) {
			return "#4361EE";
		}
		if (normalized.contains("roadmap") || normalized.contains("projection")) {
			return "#F4A261";
		}
		return "#6C757D";
	}

	private String slugify(String value) {
		String lower = value.toLowerCase(Locale.ROOT);
		String cleaned = lower.replaceAll("[^a-z0-9]+", "-");
		return cleaned.replaceAll("(^-+|-+$)", "");
	}

	private List<WorkspaceBlueprint> buildWorkspaceBlueprints() {
		return List.of(
				new WorkspaceBlueprint(
						"Campus Numerique ENSAM",
						"campus-numerique-ensam",
						List.of(
								new SpaceBlueprint(
										"Admission et Inscriptions",
										"#1F7A8C",
										false,
										List.of(
												"formulaire de preinscription",
												"verification des dossiers",
												"relance des candidats",
												"calendrier des entretiens",
												"paiement des frais",
												"confirmation d'admission"
										)
								),
								new SpaceBlueprint(
										"Scolarite et Examens",
										"#2A9D8F",
										false,
										List.of(
												"gestion des absences",
												"planification des controles",
												"publication des notes",
												"traitement des recours",
												"edition des releves",
												"comite pedagogique"
										)
								),
								new SpaceBlueprint(
										"Vie Etudiante",
										"#E76F51",
										false,
										List.of(
												"gestion des clubs",
												"organisation des evenements",
												"partenariats associatifs",
												"accompagnement social",
												"logistique campus",
												"communication interne"
										)
								),
								new SpaceBlueprint(
										"Communication Digitale",
										"#264653",
										true,
										List.of(
												"planning editorial",
												"newsletter hebdomadaire",
												"campagnes reseaux sociaux",
												"charte visuelle",
												"suivi des interactions",
												"rapport de visibilite"
										)
								)
						),
						List.of(
								new SprintBlueprint("Sprint coordination pedagogique", -14, 14,
										"Stabiliser les flux et clarifier les responsabilites", true),
								new SprintBlueprint("Sprint execution des services", 1, 14,
										"Accelerer les delais de traitement et la qualite", false),
								new SprintBlueprint("Sprint amelioration continue", 16, 14,
										"Industrialiser les bonnes pratiques", false)
						)
				),
				new WorkspaceBlueprint(
						"Produit SaaS PM-B",
						"produit-saas-pm-b",
						List.of(
								new SpaceBlueprint(
										"Discovery Produit",
										"#15616D",
										false,
										List.of(
												"interviews utilisateurs",
												"analyse de la retention",
												"priorisation des epics",
												"roadmap trimestrielle",
												"experimentation MVP",
												"definition des KPI"
										)
								),
								new SpaceBlueprint(
										"Build Frontend",
										"#3A86FF",
										false,
										List.of(
												"refonte du dashboard",
												"optimisation des performances",
												"gestion des erreurs UX",
												"accessibilite WCAG",
												"navigation contextuelle",
												"tests E2E"
										)
								),
								new SpaceBlueprint(
										"Build Backend",
										"#4D908E",
										false,
										List.of(
												"hardening des API",
												"gestion des permissions",
												"monitoring des jobs",
												"optimisation SQL",
												"cache applicatif",
												"resilience MFA"
										)
								),
								new SpaceBlueprint(
										"Qualite et Release",
										"#8338EC",
										true,
										List.of(
												"strategie de tests",
												"automatisation regression",
												"plan de release",
												"journal des incidents",
												"post mortem",
												"quality gate CI"
										)
								)
						),
						List.of(
								new SprintBlueprint("Sprint build principal", -10, 14,
										"Livrer les priorites produit sans dette critique", true),
								new SprintBlueprint("Sprint fiabilisation", 5, 14,
										"Renforcer la robustesse avant la release", false),
								new SprintBlueprint("Sprint croissance", 20, 14,
										"Preparar le prochain cycle d'adoption", false)
						)
				),
				new WorkspaceBlueprint(
						"Operations et Support Client",
						"operations-support-client",
						List.of(
								new SpaceBlueprint(
										"Onboarding Clients",
										"#5F0F40",
										false,
										List.of(
												"kickoff client",
												"parametrage initial",
												"formation equipes",
												"validation des acces",
												"go live accompagne",
												"checkpoint J+7"
										)
								),
								new SpaceBlueprint(
										"Support N1-N2",
										"#9A031E",
										false,
										List.of(
												"triage des tickets",
												"escalade N2",
												"SLA hebdomadaire",
												"knowledge base",
												"runbook incidents",
												"suivi de satisfaction"
										)
								),
								new SpaceBlueprint(
										"Data et Reporting",
										"#0F4C5C",
										false,
										List.of(
												"consolidation des KPI",
												"tableau executif",
												"qualite des donnees",
												"alerting operationnel",
												"cohorte churn",
												"rapport mensuel"
										)
								),
								new SpaceBlueprint(
										"Amelioration Continue",
										"#E36414",
										true,
										List.of(
												"retour d'experience",
												"actions correctives",
												"standardisation process",
												"automatisation repetitive",
												"formation interne",
												"audit trimestriel"
										)
								)
						),
						List.of(
								new SprintBlueprint("Sprint stabilisation operationnelle", -12, 14,
										"Faire baisser le volume d'incidents et clarifier les parcours", true),
								new SprintBlueprint("Sprint acceleration support", 3, 14,
										"Ameliorer les temps de resolution et la satisfaction", false),
								new SprintBlueprint("Sprint excellence service", 18, 14,
										"Industrialiser la boucle d'amelioration", false)
						)
				)
		);
	}

	private record SeedContext(
			UserRepository userRepository,
			WorkspaceRepository workspaceRepository,
			WorkspaceMemberRepository workspaceMemberRepository,
			SpaceRepository spaceRepository,
			FolderRepository folderRepository,
			SprintRepository sprintRepository,
			ListeRepository listeRepository,
			TaskRepository taskRepository,
			TagRepository tagRepository,
			SubtaskRepository subtaskRepository,
			CommentRepository commentRepository,
			AttachmentRepository attachmentRepository,
			ConversationRepository conversationRepository,
			ConversationMessageRepository conversationMessageRepository,
			NotificationRepository notificationRepository,
			PasswordEncoder passwordEncoder
	) {}

	private record WorkspaceBlueprint(
			String name,
			String slug,
			List<SpaceBlueprint> spaces,
			List<SprintBlueprint> sprints
	) {}

	private record SpaceBlueprint(
			String name,
			String color,
			boolean privateSpace,
			List<String> initiatives
	) {}

	private record SprintBlueprint(
			String name,
			int startOffsetDays,
			int durationDays,
			String goal,
			boolean active
	) {}
}
