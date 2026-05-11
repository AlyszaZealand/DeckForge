package feedback.deckforge.Model;

import feedback.deckforge.Model.Enum.CardRarity;
import feedback.deckforge.Model.Enum.CardType;

public class Card {

    private int cardID;
    private String cardName;
    private String cardSet;
    private CardType cardType;
    private CardRarity cardRarity;
    private String manaCost;
    private int power;
    private int health;
    private String description;
    private String colorIdentity;

    public Card() {}

    public Card(int cardID, String cardName, String cardSet, CardType cardType, CardRarity cardRarity, String manaCost, int power, int health, String description, String colorIdentity) {
        this.cardID = cardID;
        this.cardName = cardName;
        this.cardSet = cardSet;
        this.cardType = cardType;
        this.cardRarity = cardRarity;
        this.manaCost = manaCost;
        this.power = power;
        this.health = health;
        this.description = description;
        this.colorIdentity = colorIdentity;
    }

    // Getters og Setters
    public int getCardID() { return cardID; }
    public void setCardID(int cardID) { this.cardID = cardID; }

    public String getCardName() { return cardName; }
    public void setCardName(String cardName) { this.cardName = cardName; }

    public String getCardSet() { return cardSet; }
    public void setCardSet(String cardSet) { this.cardSet = cardSet; }

    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }

    public CardRarity getCardRarity() { return cardRarity; }
    public void setCardRarity(CardRarity cardRarity) { this.cardRarity = cardRarity; }

    public String getManaCost() { return manaCost; }
    public void setManaCost(String manaCost) { this.manaCost = manaCost; }

    public int getPower() { return power; }
    public void setPower(int power) { this.power = power; }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColorIdentity(){
        return this.colorIdentity;
    }
    public void setColorIdentity(String colorIdentity){
        this.colorIdentity = colorIdentity;
    }
}

