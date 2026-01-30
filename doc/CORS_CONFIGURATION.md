# Configuration CORS - Documentation

## Vue d'ensemble

L'application est maintenant configurée pour autoriser les requêtes **Cross-Origin Resource Sharing (CORS)** depuis n'importe quelle origine. Cela permet aux applications web hébergées sur d'autres domaines d'accéder à l'API.

## Niveaux de configuration CORS

### 1. Configuration globale (CorsConfig.java)

**Fichier:** `src/main/java/pharmacie/config/CorsConfig.java`

Cette configuration s'applique à **tous les endpoints** de l'application :

- **Endpoints couverts:** `/**` (tous)
- **Origines autorisées:** `*` (toutes)
- **Méthodes HTTP:** GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD
- **Headers:** Tous autorisés (`*`)
- **Credentials:** Autorisés (cookies, authorization headers)
- **Max Age:** 3600 secondes (1 heure)

**Deux mécanismes implémentés:**

1. **WebMvcConfigurer** - Pour les contrôleurs Spring MVC/REST classiques
2. **CorsFilter** - Filtre global qui s'applique en premier, couvre Spring Data REST

### 2. Configuration Spring Data REST (SpringDataRestConfig.java)

**Fichier:** `src/main/java/pharmacie/config/SpringDataRestConfig.java`

Configuration spécifique pour l'API auto-générée par Spring Data REST :

