package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.Format;

import java.util.List;
import java.util.Optional;

public interface IFormatRepository {

    void saveFormat(Format format);
    void deleteFormat(int formatID);
    void updateFormat(Format format);
    List<Format> findAllFormats();
    Optional<Format> findFormatByID(int formatID);
}
