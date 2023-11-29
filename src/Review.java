public class Review {
    private String name;
    private double rating;
    private String comment;

    public Review(String name, double rating, String comment) {
        this.name = name;
        this.rating = rating;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}
