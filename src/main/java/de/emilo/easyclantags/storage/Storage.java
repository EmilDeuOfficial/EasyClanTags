package de.emilo.easyclantags.storage;

import de.emilo.easyclantags.model.ClanData;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface Storage {

    List<ClanData> load() throws IOException;

    void save(Collection<ClanData> clans) throws IOException;

    String describe();
}
