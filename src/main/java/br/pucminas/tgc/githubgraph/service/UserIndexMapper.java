package br.pucminas.tgc.githubgraph.service;

import br.pucminas.tgc.githubgraph.model.GitHubUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapeia usuários do GitHub para índices inteiros usados pela API de grafos.
 */
public final class UserIndexMapper {

    private final List<GitHubUser> usersByIndex;
    private final Map<GitHubUser, Integer> indexByUser;

    public UserIndexMapper(Collection<GitHubUser> users) {
        if (users == null) {
            throw new IllegalArgumentException("A coleção de usuários não pode ser nula.");
        }

        List<GitHubUser> sortedUsers = new ArrayList<>(users);
        sortedUsers.sort((left, right) -> left.getLogin().compareToIgnoreCase(right.getLogin()));

        this.usersByIndex = List.copyOf(sortedUsers);
        this.indexByUser = new HashMap<>();
        for (int index = 0; index < usersByIndex.size(); index++) {
            indexByUser.put(usersByIndex.get(index), index);
        }
    }

    public int getUserCount() {
        return usersByIndex.size();
    }

    public int getIndex(GitHubUser user) {
        Integer index = indexByUser.get(user);
        if (index == null) {
            throw new IllegalArgumentException("Usuário não mapeado: " + user);
        }
        return index;
    }

    public GitHubUser getUser(int index) {
        if (index < 0 || index >= usersByIndex.size()) {
            throw new IllegalArgumentException("Índice de usuário inválido: " + index);
        }
        return usersByIndex.get(index);
    }

    public List<GitHubUser> getUsersInOrder() {
        return Collections.unmodifiableList(usersByIndex);
    }
}
