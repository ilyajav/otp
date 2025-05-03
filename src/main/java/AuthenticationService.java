import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;


public class AuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

    /**
     * Аутентифицирует пользователя по имени пользователя и паролю.
     *
     * @param username Имя пользователя.
     * @param password Пароль.
     * @return Объект User, если аутентификация прошла успешно, иначе null.
     * @throws SQLException Если произошла ошибка при работе с базой данных.
     */
    public User authenticate(String username, String password) throws SQLException {
        final String sql = "SELECT id, username, password, email, phone, telegram_chat_id, is_admin " +
                "FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Безопасность: Сравнение паролей следует выполнять с использованием хеширования с солью.
                    String hashedPassword = rs.getString("password"); // Получаем захешированный пароль из базы
                    if (PasswordUtils.verifyPassword(password, hashedPassword)) { // Используем утилиту для сравнения хешей
                        User user = new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                hashedPassword, // Сохраняем захешированный пароль в объекте User (не plain text)
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("telegram_chat_id"),
                                rs.getBoolean("is_admin")
                        );
                        LOGGER.log(Level.INFO, "Пользователь {0} успешно аутентифицирован.", username);
                        return user;
                    } else {
                        LOGGER.log(Level.WARNING, "Неверный пароль для пользователя {0}.", username);
                    }
                } else {
                    LOGGER.log(Level.INFO, "Пользователь {0} не найден.", username);
                }
            }

        } catch (SQLException e) {

            LOGGER.log(Level.SEVERE, "Ошибка при аутентификации пользователя {0}: {1}", new Object[]{username, e.getMessage()});
            throw e; // Пробрасываем исключение
        }

        return null; // Аутентификация не удалась
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param username Имя пользователя.
     * @param password Пароль.
     * @param email    Адрес электронной почты.
     * @param phone    Номер телефона.
     * @param telegramChatId ID чата Telegram.
     * @param isAdmin  Флаг, указывающий, является ли пользователь администратором.
     * @param creator  Пользователь, который регистрирует нового пользователя (может быть null).
     * @return true, если регистрация прошла успешно, иначе false.
     * @throws SQLException Если произошла ошибка при работе с базой данных.
     */
    public boolean register(String username, String password,
                            String email, String phone, String telegramChatId,
                            boolean isAdmin, User creator) throws SQLException {

        // Проверка прав доступа: только администратор может создавать администраторов
        if (creator != null && isAdmin && !creator.isAdmin()) {
            LOGGER.log(Level.WARNING, "Пользователь {0} попытался зарегистрировать администратора без прав.", creator.getUsername());
            return false;
        }

        final String sql = "INSERT INTO users (username, password, email, phone, telegram_chat_id, is_admin) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Безопасность: Хешируем пароль перед сохранением в базу данных
            String hashedPassword = PasswordUtils.hashPassword(password); // Используем утилиту для хеширования
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword); // Сохраняем захешированный пароль
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, telegramChatId);
            stmt.setBoolean(6, isAdmin);

            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;
            if (success) {
                LOGGER.log(Level.INFO, "Пользователь {0} успешно зарегистрирован.", username);
            } else {
                LOGGER.log(Level.WARNING, "Не удалось зарегистрировать пользователя {0}.", username);
            }
            return success;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при регистрации пользователя {0}: {1}", new Object[]{username, e.getMessage()});
            throw e; // Пробрасываем исключение
        }
    }

}

