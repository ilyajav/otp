import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TOTPGenerator {

    private static final Logger LOGGER = Logger.getLogger(TOTPGenerator.class.getName()); // Логгер
    private static final String HMAC_ALGORITHM  = "HmacSHA1"; // Алгоритм HMAC
    private static final int    TIME_STEP       = 30;        // Шаг времени в секундах
    private static final int    CODE_DIGITS     = 6;         // Количество цифр в OTP
    private static final int    SECRET_KEY_SIZE = 20;         // Размер секретного ключа в байтах

    private final byte[] secretKey; // Секретный ключ

    public TOTPGenerator(byte[] secretKey) {
        this.secretKey = secretKey.clone(); // Клонирование для безопасности
    }

    public static byte[] generateSecretKey() {
        byte[] key = new byte[SECRET_KEY_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return key;
    }

    public String generateTOTP() {
        long timeCounter = System.currentTimeMillis() / 1000 / TIME_STEP;
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeCounter).array();

        try {
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, HMAC_ALGORITHM);
            hmac.init(keySpec);
            byte[] hash = hmac.doFinal(timeBytes);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при генерации TOTP", e); // Логирование ошибки
            throw new RuntimeException("Ошибка при генерации TOTP", e);
        }
    }

    public String generateAndSaveTOTP(int userId) throws SQLException {
        String otp = generateTOTP();
        try {
            OTPService.saveOTP(userId, otp);
            FileOTPService.saveOTP(userId, otp);
            LOGGER.log(Level.INFO, "TOTP сгенерирован и сохранен для пользователя: " + userId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при сохранении TOTP для пользователя: " + userId, e);
            throw e;
        }
        return otp;
    }

    public boolean validateAndMarkUsed(int userId, String code) throws SQLException {
        boolean isValid = false;
        try {
            isValid = OTPService.validateOTP(userId, code);
            FileOTPService.logOTPValidation(userId, code, isValid);
            LOGGER.log(Level.INFO, "TOTP проверен для пользователя: " + userId + ", результат: " + isValid);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при валидации и пометке OTP для пользователя: " + userId, e);
            throw e;
        }
        return isValid;
    }

    public static String bytesToBase32(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base32ToBytes(String base32) {
        return Base64.getDecoder().decode(base32);
    }
}
