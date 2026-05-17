package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Event;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;


@Component
public class EventValidation {

    public ValidationResult validateEvent(Event event){
        ValidationResult result = new ValidationResult();

        if (event.getEventName() == null || event.getEventName().length() < 5) {
            result.addError("Udstillings Navn skal være mindst '5' tegn");
        }

        if (event.getEventDescription() == null || event.getEventDescription().trim().length() < 5) {
            result.addError("Udstillingens beskrivelse skal være mindst 5 tegn lang.");
        }

        if (event.getEventDate() == null || event.getEventDate().isBefore(LocalDateTime.now())) {
            result.addError("Udstillingen skal have en fastsat dato. Datoen må inte være sat tilbage i tiden.");
        }

        if (event.getEventEndDate() != null && event.getEventDate() != null) {
            if (event.getEventEndDate().isBefore(event.getEventDate()) || event.getEventEndDate().isEqual(event.getEventDate())) {
                result.addError("Slutdatoen skal ligge efter startdatoen.");
            }
        }

        if(event.getEventSize() != 8 && event.getEventSize() != 16 && event.getEventSize() != 32){
            result.addError("Udstillingen skal have en deltager størrelse på enten 8, 16 eller 32");
        }

        return result;

    }

}
