# PM-B Backend

API REST du gestionnaire de projet **PM-B** — une plateforme collaborative de gestion de projets (espaces, listes, tâches, sprints) avec authentification, invitations, notifications et intégration GitHub.

## 🛠️ Stack technique

- **Java 17** + **Spring Boot 4.0**
- **Spring Data JPA** (persistance)
- **Spring Security** (authentification JWT + MFA/OTP)
- **Spring Mail** (envoi d'emails d'invitation)
- **PostgreSQL** (base de données)
- **Maven** (build)

## ✨ Fonctionnalités

- **Workspaces & Espaces** : organisation hiérarchique des projets
- **Listes & Tâches** : gestion type Kanban avec statuts
- **Sprints** : planification et suivi d'itérations
- **Membres & Invitations** : collaboration par email
- **Authentification** : JWT (access + refresh) avec **MFA/OTP**
- **Notifications** : alertes en temps réel
- **Conversations** : discussion par espace
- **Intégration GitHub** : liaison de dépôts (via le service IA)
- **Assistant IA** : génération d'entités et réponses aux questions (via le service IA)

## 🚀 Démarrage rapide

### Prérequis

- JDK 17+
- Maven 3.9+
- PostgreSQL (ou Docker)

### Configuration

Copiez le fichier de configuration et renseignez vos valeurs :

```bash
# Les variables sont lues depuis les variables d'environnement
# (voir PM-B-infra/backend.env.example pour la liste complète)
```

Variables principales :

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | URL JDBC PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur DB |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe DB |
| `JWT_SECRET` | Clé de signature JWT |
| `SPRING_MAIL_USERNAME` | Email SMTP (Gmail) |
| `SPRING_MAIL_PASSWORD` | Mot de passe d'application SMTP |

### Lancer

```bash
./mvnw spring-boot:run
```

L'API est disponible sur `http://localhost:8080`.

## 📁 Structure

```
src/main/java/com/backend/backend/
├── config/        # Configuration (sécurité, JWT, CORS)
├── dao/
│   ├── entities/  # Entités JPA
│   ├── enums/     # Énumérations
│   └── repositories/
├── dto/           # Objets de transfert
├── mapper/        # Mapping entité ↔ DTO
├── service/       # Logique métier
└── web/           # Contrôleurs REST
```

## 🔗 Projets liés

- [PM-B-frontend](https://github.com/jadliaissam-edu/PM-B-frontend) — interface React
- [PM-B-ia](https://github.com/jadliaissam-edu/PM-B-ia) — service d'intelligence artificielle
- [PM-B-infra](https://github.com/jadliaissam-edu/PM-B-infra) — déploiement (Docker, Terraform, Ansible)
