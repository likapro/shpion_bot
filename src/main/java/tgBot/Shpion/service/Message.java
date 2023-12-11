package tgBot.Shpion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import tgBot.Shpion.entity.Team;

import java.util.*;

@Component
@Slf4j
public class Message {

    public static Map<Long, User> userChatId = new HashMap<>();

    public SendMessage inGroup(long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Набираем команду");

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Добавиться в игру");
        inlineKeyboardButton1.setCallbackData("1");
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(inlineKeyboardButton1);
        rowsInline.add(rowInline1);

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("Начать");
        inlineKeyboardButton2.setCallbackData("2");
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(inlineKeyboardButton2);
        rowsInline.add(rowInline2);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        Team.teams.put(chatId, new HashSet<>());

        return message;
    }

    public SendMessage voting(Set<User> users) {
        SendMessage message = new SendMessage();
        message.setText("Голосуем");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        for (User user : users) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(user.getFirstName());
            button.setCallbackData("voting " + user.getUserName());

            List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
            inlineKeyboardButtons.add(button);
            buttons.add(inlineKeyboardButtons);
        }

        markup.setKeyboard(buttons);
        message.setReplyMarkup(markup);

        return message;
    }

    public SendMessage m(long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Набираем команду");

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("В игру");
        inlineKeyboardButton1.setCallbackData("1");
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(inlineKeyboardButton1);
        rowsInline.add(rowInline1);

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("Начать");
        inlineKeyboardButton2.setCallbackData("2");
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(inlineKeyboardButton2);
        rowsInline.add(rowInline2);


        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        Team.teams.put(chatId, new HashSet<>());

        return message;
    }


//    public SendMessage voting(Map<Roles, List<User>> roles){
//        SendMessage message = new SendMessage();
//        message.setText("Голосуем");
//
//        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
//
//        for (Map.Entry<Roles, List<User>> rolesListEntry : roles.entrySet()) {
//            for (User user : rolesListEntry.getValue()) {
//
//                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
//                inlineKeyboardButton.setText(user.getFirstName());
//                inlineKeyboardButton.setCallbackData(user.getFirstName());
//
//                List<InlineKeyboardButton> rowInline = new ArrayList<>();
//                rowInline.add(inlineKeyboardButton);
//                rowsInline.add(rowInline);
//            }
//        }
//
//        markupInline.setKeyboard(rowsInline);
//
//        message.setReplyMarkup(markupInline);
//
//        return message;
//    }
}