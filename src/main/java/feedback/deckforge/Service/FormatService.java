package feedback.deckforge.Service;

import feedback.deckforge.Model.Format;
import feedback.deckforge.Service.RepoInterfaces.IFormatRepository;
import feedback.deckforge.Service.Validation.FormatValidation;
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

    public void createNewFormat(Format format){
        formatValidation.validateFormat(format);
        formatRepository.saveFormat(format);
    }

    public void deleteExitingFormat(int formatID){
        formatRepository.deleteFormat(formatID);
    }

    public void updateExitingFormat(Format format){
        formatRepository.updateFormat(format);
    }

    public List<Format> getAllFormats(){
        return formatRepository.findAllFormats();
    }



}
