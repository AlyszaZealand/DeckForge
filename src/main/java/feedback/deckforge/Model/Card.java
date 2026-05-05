package feedback.deckforge.Model;

import feedback.deckforge.Model.Enum.CardRarity;
import feedback.deckforge.Model.Enum.CardType;

public class Card {

    private int cardId;
    private String cardName;
    private String cardSet;
    private CardType cardType;
    private CardRarity cardRarity;
    private String manaCost;
    private int power;
    private int health;
    private String description;

    public Card() {}

    public Card(int cardId, String cardName, String cardSet, CardType cardType, CardRarity cardRarity, String manaCost, int power, int health, String description) {
        this.cardId = cardId;
        this.cardName = cardName;
        this.cardSet = cardSet;
        this.cardType = cardType;
        this.cardRarity = cardRarity;
        this.manaCost = manaCost;
        this.power = power;
        this.health = health;
        this.description = description;
    }

    // Getters og Setters
    public int getCardId() { return cardId; }
    public void setCardId(int cardId) { this.cardId = cardId; }

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
}

