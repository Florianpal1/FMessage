

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Context

FMessage is a Minecraft network plugin: cross-server chat, private messages, groups,
nicknames, an ignore list, staff chat and a chat spy for moderation.

It ships as **two inseparable jars**:

- `FMessage-Proxy-x.x.x.jar` on the proxy (BungeeCord **or** Velocity) — commands, database,
  routing between servers.
- `FMessageBukkit-x.x.x.jar` on every game server — captures chat and renders it.

The Bukkit jar cancels every chat event and forwards it to the proxy: **it does not work on its
own**. Java 21, MariaDB/MySQL, ACF (Aikar Command Framework) for commands.

Because they are inseparable, they are distributed together: the `FMessageDist` module (no code,
last in the reactor) packages both into `FMessageDist/target/FMessage-x.x.x.zip` on every
`package`. The file names inside the zip are pinned by `FMessageDist/src/assembly/dist.xml`.

## Commands

```bash
mvn clean package     # build + FMessageDist/target/FMessage-x.x.x.zip (the two jars)
mvn clean install     # build + unit tests
mvn clean verify      # adds PackagingIT, which inspects the shaded jar
mvn test              # unit tests only

# A single test, nested class, or method:
mvn -pl FMessageProxy -am test -Dsurefire.failIfNoSpecifiedTests=false \
    -Dtest='LegacyFormatterTest$HexTokens#converts_a_hex_token_to_the_repeated_x_form'
```

`-Dsurefire.failIfNoSpecifiedTests=false` is required whenever you filter with `-Dtest`: `-am`
pulls in `FMessageCommon`, which has no tests, and surefire would fail otherwise. Tests live in
`FMessageProxy` only.

The build targets Java 21 (`maven.compiler.release`) and works on a newer JDK.

## Architecture

### The flow, end to end

Everything travels over Minecraft plugin messages, on two channels:

```
                fmessage:chatbungee                   fmessage:chatbukkit
  Bukkit  ─────────────────────────────►  Proxy  ─────────────────────────────►  Bukkit
  (chat cancelled, format and            (nickname, ignores, groups,    (final rendering,
   placeholders resolved locally)         routing)                       local filtering)
```

**Public chat**: `ChatListener` (Bukkit) cancels the event, applies the anti-flood and
anti-caps checks, resolves the format and PlaceholderAPI placeholders **on the originating
server**, then sends the frame to the proxy. `ChatRelayService` (proxy) adds the nickname and
the list of players ignoring the author, then rebroadcasts to **every** server, each of which
filters locally.

**Private message**: handled entirely on the proxy (`PrivateMessageService`), never touching
Bukkit — except for the notification sound on BungeeCord, which has no sound API and delegates
to the backend server through a `Sound` frame.

**Staff chat**: `/staffchat` is a **Bukkit** command, rebroadcast by the proxy to every server,
where only holders of `fmessage.staffchat` see it.

**Group toggle**: if the author has an active `/group toggle`, their public chat is diverted to
that group and **never** reaches public chat. This branch takes priority over everything else,
including the staff subchannel.

### FMessageProxy: one jar, two proxies

This is the defining trait of the repository. The same jar loads on BungeeCord and on Velocity,
thanks to three properties:

1. **Two descriptors coexist**: `plugin.yml` (read by BungeeCord) and `velocity-plugin.json`
   (read by Velocity, generated at build time by the `velocity-api` annotation processor from
   `@Plugin`).
2. **Class loading is lazy**: no shared class references either entry point, so
   `VelocityBootstrap` is never loaded on BungeeCord and vice versa.
3. **`acf-bungee` and `acf-velocity` can be merged**: their 125 shared classes (`acf-core`)
   have identical bytecode, so the shade plugin reduces them to a single copy while keeping
   both platforms' specific classes.

Package layout under `fr.florianpal.fmessage`:

| Package | Role |
| --- | --- |
| `platform/` | The abstraction contract: `ProxyPlatform`, `ProxyPlayer`, `ProxyBackendServer`, `ProxyLogger`. Depends on nothing. |
| `core/` | **All the logic**: commands, services, protocol, formatting, state. Written against `platform/`. |
| `bungee/` | BungeeCord adapter + `BungeeBootstrap` entry point. |
| `velocity/` | Velocity adapter + `VelocityBootstrap` entry point. |

**A functional change belongs in `core/`, written once.** Only reach into the adapters for
something a proxy API genuinely does differently — and in that case add a method to
`platform/`, never an import to `core/`.

