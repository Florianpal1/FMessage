package fr.florianpal.fmessage.core.service;

import fr.florianpal.fmessage.fakes.FakeProxyPlatform;
import fr.florianpal.fmessage.objects.Group;
import fr.florianpal.fmessage.objects.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CompletionProvidersTest {

    private final UUID alice = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();

    private FakeProxyPlatform platform;
    private Map<Integer, Group> groups;
    private CompletionProviders providers;

    @BeforeEach
    void setUp() {
        platform = new FakeProxyPlatform();
        groups = new HashMap<>();
        providers = new CompletionProviders(platform, () -> groups);
    }

    private Group group(int id, String name, UUID owner, UUID... members) {
        Group group = new Group(id, owner, name);
        group.setMember(java.util.Arrays.stream(members).map(uuid -> new Member(uuid, false)).toList());
        groups.put(id, group);
        return group;
    }

    @Test
    void lists_every_online_player() {
        platform.addPlayer("Alice");
        platform.addPlayer("Bob");

        assertThat(providers.onlinePlayerNames()).containsExactly("Alice", "Bob");
    }

    @Test
    void lists_no_player_when_the_proxy_is_empty() {
        assertThat(providers.onlinePlayerNames()).isEmpty();
    }

    @Test
    void lists_the_groups_the_player_belongs_to() {
        group(1, "Amis", alice, alice, bob);
        group(2, "Guilde", bob, bob);

        assertThat(providers.groupsOf(alice)).containsExactly("Amis");
    }

    @Test
    void lists_a_group_once_even_with_several_members() {
        group(1, "Amis", alice, alice, bob);

        assertThat(providers.groupsOf(alice)).containsExactly("Amis");
    }

    @Test
    void lists_nothing_for_a_player_in_no_group() {
        group(1, "Amis", bob, bob);

        assertThat(providers.groupsOf(alice)).isEmpty();
    }

    @Test
    void lists_only_the_groups_the_player_owns() {
        group(1, "Amis", alice, alice, bob);
        group(2, "Guilde", bob, alice, bob);

        assertThat(providers.ownedGroupsOf(alice)).containsExactly("Amis");
    }

    @Test
    void lists_no_owned_group_for_a_simple_member() {
        group(1, "Amis", bob, alice, bob);

        assertThat(providers.ownedGroupsOf(alice)).isEmpty();
    }

    @Test
    void reflects_the_group_cache_at_call_time() {
        assertThat(providers.groupsOf(alice)).isEmpty();

        group(1, "Amis", alice, alice);

        assertThat(providers.groupsOf(alice)).isEqualTo(List.of("Amis"));
    }
}
