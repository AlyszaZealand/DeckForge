package feedback.deckforge.Repository;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Collection;
import feedback.deckforge.Model.Enum.CardRarity;
import feedback.deckforge.Model.Enum.CardType;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.RepoInterfaces.ICollectionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MySqlCollectionRepository implements ICollectionRepository {

    private final JdbcTemplate jdbcTemplate;

    public MySqlCollectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Collection> findCollectionByUserId(int userId){
            try {
                // TRIN 1: Find ID'et på den samling, der tilhører brugeren
                String sql = "SELECT collection_id FROM collections WHERE user_id = ?";
                Integer collectionID = jdbcTemplate.queryForObject(sql, Integer.class, userId);

                // Opret selve Java-objektet og tilknyt brugeren
                Collection collection = new Collection();
                collection.setCollectionId(collectionID);

                User user = new User();
                user.setUserID(userId);
                collection.setUser(user);

                // TRIN 2: Hent alle kort og mængder, der ligger i denne samling
                String itemsSql = "SELECT ci.quantity, c.* FROM collection_items ci " +
                        "JOIN cards c ON ci.card_id = c.card_id " +
                        "WHERE ci.collection_id = ?";

                jdbcTemplate.query(itemsSql, rs -> {
                    Card card = new Card();
                    card.setCardId(rs.getInt("card_id"));
                    card.setCardName(rs.getString("card_name"));
                    card.setCardRarity(CardRarity.valueOf(rs.getString("card_rarity")));
                    card.setCardType(CardType.valueOf(rs.getString("card_type")));
                    card.setCardSet(rs.getString("card_set"));
                    card.setManaCost(rs.getString("mana_cost"));
                    card.setColorIdentity(rs.getString("color_identity"));
                    card.setPower(rs.getInt("power"));
                    card.setHealth(rs.getInt("health"));
                    card.setDescription(rs.getString("description"));




                    // Læg kortet i samlingen med det rigtige antal
                    collection.addCard(card, rs.getInt("quantity"));
                }, collectionId);

                return Optional.of(collection);

            } catch (EmptyResultDataAccessException e) {
                return Optional.empty();
            }
    }

    public void addCardToCollection(int collectionId, int cardId, int quantity){
        String sql = "INSERT INTO collection_items (collection_id, card_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        jdbcTemplate.update(sql, collectionId, cardId, quantity);
    }


    public void removeCardFromCollection(int collectionId, int cardId){
        String sql = "DELETE FROM collection_items WHERE collection_id = ? AND card_id = ?";
        jdbcTemplate.update(sql, collectionId, cardId);
    }


    //hvis man har 4 kopier, men fjerner en fra sin collection
    public void updateCardQuantity(int collectionId, int cardId, int newQuantity){
        String sql = "UPDATE collection_items SET quantity = ? WHERE collection_id = ? AND card_id = ?";
        jdbcTemplate.update(sql, newQuantity, collectionId, cardId);
    }



}
