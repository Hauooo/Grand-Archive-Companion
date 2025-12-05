package my.edu.utar.grandarchivecompanion.ui.cards;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CardItem implements Parcelable {

    // --- Legacy / Display Fields ---
    private String displayCost;
    private String displayType;
    private String displayRulings;
    private String displayLegality;
    private String displayImageUrl;
    private boolean isBanned;

    // --- API Fields ---
    @SerializedName("name")
    private String name;

    @SerializedName("uuid")
    private String uuid;

    @SerializedName("element")
    private String element;

    @SerializedName("image")
    private String apiImage;

    @SerializedName("edition")
    private Edition edition;

    @SerializedName("effect")
    private String effect;

    @SerializedName("effect_raw")
    private String effectRaw;

    // "classes" field removed as requested

    @SerializedName("types")
    private List<String> types;

    @SerializedName("subtypes")
    private List<String> subtypes;

    @SerializedName("rule")
    private List<Rule> rules;

    @SerializedName("cost_memory")
    private Integer costMemory;

    @SerializedName("cost_reserve")
    private Integer costReserve;

    @SerializedName("level")
    private Integer level;

    @SerializedName("other_orientations")
    private List<CardItem> otherOrientations;

    // --- Constructors ---

    public CardItem() {
        this.otherOrientations = new ArrayList<>();
    }

    public CardItem(String name, String imageUrl, String cost, String type, String effect, String rulings, String legality, boolean isBanned) {
        this(name, imageUrl, cost, type, effect, rulings, legality, isBanned, new ArrayList<>());
    }

    public CardItem(String name, String imageUrl, String cost, String type, String effect, String rulings, String legality, boolean isBanned, List<CardItem> otherOrientations) {
        this.name = name;
        this.displayImageUrl = imageUrl;
        this.displayCost = cost;
        this.displayType = type;
        this.effect = effect;
        this.effectRaw = effect;
        this.displayRulings = rulings;
        this.displayLegality = legality;
        this.isBanned = isBanned;
        this.otherOrientations = otherOrientations != null ? otherOrientations : new ArrayList<>();
    }

    // --- Parcelable Implementation ---

    protected CardItem(Parcel in) {
        displayCost = in.readString();
        displayType = in.readString();
        displayRulings = in.readString();
        displayLegality = in.readString();
        displayImageUrl = in.readString();
        isBanned = in.readByte() != 0;

        name = in.readString();
        uuid = in.readString();
        element = in.readString();
        apiImage = in.readString();
        edition = in.readParcelable(Edition.class.getClassLoader());
        effect = in.readString();
        effectRaw = in.readString();
        // classes removed
        types = in.createStringArrayList();
        subtypes = in.createStringArrayList();
        rules = in.createTypedArrayList(Rule.CREATOR);
        costMemory = (Integer) in.readValue(Integer.class.getClassLoader());
        costReserve = (Integer) in.readValue(Integer.class.getClassLoader());
        level = (Integer) in.readValue(Integer.class.getClassLoader());
        otherOrientations = in.createTypedArrayList(CardItem.CREATOR);

        if (otherOrientations == null) otherOrientations = new ArrayList<>();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(displayCost);
        dest.writeString(displayType);
        dest.writeString(displayRulings);
        dest.writeString(displayLegality);
        dest.writeString(displayImageUrl);
        dest.writeByte((byte) (isBanned ? 1 : 0));

        dest.writeString(name);
        dest.writeString(uuid);
        dest.writeString(element);
        dest.writeString(apiImage);
        dest.writeParcelable(edition, flags);
        dest.writeString(effect);
        dest.writeString(effectRaw);
        // classes removed
        dest.writeStringList(types);
        dest.writeStringList(subtypes);
        dest.writeTypedList(rules);
        dest.writeValue(costMemory);
        dest.writeValue(costReserve);
        dest.writeValue(level);
        dest.writeTypedList(otherOrientations);
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

    // --- Getters ---

    public String getName() { return name; }
    public boolean isBanned() { return isBanned; }
    public void setBanned(boolean banned) { isBanned = banned; }

    public String getImageUrl() {
        if (displayImageUrl != null && !displayImageUrl.isEmpty()) return displayImageUrl;
        if (apiImage != null && !apiImage.isEmpty()) return apiImage;
        if (edition != null && edition.image != null) return edition.image;
        return null;
    }

    public String getCost() {
        if (displayCost != null) return displayCost;
        StringBuilder sb = new StringBuilder();
        if (costMemory != null) sb.append(costMemory);
        else if (costReserve != null) sb.append(costReserve).append(" Reserve");
        else if (level != null) sb.append("Lv.").append(level);
        return sb.toString();
    }

    public String getType() {
        if (displayType != null) return displayType;

        List<String> combined = new ArrayList<>();
        // Logic for classes removed
        if (types != null) combined.addAll(types);
        if (subtypes != null) combined.addAll(subtypes);

        if (combined.isEmpty()) return "Unknown Type";
        return TextUtils.join(" - ", combined);
    }

    public String getEffectRaw() {
        return effectRaw != null ? effectRaw : (effect != null ? effect : "");
    }

    public String getRulings() {
        if (displayRulings != null) return displayRulings;
        if (rules == null || rules.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (Rule rule : rules) {
            if (sb.length() > 0) sb.append("\n\n");
            if (rule.dateAdded != null) sb.append("[").append(rule.dateAdded).append("] ");
            sb.append(rule.description);
        }
        return sb.toString();
    }

    public String getLegality() {
        return displayLegality != null ? displayLegality : "Unknown";
    }

    public List<CardItem> getOtherOrientations() { return otherOrientations; }
    public void setOtherOrientations(List<CardItem> otherOrientations) { this.otherOrientations = otherOrientations; }
    public String getElement() { return element; }

    // --- Inner Classes ---

    public static class Edition implements Parcelable {
        @SerializedName("image") public String image;
        @SerializedName("card_id") public String cardId;
        @SerializedName("collector_number") public String collectorNumber;
        @SerializedName("rarity") public Integer rarity;

        protected Edition(Parcel in) {
            image = in.readString();
            cardId = in.readString();
            collectorNumber = in.readString();
            rarity = in.readByte() == 0 ? null : in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(image);
            dest.writeString(cardId);
            dest.writeString(collectorNumber);
            if (rarity == null) dest.writeByte((byte) 0);
            else { dest.writeByte((byte) 1); dest.writeInt(rarity); }
        }

        @Override
        public int describeContents() { return 0; }
        public static final Creator<Edition> CREATOR = new Creator<Edition>() {
            @Override public Edition createFromParcel(Parcel in) { return new Edition(in); }
            @Override public Edition[] newArray(int size) { return new Edition[size]; }
        };
    }

    public static class Rule implements Parcelable {
        @SerializedName("date_added") public String dateAdded;
        @SerializedName("description") public String description;

        protected Rule(Parcel in) {
            dateAdded = in.readString();
            description = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(dateAdded);
            dest.writeString(description);
        }

        @Override
        public int describeContents() { return 0; }
        public static final Creator<Rule> CREATOR = new Creator<Rule>() {
            @Override public Rule createFromParcel(Parcel in) { return new Rule(in); }
            @Override public Rule[] newArray(int size) { return new Rule[size]; }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardItem cardItem = (CardItem) o;
        return Objects.equals(name, cardItem.name) && Objects.equals(uuid, cardItem.uuid);
    }

    @Override
    public int hashCode() { return Objects.hash(name, uuid); }
}