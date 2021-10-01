package cz.muni.ics.kypo.userandgroup.security.util;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);
    private static final Gson gson = new Gson();

    public JsonUtils() {
    }

    public static String getAsString(JsonObject o, String member) {
        if (o.has(member)) {
            JsonElement e = o.get(member);
            return e != null && e.isJsonPrimitive() ? e.getAsString() : null;
        } else {
            return null;
        }
    }

    public static Boolean getAsBoolean(JsonObject o, String member) {
        if (o.has(member)) {
            JsonElement e = o.get(member);
            return e != null && e.isJsonPrimitive() ? e.getAsBoolean() : null;
        } else {
            return null;
        }
    }

    public static List<String> getAsStringList(JsonObject o, String member) throws JsonSyntaxException {
        if (o.has(member)) {
            return o.get(member).isJsonArray() ? (List) gson.fromJson(o.get(member), (new TypeToken<List<String>>() {
            }).getType()) : Lists.newArrayList(new String[]{o.get(member).getAsString()});
        } else {
            return null;
        }
    }

    public static List<JWSAlgorithm> getAsJwsAlgorithmList(JsonObject o, String member) {
        List<String> strings = getAsStringList(o, member);
        if (strings == null) {
            return null;
        } else {
            List<JWSAlgorithm> algs = new ArrayList<>();
            for (String alg : strings) {
                algs.add(JWSAlgorithm.parse(alg));
            }
            return algs;
        }
    }

    public static List<JWEAlgorithm> getAsJweAlgorithmList(JsonObject o, String member) {
        List<String> strings = getAsStringList(o, member);
        if (strings == null) {
            return null;
        } else {
            List<JWEAlgorithm> algs = new ArrayList<>();

            for (String alg : strings) {
                algs.add(JWEAlgorithm.parse(alg));
            }

            return algs;
        }
    }

    public static List<EncryptionMethod> getAsEncryptionMethodList(JsonObject o, String member) {
        List<String> strings = getAsStringList(o, member);
        if (strings == null) {
            return null;
        } else {
            List<EncryptionMethod> algs = new ArrayList<>();

            for (String alg : strings) {
                algs.add(EncryptionMethod.parse(alg));
            }

            return algs;
        }
    }
}
