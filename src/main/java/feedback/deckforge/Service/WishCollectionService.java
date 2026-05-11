package feedback.deckforge.Service;


import feedback.deckforge.Model.WishCollection;
import feedback.deckforge.Service.RepoInterfaces.IWishCollectionRepository;
import feedback.deckforge.Service.Validation.ValidationResult;
import feedback.deckforge.Service.Validation.WishCollectionValidation;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class WishCollectionService {

    private IWishCollectionRepository wishCollectionRepository;
    private final WishCollectionValidation wishCollectionValidation;
    public WishCollectionService(IWishCollectionRepository wishCollectionRepository, WishCollectionValidation wishCollectionValidation) {
        this.wishCollectionRepository = wishCollectionRepository;
        this.wishCollectionValidation = wishCollectionValidation;
    }

    
    public Optional<WishCollection> getWishCollectionByUserID(int userID){

        return wishCollectionRepository.findWishCollectionByUserId(userID);
    }
    
    public ValidationResult addCardToWishCollection(int wishCollectionID, int cardID){
        ValidationResult result = wishCollectionValidation.validateAddCardToWishlist(cardID);
        if (!result.hasErrors()) {
            wishCollectionRepository.addCardToWishCollection(wishCollectionID, cardID);
        }
        return result;
    }
    
    public void removeCardFromWishCollection(int wishCollectionID, int cardID){
        wishCollectionRepository.removeCardFromWishCollection(wishCollectionID, cardID);
    }




}
