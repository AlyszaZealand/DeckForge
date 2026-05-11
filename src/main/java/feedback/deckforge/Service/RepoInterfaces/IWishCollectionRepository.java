package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.WishCollection;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

public interface IWishCollectionRepository {


    Optional<WishCollection> findWishCollectionByUserId(int userID);

    void addCardToWishCollection(int wishCollectionID, int cardID);

    void removeCardFromWishCollection(int wishCollectionID, int cardID);

    void initWishCollection(int userID);
}
