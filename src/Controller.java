import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Controller {

    private ApiHandler apiHandler;

    public Controller() {
        this.apiHandler = new ApiHandler();
    }

    public JSONArray searchGasStations(String zipCode) {
        try {
            return apiHandler.getGasStations(zipCode);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the error appropriately, e.g. return an empty JSONArray
            return new JSONArray();
        }
    }

    public String parseStationName(JSONObject station) {
        try {
            return station.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle the error, e.g. return a default name or log the error
            return "Unknown Station";
        }
    }
}
