# Plan de fusion FMessageBungee + FMessageVelocity → module unique

> Document d'architecture — analyse de faisabilité et plan de transformation.
> Objectif : regrouper les modules Bungee et Velocity en un seul module produisant
> un jar unique chargeable sur les deux plateformes, afin d'éliminer la duplication
> de code.

---

## Sommaire

1. [Constat chiffré](#1-constat-chiffré)
2. [Faisabilité : les 3 verrous, vérifiés](#2-faisabilité--les-3-verrous-vérifiés)
3. [Architecture cible](#3-architecture-cible)
4. [L'API d'abstraction](#4-lapi-dabstraction)
5. [Modifications de code, classe par classe](#5-modifications-de-code-classe-par-classe)
6. [Points durs](#6-points-durs)
7. [Build et packaging](#7-build-et-packaging)
8. [Plan de migration — 8 lots](#8-plan-de-migration--8-lots)
9. [Tests unitaires](#9-tests-unitaires)
10. [Estimation et recommandations](#10-estimation-et-recommandations)
11. [Arbitrages en attente](#11-arbitrages-en-attente)

---

## 1. Constat chiffré

| Module | Lignes Java | Fichiers |
|---|---|---|
| FMessageBungee | 1 642 | 18 |
| FMessageVelocity | 1 690 | 18 |
| FMessageCommon | 1 787 | 17 |

Les 18 fichiers sont **strictement les mêmes noms de classes dans les mêmes packages**.
Écart réel mesuré (`diff -w`) :

| Fichier | Lignes | Lignes divergentes |
|---|---|---|
| `MessageKeys.java` | 82 | **0** |
| `FileUtils.java` | 64 | 2 |
| `ReloadCommand` / `MSGToggleCommand` / `NickCommand` / `MSGSpyCommand` | ~55 | 4 |
| `RCommand` | 63 | 6 |
| `MSGCommand` | 60 | 12 |
| `UnIgnoreCommand` / `IgnoreCommand` | ~85 | 13–15 |
| `CommandManager` / `CommandCompletionsManager` | ~90 | 23–24 |
| `GroupCommand` | 232 | 35 |
| `MessageListener` | 140 | 40 |
| `MessageManager` | 129 | 59 |
| `StringUtils` / `FormatUtil` | ~24 | 18–22 (réécriture complète) |
| `FMessage.java` | 264 | 101 |

Les 4 fichiers de ressources (`config.yml`, `lang_en.yml`, `lang_fr.yml`, `database.yml`)
sont **identiques octet pour octet**.

**Traduction** : ~88 % du code proxy est de la duplication pure, et les 12 % restants sont
quasi exclusivement des substitutions de types (`ProxiedPlayer` ↔ `Player`,
`String` ↔ `TextComponent`, `null` ↔ `Optional`).

> **Il n'existe aucun test dans le projet aujourd'hui** (0 fichier sous `src/test`).
> C'est le principal risque de cette migration, et aussi la meilleure occasion d'en poser.

---

## 2. Faisabilité : les 3 verrous, vérifiés

Trois points peuvent tuer ce projet. Ils ont été vérifiés avant toute proposition.

### Verrou 1 — Un jar unique peut-il être chargé par les deux plateformes ? ✅ Oui

- **BungeeCord** lit `plugin.yml` → charge la classe `main:` qui `extends net.md_5.bungee.api.plugin.Plugin`.
- **Velocity** lit `velocity-plugin.json` (généré à la compilation par l'annotation processor
  de `velocity-api` à partir de `@Plugin`) → instancie la classe via Guice.

Les deux descripteurs ont des **noms de fichiers différents** et cohabitent sans conflit dans
le même jar. Le chargement de classes Java étant paresseux, `VelocityBootstrap` n'est jamais
chargée sur Bungee (et inversement) **tant qu'aucune classe du cœur ne la référence**.

> C'est l'invariant central de l'architecture — il devra être garanti par un test (§9.6).

### Verrou 2 — ACF : `acf-bungee` et `acf-velocity` sont-ils compatibles dans le même jar ? ✅ Oui (vérifié)

C'est le vrai point dur, et il est levé. Comparaison des deux jars `0.5.1-SNAPSHOT` du
dépôt local :

```
acf-bungee   : 140 classes
acf-velocity : 139 classes
communes     : 125 classes  (= acf-core)
   → 124 avec CRC strictement identique
   → 1 (MinecraftMessageKeys) CRC différent mais bytecode
     sémantiquement identique (diff javap vide → artefact de constant pool)
spécifiques  : 15 côté Bungee (BungeeCommandManager, BungeeLocales…)
               14 côté Velocity (VelocityCommandManager, VelocityLocales…)
```

**Conclusion** : on peut déclarer les deux dépendances dans le même module et shader les deux.
Le shade fusionne les 125 classes communes en une seule copie (identiques) et conserve les
29 classes spécifiques. À l'exécution, seule `BungeeCommandManager` **ou**
`VelocityCommandManager` est chargée.

Trois API ACF sont portées par la classe de base commune `co.aikar.commands.CommandManager`,
donc **partageables** :

```java
registerCommand(BaseCommand)          registerDependency(Class<T>, T)
getCommandIssuer(Object)              getLocales() / addSupportedLanguage(Locale)
```

Deux ne le sont pas et resteront dans les adaptateurs :

```java
setFormat(MessageType, FT... colors)              // FT = ChatColor vs NamedTextColor
getCommandCompletions() → CommandCompletions<?>   // générique wildcard sur la base
```

Et surtout : **`co.aikar.commands.CommandIssuer` est une classe commune** et expose tout ce
dont les commandes ont besoin :

```java
UUID getUniqueId();   boolean isPlayer();   boolean hasPermission(String);
void sendInfo(MessageKeyProvider, String...);
```

→ **Les 9 classes de commandes peuvent devenir 100 % partagées** en prenant `CommandIssuer`
en premier paramètre au lieu de `ProxiedPlayer` / `Player`.

### Verrou 3 — bStats ? ✅ Aucun conflit

`bstats-bungeecord` et `bstats-velocity` ont **zéro classe en commun**
(`org.bstats.bungeecord.Metrics` vs `org.bstats.velocity.Metrics`, le tronc `bstats-base`
étant une dépendance séparée).

---

## 3. Architecture cible

```
FMessage (pom parent)
├── FMessageCommon          [inchangé]  DB, config, objets, queries, commandManagers métier
├── FMessageProxy           [NOUVEAU]   remplace FMessageBungee + FMessageVelocity
│                                       → produit UN jar : FMessage-Proxy-3.0.0.jar
└── FMessageBukkit          [inchangé]  côté serveur de jeu
```

Organisation interne de `FMessageProxy` — **un seul module Maven, quatre zones de
responsabilité** :

```
FMessageProxy/src/main/java/fr/florianpal/fmessage/
│
├── platform/                  ← contrat d'abstraction (0 dépendance plateforme)
│   ├── ProxyPlatform.java
│   ├── ProxyPlayer.java
│   ├── ProxyBackendServer.java
│   └── ProxyLogger.java
│
├── core/                      ← 100 % du code partagé
│   │                            0 import net.md_5 / com.velocitypowered / net.kyori
│   ├── FMessageCore.java              (ex-FMessage.java, sans le cycle de vie plateforme)
│   ├── commands/                      (les 9 commandes, signature CommandIssuer)
│   ├── service/
│   │   ├── PrivateMessageService.java (ex-MessageManager)
│   │   ├── ChatRelayService.java      (ex-MessageListener, logique pure)
│   │   └── CompletionProviders.java   (logique des complétions, retourne List<String>)
│   ├── protocol/
│   │   ├── ChatPacket.java            (encodage/décodage des trames plugin message)
│   │   └── StaffPacket.java
│   ├── text/
│   │   ├── LegacyFormatter.java       (ex-FormatUtil, String → String)
│   │   └── MessageTemplate.java       (ex-StringUtils)
│   ├── languages/MessageKeys.java
│   └── AcfLanguageLoader.java         (le gros de l'ex-CommandManager, partagé)
│
├── bungee/                    ← adaptateur mince
│   ├── BungeeBootstrap.java           (extends Plugin, point d'entrée plugin.yml)
│   ├── BungeePlatform.java            implements ProxyPlatform
│   ├── BungeePlayer.java              implements ProxyPlayer
│   ├── BungeeCommandManagerImpl.java  extends BungeeCommandManager
│   ├── BungeeCompletionsRegistrar.java
│   └── BungeeEventBridge.java         (@EventHandler → ChatRelayService)
│
└── velocity/                  ← adaptateur mince, symétrique
    ├── VelocityBootstrap.java         (@Plugin, @Inject, @Subscribe)
    ├── VelocityPlatform.java
    ├── VelocityPlayer.java
    ├── VelocityCommandManagerImpl.java
    ├── VelocityCompletionsRegistrar.java
    └── VelocityEventBridge.java

FMessageProxy/src/main/resources/
├── plugin.yml                 (descripteur Bungee)
├── config.yml, database.yml, lang_en.yml, lang_fr.yml
└── velocity-plugin.json       (généré automatiquement par l'AP velocity-api)
```

**Volumétrie visée** : ~1 500 lignes de `core` + `platform`, ~250 lignes par adaptateur
≈ **2 000 lignes au lieu de 3 332**.

> Le gain réel n'est pas le nombre de lignes : c'est qu'une évolution fonctionnelle se fait
> désormais à **un seul endroit** au lieu de deux.

---

## 4. L'API d'abstraction

Le point de conception le plus important. Volontairement minimale : on n'abstrait que ce que
le cœur consomme réellement.

```java
package fr.florianpal.fmessage.platform;

public interface ProxyPlatform {
    Optional<ProxyPlayer> getPlayer(UUID uuid);
    Optional<ProxyPlayer> getPlayer(String name);
    Collection<ProxyPlayer> getOnlinePlayers();
    Collection<ProxyBackendServer> getServers();

    File getDataFolder();
    InputStream getResource(String name);
    ProxyLogger getLogger();
}

public interface ProxyPlayer {
    UUID getUniqueId();
    String getName();
    boolean hasPermission(String node);

    /** Texte au format legacy '&' ; l'adaptateur fait la conversion finale. */
    void sendMessage(String legacyText);

    void playSound(String soundKey);
    Optional<ProxyBackendServer> getCurrentServer();
}

public interface ProxyBackendServer {
    String getName();
    void sendData(byte[] payload);   // toujours sur le canal fmessage:chatbukkit
}

public interface ProxyLogger {
    void info(String msg);
    void warn(String msg);
    void error(String msg, Throwable t);
}
```

### Décision structurante : le cœur manipule des `String` legacy `&`, pas des `Component`

C'est le choix qui débloque tout le reste.

**Contre l'alternative « Adventure partout »** : BungeeCord ne fournit pas
`net.kyori.adventure`. Il faudrait la shader — mais Velocity la fournit déjà via son
classloader d'API. Un `Component` shadé passé à `Player.sendMessage()` sur Velocity produit
un **`ClassCastException`** (classes homonymes, classloaders différents). Relocaliser ne
résout rien : Velocity attend *ses* classes. `adventure-platform-bungeecord` existe mais
ajoute ~400 Ko et une dépendance de plus à faire vivre.

**Le choix retenu** : le cœur produit du `String` legacy (ce que Bungee fait déjà
aujourd'hui). La conversion en composant a lieu **dans l'adaptateur, au dernier moment** :

```java
// BungeePlayer
public void sendMessage(String legacyText) {
    handle.sendMessage(TextComponent.fromLegacyText(legacyText));
}

// VelocityPlayer — attention au support hex, cf. §6.1
private static final LegacyComponentSerializer SERIALIZER =
    LegacyComponentSerializer.builder()
        .character(LegacyComponentSerializer.SECTION_CHAR)
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();

public void sendMessage(String legacyText) {
    handle.sendMessage(SERIALIZER.deserialize(legacyText));
}
```

**Bénéfice collatéral** : `StringUtils.replace()` redevient un simple `String.replace()`, et
la version Velocity actuelle (construction de `TextReplacementConfig`, cast `(TextComponent)`
non sûr) disparaît.

---

## 5. Modifications de code, classe par classe

### 5.1 `FMessage.java` → `core/FMessageCore.java` + 2 bootstraps

Le plus gros morceau (264 lignes, 101 divergentes). En réalité, **~160 lignes sont des
getters strictement identiques**.

Découpage :

- **`FMessageCore`** reçoit un `ProxyPlatform` au constructeur, porte tout l'état
  (`groups`, `ignores`, `playerMessage`, `playerSpy`, `messagesDisabled`, `playerStaff`),
  tous les managers et queries, et expose `start()` / `shutdown()`.
- **`BungeeBootstrap extends Plugin`** : `onEnable()` → `new FMessageCore(new BungeePlatform(this))`,
  enregistrement des canaux, du listener, de bStats bungee.
- **`VelocityBootstrap`** (annoté `@Plugin`) : `@Inject` du `ProxyServer` / `Logger` / `Path` /
  `Metrics.Factory`, `@Subscribe ProxyInitializeEvent` → même séquence.

> **Correction à faire au passage** : `playerSpy`, `messagesDisabled` et `playerStaff` sont
> des `ArrayList` mutées depuis des threads réseau → passer en `Set` concurrents
> (`ConcurrentHashMap.newKeySet()`), et `playerMessage` en `ConcurrentHashMap`.
> C'est un bug de concurrence latent, présent à l'identique dans les deux modules actuels.

### 5.2 Les 9 commandes → 100 % partagées

Transformation type, sur `MSGCommand` :

```java
// AVANT (×2, une fois par plateforme)
public void onMSG(ProxiedPlayer playerSender, String targetName, String message) {
    ProxiedPlayer target = plugin.getProxy().getPlayer(targetName);
    if (target == null) { issuer.sendInfo(MessageKeys.PLAYER_OFFLINE); return; }
    ...
}

// APRÈS (×1)
public void onMSG(CommandIssuer issuer, String targetName, String message) {
    ProxyPlayer sender = core.requirePlayer(issuer);
    Optional<ProxyPlayer> target = core.getPlatform().getPlayer(targetName);
    if (target.isEmpty()) { issuer.sendInfo(MessageKeys.PLAYER_OFFLINE); return; }
    messageService.send(sender, target.get(), message);
}
```

`@CommandAlias`, `@Subcommand`, `@CommandPermission`, `@CommandCompletion` et `@Syntax`
vivent dans `co.aikar.commands.annotation` (classes communes) → **aucune modification**.
`MessageKeys.java` (82 lignes, 0 divergence) est déplacé tel quel.

`GroupCommand` (232 lignes) est le plus gros chantier ; il concentre l'essentiel des
35 divergences dans les résolutions de joueurs — exactement ce que l'abstraction absorbe.

### 5.3 `MessageManager` → `core/service/PrivateMessageService`

Après le passage en `String` legacy, le corps devient identique aux deux versions actuelles.

Seule la notification sonore diverge réellement : Velocity a `playSound` nativement, Bungee
doit passer par un plugin message vers le serveur backend. **C'est exactement le rôle de
`ProxyPlayer.playSound(String)`**, implémenté différemment dans les deux adaptateurs.

### 5.4 `MessageListener` → `protocol/ChatPacket` + `service/ChatRelayService` + 2 ponts

Découpage en trois :

1. **`ChatPacket`** — encodage/décodage des trames `ByteStreams`, pur, sans plateforme.
   C'est le contrat binaire avec `FMessageBukkit`, **qui n'est pas modifié** → à sécuriser
   par des tests d'octets (§9.4).
2. **`ChatRelayService`** — les trois branches métier (groupe / staff / chat public), écrite
   contre `ProxyPlatform`.
3. **Ponts d'événements** — 15 lignes par plateforme :

```java
// Bungee
@EventHandler public void onMessage(PluginMessageEvent e) {
    if (e.getTag().equalsIgnoreCase(BUNGEE_CHAT)) relay.handle(e.getData());
}

// Velocity
@Subscribe public void onMessage(PluginMessageEvent e) {
    if (e.getIdentifier().equals(BUNGEE_CHAT_ID)) relay.handle(e.getData());
}
```

> La constante de canal diffère de type (`String` vs `MinecraftChannelIdentifier`). Le cœur
> garde la `String` `"fmessage:chatbungee"` ; chaque adaptateur construit son identifiant.

### 5.5 `CommandManager` → `AcfLanguageLoader` + 2 sous-classes minces

Sur 92 lignes, ~65 sont le chargement du fichier de langue YAML →
`AcfLanguageLoader.load(CommandManager acf, File dataFolder, InputStream defaults, String fileName, Locale locale)`,
partagé (n'utilise que `getLocales()` / `addSupportedLanguage()`, présents sur la base commune).

Il reste **~12 lignes par plateforme** : le `setFormat` avec ses couleurs typées, et le
`super(...)` dont la signature diffère (`super(plugin)` vs `super(proxyServer, plugin)`).

### 5.6 `CommandCompletionsManager` → `CompletionProviders` + 2 registrars

`getCommandCompletions()` retourne `CommandCompletions<?>` sur la classe de base : le
wildcard interdit d'appeler `registerAsyncCompletion` de façon générique.

**Duplication résiduelle assumée : ~25 lignes par plateforme.** La logique (filtrage des
joueurs, listes de groupes, listes de membres) part dans `CompletionProviders`, testable ;
le registrar ne fait que brancher.

### 5.7 `FormatUtil` / `StringUtils` → `core/text/`

- **`LegacyFormatter.format(String) → String`** : reprend la version Bungee (regex
  `{#rrggbb}` + `translateAlternateColorCodes`), mais réimplémentée **sans
  `net.md_5.bungee.api.ChatColor`** — c'est une dizaine de lignes de manipulation de `String`,
  aucune raison de dépendre de Bungee.
- **`MessageTemplate.replace(String, String, String, boolean)`** : `String.replace()`
  conditionnel.

Ces deux classes deviennent du code pur, sans I/O ni plateforme → **les plus faciles à
couvrir par des tests**.

### 5.8 `FileUtils` (2 lignes divergentes)

Passe dans `core`, la seule divergence (`logger.severe` vs `logger.error`) est absorbée par
`ProxyLogger.error()`.

---

## 6. Points durs

### 6.1 ⚠️ Couleurs hexadécimales — divergence fonctionnelle réelle

Aujourd'hui, `{#rrggbb}` **fonctionne sur Bungee** (`ChatColor.of()` → `§x§r§r§g§g§b§b`) et
**ne fonctionne pas sur Velocity** (`legacyAmpersand()` ne connaît que `&0-&f`, et n'a pas
`.hexColors()` activé).

La fusion va donc **corriger un bug côté Velocity** — souhaitable, mais c'est un changement
de comportement à annoncer. Il impose de construire le `LegacyComponentSerializer` Velocity
avec `.hexColors().useUnusualXRepeatedCharacterHexFormat()` (cf. §4).

> **À couvrir par un test dédié** : c'est le piège le plus facile à rater.

### 6.2 Ordre de remplacement des placeholders

Velocity remplace aujourd'hui *dans l'arbre de composants* (`replaceText`), Bungee *dans la
chaîne* (`String.replace`). Comportements subtilement différents quand un pseudo ou un
message contient lui-même du texte ressemblant à un placeholder.

Le passage au tout-`String` **aligne Velocity sur Bungee**. Acceptable, mais à tester
explicitement (§9.2).

### 6.3 ⚠️ Nom du dossier de données — impact opérationnel

- **Bungee** : dossier dérivé de `name:` dans `plugin.yml` → aujourd'hui `plugins/FMessageBungee/`
- **Velocity** : dérivé de l'`id` du `@Plugin` → aujourd'hui `plugins/fmessage/`

> **Recommandation : ne pas toucher à ces deux valeurs** (`name: FMessageBungee`,
> `id = "fmessage"`), même si le module s'appelle désormais `FMessageProxy`. Sinon les
> configurations et fichiers de langue des installations existantes deviennent invisibles au
> redémarrage. Le confort cosmétique ne vaut pas un ticket de support par utilisateur.

### 6.4 Conflits de dépendances transitives à la compilation

`bungeecord-api` et `velocity-api` apportent tous deux Guava et Gson. Les deux étant en
`provided`, il n'y a pas de risque au packaging, mais Maven résout une seule version au
compile. À vérifier avec `mvn dependency:tree` et, si besoin, à fixer par
`<dependencyManagement>` dans le pom parent.

### 6.5 Cohérence de version ACF

La fusion repose sur le fait qu'`acf-bungee` et `acf-velocity` sont **construits depuis le
même commit** (aujourd'hui : même timestamp `20260511.221425`). En SNAPSHOT, ce n'est pas
garanti dans le temps.

**Mitigation** : un test de packaging qui échoue si le shade signale des classes dupliquées
non identiques (§9.7). À moyen terme, envisager d'épingler ACF sur une release fixe.

---

## 7. Build et packaging

`FMessageProxy/pom.xml`, points clés :

```xml
<dependencies>
  <!-- Les deux APIs de plateforme : provided, jamais embarquées -->
  <dependency> net.md-5:bungeecord-api:1.20-R0.2               scope=provided </dependency>
  <dependency> com.velocitypowered:velocity-api:3.5.0-SNAPSHOT scope=provided </dependency>

  <!-- Les deux flavors ACF : compile, shadées et fusionnées (cf. §2 verrou 2) -->
  <dependency> co.aikar:acf-bungee:0.5.1-SNAPSHOT </dependency>
  <dependency> co.aikar:acf-velocity:0.5.1-SNAPSHOT </dependency>

  <!-- bStats : aucun conflit -->
  <dependency> org.bstats:bstats-bungeecord:3.2.1 </dependency>
  <dependency> org.bstats:bstats-velocity:3.2.1 </dependency>

  <dependency> fr.florianpal:FMessageCommon </dependency>
  <!-- + mariadb, HikariCP, boosted-yaml (inchangés) -->
</dependencies>
```

**Relocalisations shade** : strictement les mêmes qu'aujourd'hui
(`co.aikar.commands` → `fr.florianpal.fmessage.acf`, etc.).
Ne **jamais** relocaliser `net.kyori` ni `net.md_5`.

Deux corrections à faire au passage, indépendantes de la fusion mais à ne pas rater :

- `maven-compiler-plugin` est configuré en **source/target 16** dans les trois poms, alors que
  `claude.md` annonce Java 21 → aligner sur `<release>21</release>`.
- Ajouter `maven-surefire-plugin` 3.x (**absent**) — sans lui, aucun test ne s'exécutera.

**Version** : passer en `3.0.0`. Le changement de nom de jar est cassant pour les utilisateurs
(il faut supprimer l'ancien jar avant d'installer le nouveau, sous peine de double chargement).
À documenter dans le README et la note de release.

---

## 8. Plan de migration — 8 lots

Chaque lot est compilable et livrable. Les lots 1 à 4 laissent les modules Bungee et Velocity
existants **intacts et fonctionnels**, ce qui permet d'arrêter ou de revenir en arrière à tout
moment.

| # | Lot | Contenu | Sortie |
|---|---|---|---|
| **1** | Socle de test | Ajout surefire + JUnit 5 + Mockito + AssertJ + ArchUnit ; passage `release=21` ; premiers tests sur `FMessageCommon` (queries, config) | CI verte, filet de sécurité posé |
| **2** | Création du module | `FMessageProxy` vide + pom + `platform/` (4 interfaces) + ArchUnit qui interdit déjà les imports plateforme dans `core/` | Module qui compile |
| **3** | Code pur | `text/` (`LegacyFormatter`, `MessageTemplate`), `protocol/` (`ChatPacket`), `MessageKeys`, `FileUtils` **+ leurs tests** | Le socle testé à 100 % |
| **4** | Services | `PrivateMessageService`, `ChatRelayService`, `CompletionProviders`, `AcfLanguageLoader` + tests avec `FakeProxyPlatform` | Toute la logique métier testée hors serveur |
| **5** | Commandes | Les 9 commandes en version `CommandIssuer` + tests | Cœur complet |
| **6** | Adaptateur Bungee | `BungeeBootstrap`, `BungeePlatform`, `BungeePlayer`, managers, `plugin.yml` | **Jar testable sur un vrai BungeeCord** |
| **7** | Adaptateur Velocity | Symétrique + `@Plugin` | **Jar unique testé sur les deux plateformes** |
| **8** | Nettoyage | Suppression de `FMessageBungee` et `FMessageVelocity`, mise à jour du pom parent, du README, du workflow GitHub Actions | Livraison |

> **Point de non-retour : le lot 8.** Tant qu'il n'est pas franchi, les anciens modules restent
> disponibles.

### Validation manuelle indispensable avant le lot 8

Les tests unitaires ne couvriront jamais ceci :

- Chargement effectif sur un BungeeCord réel **et** un Velocity réel, avec le **même fichier jar**.
- Un cycle complet `/msg` inter-serveurs avec `FMessageBukkit` **non modifié** de part et d'autre.
- Rechargement `/fmessage reload` sur les deux plateformes.

---

## 9. Tests unitaires

C'est la partie qui rend la migration sûre. Aujourd'hui, rien ne détecte une régression ;
après, une divergence de comportement entre les deux plateformes devient impossible **par
construction** (il n'y a plus qu'un code), et le contrat avec Bukkit est verrouillé.

**Stack** : JUnit 5 (Jupiter) + AssertJ + Mockito 5 + **ArchUnit** + `maven-surefire-plugin` 3.2.x.

### 9.1 Fakes de plateforme — la fondation

```java
public class FakeProxyPlatform implements ProxyPlatform {
    private final Map<UUID, FakeProxyPlayer> players = new LinkedHashMap<>();
    private final List<FakeBackendServer> servers = new ArrayList<>();
    public final List<String> loggedLines = new ArrayList<>();
    // + helpers : addPlayer(name), disconnect(uuid), sentPayloads()
}

public class FakeProxyPlayer implements ProxyPlayer {
    public final List<String> received = new ArrayList<>();   // messages legacy reçus
    public final List<String> soundsPlayed = new ArrayList<>();
    private final Set<String> permissions = new HashSet<>();
}
```

Ces deux classes (~120 lignes) permettent de tester **la totalité du cœur sans démarrer aucun
serveur Minecraft**. Elles sont l'investissement le plus rentable du chantier.

### 9.2 `LegacyFormatterTest` / `MessageTemplateTest` (code pur)

| Test | Attendu |
|---|---|
| `format("&aBonjour")` | `§aBonjour` |
| `format("{#FF0000}Rouge")` | `§x§F§F§0§0§0§0Rouge` |
| `format("{#ff0000} et {#00ff00}")` | les deux convertis (boucle regex) |
| `format("{#GGGGGG}")` | inchangé, pas d'exception |
| `format("")` / `format(null)` | pas de `NullPointerException` |
| `replace(t, "{sender}", "Bob", false)` | remplacement littéral, `&` non interprété |
| `replace(t, "{sender}", "&cBob", true)` | `&c` converti |
| `replace(t, "{message}", "j'ai dit {sender}", false)` | le placeholder injecté n'est **pas** ré-interprété |
| placeholder absent du template | template inchangé |

### 9.3 `PrivateMessageServiceTest`

| Test | Attendu |
|---|---|
| sender ignore target | `SENDER_IGNORE_MESSAGE` au sender, **0 message** au target |
| target ignore sender | `TARGET_IGNORE_MESSAGE`, 0 message au target |
| sender a `msgtoggle` actif | `SENDER_MSGTOGGLE`, aucun envoi |
| target a `msgtoggle` actif | `TARGET_MSGTOGGLE` avec `{player}` résolu |
| cas nominal | target reçoit le format target, sender le format sender |
| cas nominal | `previousPlayer` mis à jour **dans les deux sens** |
| pseudo / nick | le nick prime sur le nom quand présent ; le nom sinon |
| permission `fmessage.colors` absente | `&c` dans le message reste littéral |
| permission `fmessage.nick.colors` | appliquée au nick, indépendamment de `fmessage.colors` |
| espion | chaque joueur de `playerSpy` reçoit le format spy |
| espion déconnecté | ignoré silencieusement, pas de `NullPointerException` |
| espion | la ligne est aussi écrite dans le log console |
| son activé | `playSound` appelé une fois sur le **target uniquement** |
| son désactivé ou clé vide | `playSound` non appelé |

### 9.4 🔴 `ChatPacketTest` — le test le plus critique

`FMessageBukkit` n'est pas modifié : le format binaire des trames est un **contrat figé**.
Un octet de décalé et le chat inter-serveurs casse en production, **sans erreur visible**.

| Test | Attendu |
|---|---|
| aller-retour encode → decode | égalité champ à champ |
| chat public, encodage | ordre exact : `subchannel, uuid, displayName, nickName, format, message, ignoresCsv, colors:bool, nickColors:bool` |
| trame staff, encodage | ordre exact : `"StaffMessage", uuid, displayName, nickName, format, message` (**pas de booléens**) |
| nickName absent | encodé en `""`, jamais `null` |
| liste d'ignores | sérialisée `uuid1;uuid2;` (avec le `;` final) |
| liste d'ignores vide | chaîne vide, pas `"null"` |
| **comparaison octet à octet** | contre une trame de référence figée en base64 dans le test |

> Cette dernière ligne est le garde-fou : elle échoue à la moindre modification involontaire
> du protocole.

### 9.5 `ChatRelayServiceTest`

| Test | Attendu |
|---|---|
| toggle de groupe actif | seuls les **membres en ligne** du groupe reçoivent `GROUP_MSG` |
| membre hors ligne | ignoré sans erreur |
| groupe supprimé entre-temps (`getGroups()` → null) | sortie propre, aucun envoi |
| groupe | la ligne est journalisée `[nom] auteur : message` |
| nick présent | l'auteur affiché est le nick, sinon le displayName |
| sous-canal `StaffMessage` | rediffusion vers **tous** les serveurs backend |
| chat public | rediffusion vers tous les serveurs, avec les ignores du joueur dans la trame |
| aucun serveur backend | pas d'exception |
| trame sur un autre canal | ignorée |

### 9.6 🔴 `ArchitectureTest` (ArchUnit) — le garant de l'invariant

Le risque numéro un de cette architecture : un `import com.velocitypowered...` qui se glisse
dans `core/`. Le projet compile (les deux APIs sont au classpath), les tests passent, **et le
plugin explose au démarrage sur BungeeCord** avec un `NoClassDefFoundError`.

Ce test rend cette erreur impossible.

```java
@AnalyzeClasses(packages = "fr.florianpal.fmessage")
class ArchitectureTest {

    @ArchTest
    static final ArchRule core_ne_depend_d_aucune_plateforme =
        noClasses().that().resideInAnyPackage("..core..", "..platform..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("net.md_5..", "com.velocitypowered..", "net.kyori..");

    @ArchTest
    static final ArchRule les_adaptateurs_ne_se_voient_pas =
        noClasses().that().resideInAPackage("..bungee..")
            .should().dependOnClassesThat().resideInAPackage("..velocity..");
        // + la règle symétrique

    @ArchTest
    static final ArchRule seuls_les_adaptateurs_touchent_ACF_plateforme =
        noClasses().that().resideInAPackage("..core..")
            .should().dependOnClassesThat()
            .haveNameMatching(".*(Bungee|Velocity)(CommandManager|CommandIssuer|Locales).*");
}
```

**Complément** (optionnel mais très robuste) : un test qui charge `BungeeBootstrap` dans un
`URLClassLoader` **amputé de `velocity-api`** et vérifie l'absence de `NoClassDefFoundError`,
et le symétrique. C'est la reproduction exacte des conditions de production.

### 9.7 `PackagingTest` (test d'intégration, phase `verify`)

Vérifie le jar final :

| Test | Attendu |
|---|---|
| descripteurs | le jar contient **`plugin.yml` ET `velocity-plugin.json`** |
| `velocity-plugin.json` | `main` pointe vers `VelocityBootstrap` |
| `plugin.yml` | `main` pointe vers `BungeeBootstrap`, `name` inchangé (§6.3) |
| APIs non embarquées | aucune entrée `net/md_5/` ni `com/velocitypowered/` ni `net/kyori/` |
| ACF | une seule copie de chaque classe, sous le package relocalisé |
| ACF | `BungeeCommandManager` **et** `VelocityCommandManager` présents |
| bStats | les deux `Metrics` présents |

### 9.8 Tests des adaptateurs (Mockito)

Peu nombreux mais indispensables : ils couvrent la seule chose que les fakes ne peuvent pas
voir, **la conversion finale texte → composant**.

- `BungeePlayerTest` : `sendMessage("&aBonjour")` → `TextComponent.fromLegacyText` appelé,
  couleur verte dans le composant produit.
- `VelocityPlayerTest` : `sendMessage("§x§F§F§0§0§0§0Rouge")` → composant avec
  `TextColor.color(0xFF0000)`. **C'est le test qui protège la correction du §6.1.**
- `BungeePlayerTest` : `playSound` → un plugin message `"Sound"` est envoyé au serveur
  courant ; **aucun envoi si le joueur n'est sur aucun serveur**.
- `VelocityPlayerTest` : `playSound` avec une clé invalide → log d'avertissement, pas de
  propagation d'exception.

### Récapitulatif de couverture

| Zone | Tests | Couverture visée |
|---|---|---|
| `core/text/` | ~15 | 100 % |
| `core/protocol/` | ~10 | 100 % |
| `core/service/` | ~30 | > 90 % |
| `core/commands/` | ~25 | > 80 % |
| `platform` (architecture) | ~5 | règles |
| adaptateurs | ~12 | > 60 % (le reste = délégation) |
| packaging | ~7 | — |
| **Total** | **~105 tests** | |

---

## 10. Estimation et recommandations

| Lot | Charge |
|---|---|
| 1 — Socle de test | 0,5 j |
| 2 — Module + abstraction | 0,5 j |
| 3 — Code pur + tests | 1 j |
| 4 — Services + tests | 2 j |
| 5 — Commandes + tests | 2 j |
| 6 — Adaptateur Bungee | 1 j |
| 7 — Adaptateur Velocity | 1 j |
| 8 — Nettoyage, doc, validation manuelle | 1 j |
| **Total** | **~9 jours** |

**Retour sur investissement** : aujourd'hui, toute évolution fonctionnelle du proxy se paie
deux fois (écriture, relecture, test, correction). Sur les derniers commits visibles
(`Add message log`, la correction du cache de membres dans `updateGroups()` — présente à
l'identique dans les deux fichiers), c'est exactement ce qui s'est produit. L'amortissement
se fait sur quelques évolutions.

### Ce que je recommande de NE PAS faire dans ce chantier

- **Ne pas toucher à `FMessageBukkit` ni au protocole binaire.** Un seul axe de changement à
  la fois — le protocole est la seule chose qui puisse casser en production silencieusement.
- **Ne pas fusionner aussi `FMessageBukkit`** dans le jar unique. Les cycles de vie et les
  APIs sont trop éloignés, et le gain de duplication est nul (le code Bukkit ne ressemble pas
  au code proxy).
- **Ne pas en profiter pour introduire Adventure côté Bungee** (§4). C'est un chantier
  séparé, avec son propre risque.
- **Ne pas renommer les dossiers de données** (§6.3).

---

## 11. Arbitrages en attente

Deux points où une décision change la suite :

1. **Nom et id du plugin** — le plan part sur `name: FMessageBungee` / `id = "fmessage"`
   conservés, pour ne pas casser les installations existantes, malgré l'incohérence
   cosmétique. À confirmer, ou choisir de renommer proprement en fournissant une migration de
   dossier.

2. **ACF en SNAPSHOT** — la fusion repose sur la cohérence des deux artefacts (§6.5). On peut
   ajouter au lot 1 une tâche d'épinglage sur une version fixe pour supprimer ce risque à la
   racine.