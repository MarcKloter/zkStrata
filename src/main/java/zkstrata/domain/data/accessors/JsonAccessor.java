package zkstrata.domain.data.accessors;

import org.json.JSONArray;
import org.json.JSONObject;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.custom.HexLiteral;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonAccessor implements ValueAccessor {
    private String filename;
    private JSONObject jsonObject;

    public JsonAccessor(String filename) {
        this.filename = filename;

        try {
            this.jsonObject = new JSONObject(Files.readString(Path.of(filename), StandardCharsets.UTF_8));
        } catch (IOException e) {
            String msg = String.format("Unable to read file %s.", filename);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public Value getValue(Selector selector) {
        Object object = this.jsonObject;
        for (String key : selector.getSelectors()) {
            if (object instanceof JSONObject)
                if (((JSONObject) object).has(key))
                    object = ((JSONObject) object).get(key);
                else
                    return null;
            else
                return null;
        }

        if (object instanceof JSONObject)
            return null;

        if (object instanceof JSONArray)
            object = ((JSONArray) object).toList();

        if (object instanceof Integer)
            return new Literal(BigInteger.valueOf((Integer) object));

        if (object instanceof String && ((String) object).startsWith("0x"))
            return new HexLiteral((String) object);

        return new Literal(object);
    }

    @Override
    public String getSource() {
        return filename;
    }
}
