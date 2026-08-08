package fr.florianpal.fmessage.core.state;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SessionStateTest {

    private final SessionState state = new SessionState();
    private final UUID alice = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();

    @Test
    void has_no_reply_target_until_a_conversation_happened() {
        assertThat(state.hasPreviousPlayer(alice)).isFalse();
        assertThat(state.getPreviousPlayer(alice)).isEmpty();
    }

    @Test
    void remembers_the_reply_target() {
        state.setPreviousPlayer(alice, bob);

        assertThat(state.hasPreviousPlayer(alice)).isTrue();
        assertThat(state.getPreviousPlayer(alice)).contains(bob);
    }

    @Test
    void overwrites_the_reply_target_on_a_newer_conversation() {
        UUID carol = UUID.randomUUID();
        state.setPreviousPlayer(alice, bob);
        state.setPreviousPlayer(alice, carol);

        assertThat(state.getPreviousPlayer(alice)).contains(carol);
    }

    @Test
    void toggles_spy_mode() {
        assertThat(state.isSpy(alice)).isFalse();

        state.addSpy(alice);
        assertThat(state.isSpy(alice)).isTrue();
        assertThat(state.getSpies()).containsExactly(alice);

        state.removeSpy(alice);
        assertThat(state.isSpy(alice)).isFalse();
    }

    @Test
    void exposes_spies_as_an_unmodifiable_view() {
        state.addSpy(alice);

        assertThatCode(() -> state.getSpies().add(bob))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void toggles_message_muting() {
        assertThat(state.hasMessagesDisabled(alice)).isFalse();

        state.disableMessages(alice);
        assertThat(state.hasMessagesDisabled(alice)).isTrue();

        state.enableMessages(alice);
        assertThat(state.hasMessagesDisabled(alice)).isFalse();
    }

    @Test
    void keeps_players_independent() {
        state.disableMessages(alice);
        state.addSpy(bob);

        assertThat(state.hasMessagesDisabled(bob)).isFalse();
        assertThat(state.isSpy(alice)).isFalse();
    }

    /**
     * The previous implementations used ArrayList and HashMap while being written from
     * proxy network threads: this is the race that used to be possible.
     */
    @Test
    void survives_concurrent_writes_and_reads() throws InterruptedException {
        int threads = 8;
        int perThread = 500;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < perThread; i++) {
                        UUID uuid = UUID.randomUUID();
                        state.addSpy(uuid);
                        state.disableMessages(uuid);
                        state.setPreviousPlayer(uuid, alice);
                        state.getSpies().forEach(ignored -> {
                        });
                        state.removeSpy(uuid);
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();
        assertThat(state.getSpies()).isEmpty();
    }
}
