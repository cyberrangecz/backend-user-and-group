package cz.cyberrange.platform.userandgroup.security.util;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Reads typed values out of a Gson {@link JsonObject}. */
public class JsonUtils {
  private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);
  private static final Gson gson = new Gson();

  public JsonUtils() {}

  /**
   * Returns the given member's value as a string.
   *
   * @param o the JSON object to read from
   * @param member name of the member to read
   * @return the member's value as a string, or null when the member is absent or not a primitive
   *     value
   */
  public static String getAsString(JsonObject o, String member) {
    if (o.has(member)) {
      JsonElement e = o.get(member);
      return e != null && e.isJsonPrimitive() ? e.getAsString() : null;
    } else {
      return null;
    }
  }

  /**
   * Returns the given member's value as a boolean.
   *
   * @param o the JSON object to read from
   * @param member name of the member to read
   * @return the member's value as a boolean, or null when the member is absent or not a primitive
   *     value
   */
  public static Boolean getAsBoolean(JsonObject o, String member) {
    if (o.has(member)) {
      JsonElement e = o.get(member);
      return e != null && e.isJsonPrimitive() ? e.getAsBoolean() : null;
    } else {
      return null;
    }
  }

  /**
   * Returns the given member's value as a list of strings. A value that is not a JSON array is
   * wrapped into a single-element list.
   *
   * @param o the JSON object to read from
   * @param member name of the member to read
   * @return the member's value as a list of strings, or null when the member is absent
   * @throws JsonSyntaxException when the member's value cannot be parsed as strings
   */
  public static List<String> getAsStringList(JsonObject o, String member)
      throws JsonSyntaxException {
    if (o.has(member)) {
      return o.get(member).isJsonArray()
          ? (List) gson.fromJson(o.get(member), (new TypeToken<List<String>>() {}).getType())
          : Lists.newArrayList(o.get(member).getAsString());
    } else {
      return null;
    }
  }

  /**
   * Returns the given member's value as a list of JWS algorithms.
   *
   * @param o the JSON object to read from
   * @param member name of the member to read
   * @return the parsed algorithms, or null when the member is absent
   */
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

  /**
   * Returns the given member's value as a list of JWE algorithms.
   *
   * @param o the JSON object to read from
   * @param member name of the member to read
   * @return the parsed algorithms, or null when the member is absent
   */
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

  /**
   * Returns the given member's value as a list of encryption methods.
   *
   * @param o the JSON object to read from
   * @param member name of the member to read
   * @return the parsed encryption methods, or null when the member is absent
   */
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
