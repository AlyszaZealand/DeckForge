package feedback.deckforge.Model;

public class Format {

    private int formatID;
    private String formatName;           // F.eks. "Standard", "Commander", "Pauper"

    // Størrelsesregler
    private int minDeckSize;             // F.eks. 60 eller 100
    private int maxDeckSize;             // F.eks. 60 (hvis strict) eller noget højt som 250
    private int maxCopiesOfCard;         // F.eks. 4 (Standard) eller 1 (Commander)

    // Specifikke MTG regler (Sættes via checkbokse af admin)
    private boolean requiresCommander;   // Skal decket have en commander?
    private String allowedRarities; //

    public Format() {}

    public Format(String formatName, int minDeckSize, int maxDeckSize, int maxCopiesOfCard, boolean requiresCommander, String allowedRarities) {
        this.formatName = formatName;
        this.minDeckSize = minDeckSize;
        this.maxDeckSize = maxDeckSize;
        this.maxCopiesOfCard = maxCopiesOfCard;
        this.requiresCommander = requiresCommander;
        this.allowedRarities = allowedRarities;
    }

    // --- Getters og Setters ---
    public int getFormatID() { return formatID; }
    public void setFormatID(int formatID) { this.formatID = formatID; }

    public String getFormatName() { return formatName; }
    public void setFormatName(String formatName) { this.formatName = formatName; }

    public int getMinDeckSize() { return minDeckSize; }
    public void setMinDeckSize(int minDeckSize) { this.minDeckSize = minDeckSize; }

    public int getMaxDeckSize() { return maxDeckSize; }
    public void setMaxDeckSize(int maxDeckSize) { this.maxDeckSize = maxDeckSize; }

    public int getMaxCopiesOfCard() { return maxCopiesOfCard; }
    public void setMaxCopiesOfCard(int maxCopiesOfCard) { this.maxCopiesOfCard = maxCopiesOfCard; }

    public boolean isRequiresCommander() { return requiresCommander; }
    public void setRequiresCommander(boolean requiresCommander) { this.requiresCommander = requiresCommander; }

    public String getAllowedRarities() { return allowedRarities; }
    public void setAllowedRarities(String allowedRarities) {
        if (allowedRarities == null || allowedRarities.trim().isEmpty()) {
            this.allowedRarities = "ALL";
        } else {
            this.allowedRarities = allowedRarities;
        }
    }
}

