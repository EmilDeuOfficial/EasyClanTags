package de.emilo.easyclantags.storage;

import de.emilo.easyclantags.model.ClanData;
import de.emilo.easyclantags.model.TagFormats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public final class SqliteStorage implements Storage {

    private final Path file;
    private final Driver driver;

    public SqliteStorage(Path file, ClassLoader loader) throws IOException {
        this.file = file;
        try {
            this.driver = (Driver) Class.forName("org.sqlite.JDBC", true, loader).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | LinkageError e) {
            throw new IOException("SQLite driver is not available: " + e.getMessage(), e);
        }
        try (Connection connection = connect()) {
            createTables(connection);
        } catch (SQLException e) {
            throw new IOException("Could not open SQLite database: " + e.getMessage(), e);
        }
    }

    private Connection connect() throws SQLException {
        Path parent = file.toAbsolutePath().getParent();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new SQLException("Could not create database folder", e);
        }
        Connection connection = driver.connect("jdbc:sqlite:" + file.toAbsolutePath(), new Properties());
        if (connection == null) {
            throw new SQLException("Driver rejected the database URL");
        }
        return connection;
    }

    private void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clans ("
                    + "id TEXT PRIMARY KEY, name TEXT NOT NULL, owner TEXT NOT NULL, created INTEGER NOT NULL, "
                    + "tag_changed INTEGER NOT NULL, tag_text TEXT NOT NULL, tag_colors TEXT NOT NULL, "
                    + "tag_formats TEXT NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS members ("
                    + "clan_id TEXT NOT NULL, player_id TEXT NOT NULL, name TEXT NOT NULL, "
                    + "position INTEGER NOT NULL, PRIMARY KEY (clan_id, player_id))");
        }
    }

    @Override
    public List<ClanData> load() throws IOException {
        Map<UUID, Map<UUID, String>> memberMap = new LinkedHashMap<>();
        List<Object[]> rows = new ArrayList<>();
        try (Connection connection = connect()) {
            try (Statement statement = connection.createStatement();
                 ResultSet set = statement.executeQuery("SELECT clan_id, player_id, name FROM members ORDER BY position")) {
                while (set.next()) {
                    memberMap.computeIfAbsent(UUID.fromString(set.getString(1)), key -> new LinkedHashMap<>())
                            .put(UUID.fromString(set.getString(2)), set.getString(3));
                }
            }
            try (Statement statement = connection.createStatement();
                 ResultSet set = statement.executeQuery("SELECT id, name, owner, created, tag_changed, tag_text, "
                         + "tag_colors, tag_formats FROM clans")) {
                while (set.next()) {
                    rows.add(new Object[]{set.getString(1), set.getString(2), set.getString(3), set.getLong(4),
                            set.getLong(5), set.getString(6), set.getString(7), set.getString(8)});
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new IOException("Could not read clans from SQLite: " + e.getMessage(), e);
        }
        List<ClanData> result = new ArrayList<>();
        for (Object[] row : rows) {
            UUID id = UUID.fromString((String) row[0]);
            TagFormats formats = TagCodec.formatsFromString((String) row[7]);
            result.add(new ClanData(id, (String) row[1], UUID.fromString((String) row[2]),
                    memberMap.getOrDefault(id, Collections.emptyMap()),
                    TagCodec.build((String) row[5], TagCodec.colorsFromString((String) row[6]), formats),
                    (Long) row[4], (Long) row[3]));
        }
        return result;
    }

    @Override
    public void save(Collection<ClanData> clans) throws IOException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DELETE FROM members");
                    statement.executeUpdate("DELETE FROM clans");
                }
                try (PreparedStatement clanStatement = connection.prepareStatement(
                        "INSERT INTO clans (id, name, owner, created, tag_changed, tag_text, tag_colors, tag_formats) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                     PreparedStatement memberStatement = connection.prepareStatement(
                             "INSERT INTO members (clan_id, player_id, name, position) VALUES (?, ?, ?, ?)")) {
                    for (ClanData clan : clans) {
                        clanStatement.setString(1, clan.id().toString());
                        clanStatement.setString(2, clan.name());
                        clanStatement.setString(3, clan.owner().toString());
                        clanStatement.setLong(4, clan.createdAt());
                        clanStatement.setLong(5, clan.tagChangedAt());
                        clanStatement.setString(6, clan.tag().plain());
                        clanStatement.setString(7, TagCodec.colorsToString(clan.tag()));
                        clanStatement.setString(8, TagCodec.formatsToString(clan.tag().formats()));
                        clanStatement.addBatch();
                        int position = 0;
                        for (Map.Entry<UUID, String> member : clan.members().entrySet()) {
                            memberStatement.setString(1, clan.id().toString());
                            memberStatement.setString(2, member.getKey().toString());
                            memberStatement.setString(3, member.getValue());
                            memberStatement.setInt(4, position++);
                            memberStatement.addBatch();
                        }
                    }
                    clanStatement.executeBatch();
                    memberStatement.executeBatch();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IOException("Could not write clans to SQLite: " + e.getMessage(), e);
        }
    }

    @Override
    public String describe() {
        return "SQLite (" + file.getFileName() + ")";
    }
}
