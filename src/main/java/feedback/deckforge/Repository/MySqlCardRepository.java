package feedback.deckforge.Repository;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.CardRarity;
import feedback.deckforge.Model.Enum.CardType;
import feedback.deckforge.Service.RepoInterfaces.ICardRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MySqlCardRepository implements ICardRepository {

    private final JdbcTemplate jdbcTemplate;

    public MySqlCardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public void saveCard(Card card) {
        // Tjek at dette matcher de rigtige kolonnenavne i jeres schema.sql
        String sql = "INSERT INTO cards (card_name, card_type, card_rarity, card_set, mana_cost, color_identity, power, health, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                card.getCardName(),
                card.getCardType().name(),
                card.getCardRarity().name(),
                card.getCardSet(),
                card.getManaCost(),
                card.getColorIdentity(),
                card.getPower(),
                card.getHealth(),
                card.getDescription()
        );
    }

    @Override
    public Optional<Card> findCardById(int cardID) {
        String sql = "select * from cards where card_id = ?";

        try{
            Card card =jdbcTemplate.queryForObject(sql, cardRowMapper,cardID);
            return Optional.of(card);
        } catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Override
    public List<Card> findAllCards() {
        String sql = "Select * from cards";
        return jdbcTemplate.query(sql,cardRowMapper);
    }

    @Override
    public List<Card> searchAllCards(String name, String rarity, String color) {
        StringBuilder sql = new StringBuilder("SELECT * FROM cards WHERE 1=1");
        List<Object> params = new ArrayList<>();
        addFilters(sql, params, name, rarity, color);
        return jdbcTemplate.query(sql.toString(), cardRowMapper, params.toArray());
    }

    @Override
    public List<Card> searchCollectionCards(int userId, String name, String rarity, String color) {
        StringBuilder sql = new StringBuilder(
                "SELECT c.* FROM cards c " +
                        "JOIN collection_items ci ON c.card_id = ci.card_id " +
                        "JOIN collections col ON ci.collection_id = col.collection_id " +
                        "WHERE col.user_id = ?"
        );
        List<Object> params = new ArrayList<>();
        params.add(userId);
        addFilters(sql, params, name, rarity, color);
        return jdbcTemplate.query(sql.toString(), cardRowMapper, params.toArray());
    }

    @Override
    public List<Card> searchTradeCards(int userId, String name, String rarity, String color) {
        StringBuilder sql = new StringBuilder(
                "SELECT c.* FROM cards c " +
                        "JOIN tradecollection_items tci ON c.card_id = tci.card_id " +
                        "JOIN tradecollections tc ON tci.tradecollection_id = tc.tradecollection_id " +
                        "WHERE tc.user_id = ?"
        );
        List<Object> params = new ArrayList<>();
        params.add(userId);
        addFilters(sql, params, name, rarity, color);
        return jdbcTemplate.query(sql.toString(), cardRowMapper, params.toArray());
    }

    @Override
    public List<Card> searchWishlistCards(int userId, String name, String rarity, String color) {
        StringBuilder sql = new StringBuilder(
                "SELECT c.* FROM cards c " +
                        "JOIN wishcollection_items wci ON c.card_id = wci.card_id " +
                        "JOIN wishcollections wc ON wci.wishcollection_id = wc.wishcollection_id " +
                        "WHERE wc.user_id = ?"
        );
        List<Object> params = new ArrayList<>();
        params.add(userId);
        addFilters(sql, params, name, rarity, color);
        return jdbcTemplate.query(sql.toString(), cardRowMapper, params.toArray());
    }

    private void addFilters(StringBuilder sql, List<Object> params, String name, String rarity, String color) {
        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND card_name LIKE ?");
            params.add("%" + name + "%");
        }
        if (rarity != null && !rarity.trim().isEmpty()) {
            sql.append(" AND card_rarity = ?");
            params.add(rarity);
        }
        if (color != null && !color.trim().isEmpty()) {
            // NYT: Hvis man søger på "C" (Colorless), skal den lede efter tomme felter
            if (color.equals("C")) {
                sql.append(" AND (color_identity = '' OR color_identity IS NULL)");
            } else {
                sql.append(" AND color_identity LIKE ?");
                params.add("%" + color + "%");
            }
        }
    }



    private final RowMapper<Card> cardRowMapper = (rs, rowNum) -> {
        Card card = new Card();

        card.setCardID(rs.getInt("card_id"));
        card.setCardName(rs.getString("card_name"));
        card.setCardSet(rs.getString("card_set"));

        // Safely mapping the CardType Enum
        String typeStr = rs.getString("card_type");
        if (typeStr != null) {
            card.setCardType(CardType.valueOf(typeStr.toUpperCase()));
        }

        // Safely mapping the CardRarity Enum
        String rarityStr = rs.getString("card_rarity");
        if (rarityStr != null) {
            card.setCardRarity(CardRarity.valueOf(rarityStr.toUpperCase()));
        }

        card.setManaCost(rs.getString("mana_cost"));
        card.setColorIdentity(rs.getString("color_identity"));

        // Stats and descriptions
        card.setPower(rs.getInt("power"));
        card.setHealth(rs.getInt("health"));
        card.setDescription(rs.getString("description"));

        return card;
    };
}
