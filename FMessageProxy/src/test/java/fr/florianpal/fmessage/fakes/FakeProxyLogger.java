package fr.florianpal.fmessage.fakes;

import fr.florianpal.fmessage.platform.ProxyLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Captures console output so tests can assert on it instead of watching it scroll by.
 */
public class FakeProxyLogger implements ProxyLogger {

    public final List<String> info = new ArrayList<>();
    public final List<String> warnings = new ArrayList<>();
    public final List<String> errors = new ArrayList<>();

    @Override
    public void info(String message) {
        info.add(message);
    }

    @Override
    public void warn(String message) {
        warnings.add(message);
    }

    @Override
    public void error(String message) {
        errors.add(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        errors.add(message);
    }
}
