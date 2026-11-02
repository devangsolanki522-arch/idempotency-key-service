
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class IdempotencyService {

    private final IdempotencyStore store;

    public IdempotencyService(IdempotencyStore store) {
        this.store = store;
    }

    public Result process(
            String idemKey,
            String requestBody
    ) throws SQLException {

        String requestHash;

        try {
            requestHash = Hasher.sha256(requestBody);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to hash request", e
            );
        }

        Optional<IdempotencyStore.StoredRecord> existing =
                store.find(idemKey);

        if (existing.isPresent()) {

            IdempotencyStore.StoredRecord record =
                    existing.get();

            // Check whether the existing record is still valid.
            if (record.expiresAt().isAfter(LocalDateTime.now())) {

                // Same key but different request body.
                if (!record.requestHash().equals(requestHash)) {
                    return new Result(
                            422,
                            "Idempotency key was already used with a different request"
                    );
                }

                // Same key and same request: return saved response.
                return new Result(
                        record.statusCode(),
                        record.responseBody()
                );
            }

            // The record has expired, so remove it.
            store.delete(idemKey);
        }

        // Generate a new response for a new or expired key.
        String responseBody =
                "{\"id\":\"" + UUID.randomUUID()
                        + "\",\"status\":\"SUCCESS\"}";

        int statusCode = 200;

        LocalDateTime expiresAt =
                LocalDateTime.now().plusHours(24);

        try {
            store.save(
                    idemKey,
                    requestHash,
                    responseBody,
                    statusCode,
                    expiresAt
            );

        } catch (SQLException e) {

            // Another request may have inserted this key first.
            Optional<IdempotencyStore.StoredRecord> retry =
                    store.find(idemKey);

            if (retry.isPresent()) {

                IdempotencyStore.StoredRecord record =
                        retry.get();

                if (!record.requestHash().equals(requestHash)) {
                    return new Result(
                            422,
                            "Idempotency key was already used with a different request"
                    );
                }

                return new Result(
                        record.statusCode(),
                        record.responseBody()
                );
            }

            throw e;
        }

        return new Result(statusCode, responseBody);
    }

    public record Result(
            int statusCode,
            String responseBody
    ) {
    }
}
