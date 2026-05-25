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
        validateCardName(cardName);
        this.cardName = cardName;
        validateCardSet(cardSet);
        this.cardSet = cardSet;
        validateCardType(cardType);
        this.cardType = cardType;
        validateCardRarity(cardRarity);
        this.cardRarity = cardRarity;
        validateManaCost(manaCost);
        this.manaCost = manaCost;
        validatePower(power);
        this.power = power;
        validateHealth(health);
        this.health = health;
        validateDescription(description);
        this.description = description;
        validateColorIdentity(colorIdentity);
        this.colorIdentity = colorIdentity;
    }

    // Getters og Setters
    public int getCardID() { return cardID; }
    public void setCardID(int cardID) { this.cardID = cardID; }

    public String getCardName() { return cardName; }
    public void setCardName(String cardName) {this.cardName = cardName; }
    private void validateCardName(String cardName) {
        if (cardName == null) {
            throw new IllegalArgumentException("Kort navn kan ikke være null");
        }
        if (cardName.trim().isEmpty()) {
            throw new IllegalArgumentException("Kort navn kan ikke være null");
        }
    }

    public String getCardSet() { return cardSet; }
    public void setCardSet(String cardSet) { this.cardSet = cardSet; }
    private void validateCardSet(String cardSet) {
        if (cardSet == null) {
            throw new IllegalArgumentException("Kort skal have et kortset");
        }
    }

    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }
    private void validateCardType(CardType cardType) {
        if (cardType == null) {
            throw new IllegalArgumentException("Kort skal have en kort type");
        }
    }

    public CardRarity getCardRarity() { return cardRarity; }
    public void setCardRarity(CardRarity cardRarity) { this.cardRarity = cardRarity; }
    private void validateCardRarity(CardRarity cardRarity) {
        if (cardRarity == null) {
            throw new IllegalArgumentException("Kort skal have en sjældenhed");
        }
    }

    public String getManaCost() { return manaCost; }
    public void setManaCost(String manaCost) { this.manaCost = manaCost; }
    private void validateManaCost(String manaCost) {
        if (manaCost == null) {
            throw new IllegalArgumentException("Kort skal have mana");
        }
    }

    public int getPower() { return power; }
    public void setPower(int power) { this.power = power; }
    private void validatePower(int power) {
        if (power < 0) {
            throw new IllegalArgumentException("Kort skal have en skade");
        }
    }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    private void validateHealth(int health) {
        if (health < 0) {
            throw new IllegalArgumentException("Kort skal have liv");
        }
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    private void validateDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Kort skal have en beskrivelse");
        }
    }

    public String getColorIdentity(){ return this.colorIdentity; }
    public void setColorIdentity(String colorIdentity){ this.colorIdentity = colorIdentity; }
    private void validateColorIdentity(String colorIdentity) {
        if (colorIdentity == null) {
            throw new IllegalArgumentException("Kort skal have en farve gruppe");
        }
    }
}

