package cs.ubb.socialnetworkfx.dto;

import java.util.List;
import java.util.Optional;

public class ChatRoomFilterDTO implements FilterDTO {
    Optional<List<Long>> userIds = Optional.empty();

    public void setUserIds(List<Long> userIds) {
        this.userIds = Optional.of(userIds);
    }

    public Optional<List<Long>> getUserIds() {
        return userIds;
    }
}
