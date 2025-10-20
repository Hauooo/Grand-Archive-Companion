// In CardItem.java

package my.edu.utar.grandarchivecompanion.ui.cards;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.Objects;

public class CardItem implements Parcelable {
    private final String name;
    private final String imageUrl;
    private final String type;
    private final String text;
    private final String rulings;   // New field
    private final String legality;  // New field
    private final boolean isBanned;

    public CardItem(String name, String imageUrl, String type, String text, String rulings, String legality, boolean isBanned) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.type = type;
        this.text = text;
        this.rulings = rulings;   // New field
        this.legality = legality; // New field
        this.isBanned = isBanned;
    }

    protected CardItem(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        type = in.readString();
        text = in.readString();
        rulings = in.readString();  // New field
        legality = in.readString(); // New field
        isBanned = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(type);
        dest.writeString(text);
        dest.writeString(rulings);  // New field
        dest.writeString(legality); // New field
        dest.writeByte((byte) (isBanned ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CardItem> CREATOR = new Creator<CardItem>() {
        @Override
        public CardItem createFromParcel(Parcel in) {
            return new CardItem(in);
        }

        @Override
        public CardItem[] newArray(int size) {
            return new CardItem[size];
        }
    };

    // Add getters for the new fields
    public String getRulings() { return rulings; }
    public String getLegality() { return legality; }
    public boolean isBanned() { return isBanned; }

    // Existing getters
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public String getType() { return type; }
    public String getText() { return text; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardItem cardItem = (CardItem) o;
        return Objects.equals(name, cardItem.name) &&
                Objects.equals(imageUrl, cardItem.imageUrl) &&
                Objects.equals(type, cardItem.type) &&
                Objects.equals(text, cardItem.text) &&
                Objects.equals(rulings, cardItem.rulings) &&   // New field
                Objects.equals(legality, cardItem.legality) && // New field
                Objects.equals(isBanned, cardItem.isBanned); // New field
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, imageUrl, type, text, rulings, legality, isBanned); // New fields
    }
}