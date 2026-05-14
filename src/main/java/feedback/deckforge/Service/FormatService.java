package feedback.deckforge.Service;

import feedback.deckforge.Model.Format;
import feedback.deckforge.Service.RepoInterfaces.IFormatRepository;
import feedback.deckforge.Service.Validation.FormatValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormatService {

    private IFormatRepository formatRepository;
    private FormatValidation formatValidation;

    public FormatService(IFormatRepository formatRepository, FormatValidation formatValidation) {
        this.formatRepository = formatRepository;
        this.formatValidation = formatValidation;
    }

    public ValidationResult createNewFormat(Format format){
        ValidationResult result = formatValidation.validateFormat(format);
        if (!result.hasErrors()) {
            formatRepository.saveFormat(format);
        }
        return result;
    }

    public ValidationResult updateExitingFormat(Format format){
        ValidationResult result = formatValidation.validateFormat(format);
        if (!result.hasErrors()) {
            formatRepository.updateFormat(format);
        }
        return result;
    }

    public void deleteExitingFormat(int formatID){
        formatRepository.deleteFormat(formatID);
    }

    public List<Format> getAllFormats(){
        return formatRepository.findAllFormats();
    }

    // NY METODE: Bruges til at hente det format vi gerne vil opdatere (GET Update)
    public Format getFormatById(int formatId) {
        return formatRepository.findAllFormats().stream()
                .filter(format -> format.getFormatID() == formatId)
                .findFirst()
                .orElse(null);
    }
}