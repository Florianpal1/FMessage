# FMessage

 *   [Wiki](https://github.com/Florianpal1/FMessage/wiki)
 *   [Spigot]()

## Commands and Permissions

### Common

| Command | Permission  | Description |
| :------: | :------: | :------: |
| /m [message] or /msg [message] | fmessage.msg | Allows you to send a message to a person |
| /r [message] | fmessage.r | Allows you to reply to the last person who sent you a message.  |
| /ignore [username] | fmessage.ignore | Allows you to ignore a person’s messages |
| /unignore [username] | fmessage.unignore | Stops ignoring a person’s messages  |

### Group

| Command | Permission  | Description |
| :------: | :------: | :------: |
| /group create [groupName] | fmessage.group.create | Creates a discussion group  |
| /group remove [groupName] | fmessage.group.remove | Destroys a discussion group  |
| /group member add [groupName] [playerName] | fmessage.group.member.add | Add a member to a group |
| /group member kick [groupName] [playerName] | fmessage.group.member.kick | Removes a member from a group   |
| /group msg [groupName] [message] | fmessage.group.msg | Allows you to send a message in a group |
| /group toggle [groupName] | fmessage.group.toggle | Send any message written in a group |

### Staff

| Command | Permission  | Description |
| :------: | :------: | :------: |
| /chatspy | fmessage.chatspy | Displays messages sent by msg/group  |