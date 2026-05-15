// ─── FICHIER SUPPRIMÉ — NE PAS UTILISER ──────────────────────────────────────
//
// Ce fichier (GitHubOAuthController) a été désactivé et remplacé.
//
// Raison du nettoyage :
//   L'authentification aux dépôts GitHub via une "GitHub App" (OAuth flow,
//   client_id, client_secret, code exchange) a été intégralement supprimée.
//
// Nouvelle architecture :
//   Les dépôts privés sont maintenant accessibles via un Personal Access Token
//   (PAT) fourni directement par l'utilisateur dans le formulaire de la page IA.
//   Le PAT est chiffré (Fernet AES-128-CBC) par le service Python PM-B-ia
//   avant d'être stocké en base de données. Il n'est jamais stocké en clair.
//
// Fichiers concernés par cette migration :
//   - PM-B-ia/services/encryption_service.py   (chiffrement Fernet)
//   - PM-B-ia/services/database_service.py     (persistance SQLAlchemy)
//   - PM-B-ia/services/github_service.py       (appels API GitHub avec PAT)
//   - PM-B-ia/main.py                          (endpoint POST /api/ia/repos/add)
//   - PM-B-frontend/src/pages/AIPage.tsx       (formulaire Public/Privé + PAT)
//   - PM-B-frontend/src/api/iaApi.tsx          (API client mis à jour)
//
// Pour supprimer ce fichier, exécutez :
//   git rm src/main/java/com/backend/backend/web/GitHubOAuthController.java
// ─────────────────────────────────────────────────────────────────────────────
