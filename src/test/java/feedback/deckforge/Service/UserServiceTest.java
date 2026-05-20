package feedback.deckforge.Service;

import feedback.deckforge.Exceptions.EmailAlreadyInUseException;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.RepoInterfaces.*;
import feedback.deckforge.Service.Validation.UserValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private IUserRepository userRepository;
    @Mock private ICollectionRepository collectionRepository;
    @Mock private ITradeCollectionRepository tradeCollectionRepository;
    @Mock private IWishCollectionRepository wishCollectionRepository;
    @Mock private UserValidation userValidation;

    @InjectMocks
    private UserService userService;

    // 1. Succesfuld registrering hasher password og opretter samlinger
    @Test
    void registerNewUser_ShouldHashPasswordAndInitCollections_OnSuccess() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("test@test.dk");
        newUser.setPassword("mitKodeord123");

        // Få valideringen til at passere uden fejl
        when(userValidation.validateRegisterUser(any(User.class))).thenReturn(new ValidationResult());
        // Simuler at emailen IKKE er i brug
        when(userRepository.findUserByEmail("test@test.dk")).thenReturn(Optional.empty());
        // Simuler at brugeren gemmes og får ID 1
        when(userRepository.saveUser(any(User.class))).thenReturn(1);

        // Act
        ValidationResult result = userService.registerNewUser(newUser);

        // Assert
        assertFalse(result.hasErrors());

        // Tjek at adgangskoden er blevet hashet (den er ikke længere plain text)
        assertNotEquals("mitKodeord123", newUser.getPassword());
        assertTrue(BCrypt.checkpw("mitKodeord123", newUser.getPassword()));

        // Tjek at databasen blev kaldt til at oprette de 3 samlinger for bruger ID 1
        verify(collectionRepository, times(1)).initCollection(1);
        verify(tradeCollectionRepository, times(1)).initTradeCollection(1);
        verify(wishCollectionRepository, times(1)).initWishCollection(1);
    }

    // 2. Registrering fejler hvis emailen allerede findes
    @Test
    void registerNewUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail("brugt@email.dk");

        User newUser = new User();
        newUser.setEmail("brugt@email.dk");

        when(userValidation.validateRegisterUser(any(User.class))).thenReturn(new ValidationResult());

        // Simuler at repositoriet returnerer en bruger, når vi søger på emailen
        when(userRepository.findUserByEmail("brugt@email.dk")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(EmailAlreadyInUseException.class, () -> {
            userService.registerNewUser(newUser);
        }, "Forventede en EmailAlreadyInUseException fordi e-mailen findes i forvejen.");

        // Verificér at saveUser aldrig blev kaldt, fordi metoden kastede en fejl før det
        verify(userRepository, never()).saveUser(any(User.class));
    }
}