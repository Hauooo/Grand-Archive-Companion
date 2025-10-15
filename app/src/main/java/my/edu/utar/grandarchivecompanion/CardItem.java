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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CardItem)) return false;
        CardItem other = (CardItem) obj;
        return name.equals(other.name) && imageUrl.equals(other.imageUrl)
                && type.equals(other.type) && text.equals(other.text);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + imageUrl.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }
}
