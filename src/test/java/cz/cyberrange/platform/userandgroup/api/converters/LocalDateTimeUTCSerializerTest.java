package cz.cyberrange.platform.userandgroup.api.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.userandgroup.api.dto.group.GroupDTO;
import cz.cyberrange.platform.userandgroup.api.dto.group.NewGroupDTO;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

class LocalDateTimeUTCSerializerTest {

  private static final JsonMapper JSON_MAPPER =
      JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build();

  @ParameterizedTest
  @CsvSource({
    "2019-11-20T10:28:02.727, 2019-11-20T10:28:02.727Z",
    "2019-11-20T10:28:02, 2019-11-20T10:28:02.000Z",
    "2019-11-20T10:28:02.727345, 2019-11-20T10:28:02.727Z",
    "2019-11-20T10:28:02.000000001, 2019-11-20T10:28:02.000Z",
    "2019-11-20T10:28:00.5, 2019-11-20T10:28:00.500Z"
  })
  void writesExpirationDateAsUtcInstantWithThreeFractionalDigits(
      LocalDateTime expirationDate, String expectedWireValue) {
    GroupDTO group = new GroupDTO();
    group.setExpirationDate(expirationDate);

    String json = JSON_MAPPER.writeValueAsString(group);

    assertEquals(expectedWireValue, JSON_MAPPER.readTree(json).get("expiration_date").asString());
  }

  @Test
  void writesAbsentExpirationDateAsJsonNull() {
    String json = JSON_MAPPER.writeValueAsString(new GroupDTO());

    assertTrue(JSON_MAPPER.readTree(json).get("expiration_date").isNull());
  }

  @ParameterizedTest
  @CsvSource({
    "2019-11-20T10:28:02.727Z",
    "2019-11-20T10:28:02Z",
    "2019-11-20T10:28:02.727",
    "2019-11-20T10:28:02"
  })
  void readsExpirationDateWithOrWithoutZoneDesignator(String wireValue) {
    String body =
        "{\"name\":\"n\",\"description\":\"d\",\"expiration_date\":\"" + wireValue + "\"}";

    NewGroupDTO group = JSON_MAPPER.readValue(body, NewGroupDTO.class);

    assertEquals(LocalDateTime.parse(wireValue.replace("Z", "")), group.getExpirationDate());
  }
}
