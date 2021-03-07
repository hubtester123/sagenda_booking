package model;

import java.util.Date;
import java.util.Map;

public class SagendaSchedule {

    String identifier;
    String type;
    Date from;
    Date to;
    Map<String, Object> membership; //maximum //reserved

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Map<String, Object> getMembership() {
        return membership;
    }

    public void setMembership(Map<String, Object> membership) {
        this.membership = membership;
    }
}
