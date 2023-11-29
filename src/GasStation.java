import java.util.ArrayList;
import java.util.List;

public class GasStation {
    private String name;
    private String address;
    private List<Review> reviews;

    public GasStation(String name, String address) {
        this.name = name;
        this.address = address;
        this.reviews = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void addReview(Review review) {
        reviews.add(review);
    }
}
