package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.User;
import org.springframework.stereotype.Component;

@Component
public class UserValidation {



    public ValidationResult validateRegisterUser(User user){

        ValidationResult result = new ValidationResult();

        if(user.getUsername() == null || user.getUsername().trim().isEmpty()){
            result.addError("Du skal indtaste et navn");
        }

        if(user.getEmail() == null || !user.getEmail().contains("@")){
            result.addError("Du skal indtaste en  gyldig e-mailadresse (skal indholde @).");
        }

        if(user.getPassword() == null || user.getPassword().trim().isEmpty()){
            result.addError("Du skal indtaste et password");
        }

        else if(user.getPassword().length() < 6){
            result.addError("Dit password skal være mindst 6 tegn langt");
        }
        return result;
    }

    public ValidationResult validateUpdateUser(User user){
        ValidationResult result = new ValidationResult();

        if(user.getUsername() == null || user.getUsername().trim().isEmpty()){
            result.addError("Du skal indtaste et navn");
        }

        if(user.getEmail() == null || !user.getEmail().contains("@")){
            result.addError("Du skal indtaste en  gyldig e-mailadresse (skal indholde @).");
        }
        return result;
    }

}
