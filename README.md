# FMessage

> Cross-server chat and private messaging for Minecraft networks — BungeeCord or Velocity proxy + Paper/Spigot/Bukkit backend servers.

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](https://github.com/Florianpal1/FMessage/blob/master/LICENSE)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://github.com/Florianpal1/FMessage/search?l=java)
[![Latest Release](https://img.shields.io/github/v/release/Florianpal1/FMessage)](https://github.com/Florianpal1/FMessage/releases/latest)

---

## 📖 Overview

**FMessage** relays chat between the backend servers of a BungeeCord/Velocity network and adds private messages, discussion groups, nicknames, an ignore list, a staff chat and a chat-spy for moderators.

> ⚠️ **FMessage is a network plugin.** It ships as two jars that work together: one on the proxy, one on every backend server. The Bukkit jar cancels every chat event and forwards it to the proxy — **it does not work on its own**. Install both sides, or neither.

---

## 🔗 Links

- 📚 [Wiki / Documentation](https://github.com/Florianpal1/FMessage/wiki)
- 🛒 [SpigotMC](https://www.spigotmc.org/resources/fchat.121099/)
- 🪣 [Hangar (PaperMC)](https://hangar.papermc.io/Florianpal/FMessage)
- 🐛 [Issues / Bug reports](https://github.com/Florianpal1/FMessage/issues)

---

## ✨ Features

- **Private messages**: `/msg`, `/m` and `/r`, across every server of the network, with a notification sound
- **Do not disturb**: `/msgtoggle` stops incoming private messages
- **Discussion groups**: create groups, add or kick members, toggle your chat into a group
- **Nicknames**: `/nick`, stored network-wide, color support gated by permission
- **Ignore list**: `/ignore` and `/unignore`, offline players included, applied to private messages and public chat
- **Staff chat**: `/staffchat` (`/mc`) broadcast to every server of the network
- **ChatSpy** (Staff): `/chatspy` shows every private message going through the proxy
- **Anti-flood & anti-caps**: configurable thresholds, with bypass permissions
- **Cross-server chat relay**: a message written on one backend server is displayed on all of them
- **MariaDB/MySQL persistence**: groups, members, nicknames and ignores survive restarts
- **Live reload**: `/fmessage reload` applies configuration changes without a restart
- **PlaceholderAPI support**: optional, resolved on the backend server before the relay
- **Customizable formats**: chat, private-message, spy and staff formats are all configurable

---

## 📋 Requirements

| | |
|---|---|
| Proxy | BungeeCord (1.20+) **or** Velocity 3.x — one of the two, never both |
| Backend servers | Bukkit / Spigot / Paper, API 1.16+ (tested against Paper 1.21.7) |
| Java | 21 |
| Database | MariaDB or MySQL, reachable from the proxy |
| Backend dependency | [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) — optional, only for placeholders in formats |

---

## 🏗️ Project Structure

```
FMessage/
├── FMessageCommon/    # Shared code: database, queries, configuration (no Minecraft API)
├── FMessageBukkit/    # Backend server plugin (Paper API) — captures and displays chat
└── FMessageProxy/     # Proxy plugin — one jar for BungeeCord AND Velocity
    ├── platform/      # Abstraction contract: ProxyPlatform, ProxyPlayer, ProxyBackendServer
    ├── core/          # All the logic: commands, services, protocol — platform-free
    ├── bungee/        # BungeeCord adapter + entry point (plugin.yml)
    └── velocity/      # Velocity adapter + entry point (velocity-plugin.json)
```

Since 3.0.0 the two proxy modules are merged: **a single jar loads on either proxy**. BungeeCord reads `plugin.yml`, Velocity reads the `velocity-plugin.json` generated at build time, and class loading being lazy, neither ever touches the other's classes. All the logic lives once in `core/`, written against the `platform/` interfaces; each adapter is a thin translation layer.

See the [Architecture](https://github.com/Florianpal1/FMessage/wiki/Architecture) page.

---

## ⚙️ Installation

### 1. Proxy (BungeeCord **or** Velocity)

1. Download `FMessage-Proxy-x.x.x.jar` from the [releases page](https://github.com/Florianpal1/FMessage/releases) — the same jar works on both proxies.
2. Drop it into the proxy's `plugins/` folder and start the proxy once to generate the configuration.
3. Edit `database.yml` (`plugins/fmessage/` on Velocity, `plugins/FMessageBungee/` on BungeeCord).
4. Restart. The four tables are created automatically.

> ⬆️ **Upgrading from 2.x**: delete the old `FMessageBungee-x.x.x.jar` or `FMessageVelocity-x.x.x.jar` before dropping in the new jar, otherwise the plugin is loaded twice. Configuration folders are unchanged, so your settings, languages and database carry over as-is.

### 2. Backend servers (each one)

1. Make sure PlaceholderAPI is installed.
2. Drop `FMessageBukkit-x.x.x.jar` into `plugins/` and restart.
3. Edit `plugins/FMessageBukkit/config.yml`.

Full walkthrough: [Installation](https://github.com/Florianpal1/FMessage/wiki/Installation).

---

## 🔧 Configuration

### Backend server — `plugins/FMessageBukkit/config.yml`

```yaml
lang: "en"

chatFormat:
  general: "&7{displayName}&7: &r{message}"
  staffChat: "[StaffChat] &7{displayName}&7: &r{message}"

ignoreFormat: "&7This is a message you have decided to ignore."
floodFormat: "Your message contains a flood. It was automatically deleted."
spamFormat: "Your message contains too many uppercase letters. It has been automatically written in lower case."
```

### Proxy — `config.yml`

```yaml
version: 2

lang: "en"
format:
  senderChatFormat: "&f[&7me &f => &7 {target}&f]&7: &r{message}"
  targetChatFormat: "&f[&7{sender} &f => &7 me&f]&7: &r{message}"
  spyChatFormat: "&f[&7{sender} &f => &7 {target}&f]&7: &r{message}"

# Sound played to the receiver of a private message.
notification:
  enabled: true
  sound: "entity.experience_orb.pickup"
```

Group messages are formatted by the `acf-fmessage.group_msg` key of the language file, not by `config.yml`.

### Proxy — `database.yml`

```yaml
version: 1
database:
  url: "jdbc:mariadb://localhost:3306/test"
  user: "root"
  password: ""
```

> The bundled JDBC driver is **MariaDB** (`org.mariadb.jdbc.Driver`), so the URL must start with `jdbc:mariadb://` — including when you connect to a MySQL server.

Formats use legacy ampersand color codes (`&0`–`&f`, `&k`–`&o`, `&r`). Proxy formats also accept hex colors written `{#rrggbb}`; the backend `chatFormat` does not. MiniMessage tags are not supported anywhere. Apply changes with `/fmessage reload`; only `database.yml` still requires a restart.

> Hex colors used to work on BungeeCord only and were silently dropped on Velocity. Since 3.0.0 both proxies share the same formatter, so they behave identically.

More: [Bukkit Configuration](https://github.com/Florianpal1/FMessage/wiki/Bukkit-Configuration) · [Proxy Configuration](https://github.com/Florianpal1/FMessage/wiki/Proxy-Configuration) · [Languages](https://github.com/Florianpal1/FMessage/wiki/Languages) · [Database](https://github.com/Florianpal1/FMessage/wiki/Database)

---

## 📜 Commands & Permissions

### General

| Command | Permission | Description |
|---|---|---|
| `/msg <player> <message>` or `/m` | `fmessage.msg` | Send a private message to a player |
| `/r <message>` | `fmessage.r` | Reply to the last player you exchanged a message with |
| `/msgtoggle` or `/mt` | `fmessage.msgtoggle` | Toggle the reception of private messages |
| `/ignore <player>` | `fmessage.ignore` | Ignore messages from a player, online or not |
| `/unignore <player>` | `fmessage.unignore` | Stop ignoring a player |
| `/nick [nickname]` or `/nickname` | `fmessage.nick` | Set your nickname — no argument removes it |

### Groups

| Command | Permission | Description |
|---|---|---|
| `/group create <name>` | `fmessage.group.create` | Create a discussion group |
| `/group remove <name>` | `fmessage.group.remove` | Delete a group you own |
| `/group member add <group> <player>` | `fmessage.group.member.add` | Add a member to a group |
| `/group member kick <group> <player>` | `fmessage.group.member.kick` | Remove a member from a group |
| `/group msg <group> <message>` | `fmessage.group.msg` | Send a message in a group |
| `/group toggle <group>` | `fmessage.group.toggle` | Redirect all your chat messages to a group |

### Staff

| Command | Permission | Description |
|---|---|---|
| `/chatspy` | `fmessage.chatspy` | Monitor all private messages in real time |
| `/staffchat <message>` or `/mc` | `fmessage.staffchat` | Message every staff member on the network |
| `/fmessage reload` | `fmessage.reload` | Reload `config.yml` and the language file, proxy and backend alike |

### Permissions without a command

| Permission | Description |
|---|---|
| `fmessage.colors` | Use color codes in messages |
| `fmessage.nick.colors` | Use color codes in nicknames |
| `fmessage.cannot_ignore` | This player cannot be ignored by others |
| `fmessage.bypass.flood` | Skip the anti-flood check |
| `fmessage.bypass.spam` | Skip the anti-caps check |
| `fmessage.staffchat` | Also grants **reception** of staff-chat messages |

Details and behavior notes: [Commands and Permissions](https://github.com/Florianpal1/FMessage/wiki/Commands-and-Permissions).

---

## 🔨 Building

```bash
mvn clean install      # build + unit tests
mvn clean verify       # adds the packaging checks on the shaded jar
```

Requires JDK 21. Jars land in `FMessage*/target/`: `FMessage-Proxy-x.x.x.jar` for the proxy, `FMessageBukkit-x.x.x.jar` for the backend servers.

The test suite is the safety net of the proxy merge. Two parts matter most:

- `ChatProtocolTest` pins the binary frame format shared with FMessageBukkit down to the byte. FMessageBukkit is not rebuilt with the proxy, so that format is a frozen contract.
- `ArchitectureTest` (ArchUnit) forbids any reference to a platform API from `core/` or `platform/`. Both proxy APIs sit on the compile classpath, so a stray import would compile, pass every other test, and only fail at startup on the other proxy.

See [Building from source](https://github.com/Florianpal1/FMessage/wiki/Building).

---

## 🤝 Contributing

Contributions are welcome! Feel free to open an [issue](https://github.com/Florianpal1/FMessage/issues) to report a bug or suggest an improvement, or submit a Pull Request.

When changing commands or message handling, write it once in `FMessageProxy/core/` — it runs on both proxies. Only reach into `bungee/` or `velocity/` for something a proxy API genuinely does differently, and add the abstraction to `platform/` rather than an import to `core/`; `ArchitectureTest` will tell you if you got it wrong.

---

## 📄 License

This project is distributed under the [GPL-3.0 license](https://github.com/Florianpal1/FMessage/blob/master/LICENSE).
