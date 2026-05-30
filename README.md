# FMessage

> Multi-platform Minecraft private messaging plugin — compatible with Bukkit/Spigot/Paper, BungeeCord and Velocity.

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](https://github.com/Florianpal1/FMessage/blob/master/LICENSE)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://github.com/Florianpal1/FMessage/search?l=java)
[![Latest Release](https://img.shields.io/github/v/release/Florianpal1/FMessage)](https://github.com/Florianpal1/FMessage/releases/tag/V2.4.4)

---

## 📖 Overview

**FMessage** is a chat and private messaging plugin for Minecraft servers. It lets players send private messages, create discussion groups, manage colored nicknames, and more. The plugin works both on standalone servers (Bukkit/Spigot/Paper) and multi-server networks (BungeeCord, Velocity).

---

## 🔗 Links

- 📚 [Wiki / Documentation](https://github.com/Florianpal1/FMessage/wiki)
- 🛒 [SpigotMC](https://www.spigotmc.org/resources/fchat.121099/)
- 🪣 [Hangar (PaperMC)](https://hangar.papermc.io/Florianpal/FMessage)
- 🐛 [Issues / Bug reports](https://github.com/Florianpal1/FMessage/issues)

---

## ✨ Features

- **Private messages**: send messages to a player with `/msg` or `/m`, and reply quickly with `/r`
- **Discussion groups**: create groups, manage members and send group messages
- **Nicknames**: change your display name with `/nick`, with full color and formatting support
- **Ignore players**: block and unblock messages from a player with `/ignore` and `/unignore`
- **ChatSpy** (Staff): monitor all private messages in real time with `/chatspy`
- **Anti-flood & anti-spam**: automatic detection of repeated messages and excessive uppercase usage
- **Multi-server support**: works on BungeeCord and Velocity for network setups
- **MySQL database**: data persistence via a MySQL connection (network mode)
- **Customizable chat format**: tailor message formats to your needs

---

## 🏗️ Project Structure

```
FMessage/
├── FMessageCommon/    # Shared code across all platforms
├── FMessageBukkit/    # Bukkit / Spigot / Paper implementation
├── FMessageBungee/    # BungeeCord implementation
└── FMessageVelocity/  # Velocity implementation
```

---

## ⚙️ Installation

### Bukkit / Spigot / Paper

1. Download `FMessageBukkit-x.x.x.jar` from the [releases page](https://github.com/Florianpal1/FMessage/releases).
2. Drop it into your server's `plugins/` folder.
3. Restart the server.
4. Edit `plugins/FMessage/config.yml` to your liking.

### BungeeCord / Velocity Network

1. Download the jar matching your proxy from the [releases page](https://github.com/Florianpal1/FMessage/releases).
2. Drop it into your proxy's `plugins/` folder.
3. Restart the proxy.
4. Configure `config.yml` and `database.yml` (MySQL connection required).

---

## 🔧 Configuration

### Bukkit — `config.yml`

```yaml
lang: "en"

chatFormat:
  general: "&7{displayName}&7: &r{message}"
  staffChat: "[StaffChat] &7{displayName}&7: &r{message}"

ignoreFormat: "&7This is a message you have decided to ignore."
floodFormat: "Your message contains a flood. It was automatically deleted."
spamFormat: "Your message contains too many uppercase letters. It has been automatically written in lower case."
```

### BungeeCord / Velocity — `config.yml`

```yaml
version: 1

lang: "en"
format:
  senderChatFormat: "&f[&7me &f => &7 {target}&f]&7: &r{message}"
  targetChatFormat: "&f[&7{sender} &f => &7 me&f]&7: &r{message}"
  spyChatFormat: "&f[&7{sender} &f => &7 {target}&f]&7: &r{message}"
  groupFormat: ""
```

### BungeeCord / Velocity — `database.yml`

```yaml
version: 1
database:
  url: "jdbc:mysql://localhost:3306/test"
  user: "root"
  password: ""
```

---

## 📜 Commands & Permissions

### General

| Command | Permission | Description |
|---|---|---|
| `/m [message]` or `/msg [message]` | `fmessage.msg` | Send a private message to a player |
| `/r [message]` | `fmessage.r` | Reply to the last message received |
| `/ignore [player]` | `fmessage.ignore` | Ignore messages from a player |
| `/unignore [player]` | `fmessage.unignore` | Stop ignoring a player |
| `/nick (nickname)` | `fmessage.nick` | Change your display name |

### Groups

| Command | Permission | Description |
|---|---|---|
| `/group create [name]` | `fmessage.group.create` | Create a discussion group |
| `/group remove [name]` | `fmessage.group.remove` | Delete a group |
| `/group member add [group] [player]` | `fmessage.group.member.add` | Add a member to a group |
| `/group member kick [group] [player]` | `fmessage.group.member.kick` | Remove a member from a group |
| `/group msg [group] [message]` | `fmessage.group.msg` | Send a message in a group |
| `/group toggle [group]` | `fmessage.group.toggle` | Redirect all chat messages to a group |

### Staff

| Command | Permission | Description |
|---|---|---|
| `/chatspy` | `fmessage.chatspy` | Monitor all private messages in real time |

### Special Permissions

| Permission | Description |
|---|---|
| `fmessage.colors` | Use colors and formatting effects in messages |
| `fmessage.nick.colors` | Use colors and formatting effects in nicknames |

---

## 🤝 Contributing

Contributions are welcome! Feel free to open an [issue](https://github.com/Florianpal1/FMessage/issues) to report a bug or suggest an improvement, or submit a Pull Request.

---

## 📄 License

This project is distributed under the [GPL-3.0 license](https://github.com/Florianpal1/FMessage/blob/master/LICENSE).
