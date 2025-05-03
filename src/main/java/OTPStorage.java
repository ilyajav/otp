import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OTPStorage {

    private static final Logger LOGGER = Logger.getLogger(OTPStorage.class.getName()); // Логгер

    /**
     * Получает секретный ключ для пользователя из базы данных.
     *
     * @param userId ID пользователя.
     * @return Секретный ключ в виде массива байтов или null, если ключ не найден.
     */
    public static byte[] getSecretKey(int userId) {
        final String sql = "SELECT secret_key FROM secrets WHERE user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] secretKey = rs.getBytes("secret_key");
                    LOGGER.log(Level.FINE, "Секретный ключ получен для пользователя: {0}", userId);
                    return secretKey;
                } else {
                    LOGGER.log(Level.INFO, "Секретный ключ не найден для пользователя: {0}", userId);
                    return null;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при получении секретного ключа для пользователя {0}: {1}", new Object[]{userId, e.getMessage()});
            return null; // Важно: вернуть null при ошибке, чтобы не сломать логику выше
        }
    }

    /**
     * Сохраняет секретный ключ для пользователя в базе данных.
     * Если ключ уже существует, он будет обновлен.
     *
     * @param userId    ID пользователя.
     * @param secretKey Секретный ключ в виде массива байтов.
     */
    public static void saveSecretKey(int userId, byte[] secretKey) {
        final String sql = "INSERT INTO secrets (user_id, secret_key) VALUES (?, ?) " +
                "ON CONFLICT (user_id) DO UPDATE SET secret_key = EXCLUDED.secret_key";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setBytes(2, secretKey);
            stmt.executeUpdate();
            LOGGER.log(Level.INFO, "Секретный ключ сохранен для пользователя: {0}", userId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при сохранении секретного ключа для пользователя {0}: {1}", new Object[]{userId, e.getMessage()});
        }
    }
}