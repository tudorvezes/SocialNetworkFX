package cs.ubb.socialnetworkfx.service;

import cs.ubb.socialnetworkfx.domain.Friendship;
import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.dto.FriendshipFilterDTO;
import cs.ubb.socialnetworkfx.dto.UserFilterDTO;
import cs.ubb.socialnetworkfx.repository.Repository;

import java.util.*;

public class Graph {
    private final Repository<Long, User, UserFilterDTO> userDatabase;
    private final Repository<Long, Friendship, FriendshipFilterDTO> friendshipDatabase;

    public Graph(Repository<Long, User, UserFilterDTO> userDatabase, Repository<Long, Friendship, FriendshipFilterDTO> friendshipDatabase) {
        this.userDatabase = userDatabase;
        this.friendshipDatabase = friendshipDatabase;
    }

    public int countConnectedComponents() {
        Set<Long> visited = new HashSet<>();
        int components = 0;

        for (Long userId : userDatabase.getAllKeys()) {
            if (!visited.contains(userId)) {
                components++;
                dfs(userId, visited);
            }
        }

        return components;
    }

    public List<User> findLongestRoadComponent() {
        List<User> longestRoadComponent = new ArrayList<>();
        Set<Long> visited = new HashSet();

        for (Long userId : userDatabase.getAllKeys()) {
            if (!visited.contains(userId)) {
                List<User> currentPath = new ArrayList<>();
                List<User> currentComponent = new ArrayList<>();
                dfsForLongestComponent(userId, visited, currentPath, currentComponent, longestRoadComponent);
            }
        }

        return longestRoadComponent;
    }

    private void dfsForLongestComponent(Long userId, Set<Long> visited, List<User> currentPath, List<User> currentComponent, List<User> longestRoadComponent) {
        visited.add(userId);
        User user = userDatabase.findOne(userId).orElse(null);
        currentPath.add(user);
        currentComponent.add(user);

        List<Friendship> friendships = getFriendships(userId);

        for (Friendship friendship : friendships) {
            Long friendId = getOtherUserId(userId, friendship);
            if (!visited.contains(friendId)) {
                dfsForLongestComponent(friendId, visited, currentPath, currentComponent, longestRoadComponent);
            }
        }

        if (currentPath.size() > longestRoadComponent.size()) {
            longestRoadComponent.clear();
            longestRoadComponent.addAll(currentComponent);
        }

        visited.remove(userId);
        currentPath.remove(user);
    }

    private void dfs(Long userId, Set<Long> visited) {
        visited.add(userId);
        List<Friendship> friendships = getFriendships(userId);

        for (Friendship friendship : friendships) {
            Long friendId = getOtherUserId(userId, friendship);
            if (!visited.contains(friendId)) {
                dfs(friendId, visited);
            }
        }
    }

    private List<Friendship> getFriendships(Long userId) {
        List<Friendship> friendships = new ArrayList<>();
        for (Long friendshipId : friendshipDatabase.getAllKeys()) {
            Friendship friendship = friendshipDatabase.findOne(friendshipId).orElse(null);
            assert friendship != null;
            if (friendship.getUser1().equals(userId) || friendship.getUser2().equals(userId)) {
                friendships.add(friendship);
            }
        }
        return friendships;
    }

    private Long getOtherUserId(Long userId, Friendship friendship) {
        if (friendship.getUser1().equals(userId)) {
            return friendship.getUser2();
        } else if (friendship.getUser2().equals(userId)) {
            return friendship.getUser1();
        }
        return null;
    }
}
