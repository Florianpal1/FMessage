package fr.florianpal.fmessage.fakes;

import fr.florianpal.fmessage.platform.ProxyBackendServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Records every payload the proxy pushes towards a backend server.
 */
public class FakeBackendServer implements ProxyBackendServer {

    private final String name;

    public final List<byte[]> sent = new ArrayList<>();

    public FakeBackendServer(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void sendData(byte[] payload) {
        sent.add(payload);
    }
}
