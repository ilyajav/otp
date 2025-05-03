import org.jsmpp.bean.*;
import org.jsmpp.session.*;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmppService {

    private static final Logger LOGGER = Logger.getLogger(SmppService.class.getName());

    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final TypeOfNumber ton;
    private final NumberingPlanIndicator npi;
    private final String sourceAddress;

    public SmppService(String host, int port, String systemId, String password,
                       String systemType, TypeOfNumber ton, NumberingPlanIndicator npi,
                       String sourceAddress) {
        this.host = host;
        this.port = port;
        this.systemId = systemId;
        this.password = password;
        this.systemType = systemType;
        this.ton = ton;
        this.npi = npi;
        this.sourceAddress = sourceAddress;
    }

    public void sendSms(String phoneNumber, String message) throws Exception {
        SMPPSession session = new SMPPSession();
        try {
            BindParameter bindParameter = new BindParameter(BindType.BIND_TX, systemId, password,
                    systemType, ton, npi, null);
            session.connectAndBind(host, port, bindParameter);

            String messageId = session.submitShortMessage(
                    "CMT", // Service type
                    ton,            // Source address TON
                    npi,            // Source address NPI
                    sourceAddress,  // Source address
                    ton,            // Destination address TON
                    npi,            // Destination address NPI
                    phoneNumber,  // Destination address
                    new ESMClass(),  // ESM class
                    (byte) 0,       // Protocol ID
                    (byte) 1,       // Priority flag
                    null,           // Schedule delivery time
                    null,           // Validity period
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT), // Registered delivery
                    (byte) 0,       // Replace if present flag
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false), // Data coding
                    (byte) 0,       // SM default msg id
                    message.getBytes(StandardCharsets.UTF_8) // Short message bytes
            );

            LOGGER.log(Level.INFO, "SMS sent: Message ID = {0}, Phone Number = {1}", new Object[]{messageId, phoneNumber});

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending SMS to " + phoneNumber, e);
            throw e;
        } finally {
            try {
                session.unbindAndClose();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to unbind and close SMPP session", e);
            }
        }
    }
}