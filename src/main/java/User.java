import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class User {

    private static final Logger LOGGER = Logger.getLogger(User.class.getName());

    private final int id;
    private final String username;
    private final String password;
    private final String email;
    private final String phone;
    private final String telegramChatId;
    private final boolean isAdmin;

    public User(int id, String username, String password, String email, String phone, String telegramChatId, boolean isAdmin) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "Имя пользователя не может быть пустым"); // Валидация
        this.password = Objects.requireNonNull(password, "Пароль не может быть пустым"); // Валидация
        this.email = email;
        this.phone = phone;
        this.telegramChatId = telegramChatId;
        this.isAdmin = isAdmin;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    @Deprecated
    public boolean checkPassword(String inputPassword) {
        LOGGER.log(Level.WARNING, "Warning");
        return password.equals(inputPassword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && isAdmin == user.isAdmin && Objects.equals(username, user.username) && Objects.equals(password, user.password) && Objects.equals(email, user.email) && Objects.equals(phone, user.phone) && Objects.equals(telegramChatId, user.telegramChatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, email, phone, telegramChatId, isAdmin);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", telegramChatId='" + telegramChatId + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}