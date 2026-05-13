package feedback.deckforge.Service;

import feedback.deckforge.Exceptions.EmailAlreadyInUseException;
import feedback.deckforge.Exceptions.InvalidCredentialsException;
import feedback.deckforge.Exceptions.UserNotFoundException;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.RepoInterfaces.ICollectionRepository;
import feedback.deckforge.Service.RepoInterfaces.ITradeCollectionRepository;
import feedback.deckforge.Service.RepoInterfaces.IUserRepository;
import feedback.deckforge.Service.RepoInterfaces.IWishCollectionRepository;
import feedback.deckforge.Service.Validation.UserValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final IUserRepository userRepository;
    private final UserValidation userValidation;
    private final ICollectionRepository collectionRepository;
    private final ITradeCollectionRepository tradeCollectionRepository;
    private final IWishCollectionRepository wishCollectionRepository;


    public UserService(IUserRepository userRepository,
                       UserValidation userValidation,
                       ICollectionRepository collectionRepository,
                       ITradeCollectionRepository tradeCollectionRepository,
                       IWishCollectionRepository wishCollectionRepository) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
        this.collectionRepository = collectionRepository;
        this.tradeCollectionRepository = tradeCollectionRepository;
        this.wishCollectionRepository = wishCollectionRepository;
    }

    public List<User> getAllUsers(){

        List<User> userList = userRepository.findAllUsers();

        return userList;
    }

    public User getUserByID(int userID) {
        return userRepository.findUserByID(userID)
                .orElseThrow(() -> new UserNotFoundException("Brugeren med ID " + userID + " blev ikke fundet."));
    }

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Ingen bruger fundet med e-mailen: " + email));
    }

    public void deleteUser(User user){
        userRepository.deleteUser(user.getUserID());
    }


    public ValidationResult registerNewUser(User newUser) {
        ValidationResult result = userValidation.validateRegisterUser(newUser);

        if (result.hasErrors()){
            return result;
        }

        // Tjek email
        if (userRepository.findUserByEmail(newUser.getEmail()).isPresent()){
            throw new EmailAlreadyInUseException("Denne e-mail er allerede i brug");
        }

        // 1. Hash password
        String hashedPassword = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
        newUser.setPassword(hashedPassword);

        // 2. Gem brugeren og få det nye ID
        int newUserId = userRepository.saveUser(newUser);

        // 3. Opret de tre tomme samlinger med det samme
        collectionRepository.initCollection(newUserId);
        tradeCollectionRepository.initTradeCollection(newUserId);
        wishCollectionRepository.initWishCollection(newUserId);

        return result;
    }

    public User loginValidation(String email, String rawPassword) {
        // Hvis brugeren ikke findes, kastes vores nye InvalidCredentialsException direkte
        User loggedUser = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Hov! E-mailen eller kodeordet er forkert."));

        try {
            if(BCrypt.checkpw(rawPassword, loggedUser.getPassword())){
                return loggedUser; // Succes! Returner brugeren direkte
            } else {
                throw new InvalidCredentialsException("Hov! E-mailen eller kodeordet er forkert.");
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidCredentialsException("Der skete en fejl under validering af kodeord.");
        }
    }


}
