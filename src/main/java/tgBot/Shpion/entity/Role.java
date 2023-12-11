package tgBot.Shpion.entity;

import org.telegram.telegrambots.meta.api.objects.User;

public class Role {
    private User user;
    private String role;

    public Role(User user, String role) {
        this.user = user;
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }
}
