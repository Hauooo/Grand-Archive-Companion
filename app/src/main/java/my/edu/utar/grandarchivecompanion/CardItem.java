package my.edu.utar.grandarchivecompanion;

import java.util.Objects;

public class CardItem{
    private final String name;
    private final String imageUrl;
    private final String type;
    private final String text;

    public CardItem(String name, String imageUrl, String type, String text) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.type = type;
        this.text = text;
    }

    public String getName() {return name;}
    public String getImageUrl() {return imageUrl;}
    public String getType() {return type;}
    public String getText() {return text;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardItem cardItem = (CardItem) o;
        return Objects.equals(name, cardItem.name) &&
               Objects.equals(imageUrl, cardItem.imageUrl) &&
               Objects.equals(type, cardItem.type) &&
               Objects.equals(text, cardItem.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, imageUrl, type, text);
    }
}