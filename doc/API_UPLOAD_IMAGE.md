# API Upload d'Images pour Médicaments

## Description
Ce service web permet d'uploader des images pour les médicaments et de mettre à jour automatiquement l'URL de l'image dans la base de données.

## Endpoints

### 1. Upload d'une image pour un médicament

**URL:** `POST /api/upload/{reference}/image`

**Paramètres:**
- `reference` (path parameter) : La référence du médicament (Integer)
- `file` (form-data) : Le fichier image à uploader

**Types de fichiers acceptés:**
- JPEG, PNG, GIF, et tous les autres formats d'image standards

**Taille maximale:**
- Fichier : 5 MB
- Requête totale : 10 MB

**Exemple de requête avec curl:**
```bash
curl -X POST "http://localhost:8989/api/upload/1/image" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/chemin/vers/image.jpg"
```

**Réponse en cas de succès (200 OK):**
```json
{
  "success": true,
  "message": "Image uploadée avec succès",
  "imageUrl": "/images/med_1_abc123-def456.jpg",
  "medicament": {
    "reference": 1,
    "nom": "Paracétamol 500mg",
    "imageURL": "/images/med_1_abc123-def456.jpg"
  }
}
```

**Réponses d'erreur:**

- **400 Bad Request** - Fichier vide ou type de fichier invalide
```json
{
  "success": false,
  "message": "Le fichier doit être une image (JPEG, PNG, GIF, etc.)"
}
```

- **404 Not Found** - Médicament introuvable
```json
{
  "success": false,
  "message": "Médicament avec référence 999 introuvable"
}
```

- **500 Internal Server Error** - Erreur serveur
```json
{
  "success": false,
  "message": "Erreur lors de la sauvegarde du fichier: ..."
}
```

### 2. Récupérer les informations d'un médicament

**URL:** `GET /api/upload/{reference}`

**Paramètres:**
- `reference` (path parameter) : La référence du médicament (Integer)

**Exemple de requête:**
```bash
curl -X GET "http://localhost:8989/api/upload/1"
```

**Réponse en cas de succès (200 OK):**
```json
{
  "success": true,
  "medicament": {
    "reference": 1,
    "nom": "Paracétamol 500mg",
    "imageURL": "/images/med_1_abc123-def456.jpg",
    "prixUnitaire": 2.50,
    "unitesEnStock": 500,
    "categorie": "Antalgiques et Antipyrétiques"
  }
}
```

## Client HTML

Un client HTML complet est disponible à l'adresse :
```
http://localhost:8989/UploadImageMedicament.html
```

### Fonctionnalités du client :
1. **Chargement des informations du médicament** : Affiche les détails d'un médicament avant l'upload
2. **Upload d'image** : Interface avec drag & drop pour uploader une image
3. **Validation côté client** : Vérifie le type et la taille du fichier avant l'envoi
4. **Gestion des erreurs** : Affiche des messages d'erreur clairs et détaillés
5. **Aperçu de l'image** : Affiche l'image uploadée immédiatement après le succès

### Capture d'écran du client :
Le client offre une interface moderne avec :
- Formulaire de saisie de la référence du médicament
- Sélecteur de fichier avec information sur la taille
- Bouton pour charger les informations du médicament
- Bouton pour uploader l'image
- Affichage des messages de succès/erreur
- Aperçu de l'image actuelle et de la nouvelle image

## Stockage des fichiers

Les images sont stockées dans :
```
/tmp/images/
```

**Format du nom de fichier :**
```
med_{reference}_{uuid}.{extension}
```

Exemple : `med_1_a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg`

## Gestion des erreurs

Le service gère les erreurs suivantes :

1. **Fichier vide** : Vérifie que le fichier uploadé n'est pas vide
2. **Type de fichier invalide** : Vérifie que le fichier est une image
3. **Médicament introuvable** : Vérifie que le médicament existe dans la base
4. **Erreurs d'IO** : Gère les erreurs de lecture/écriture de fichiers
5. **Erreurs de base de données** : Gère les erreurs lors de la mise à jour

Toutes les erreurs sont loguées avec SLF4J pour faciliter le débogage.

## Sécurité

**Points à considérer pour la production :**

1. **Validation du type de fichier** : Vérifier le contenu réel du fichier, pas seulement l'extension
2. **Limitation de taille** : Configurée à 5 MB par fichier
3. **Nettoyage du nom de fichier** : Utilisation d'UUID pour éviter les conflits
4. **Authentification** : Ajouter une authentification pour limiter l'accès
5. **Permissions** : Vérifier que l'utilisateur a le droit de modifier le médicament

## Tests

### Test manuel avec le client HTML :
1. Démarrer l'application : `mvn spring-boot:run`
2. Ouvrir `http://localhost:8989/UploadImageMedicament.html`
3. Entrer une référence de médicament existante (ex: 1)
4. Cliquer sur "Charger les infos du médicament"
5. Sélectionner une image
6. Cliquer sur "Uploader l'image"

### Test avec curl :
```bash
# Upload d'une image
curl -X POST "http://localhost:8989/api/upload/1/image" \
  -F "file=@image.jpg"

# Récupération des infos
curl -X GET "http://localhost:8989/api/upload/1"
```

## Logs

Les opérations sont loguées avec les niveaux suivants :
- **INFO** : Upload réussi, création de répertoire
- **WARN** : Fichier vide, médicament introuvable, type de fichier invalide
- **ERROR** : Erreurs d'IO, erreurs inattendues

Exemple de logs :
```
INFO  ImageUploadController - Image sauvegardée: med_1_abc123.jpg pour le médicament 1
INFO  ImageUploadController - URL de l'image mise à jour pour le médicament 1: /images/med_1_abc123.jpg
```
