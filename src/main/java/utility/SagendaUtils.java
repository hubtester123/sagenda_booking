package utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SagendaUtils {

    private static String apiDomain = "https://sagenda.net/api/v3/";

    public static SagendaAccessToken getToken(String apiToken) throws Exception {

        String postBodyString = "grant_type=api_token&api_token=" + apiToken;
        RequestBody body = RequestBody.create(MediaType.parse("plain"), postBodyString);
        Request request = new Request.Builder()
                                     .url(apiDomain + "token")
                                     .post(body)
                                     .build();
        OkHttpClient client = new OkHttpClient.Builder()
				  .connectTimeout(10, TimeUnit.SECONDS)
				  .writeTimeout(10, TimeUnit.SECONDS)
				  .readTimeout(30, TimeUnit.SECONDS)
				  .build();

        try (Response response = client.newCall(request).execute()) {

            ObjectMapper objectMapper = new ObjectMapper();
            SagendaAccessToken accessToken = objectMapper.readValue(response.body().string(), SagendaAccessToken.class);
            return accessToken;
        }
    }

    public static List<SagendaBookableItem> getBookableItems(String accessToken) throws Exception {

        Request request = new Request.Builder()
                .url(apiDomain + "bookableItems")
                .addHeader("authorization", "bearer " + accessToken)
                .get()
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
				  .connectTimeout(10, TimeUnit.SECONDS)
				  .writeTimeout(10, TimeUnit.SECONDS)
				  .readTimeout(30, TimeUnit.SECONDS)
				  .build();
        
        try (Response response = client.newCall(request).execute()) {

            ObjectMapper objectMapper = new ObjectMapper();
            List<SagendaBookableItem> bookableItemsList = objectMapper.readValue(response.body().string(),
                                                                                  new TypeReference<List<SagendaBookableItem>>(){});

            return bookableItemsList;
        }
    }

    public static List<SagendaSchedule> getSchedule(String accessToken,
                                                    Date startDate,
                                                    Date endDate,
                                                    String bookingItemId) throws Exception  {

        SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");

        Request request = new Request.Builder()
                .url(apiDomain + "events/" + yyyyMMdd.format(startDate) + "/" + yyyyMMdd.format(endDate) + "/" + bookingItemId)
                .addHeader("authorization", "bearer " + accessToken)
                .get()
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
				  .connectTimeout(10, TimeUnit.SECONDS)
				  .writeTimeout(10, TimeUnit.SECONDS)
				  .readTimeout(30, TimeUnit.SECONDS)
				  .build();
        
        try (Response response = client.newCall(request).execute()) {


            JSONArray jsonArray = new JSONArray(response.body().string());
            ArrayList<SagendaSchedule> schedules = new ArrayList<>();
            for (int i = 0; i <jsonArray.length(); i++) {
                schedules.add(Parsor.jsonToScheudle((JSONObject)jsonArray.get(i)));
            }

            return schedules;
        }
    }

    public static List<SagendaLock> lock(String accessToken,
                                         String eventId) throws Exception {

        String json = "{\"userIdentifier\":null," +
                       "\"events\":[{\"participants\":1," +
                                    "\"eventIdentifier\":\"{{eventId}}\"}]}";
        json = json.replace("{{eventId}}", eventId);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);

        Request request = new Request.Builder()
                .url(apiDomain + "eventLocks/multi")
                .addHeader("authorization", "bearer " + accessToken)
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
				  .connectTimeout(10, TimeUnit.SECONDS)
				  .writeTimeout(10, TimeUnit.SECONDS)
				  .readTimeout(30, TimeUnit.SECONDS)
				  .build();

        try (Response response = client.newCall(request).execute()) {

            JSONObject jsonObject = new JSONObject(response.body().string());

            if (jsonObject.has("locks"))  {
                JSONArray locks = jsonObject.getJSONArray("locks");

                ArrayList<SagendaLock> locksArrayList = new ArrayList<>();
                for (int i = 0; i <locks.length(); i++) {
                    locksArrayList.add(Parsor.jsonToLock((JSONObject)locks.get(i)));
                }

                return locksArrayList;
            }

            return null;
        }
    }

    public static boolean book(String accessToken,
                               String userId,
                               String eventId,
                               String email,
                               String firstName,
                               String lastName,
                               String phoneNumber,
                               String description) throws Exception {

        String json = "{\"userIdentifier\": \"{{userId}}\"," +
                       "\"eventIdentifiers\":[\"{{eventId}}\"]," +
                       "\"member\":{\"email\":\"{{email}}\"," +
                                   "\"firstName\":\"{{firstName}}\"," +
                                   "\"lastName\":\"{{lastName}}\"," +
                                   "\"phoneNumber\":\"{{phoneNumber}}\"," +
                                   "\"description\":\"{{description}}\"," +
                                   "\"courtesy\":\"Mr.\"}}";

        json = json.replace("{{userId}}", userId);
        json = json.replace("{{eventId}}", eventId);
        json = json.replace("{{email}}", email);
        json = json.replace("{{firstName}}", firstName);
        json = json.replace("{{lastName}}", lastName);
        json = json.replace("{{phoneNumber}}", phoneNumber);
        json = json.replace("{{description}}", description);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);

        Request request = new Request.Builder()
                .url(apiDomain + "events/multi")
                .addHeader("authorization", "bearer " + accessToken)
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
				  .connectTimeout(10, TimeUnit.SECONDS)
				  .writeTimeout(10, TimeUnit.SECONDS)
				  .readTimeout(30, TimeUnit.SECONDS)
				  .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.code());
            System.out.println(response.body().string());

            if (response.code() == 201) {
                return true;
            }

            throw new Exception(response.body().string());
        }
    }
}
