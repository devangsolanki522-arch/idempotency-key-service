
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class MySqlIdempotencyStore implements IdempotencyStore {

    @Override
    public Optional<StoredRecord> find(String idemKey) throws SQLException {

        String sql = "SELECT * FROM idempotency_keys WHERE idem_key = ?";

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, idemKey);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    String requestHash = resultSet.getString("request_hash");
                    String responseBody = resultSet.getString("response_body");
                    int statusCode = resultSet.getInt("status_code");

                    Timestamp timestamp = resultSet.getTimestamp("expires_at");
                    LocalDateTime expiresAt = timestamp.toLocalDateTime();

                    StoredRecord record = new StoredRecord(
                            resultSet.getString("idem_key"),
                            requestHash,
                            responseBody,
                            statusCode,
                            expiresAt
                    );

                    return Optional.of(record);
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public void save(
            String idemKey,
            String requestHash,
            String responseBody,
            int statusCode,
            LocalDateTime expiresAt
    ) throws SQLException {

        String sql = """
                INSERT INTO idempotency_keys
                (idem_key, request_hash, response_body, status_code, expires_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, idemKey);
            statement.setString(2, requestHash);
            statement.setString(3, responseBody);
            statement.setInt(4, statusCode);
            statement.setTimestamp(5, Timestamp.valueOf(expiresAt));

            statement.executeUpdate();
        }
    }

    @Override
    public void delete(String idemKey) throws SQLException {

        String sql = """
                DELETE FROM idempotency_keys
                WHERE idem_key = ?
                """;

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, idemKey);
            statement.executeUpdate();
        }
    }
}
