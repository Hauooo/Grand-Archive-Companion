package my.edu.utar.grandarchivecompanion;

public class CardItem {
    private String name;
    private String imageUrl;
    private String type;
    private String text;

    public CardItem(String name, String imageUrl, String type, String text) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.type = type;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getType() {
        return type;
    }
    public String getText() {
        return text;
    }
}
