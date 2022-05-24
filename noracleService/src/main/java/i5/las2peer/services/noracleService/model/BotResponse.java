package i5.las2peer.services.noracleService.model;

import java.io.Serial;
import java.io.Serializable;

public class BotResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -2348162648183479462L;

    // Text that is send via the chat to the user
    private String text;

    // Determines if the communication between the bot and the service should be hold
    private boolean closeContext;

    public BotResponse() {
        this.text = "default text";
        this.closeContext = true;
    }

    public BotResponse(String text, boolean closeContext) {
        this.text = text;
        this.closeContext = closeContext;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCloseContext() {
        return closeContext;
    }

    public void setCloseContext(boolean closeContext) {
        this.closeContext = closeContext;
    }
}
