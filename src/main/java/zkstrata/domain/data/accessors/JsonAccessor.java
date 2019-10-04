package zkstrata.domain.data.accessors;

import org.json.JSONObject;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonAccessor implements ValueAccessor {
    private String subject;
    private JSONObject object;

    public JsonAccessor(String subject, String filename) {
        this.subject = subject;

        try {
            this.object = new JSONObject(Files.readString(Path.of(filename), StandardCharsets.UTF_8));
        } catch (IOException e) {
            String msg = String.format("Unable to read file %s.", filename);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public Value getValue(Selector selector) {
        // TODO: access object
        for (String key : selector.getSelectors()) {
            Object value = object.get(key);
            // TODO: check whether we can continue to call .get() on this (or replace for loop with something else)!
            // TODO: check type (JSON has limited types)
        }
        return null;
    }
}
