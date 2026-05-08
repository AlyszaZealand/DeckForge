package feedback.deckforge.Service;

import feedback.deckforge.Service.RepoInterfaces.ICardRepository;
import feedback.deckforge.Service.Validation.CardValidation;
import org.springframework.stereotype.Service;

@Service
public class CardService {

    private ICardRepository cardRepository;
    private CardValidation cardValidation;

    public CardService(ICardRepository cardRepository, CardValidation cardValidation) {
        this.cardRepository = cardRepository;
        this.cardValidation = cardValidation;
    }

    

}
