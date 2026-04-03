# TP Kubernetes — Questions Docker Build

## Sommaire

1. [Préparation : Créer le service Spring Boot](#1-préparation--créer-le-service-spring-boot)
2. [Cas 1 : Fat-Jar + Dockerfile simple](#2-cas-1--fat-jar--dockerfile-simple)
3. [Cas 2 : Dockerfile Multi-Stage](#3-cas-2--dockerfile-multi-stage)
4. [Intérêt du Multi-Stage Build](#4-intérêt-du-multi-stage-build)

---

## 1. Préparation : Créer le service Spring Boot

### 1.1 Générer le projet

Via [Spring Initializr](https://start.spring.io/) :

- **Group** : `com.tp`
- **Artifact** : `monservice`
- **Dependencies** : Spring Web
- **Build** : Maven
- **Java** : 17
- **Packaging** : Jar

### 1.2 Structure du projet

```
monservice/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/tp/monservice/
│       │       ├── MonserviceApplication.java
│       │       └── MonController.java
│       └── resources/
│           └── application.properties
```

### 1.3 Fichier `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>

    <groupId>com.tp</groupId>
    <artifactId>monservice</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Plugin Spring Boot fat-jar (uber-jar) avec repackage -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <fork>true</fork>
                    <mainClass>${start-class}</mainClass>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

> **Pourquoi cette config fat-jar ?**
> - `repackage` : repackage le jar standard Maven en **fat-jar** (uber-jar) contenant toutes les dépendances + le serveur Tomcat embarqué dans un seul `.jar` exécutable.
> - `fork: true` : lance le build dans un processus JVM séparé, évite les conflits de classloader.
> - `${start-class}` : résolu automatiquement par Spring Boot vers la classe annotée `@SpringBootApplication`.

### 1.4 Fichier `MonserviceApplication.java`

```java
package com.tp.monservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonserviceApplication.class, args);
    }
}
```

### 1.5 Fichier `MonController.java`

```java
package com.tp.monservice;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/monservice")
public class MonController {

    // GET /monservice/echo?nom=Banconle
    @GetMapping("/echo")
    public Map<String, String> echo(@RequestParam String nom) {
        return Map.of("message", "Echo : " + nom);
    }

    // POST /monservice/hello  body: {"nom": "Banconle"}
    @PostMapping("/hello")
    public Map<String, String> hello(@RequestBody Map<String, String> body) {
        String nom = body.getOrDefault("nom", "inconnu");
        return Map.of("message", "Hello " + nom + " !");
    }
}
```

### 1.6 Fichier `application.properties`

```properties
server.port=8080
```

### 1.7 Compiler le fat-jar et tester en local

```bash
# Compiler le fat-jar avec le goal repackage
mvn clean package -DskipTests

# Vérifier que le fat-jar existe (doit faire plusieurs Mo, pas quelques Ko)
ls -lh target/monservice-1.0.0.jar

# Vérifier que c'est bien un fat-jar (doit contenir BOOT-INF/)
jar tf target/monservice-1.0.0.jar | head -20

# Lancer le service
java -jar target/monservice-1.0.0.jar
```

**Tests en local (dans un autre terminal) :**

```bash
# Test GET
curl "http://localhost:8080/monservice/echo?nom=Banconle"
# Réponse attendue : {"message":"Echo : Banconle"}

# Test POST
curl -X POST http://localhost:8080/monservice/hello \
  -H "Content-Type: application/json" \
  -d '{"nom":"Banconle"}'
# Réponse attendue : {"message":"Hello Banconle !"}
```

> 📸 **Capture écran** : `mvn clean package`, `ls -lh target/`, les 2 appels curl + réponses.

---

## 2. Cas 1 : Fat-Jar + Dockerfile simple

> **Principe** : on compile d'abord en local avec Maven, puis on copie le fat-jar dans l'image Docker.

### 2.1 Dockerfile

```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# On copie le fat-jar déjà compilé en local
COPY target/monservice-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2.2 docker-compose.yml

```yaml
version: "3.8"

services:
  monservice:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    container_name: monservice-container
```

### 2.3 Build et lancement

```bash
# Pré-requis : le fat-jar DOIT être compilé avant
mvn clean package -DskipTests

# Build l'image + lancer le conteneur
docker compose up --build -d

# Vérifier que le conteneur tourne
docker ps
```

### 2.4 Tester le service conteneurisé

```bash
# Test GET
curl "http://localhost:8080/monservice/echo?nom=Banconle"

# Test POST
curl -X POST http://localhost:8080/monservice/hello \
  -H "Content-Type: application/json" \
  -d '{"nom":"Banconle"}'
```

> 📸 **Capture écran** : `docker ps` + les 2 appels curl + réponses.

```bash
# Arrêter
docker compose down
```

---

## 3. Cas 2 : Dockerfile Multi-Stage

> **Principe** : on compile les sources directement DANS Docker via un build multi-stage. Aucun Maven, aucun Java requis sur la machine hôte. Seul Docker est nécessaire.

### 3.1 Dockerfile.multistage

```dockerfile
# ===== STAGE 1 : Compilation avec Maven =====
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copier le pom.xml d'abord (optimisation : cache des dépendances Maven)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier les sources et compiler le fat-jar
COPY src ./src
RUN mvn clean package -DskipTests

# ===== STAGE 2 : Image finale légère (JRE uniquement) =====
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Récupérer UNIQUEMENT le fat-jar depuis le stage builder
COPY --from=builder /build/target/monservice-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

> **Explication des 2 stages :**
> - **Stage 1 (builder)** : image lourde avec Maven + JDK complet → compile les sources → génère le fat-jar. Cette image intermédiaire est **jetée** après le build.
> - **Stage 2 (finale)** : image légère avec uniquement le JRE. On copie le fat-jar depuis le stage 1 avec `COPY --from=builder`. C'est **cette seule image** qui sera déployée.

### 3.2 docker-compose.multistage.yml

```yaml
version: "3.8"

services:
  monservice:
    build:
      context: .
      dockerfile: Dockerfile.multistage
    ports:
      - "8080:8080"
    container_name: monservice-multistage
```

### 3.3 Build et lancement (SANS compilation locale)

```bash
# PAS BESOIN de mvn clean package !
# Le multi-stage compile tout dans Docker

docker compose -f docker-compose.multistage.yml up --build -d

# Vérifier
docker ps
```

### 3.4 Tester

```bash
# Test GET
curl "http://localhost:8080/monservice/echo?nom=Banconle"

# Test POST
curl -X POST http://localhost:8080/monservice/hello \
  -H "Content-Type: application/json" \
  -d '{"nom":"Banconle"}'
```

> 📸 **Capture écran** : `docker ps` + les 2 appels curl + réponses.

### 3.5 Comparer la taille des images

```bash
docker images | grep monservice
```

> 📸 **Capture écran** : montrer la taille des 2 images.

```bash
# Arrêter
docker compose -f docker-compose.multistage.yml down
```

---

## 4. Intérêt du Multi-Stage Build

| Critère | Dockerfile simple | Multi-Stage |
|---|---|---|
| Compilation | En local (Maven + JDK requis) | Dans Docker (juste Docker suffit) |
| Image finale | JRE + jar uniquement | JRE + jar uniquement (stage de build jeté) |
| Reproductibilité | Dépend de l'env local (version Maven, JDK…) | 100% reproductible partout |
| CI/CD | Maven + JDK requis sur le runner | Juste Docker suffit |
| Sécurité | — | Code source + outils de build absents de l'image finale |
| Portabilité | Il faut transmettre le jar compilé | Il suffit de transmettre les sources + Dockerfile |

**En résumé** : le multi-stage build permet de **compiler ET packager en une seule commande** `docker build`, sans aucune dépendance locale. L'image finale reste légère car seul le dernier stage est conservé. C'est la méthode standard en production et en CI/CD.

---

## Récap des fichiers à fournir

```
monservice/
├── pom.xml
├── src/main/java/com/tp/monservice/
│   ├── MonserviceApplication.java
│   └── MonController.java
├── src/main/resources/
│   └── application.properties
├── Dockerfile                          # Cas 1 (fat-jar compilé en local)
├── Dockerfile.multistage               # Cas 2 (multi-stage, compilation dans Docker)
├── docker-compose.yml                  # Cas 1
└── docker-compose.multistage.yml       # Cas 2
```