### Text: legacy `String` in the core, components in the adapters

The core produces legacy `§` text (`LegacyFormatter`, `MessageTemplate`) and never builds a
component. Conversion happens at the very last moment inside the adapter:
`TextComponent.fromLegacyText` on BungeeCord, a `LegacyComponentSerializer` on Velocity.

This is what keeps Adventure **out** of the core and out of the jar: BungeeCord does not ship
Adventure, and a shaded copy handed to `Player.sendMessage()` on Velocity would throw a
`ClassCastException` (same class names, different class loaders).

Proxy formats accept `&0`–`&f` and hex `{#rrggbb}`. The Bukkit side only uses
`legacyAmpersand()` and therefore does not handle `{#rrggbb}`.

## Invariants that must never break

**1. The binary format in `ChatProtocol` is a frozen contract.** `FMessageBukkit` is not
rebuilt alongside the proxy: a field moved, added or removed breaks cross-server chat in
production, silently, with no error anywhere. `ChatProtocolTest` pins it down to the byte with
golden base64 frames. If that test fails, updating the expected value is not the fix.

**2. No platform import in `core/` or `platform/`.** Both proxy APIs sit on the compile
classpath: a stray `import com.velocitypowered...` compiles, passes every other test, and only
fails at startup on BungeeCord with a `NoClassDefFoundError`. `ArchitectureTest` (ArchUnit)
forbids it and names the offending file and line. The same rule covers platform-specific ACF
classes (`BungeeCommandManager`, `VelocityCommandManager`, …): only `acf-core` is usable from
the core.

**3. `minimizeJar` must stay disabled** on `FMessageProxy`. Its reachability analysis would
start from a single entry point and strip the other platform's ACF classes, which are only
reachable at runtime.

**4. `annotationProcessorPaths` must stay explicitly declared** in the `FMessageProxy` pom.
Since JDK 23, javac no longer runs annotation processors discovered through the classpath
alone: without that declaration `velocity-plugin.json` is not generated, the build stays green,
and the jar is **invisible on Velocity**.

**5. Do not rename `name: FMessageBungee` in `plugin.yml` nor `id = "fmessage"` in `@Plugin`.**
These values determine the data folder (`plugins/FMessageBungee/` and `plugins/fmessage/`);
changing them would hide the configuration, languages and settings of every existing
installation.

## Conventions

**Commands** — ACF, with `CommandIssuer` as the first parameter (never a platform player type:
that is what allows them to be written once). Commands that require a player go through
`FMessageCommand.requirePlayer(issuer)`, which returns `null` after telling the console it
cannot run this. `/fmessage reload` is the only command the console may run.

**User-facing messages** — never hardcoded: add a constant to `MessageKeys`, then the
`acf-fmessage.<lowercase_name>` key to **all four** language files (`lang_en.yml` and
`lang_fr.yml`, present in both `FMessageProxy` and `FMessageBukkit`).

**Caches** — `FMessageCore` keeps groups and ignores in memory. Every database write must be
followed by `updateGroups()` or `updateIgnores()`. `updateGroups()` also reloads members,
otherwise the cache ends up holding groups with empty member lists.

**Session state** — `SessionState` (reply target, spies, `/msgtoggle`) is not persisted and
uses concurrent collections: it is written from the proxy's network threads and read from
command threads.

## Database

Five tables created automatically at startup through `IDatabaseTable`: `fm_groups`,
`fm_groupMembers`, `fm_ignores`, `fm_nickname`, `fm_players`.

The bundled driver is **MariaDB** (`org.mariadb.jdbc.Driver`, relocated by the shade plugin),
so the URL must start with `jdbc:mariadb://` **even for a MySQL server**. `database.yml` is
only re-read on restart: the HikariCP pool is built once.

## Tests

The fakes in `fr.florianpal.fmessage.fakes` (`FakeProxyPlatform`, `FakeProxyPlayer`,
`FakeBackendServer`, `FakeMessenger`) exercise the whole core without a server or a database.
To test a command, **mock** `FMessageCore` — its constructor opens a connection pool, and
Mockito instantiates it without running any constructor; see `CommandTestSupport`.

Watch out for the `CommandIssuer.sendInfo` overloads: an untyped `any()` resolves to
`sendInfo(MessageKey, ...)` while production code calls `sendInfo(MessageKeyProvider, ...)`,
and the verification fails. Use `any(MessageKeyProvider.class)`.

## Git

You are not allowed to commit or push on this project.
