import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OTPService {

    private static final Logger LOGGER = Logger.getLogger(OTPService.class.getName());

    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final int OTP_EXPIRATION_DAYS = 1;

    /**
     * Сохраняет OTP-код в базе данных для указанного пользователя.
     *
     * @param userId ID пользователя.
     * @param code   OTP-код, который нужно сохранить.
     * @throws SQLException Если произошла ошибка при работе с базой данных.
     */
    public static void saveOTP(int userId, String code) throws SQLException {
        final String sql = "INSERT INTO otp_codes (user_id, code, generation_time, is_used) VALUES (?, ?, NOW(), FALSE)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, code);
            stmt.executeUpdate();
            LOGGER.log(Level.INFO, "OTP сохранен для пользователя: {0}", userId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при сохранении OTP для пользователя {0}: {1}", new Object[]{userId, e.getMessage()});
            throw e;
        }
    }

    /**
     * Проверяет OTP-код для указанного пользователя и помечает его как использованный,
     * если код верен и не был использован ранее, и срок его действия не истек.
     *
     * @param userId ID пользователя.
     * @param code   OTP-код, который нужно проверить.
     * @return true, если OTP-код верен и успешно помечен как использованный, иначе false.
     * @throws SQLException Если произошла ошибка при работе с базой данных.
     */
    public static boolean validateOTP(int userId, String code) throws SQLException {
        final String sql = "UPDATE otp_codes SET is_used = TRUE " +
                "WHERE user_id = ? AND code = ? AND is_used = FALSE " +
                "AND generation_time > (NOW() - INTERVAL '" + OTP_VALIDITY_MINUTES + " minutes') " + // Используем константу
                "RETURNING id";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, code);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean isValid = rs.next();
                if (isValid) {
                    LOGGER.log(Level.INFO, "OTP верен и помечен как использованный для пользователя: {0}", userId);
                } else {
                    LOGGER.log(Level.WARNING, "Неверный или устаревший OTP для пользователя: {0}", userId);
                }
                return isValid;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при валидации OTP для пользователя {0}: {1}", new Object[]{userId, e.getMessage()});
            throw e;
        }
    }

    /**
     * Удаляет из базы данных все OTP-коды, срок действия которых истек (старше 1 дня).
     *
     * @throws SQLException Если произошла ошибка при работе с базой данных.
     */
    public static void cleanupExpiredOTPs() throws SQLException {
        final String sql = "DELETE FROM otp_codes WHERE generation_time < (NOW() - INTERVAL '" + OTP_EXPIRATION_DAYS + " day')";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            int rowsDeleted = stmt.executeUpdate(sql);
            LOGGER.log(Level.INFO, "Удалено {0} устаревших OTP кодов.", rowsDeleted);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при удалении устаревших OTP кодов: {0}", e.getMessage());
            throw e;
        }
    }
}