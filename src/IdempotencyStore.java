
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyStore {

    Optional<StoredRecord> find(String idemKey)
            throws SQLException;

    void save(
            String idemKey,
            String requestHash,
            String responseBody,
            int statusCode,
            LocalDateTime expiresAt
    ) throws SQLException;

    void delete(String idemKey)
            throws SQLException;

    record StoredRecord(
            String idemKey,
            String requestHash,
            String responseBody,
            int statusCode,
            LocalDateTime expiresAt
    ) {
    }
}
