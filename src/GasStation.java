import java.util.ArrayList;
import java.util.List;

public class GasStation {
    // Existing fields like name, address...
    private List<Review> reviews;

    public GasStation(String name, String address) {
        // Existing constructor code...
        this.reviews = new ArrayList<>();
    }

    // Method to add a review
    public void addReview(Review review) {
        reviews.add(review);
    }

    // Method to get all reviews
    public List<Review> getReviews() {
        return new ArrayList<>(reviews); // Return a copy to prevent external modification
    }
}
