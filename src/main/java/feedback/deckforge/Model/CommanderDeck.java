package feedback.deckforge.Model;

public class CommanderDeck extends Deck {
    private Card commander;

    public CommanderDeck() {
        super();
    }

    public CommanderDeck(String deckName, User user, Card commander) {
        super(deckName, user);
        this.commander = commander;
    }

    public Card getCommander() { return commander; }
    public void setCommander(Card commander) { this.commander = commander; }
}
