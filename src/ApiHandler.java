import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class ApiHandler {

    private final String apiKey = "AIzaSyCxX4yvtl3C9KNyCHR-XNAHKD2ezAT6WnU"; 
    public JSONArray getGasStations(String zipCode) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=gas+stations+in+" + zipCode + "&key=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getJSONArray("results");
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
}
