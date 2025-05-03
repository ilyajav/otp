import org.mindrot.jbcrypt.BCrypt;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PasswordUtils {

    private static final Logger LOGGER = Logger.getLogger(PasswordUtils.class.getName());

    // Хеширует пароль с использованием bcrypt
    public static String hashPassword(String password) {
        try{
            return BCrypt.hashpw(password, BCrypt.gensalt()); // Генерируем соль и хешируем пароль
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "Ошибка при хешировании пароля", e);
            throw new RuntimeException("Ошибка при хешировании пароля", e);
        }

    }

    // Проверяет, соответствует ли введенный пароль захешированному
    public static boolean verifyPassword(String password, String hashedPassword) {
        try{
            return BCrypt.checkpw(password, hashedPassword);  // Сравниваем введенный пароль с хешем
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "Ошибка при проверке пароля", e);
            return false; // В случае ошибки считаем, что пароль неверный
        }

    }
}
