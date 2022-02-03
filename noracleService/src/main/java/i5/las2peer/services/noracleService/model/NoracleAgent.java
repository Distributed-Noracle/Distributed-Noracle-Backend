package i5.las2peer.services.noracleService.model;

public class NoracleAgent {
    private String agentid;

    public String getAgentid() {
        return agentid;
    }

    public void getAgentid(String agentid) {
        this.agentid = agentid;
    }

    NoracleAgent() {}

    public NoracleAgent(String agentId) {
        this.agentid = agentId;
    }
}
