package i5.las2peer.services.noracleService.model;

import java.io.Serial;
import java.io.Serializable;

public class BotRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 6240095834112330251L;

    // The message, the user send to the bot
    private String msg;
    // The agentId of the bot?
    private String botName;
    // Id of the channel
    private String channel;
    // Intent derived by the bot
    private String intent;
    private Object entities;
    private String email;
    private String user;
    private String time;

    public BotRequest() {
        this.msg = "";
        this.botName = "";
        this.channel = "";
        this.intent = "";
        this.entities = "";
        this.email = "";
        this.user = "";
        this.time = "";
    }

    public BotRequest(String msg, String botName, String channel, String intent, String entities, String email, String user, String time) {
        this.msg = msg;
        this.botName = botName;
        this.channel = channel;
        this.intent = intent;
        this.entities = entities;
        this.email = email;
        this.user = user;
        this.time = time;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public Object getEntities() {
        return entities;
    }

    public void setEntities(String entities) {
        this.entities = entities;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
