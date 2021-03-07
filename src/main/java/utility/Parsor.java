package utility;

import com.fasterxml.jackson.databind.JsonNode;
import model.SagendaLock;
import model.SagendaSchedule;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class Parsor {

    public static SagendaSchedule jsonToScheudle(JSONObject jsonObject) throws Exception {

        SagendaSchedule schedule = new SagendaSchedule();

        SimpleDateFormat yyyyMMddTHHmmZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

        if (jsonObject.has("identifier")) schedule.setIdentifier(jsonObject.getString("identifier"));
        if (jsonObject.has("type")) schedule.setType(jsonObject.getString("type"));
        if (jsonObject.has("from")) schedule.setFrom(yyyyMMddTHHmmZ.parse(jsonObject.getString("from")));
        if (jsonObject.has("to")) schedule.setTo(yyyyMMddTHHmmZ.parse(jsonObject.getString("to")));

        return schedule;
    }

    public static SagendaLock jsonToLock(JSONObject jsonObject) throws Exception {

        SimpleDateFormat yyyyMMddTHHmmZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

        SagendaLock lock = new SagendaLock();

        if (jsonObject.has("identifier")) lock.setIdentifier(jsonObject.getString("identifier"));
        if (jsonObject.has("eventIdentifier")) lock.setEventIdentifier(jsonObject.getString("eventIdentifier"));
        if (jsonObject.has("userIdentifier")) lock.setUserIdentifier(jsonObject.getString("userIdentifier"));
        if (jsonObject.has("expires")) lock.setExpires(yyyyMMddTHHmmZ.parse(jsonObject.getString("expires")));
        if (jsonObject.has("participants")) lock.setParticipants(jsonObject.getInt("participants"));

        return lock;
    }
}
