import java.util.logging.Level;
import java.util.logging.Logger;

public class TestDriver {

    private static final Logger LOGGER = Logger.getLogger(TestDriver.class.getName());

    private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";

    public static void main(String[] args) {
        try {
            Class.forName(DRIVER_CLASS_NAME); // Загружаем класс драйвера PostgreSQL
            LOGGER.log(Level.INFO, "Драйвер {0} успешно загружен.", DRIVER_CLASS_NAME); // Логируем успешную загрузку
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Не удалось загрузить драйвер {0}.", DRIVER_CLASS_NAME); // Логируем ошибку загрузки
        }
    }
}