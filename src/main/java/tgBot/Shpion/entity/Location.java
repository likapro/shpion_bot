package tgBot.Shpion.entity;

import java.util.ArrayList;
import java.util.List;

public class Location {
    public static List<String> location = new ArrayList<>() {{
        add("Больница");
        add("Отель");
        add("Школа");
//        add("Лес");
//        add("Компьютерный клуб");
//        add("Театр");
        add("Бордель");
//        add("У мангала");
//        add("Баня");
//        add("Мусорная свалка");
//        add("Гей клуб");
        add("Клуб традиционных ценностей");
    }};

    public static String getAllLocation() {
        String result = "Локации: \n";
        for (String s : location) {
            result += s + "\n";
        }

        return result;
    }
}
