/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.mssql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import io.airbyte.commons.json.Jsons;
import io.airbyte.commons.string.Strings;
import io.airbyte.db.Database;
import io.airbyte.db.Databases;
import io.airbyte.integrations.base.JavaBaseConstants;
import io.airbyte.integrations.destination.ExtendedNameTransformer;
import io.airbyte.integrations.standardtest.destination.DestinationAcceptanceTest;
import io.airbyte.integrations.standardtest.destination.comparator.TestDataComparator;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.Record;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MSSQLServerContainer;

public class MSSQLDestinationAcceptanceTest extends DestinationAcceptanceTest {

  private static MSSQLServerContainer<?> db;
  private final ExtendedNameTransformer namingResolver = new ExtendedNameTransformer();
  private final ObjectMapper mapper = new ObjectMapper();
  private JsonNode configWithoutDbName;
  private JsonNode config;

  @Override
  protected String getImageName() {
    return "airbyte/destination-mssql:dev";
  }

  @Override
  protected boolean supportsDBT() {
    return true;
  }

  @Override
  protected boolean supportsNormalization() {
    return true;
  }

  private JsonNode getConfig(final MSSQLServerContainer<?> db) {
    return Jsons.jsonNode(ImmutableMap.builder()
        .put("host", db.getHost())
        .put("port", db.getFirstMappedPort())
        .put("username", db.getUsername())
        .put("password", db.getPassword())
        .put("schema", "test_schema")
        .build());
  }

  @Override
  protected JsonNode getConfig() {
    return config;
  }

  @Override
  protected JsonNode getFailCheckConfig() {
    return Jsons.jsonNode(ImmutableMap.builder()
        .put("host", db.getHost())
        .put("username", db.getUsername())
        .put("password", "wrong password")
        .put("schema", "public")
        .put("port", db.getFirstMappedPort())
        .put("ssl", false)
        .build());
  }

  @Override
  protected List<JsonNode> retrieveRecords(final TestDestinationEnv env,
                                           final String streamName,
                                           final String namespace,
                                           final JsonNode streamSchema)
      throws Exception {
    return retrieveRecordsFromTable(namingResolver.getRawTableName(streamName), namespace)
        .stream()
        .map(r -> r.get(JavaBaseConstants.COLUMN_NAME_DATA))
        .collect(Collectors.toList());
  }

  @Override
  protected boolean implementsNamespaces() {
    return true;
  }

  @Override
  protected List<JsonNode> retrieveNormalizedRecords(final TestDestinationEnv env, final String streamName, final String namespace)
      throws Exception {
    final String tableName = namingResolver.getIdentifier(streamName);
    return retrieveRecordsFromTable(tableName, namespace);
  }

  private List<JsonNode> retrieveRecordsFromTable(final String tableName, final String schemaName) throws SQLException {
    return Databases.createSqlServerDatabase(db.getUsername(), db.getPassword(),
        db.getJdbcUrl()).query(
            ctx -> {
              ctx.fetch(String.format("USE %s;", config.get("database")));
              return ctx
                  .fetch(String.format("SELECT * FROM %s.%s ORDER BY %s ASC;", schemaName, tableName, JavaBaseConstants.COLUMN_NAME_EMITTED_AT))
                  .stream()
                  .map(this::getJsonFromRecord)
                  .collect(Collectors.toList());
            });
  }

  private JsonNode getJsonFromRecord(Record record) {
    ObjectNode node = mapper.createObjectNode();

    Arrays.stream(record.fields()).forEach(field -> {
      var value = record.get(field);

      switch (field.getDataType().getTypeName()) {
        case "nvarchar":
          var stringValue = (String) value;
          if (stringValue != null && (stringValue.replaceAll("[^\\x00-\\x7F]", "").matches("^\\[.*\\]$")
              || stringValue.replaceAll("[^\\x00-\\x7F]", "").matches("^\\{.*\\}$"))) {
            node.set(field.getName(), Jsons.deserialize(stringValue));
          } else {
            node.put(field.getName(), stringValue);
          }
          break;
        default:
          node.put(field.getName(), (value != null ? value.toString() : null));
      }
    });
    return node;
  }

  @BeforeAll
  protected static void init() {
    db = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2019-GA-ubuntu-16.04").acceptLicense();
    db.start();
  }

  private static Database getDatabase(final JsonNode config) {
    // todo (cgardens) - rework this abstraction so that we do not have to pass a null into the
    // constructor. at least explicitly handle it, even if the impl doesn't change.
    return Databases.createDatabase(
        config.get("username").asText(),
        config.get("password").asText(),
        String.format("jdbc:sqlserver://%s:%s",
            config.get("host").asText(),
            config.get("port").asInt()),
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        null);
  }

  // how to interact with the mssql test container manaully.
  // 1. exec into mssql container (not the test container container)
  // 2. /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P "A_Str0ng_Required_Password"
  @Override
  protected void setup(final TestDestinationEnv testEnv) throws SQLException {
    configWithoutDbName = getConfig(db);
    final String dbName = Strings.addRandomSuffix("db", "_", 10);

    final Database database = getDatabase(configWithoutDbName);
    database.query(ctx -> {
      ctx.fetch(String.format("CREATE DATABASE %s;", dbName));
      ctx.fetch(String.format("USE %s;", dbName));
      ctx.fetch("CREATE TABLE id_and_name(id INTEGER NOT NULL, name VARCHAR(200), born DATETIMEOFFSET(7));");
      ctx.fetch(
          "INSERT INTO id_and_name (id, name, born) VALUES (1,'picard', '2124-03-04T01:01:01Z'),  (2, 'crusher', '2124-03-04T01:01:01Z'), (3, 'vash', '2124-03-04T01:01:01Z');");
      return null;
    });

    config = Jsons.clone(configWithoutDbName);
    ((ObjectNode) config).put("database", dbName);
  }

  @Override
  protected void tearDown(final TestDestinationEnv testEnv) {}

  @Override
  protected TestDataComparator getTestDataComparator() {
    return new MSSQLTestDataComparator();
  }

  @Override
  protected boolean supportBasicDataTypeTest() {
    return true;
  }

  @Override
  protected boolean supportArrayDataTypeTest() {
    return true;
  }

  @Override
  protected boolean supportObjectDataTypeTest() {
    return true;
  }

  @AfterAll
  static void cleanUp() {
    db.stop();
    db.close();
  }

}
