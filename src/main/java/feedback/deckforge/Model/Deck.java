package feedback.deckforge.Model;

public abstract class Deck {

    private int deckId;
    private String deckName;
    private User user; // Ejeren af decket

    public Deck() {}

    public Deck(String deckName, User user) {
        this.deckName = deckName;
        this.user = user;
    }

    public int getDeckId() { return deckId; }
    public void setDeckId(int deckId) { this.deckId = deckId; }

    public String getDeckName() { return deckName; }
    public void setDeckName(String deckName) { this.deckName = deckName; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
