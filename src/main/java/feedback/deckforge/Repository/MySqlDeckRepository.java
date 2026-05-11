package feedback.deckforge.Repository;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Deck;
import feedback.deckforge.Model.Format;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.RepoInterfaces.IDeckRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MySqlDeckRepository implements IDeckRepository {

    private final JdbcTemplate jdbcTemplate;

    public MySqlDeckRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Deck> deckRowMapper = (rs, rowNum) -> {
        Format format = new Format();
        format.setFormatId(rs.getInt("format_id"));
        format.setFormatName(rs.getString("format_name"));
        format.setMinDeckSize(rs.getInt("min_deck_size"));
        format.setMaxDeckSize(rs.getInt("max_deck_size"));
        format.setMaxCopiesOfCard(rs.getInt("max_copies_of_card"));
        format.setRequiresCommander(rs.getBoolean("requires_commander"));
        format.setAllowedRarities(rs.getString("allowed_rarities"));

        // opret user
        User owner = new User();
        owner.setUserID(rs.getInt("user_id"));

        // Opret Decket
        Deck deck = new Deck();
        deck.setDeckId(rs.getInt("deck_id"));
        deck.setDeckName(rs.getString("deck_name"));
        deck.setUser(owner);
        deck.setFormat(format);

        // Håndter Commander hvis den findes
        int commanderId = rs.getInt("commander_card_id");
        if (!rs.wasNull()) {
            Card commander = new Card();
            commander.setCardID(commanderId);
            deck.setCommander(commander);
        }

        return deck;
    };

    @Override
    public void saveDeck(Deck deck){
        String sql = "INSERT INTO decks (user_id, format_id, deck_name, commander_card_id) VALUES (?, ?, ?, ?)";

        Integer commanderID = (deck.getCommander() != null) ? deck.getCommander().getCardID() : null;

        jdbcTemplate.update(sql,
                deck.getUser().getUserID(),
                deck.getFormat().getFormatId(),
                deck.getDeckName(),
                commanderID
        );
    }

    @Override
    public void updateDeck(Deck deck) {
        String sql = "UPDATE decks SET deck_name = ?, format_id = ?, commander_card_id = ? WHERE deck_id = ?";

        Integer commanderId = (deck.getCommander() != null) ? deck.getCommander().getCardID() : null;

        jdbcTemplate.update(sql,
                deck.getDeckName(),
                deck.getFormat().getFormatId(),
                commanderId,
                deck.getDeckId()
        );
    }

    @Override
    public void deleteDeck(int deckID) {
        String sql = "DELETE FROM decks WHERE deck_ID = ?";
        jdbcTemplate.update(sql, deckID);
    }

    @Override
    public void addCardToDeck(int deckID, int cardID, int quantity) {
        String sql = "INSERT INTO deck_items (deck_id, card_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        jdbcTemplate.update(sql, deckID, cardID, quantity);
    }

    @Override
    public void removeCardFromDeck(int deckID, int cardID) {
        String sql = "DELETE FROM deck_items WHERE deck_id = ? AND card_id = ?";
        jdbcTemplate.update(sql, deckID, cardID);
    }

    @Override
    public Optional<Deck> findDeckById(int deckID) {
        String sql = "SELECT d.*, f.* FROM decks d " +
                "JOIN formats f ON d.format_id = f.format_id " +
                "WHERE d.deck_id = ?";
        try {
            Deck deck = jdbcTemplate.queryForObject(sql, deckRowMapper, deckID);
            return Optional.ofNullable(deck);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Deck> findAllDecksByUserId(int userID) {
        String sql = "SELECT d.*, f.* FROM decks d " +
                "JOIN formats f ON d.format_id = f.format_id " +
                "WHERE d.user_id = ?";
        return jdbcTemplate.query(sql, deckRowMapper, userID);
    }
}

