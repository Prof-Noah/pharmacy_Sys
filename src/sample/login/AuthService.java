package sample.login;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AuthService {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH_BITS = 256;
    private static final int ITERATIONS = 120_000;
    public static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;
    private static final String LEGACY_PHARMACY_NAME = "FCI PHARMA CARE";

    private final Path usersFile = Paths.get("Data", "users.txt");
    private final SecureRandom secureRandom = new SecureRandom();

    public UserAccount register(
            String pharmacyName,
            String username,
            String password
    ) throws IOException, GeneralSecurityException {
        String normalizedPharmacyName = pharmacyName.trim();
        String normalizedUsername = username.trim();

        if (normalizedPharmacyName.isEmpty()
                || normalizedUsername.isEmpty()
                || usernameExists(normalizedUsername)) {
            return null;
        }

        byte[] salt = newSalt();
        byte[] passwordHash = hashPassword(password, salt);
        String record = serialize(
                normalizedUsername,
                normalizedPharmacyName,
                DEFAULT_LOW_STOCK_THRESHOLD,
                salt,
                passwordHash
        );

        ensureParentDirectory();
        Files.write(
                usersFile,
                record.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
        return new UserAccount(
                normalizedUsername,
                normalizedPharmacyName,
                DEFAULT_LOW_STOCK_THRESHOLD
        );
    }

    public UserAccount authenticate(String username, String password)
            throws IOException, GeneralSecurityException {
        String normalizedUsername = username.trim();
        if (normalizedUsername.isEmpty() || password.isEmpty() || !Files.exists(usersFile)) {
            return null;
        }

        for (String line : Files.readAllLines(usersFile, StandardCharsets.UTF_8)) {
            UserRecord record = parseRecord(line);
            if (record == null || !record.username.equalsIgnoreCase(normalizedUsername)) {
                continue;
            }

            byte[] passwordHash = hashPassword(password, record.salt);
            if (MessageDigest.isEqual(record.passwordHash, passwordHash)) {
                return new UserAccount(
                        record.username,
                        record.pharmacyName,
                        record.lowStockThreshold
                );
            }
            return null;
        }
        return null;
    }

    public UpdateStatus updateAccount(
            UserAccount account,
            String currentPassword,
            String pharmacyName,
            String username,
            int lowStockThreshold,
            String newPassword
    ) throws IOException, GeneralSecurityException {
        if (!Files.exists(usersFile)) {
            return UpdateStatus.ACCOUNT_NOT_FOUND;
        }

        String normalizedPharmacyName = pharmacyName.trim();
        String normalizedUsername = username.trim();
        if (normalizedPharmacyName.isEmpty() || normalizedUsername.isEmpty()
                || lowStockThreshold < 0 || lowStockThreshold > 1_000_000) {
            return UpdateStatus.INVALID_DETAILS;
        }

        List<String> lines = Files.readAllLines(usersFile, StandardCharsets.UTF_8);
        int accountLine = -1;
        UserRecord currentRecord = null;
        for (int i = 0; i < lines.size(); i++) {
            UserRecord record = parseRecord(lines.get(i));
            if (record != null && record.username.equalsIgnoreCase(account.getUsername())) {
                accountLine = i;
                currentRecord = record;
                break;
            }
        }

        if (currentRecord == null) {
            return UpdateStatus.ACCOUNT_NOT_FOUND;
        }

        byte[] currentHash = hashPassword(currentPassword, currentRecord.salt);
        if (!MessageDigest.isEqual(currentRecord.passwordHash, currentHash)) {
            return UpdateStatus.INVALID_CREDENTIALS;
        }

        for (int i = 0; i < lines.size(); i++) {
            if (i == accountLine) {
                continue;
            }
            UserRecord other = parseRecord(lines.get(i));
            if (other != null && other.username.equalsIgnoreCase(normalizedUsername)) {
                return UpdateStatus.USERNAME_TAKEN;
            }
        }

        byte[] salt = currentRecord.salt;
        byte[] passwordHash = currentRecord.passwordHash;
        if (!newPassword.isEmpty()) {
            salt = newSalt();
            passwordHash = hashPassword(newPassword, salt);
        }

        lines.set(
                accountLine,
                serialize(
                        normalizedUsername,
                        normalizedPharmacyName,
                        lowStockThreshold,
                        salt,
                        passwordHash
                )
        );
        replaceUsersFile(lines);
        return UpdateStatus.UPDATED;
    }

    public boolean deleteAccount(UserAccount account, String currentPassword)
            throws IOException, GeneralSecurityException {
        if (!Files.exists(usersFile)) {
            return false;
        }

        List<String> lines = Files.readAllLines(usersFile, StandardCharsets.UTF_8);
        List<String> remainingLines = new ArrayList<>();
        boolean deleted = false;

        for (String line : lines) {
            UserRecord record = parseRecord(line);
            if (!deleted && record != null
                    && record.username.equalsIgnoreCase(account.getUsername())) {
                byte[] currentHash = hashPassword(currentPassword, record.salt);
                if (!MessageDigest.isEqual(record.passwordHash, currentHash)) {
                    return false;
                }
                deleted = true;
            } else {
                remainingLines.add(line);
            }
        }

        if (deleted) {
            replaceUsersFile(remainingLines);
        }
        return deleted;
    }

    private boolean usernameExists(String username) throws IOException {
        if (!Files.exists(usersFile)) {
            return false;
        }
        for (String line : Files.readAllLines(usersFile, StandardCharsets.UTF_8)) {
            UserRecord record = parseRecord(line);
            if (record != null && record.username.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    private UserRecord parseRecord(String line) {
        String[] parts = line.split(":", -1);
        try {
            if (parts.length == 3) {
                return new UserRecord(
                        decodeString(parts[0]),
                        LEGACY_PHARMACY_NAME,
                        DEFAULT_LOW_STOCK_THRESHOLD,
                        decode(parts[1]),
                        decode(parts[2])
                );
            }
            if (parts.length == 4) {
                return new UserRecord(
                        decodeString(parts[0]),
                        decodeString(parts[1]),
                        DEFAULT_LOW_STOCK_THRESHOLD,
                        decode(parts[2]),
                        decode(parts[3])
                );
            }
            if (parts.length == 5) {
                return new UserRecord(
                        decodeString(parts[0]),
                        decodeString(parts[1]),
                        Integer.parseInt(parts[2]),
                        decode(parts[3]),
                        decode(parts[4])
                );
            }
        } catch (IllegalArgumentException ignored) {
            // Ignore malformed records while keeping other accounts usable.
        }
        return null;
    }

    private String serialize(
            String username,
            String pharmacyName,
            int lowStockThreshold,
            byte[] salt,
            byte[] passwordHash
    ) {
        return encode(username) + ":"
                + encode(pharmacyName) + ":"
                + lowStockThreshold + ":"
                + encode(salt) + ":"
                + encode(passwordHash)
                + System.lineSeparator();
    }

    private void ensureParentDirectory() throws IOException {
        Path parent = usersFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private void replaceUsersFile(List<String> lines) throws IOException {
        ensureParentDirectory();
        Path temporaryFile = Files.createTempFile(
                usersFile.getParent(),
                "users-",
                ".tmp"
        );
        try {
            Files.write(
                    temporaryFile,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            try {
                Files.move(
                        temporaryFile,
                        usersFile,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temporaryFile,
                        usersFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private byte[] newSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }

    private byte[] hashPassword(String password, byte[] salt)
            throws GeneralSecurityException {
        PBEKeySpec keySpec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                HASH_LENGTH_BITS
        );
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec)
                    .getEncoded();
        } finally {
            keySpec.clearPassword();
        }
    }

    private String encode(String value) {
        return encode(value.getBytes(StandardCharsets.UTF_8));
    }

    private String encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    private String decodeString(String value) {
        return new String(decode(value), StandardCharsets.UTF_8);
    }

    private byte[] decode(String value) {
        return Base64.getDecoder().decode(value);
    }

    public enum UpdateStatus {
        UPDATED,
        USERNAME_TAKEN,
        INVALID_CREDENTIALS,
        INVALID_DETAILS,
        ACCOUNT_NOT_FOUND
    }

    private static class UserRecord {
        private final String username;
        private final String pharmacyName;
        private final int lowStockThreshold;
        private final byte[] salt;
        private final byte[] passwordHash;

        private UserRecord(
                String username,
                String pharmacyName,
                int lowStockThreshold,
                byte[] salt,
                byte[] passwordHash
        ) {
            this.username = username;
            this.pharmacyName = pharmacyName;
            this.lowStockThreshold = lowStockThreshold;
            this.salt = salt;
            this.passwordHash = passwordHash;
        }
    }
}
