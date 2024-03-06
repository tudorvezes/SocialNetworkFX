package cs.ubb.socialnetworkfx.dto;

import java.util.Optional;

public class FriendshipFilterDTO implements FilterDTO {
    private Optional<Long> user1 = Optional.empty();
    private Optional<Long> user2 = Optional.empty();

    public Optional<Long> getUser1() {
        return user1;
    }

    public Optional<Long> getUser2() {
        return user2;
    }

    public void setUser1(Long user1) {
        this.user1 = Optional.of(user1);
    }

    public void setUser2(Long user2) {
        this.user2 = Optional.of(user2);
    }
}
