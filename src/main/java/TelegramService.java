import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TelegramService extends TelegramLongPollingBot {

    private static final Logger LOGGER = Logger.getLogger(TelegramService.class.getName()); // Логгер
    private static final String OTP_MESSAGE_TEMPLATE = "Ваш OTP код: %s\nДействителен в течение 5 минут."; // Шаблон сообщения с OTP

    private final String botUsername; // Имя пользователя бота в Telegram
    private final String botToken;    // Токен бота для доступа к Telegram API

    /**
     * Конструктор класса TelegramService.
     *
     * @param botToken    Токен бота для доступа к Telegram API.
     * @param botUsername Имя пользователя бота в Telegram.
     */
    public TelegramService(String botToken, String botUsername) {
        this.botToken = botToken;
        this.botUsername = botUsername;
    }

    /**
     * Возвращает имя пользователя бота.
     *
     * @return Имя пользователя бота.
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Возвращает токен бота.
     *
     * @return Токен бота.
     */
    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     * Обработчик входящих обновлений от Telegram.  В данном случае, пока ничего не обрабатывается.
     *
     * @param update Объект, содержащий информацию об обновлении.
     */
    @Override
    public void onUpdateReceived(Update update) {
        // TODO: Реализовать обработку входящих сообщений (если требуется)
    }

    /**
     * Отправляет OTP-код пользователю в Telegram.
     *
     * @param chatId  ID чата пользователя.
     * @param otpCode OTP-код, который нужно отправить.
     * @throws TelegramApiException Если произошла ошибка при отправке сообщения.
     */
    public void sendOTP(String chatId, String otpCode) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(String.format(OTP_MESSAGE_TEMPLATE, otpCode)); // Используем шаблон для формирования сообщения

        try {
            execute(message);
            LOGGER.log(Level.INFO, "OTP отправлен в чат: {0}", chatId); // Логируем успешную отправку
        } catch (TelegramApiException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при отправке OTP в чат: " + chatId, e); // Логируем ошибку
            throw e; // Пробрасываем исключение выше
        }
    }
}