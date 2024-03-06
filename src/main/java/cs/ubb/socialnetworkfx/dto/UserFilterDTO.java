package cs.ubb.socialnetworkfx.dto;

import java.util.Optional;

public class UserFilterDTO implements FilterDTO {
    private Optional<String> name = Optional.empty();
    private Optional<String> username = Optional.empty();
    private Optional<String> maxSearch = Optional.empty();

    public Optional<String> getName() {
        return name;
    }

    public Optional<String> getUsername() {
        return username;
    }

    public Optional<String> getMaxSearch() {
        return maxSearch;
    }

    public void setName(String name) {
        this.name = Optional.of(name);
    }

    public void setUsername(String username) {
        this.username = Optional.of(username);
    }

    public void setMaxSearch(int maxSearch) {
        this.maxSearch = Optional.of(maxSearch + "");
    }
}
