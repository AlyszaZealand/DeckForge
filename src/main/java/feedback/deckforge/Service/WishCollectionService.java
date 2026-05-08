package feedback.deckforge.Service;


import feedback.deckforge.Model.WishCollection;
import feedback.deckforge.Service.RepoInterfaces.IWishCollectionRepository;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class WishCollectionService {

    private IWishCollectionRepository wishCollectionRepository;
    public WishCollectionService(IWishCollectionRepository wishCollectionRepository) {
        this.wishCollectionRepository = wishCollectionRepository;
    }

    
    public Optional<WishCollection> getWishCollectionByUserID(int userID){
        return wishCollectionRepository.findWishCollectionByUserId(userID);
    }
    
    public void addCardToWishCollection(int wishCollectionID, int cardID){
        wishCollectionRepository.addCardToWishCollection(wishCollectionID,cardID);
    }
    
    public void removeCardFromWishCollection(int wishCollectionID, int cardID){
        wishCollectionRepository.removeCardFromWishCollection(wishCollectionID, cardID);
    }




}
