package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Format;
import org.springframework.stereotype.Component;

@Component
public class FormatValidation {

    private ValidationResult validationResult;

    public FormatValidation(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public ValidationResult validateFormat(Format format){
        ValidationResult result = new ValidationResult();

        if (format.getFormatName() == null || format.getFormatName().trim().isEmpty()) {
            result.addError("Formatnavn må ikke være tomt.");
        }

        if (format.getMinDeckSize() <= 0) {
            result.addError("Minimum deckstørrelse i format'en må ikke være mindre end '0'");
        }

        if (format.getMaxDeckSize() > 500){
            result.addError("Formatens deck størrelse må ikke være mere end 500 kort");
        }

        if (format.getMaxCopiesOfCard() > 100){
            result.addError("Du må ikke lave en format hvor der må være mere end 100 kopier af det samme kort");
        }


    }


}
