package zkstrata.domain.data.accessors;

import org.json.JSONArray;
import org.json.JSONObject;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Value;

import java.io.IOException;
import java.math.BigInteger;
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
        Object object = this.object;
        for (String key : selector.getSelectors()) {
            if (object instanceof JSONObject)
                object = ((JSONObject) object).get(key);
            else
                return null;
        }

        if (object instanceof JSONObject)
            return null;

        if (object instanceof JSONArray)
            object = ((JSONArray) object).toList();

        if (object instanceof Integer)
            return new Literal(BigInteger.valueOf((Integer) object));

        return new Literal(object);
    }
}
