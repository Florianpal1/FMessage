package fr.florianpal.fmessage.objects;

import java.util.UUID;

public class Member {
    private UUID uuid;
    private boolean toggle;

    public Member(UUID uuid, boolean toggle) {
        this.uuid = uuid;
        this.toggle = toggle;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isToggle() {
        return toggle;
    }
}
