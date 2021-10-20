package fr.florianpal.fmessage.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {
    private int id;
    private UUID owner;
    private String name;
    private List<Member> member;

    public Group(int id, UUID owner, String name) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.member = new ArrayList<>();
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public List<Member> getMember() {
        return member;
    }

    public void setMember(List<Member> member) {
        this.member = member;
    }

    public int getId() {
        return id;
    }
}
