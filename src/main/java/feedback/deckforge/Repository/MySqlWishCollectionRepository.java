package feedback.deckforge.Repository;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.CardRarity;
import feedback.deckforge.Model.Enum.CardType;
import feedback.deckforge.Model.User;
import feedback.deckforge.Model.WishCollection;
import feedback.deckforge.Service.RepoInterfaces.IWishCollectionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MySqlWishCollectionRepository implements IWishCollectionRepository {

    private final JdbcTemplate jdbcTemplate;

    public MySqlWishCollectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<WishCollection> findWishCollectionByUserId(int userID){
        try {
            // TRIN 1: Find WishCollection ID baseret på User ID
            String sql = "SELECT wishcollection_id FROM wishcollections WHERE user_id = ?";
            Integer wishCollectionID = jdbcTemplate.queryForObject(sql, Integer.class, userID);

            WishCollection wc = new WishCollection();
            wc.setWishCollectionId(wishCollectionID);

            User user = new User();
            user.setUserID(userID);
            wc.setUser(user);

            // TRIN 2: Hent kortene via JOIN (Ingen quantity kolonne her!)
            String itemsSql = "SELECT c.* FROM wishcollection_items wci " +
                    "JOIN cards c ON wci.card_id = c.card_id " +
                    "WHERE wci.wishcollection_id = ?";

            jdbcTemplate.query(itemsSql, rs -> {
                Card card = new Card();
                card.setCardID(rs.getInt("card_id"));
                card.setCardName(rs.getString("card_name"));
                card.setCardRarity(CardRarity.valueOf(rs.getString("card_rarity")));
                card.setCardType(CardType.valueOf(rs.getString("card_type")));
                card.setCardSet(rs.getString("card_set"));
                card.setManaCost(rs.getString("mana_cost"));
                card.setColorIdentity(rs.getString("color_identity"));
                card.setPower(rs.getInt("power"));
                card.setHealth(rs.getInt("health"));
                card.setDescription(rs.getString("description"));

                wc.addCard(card);
            }, wishCollectionID);

            return Optional.of(wc);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    @Override
    public void addCardToWishCollection(int wishCollectionID, int cardID){
        String sql = "insert into wishcollection_items (wishcollection_id, card_id) values (?,?)";
        jdbcTemplate.update(sql,wishCollectionID,cardID);
    }

    @Override
    public void removeCardFromWishCollection(int wishCollectionID, int cardID){
        String sql = "DELETE FROM wishcollection_items WHERE wishcollection_id = ? AND card_id = ?";
        jdbcTemplate.update(sql, wishCollectionID, cardID);

    }

    @Override
    public void initWishCollection(int userID) {
        String sql = "INSERT INTO wishcollections (user_id) VALUES (?)";
        jdbcTemplate.update(sql, userID);
    }


}
