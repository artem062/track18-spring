package ru.track;

/**
 * TASK:
 * POST request to  https://guarded-mesa-31536.herokuapp.com/track
 * fields: name,github,email
 *
 * LIB: http://unirest.io/java.html
 *
 *
 */

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

public class App2 {

    public static final String URL = "http://guarded-mesa-31536.herokuapp.com/track";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_GITHUB = "github";
    public static final String FIELD_EMAIL = "email";

    public static void main(String[] args) throws Exception {
        HttpResponse<JsonNode> req = Unirest.post(URL)
                .field(FIELD_NAME, "Voronov Artyom")
                .field(FIELD_GITHUB, "artem062")
                .field(FIELD_EMAIL, "artem_062@mail.ru")
                .asJson();
        boolean success = req.getBody().getObject().get("success").equals(true);
        System.out.println(success);

    }

}