- **Endpoints couverts:** `/**` (tous les endpoints de l'API JPA)
- **Origines autorisées:** `*` (toutes)
- **Méthodes HTTP:** GET, PUT, POST, PATCH, DELETE, OPTIONS, HEAD
- **Headers:** Tous autorisés (`*`)
- **Credentials:** Autorisés
- **Max Age:** 3600 secondes

### 3. Annotations sur les contrôleurs

**Fichiers modifiés:**
- `ImageUploadController.java`
- `SimpleRestController.java`
- `StatisticsRestController.java`
- `CommandeController.java`

Chaque contrôleur REST possède l'annotation `@CrossOrigin` :

```java
@CrossOrigin(origins = "*", allowedHeaders = "*")
```

Cette annotation fournit une **couverture supplémentaire** au niveau du contrôleur.

## Endpoints couverts

### API JPA Auto-générée (Spring Data REST)
```
http://localhost:8989/api/
├── categories
├── medicaments
├── dispensaires
├── commandes
└── lignes
```

### API REST personnalisée
```
http://localhost:8989/api/
├── simple/*               (SimpleRestController)
├── stats/*                (StatisticsRestController)
├── services/commandes/*   (CommandeController)
└── medicaments/*          (ImageUploadController)
```

### Tous les autres endpoints
- Swagger UI: `/swagger-ui.html`
- API Docs: `/v3/api-docs`
- H2 Console: `/h2-console`
- Static resources: `/images/*`, `/css/*`, `/js/*`, etc.

## Configuration des requêtes autorisées

### Origines (Origins)
```
✅ http://localhost:3000
✅ http://127.0.0.1:3000
✅ https://monsite.com
✅ https://example.org
✅ Toutes les autres origines
```

### Méthodes HTTP
```
✅ GET     - Récupération de ressources
✅ POST    - Création de ressources
✅ PUT     - Mise à jour complète
✅ PATCH   - Mise à jour partielle
✅ DELETE  - Suppression
✅ OPTIONS - Requête preflight
✅ HEAD    - Métadonnées uniquement
```

### Headers autorisés
```
✅ Content-Type
✅ Authorization
✅ X-Requested-With
✅ Accept
✅ Origin
✅ Access-Control-Request-Method
✅ Access-Control-Request-Headers
✅ Tous les autres headers personnalisés
```

### Credentials
```
✅ Cookies
✅ HTTP Authentication
✅ Client-side SSL certificates
```

## Tester CORS

### Test 1: Depuis une autre origine (navigateur)

Créez un fichier HTML sur un serveur différent ou utilisez un domaine différent :

```html
<!DOCTYPE html>
<html>
<head>
    <title>Test CORS</title>
</head>
<body>
    <h1>Test CORS API</h1>
    <button onclick="testCORS()">Test GET Categories</button>
    <div id="result"></div>

    <script>
        async function testCORS() {
            try {
                const response = await fetch('http://localhost:8989/api/categories');
                const data = await response.json();
                document.getElementById('result').innerHTML =
                    '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
            } catch (error) {
                document.getElementById('result').innerHTML =
                    '<p style="color: red;">Erreur: ' + error.message + '</p>';
            }
        }
    </script>
</body>
</html>
```

### Test 2: Avec curl (inclure headers CORS)

```bash
# Test simple GET
curl -X GET "http://localhost:8989/api/categories" \
  -H "Origin: http://example.com" \
  -v

# Vous devriez voir dans la réponse:
# Access-Control-Allow-Origin: *
# Access-Control-Allow-Credentials: true
```

### Test 3: Requête preflight (OPTIONS)

```bash
# Test preflight avant POST
curl -X OPTIONS "http://localhost:8989/api/upload/1/image" \
  -H "Origin: http://example.com" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v

# Vous devriez voir:
# Access-Control-Allow-Origin: *
# Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD
# Access-Control-Allow-Headers: *
# Access-Control-Max-Age: 3600
```

### Test 4: Depuis React/Vue/Angular

```javascript
// React/Vue/Angular - Fetch API
fetch('http://localhost:8989/api/categories', {
    method: 'GET',
    headers: {
        'Content-Type': 'application/json',
    },
    credentials: 'include' // Si vous utilisez des cookies
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Erreur CORS:', error));
```

```javascript
// Axios
axios.get('http://localhost:8989/api/categories', {
    withCredentials: true // Si vous utilisez des cookies
})
.then(response => console.log(response.data))
.catch(error => console.error('Erreur CORS:', error));
```

## Vérification dans les DevTools du navigateur

### Console du navigateur
Si CORS est mal configuré, vous verrez :
```
Access to fetch at 'http://localhost:8989/api/categories'
from origin 'http://localhost:3000' has been blocked by CORS policy:
No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

Avec notre configuration, les requêtes devraient **fonctionner** sans erreur.

### Onglet Network
Dans l'onglet Network des DevTools :

1. Sélectionnez la requête
2. Allez dans l'onglet "Headers"
3. Vérifiez les "Response Headers" :

```
Access-Control-Allow-Origin: *
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD
Access-Control-Allow-Headers: *
Access-Control-Expose-Headers: *
Access-Control-Max-Age: 3600
```

## Sécurité en production

⚠️ **IMPORTANT** : La configuration actuelle (`origins = "*"`) est permissive et adaptée au développement.

### Pour la production, il est recommandé de :

1. **Limiter les origines autorisées** :
```java
config.setAllowedOrigins(Arrays.asList(
    "https://monapp.com",
    "https://www.monapp.com",
    "https://mobile.monapp.com"
));
```

2. **Désactiver credentials si non nécessaire** :
```java
config.setAllowCredentials(false);
```

3. **Limiter les méthodes HTTP** :
```java
config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
```

4. **Limiter les headers exposés** :
```java
config.setExposedHeaders(Arrays.asList("X-Custom-Header", "Authorization"));
```

5. **Ajouter un profil Spring pour différencier dev/prod** :
```java
@Profile("dev")
public class DevCorsConfig implements WebMvcConfigurer {
    // Configuration permissive pour le développement
}

@Profile("prod")
public class ProdCorsConfig implements WebMvcConfigurer {
    // Configuration restrictive pour la production
}
```

## Résumé

✅ **CORS activé** sur tous les endpoints
✅ **Toutes les origines** autorisées (`*`)
✅ **Toutes les méthodes HTTP** autorisées
✅ **Tous les headers** autorisés
✅ **Credentials** autorisés (cookies, auth)
✅ **API JPA** couverte
✅ **API REST personnalisée** couverte
✅ **Cache preflight** de 1 heure

L'application est maintenant accessible depuis n'importe quelle application web externe !

## Fichiers de configuration

```
src/main/java/pharmacie/config/
├── CorsConfig.java              ← Configuration CORS globale
├── SpringDataRestConfig.java    ← Configuration CORS pour API JPA
└── ...

src/main/java/pharmacie/rest/
├── ImageUploadController.java   ← @CrossOrigin
├── SimpleRestController.java    ← @CrossOrigin
├── StatisticsRestController.java ← @CrossOrigin
└── CommandeController.java      ← @CrossOrigin
```
