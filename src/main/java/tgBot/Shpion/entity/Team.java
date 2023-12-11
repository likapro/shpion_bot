package tgBot.Shpion.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;

@Slf4j
@Data
public class Team {

    public static Map<Long, Set<User>> teams = new HashMap();
    public static Map<Long, Boolean> gameRun = new HashMap<>();
    public static Map<Long, Map<User, String>> votes = new HashMap<>();

    public static Map<Long, User> userSpyGroup = new HashMap<>();

}
