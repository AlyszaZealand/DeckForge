package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.Format;

import java.util.List;

public interface IFormatRepository {

    void saveFormat(Format format);
    void deleteFormat(int formatID);
    void updateFormat(Format format);
    List<Format> findAllFormats();
}
