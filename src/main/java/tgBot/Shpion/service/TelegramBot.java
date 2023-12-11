package tgBot.Shpion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tgBot.Shpion.entity.Team;
import tgBot.Shpion.entity.Location;
import tgBot.Shpion.config.BotConfig;

import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    BotConfig botConfig;

    @Autowired
    Message message;

    @Override
    public void onUpdateReceived(Update update) {
        handlerUpdate(update);

    }

    public void hasMessage(Update update) {
        String message = update.getMessage().getText().toLowerCase();
        long chatId = update.getMessage().getChatId();
        String name = update.getMessage().getChat().getFirstName();

        User user = update.getMessage().getFrom();
        String chatType = update.getMessage().getChat().getType();

        log.info(chatId + ", " + name + ", " + message);

        switch (message) {
            case "/start":
                if (chatType.equalsIgnoreCase("private")) {
                    sendMessage(user.getId(), "Теперь можешь играть!");
                } else {
                    executeMessage(this.message.inGroup(chatId));
                }
                break;

//            case "/run":
//                if (chatType.equalsIgnoreCase("private")) {
//                    sendMessage(user.getId(), "Играть можно только в групповом чате");
//                } else {
//                    try {
//                        gameRun(chatId, 0);
//                    } catch (InterruptedException e) {
//
//                    }
//                }
//                break;

            case "/voting":
            case "голосование":
                if (chatType.equalsIgnoreCase("private")) {
                    sendMessage(user.getId(), "Играть можно только в групповом чате");
                } else if (Team.teams.get(chatId).contains(user)) {
                    voting(chatId);
                } else {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setMessageId(update.getMessage().getMessageId());
                    deleteMessage.setChatId(chatId);
                    executeMessage(deleteMessage);
                }
                break;

            case "/end":
                if (chatType.equalsIgnoreCase("private")) {
                    sendMessage(user.getId(), "А ты и не играешь");
                } else if (Team.teams.get(chatId).contains(user)) {
                    gameEnd(chatId);
                } else {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setMessageId(update.getMessage().getMessageId());
                    deleteMessage.setChatId(chatId);
                    executeMessage(deleteMessage);
                }
                break;

            default:
                if (Team.gameRun.get(chatId) && !Team.teams.get(chatId).contains(user)) {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setMessageId(update.getMessage().getMessageId());
                    deleteMessage.setChatId(chatId);
                    executeMessage(deleteMessage);
                }

        }
    }

    public void handlerUpdate(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            hasMessage(update);
        } else if (update.hasCallbackQuery()){

            long chatId = update.getCallbackQuery().getMessage().getChatId();
            User user = update.getCallbackQuery().getFrom();
            String callbackData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (callbackData.equals("1")) {
                addGroup(update);
            } else if (callbackData.equals("2") && Team.teams.get(chatId).contains(user)) {
                try {
                    gameRun(update.getCallbackQuery().getMessage().getChatId(), messageId);
                } catch (InterruptedException e) {

                }
            } else if (callbackData.startsWith("voting")){
                voting(update);
            }
        }
    }

    private void voting(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        String callbackDataUser = callbackData.substring(7);
        User voter = update.getCallbackQuery().getFrom();
        log.info(String.format("Чат %d, %s проголосовал за %s", chatId, voter.getUserName(), callbackDataUser));

        if (callbackDataUser.equals(voter.getUserName())) {
            sendMessage(chatId, "@" + voter.getUserName() + " за себя голосовать нельзя");
            return;
        }

        if (Team.votes.containsKey(chatId)) {
            Team.votes.get(chatId).put(voter, callbackDataUser);
        } else {
            Map<User, String> mapUser = new HashMap<>();
            mapUser.put(voter, callbackDataUser);
            Team.votes.put(chatId, mapUser);
        }

        if (Team.votes.get(chatId).size() == Team.teams.get(chatId).size()) {
            couting(chatId, update.getCallbackQuery().getMessage().getMessageId());
        }
    }

    private void couting(long chatId, int messageIdForEdit) {
        Map<String, Integer> votes = new HashMap<>();
        Map<User, String> votesVoters = Team.votes.get(chatId);

        for (String name : votesVoters.values()) {
            if (votes.containsKey(name)) {
                votes.put(name, votes.get(name)+1);
            } else {
                votes.put(name, 1);
            }
        }

        int maxVote = votes.values().stream().max(Integer::compare).get();
        int countMaxVote = 0;
        String nameMaxVote = "";
        StringBuilder countVotes = new StringBuilder("Итоги голосования: \n");
        for (String name : votes.keySet()) {
            if (votes.get(name) == maxVote) {
                countMaxVote++;
                nameMaxVote = name;
            }
            countVotes.append(name + ": " + votes.get(name) + "\n");
        }
        String result = countVotes.toString();
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setText(result);
        editMessage.setMessageId(messageIdForEdit);
        executeMessage(editMessage);

        User userSpy = Team.userSpyGroup.get(chatId);
        if (countMaxVote == 1) {
            sendMessage(chatId, "Вы проголосовали за: " + nameMaxVote);
            if (nameMaxVote.equals(userSpy.getUserName())) {
                sendMessage(chatId, "Вы вычислили шпиона! Поздравляем!");
            } else {
                sendMessage(chatId, String.format("Шпион %s, победил! Пам-пам...", userSpy.getFirstName()));
            }
        } else {
            sendMessage(chatId, "Мнения разошлись");
        }

        Team.votes.remove(chatId);
    }

    private void gameEnd(long chatId) {
        Team.gameRun.put(chatId, false);

        if (Team.teams.containsKey(chatId)) {
            for (User user : Team.teams.get(chatId)) {
                sendMessage(user.getId(), "Игра окончена");
            }
            Team.teams.remove(chatId);
            sendMessage(chatId, "Игра окончена");
        }
    }

    public void addGroup(Update update) {
        User user = update.getCallbackQuery().getFrom();
        long idChat = update.getCallbackQuery().getMessage().getChat().getId();
//        Map<Long, User> userChatId = Message.userChatId;
//        if(!userChatId.containsValue(user)) {
//            sendMessage(idChat, String.format("@%s, надо начать личный чат с ботом", user.getUserName()));
//            return;
//        } else
        if (Team.teams.containsKey(idChat)) {
            Set<User> users = Team.teams.get(idChat);
            if (!users.contains(user)) {
                users.add(user);
                sendMessage(idChat, "+ " + user.getFirstName());
                log.info("Игрок " + user.getFirstName() + " (" + user.getUserName() + ") добавлен в игру в чате " + update.getCallbackQuery().getMessage().getChat().getTitle());
            }
        }
    }

    private void voting(long chatId) {
        Set<User> users = Team.teams.get(chatId);
        SendMessage message1 = message.voting(users);
        message1.setChatId(chatId);
        executeMessage(message1);
    }

    public void gameRun(long chatId, int messageIdForEdit) throws InterruptedException {

        if (Team.gameRun.containsKey(chatId) && Team.gameRun.get(chatId).booleanValue()) {
            return;
        }

        Map<Long, Set<User>> teams = Team.teams;
        int size = teams.get(chatId).size();

        if(teams.containsKey(chatId) && size > 0) {

            Set<User> users = teams.get(chatId);
            List<User> userList = new ArrayList<>(users);

            // Определяем кто начинает вопросы
            int randomStart = (int) (Math.random() * userList.size());
            String userStart = userList.get(randomStart).getFirstName();

            // Раздаем роли
            int randomSpy = (int) (Math.random() * userList.size());
            log.info("Рандомное число для шпиона " + randomSpy);
            User userSpy = userList.remove(randomSpy);
            Team.userSpyGroup.put(chatId, userSpy);
            log.info("Шпион " + userSpy.getFirstName());

            // Выбираем локацию
            int randomLocation = (int) (Math.random() * Location.location.size());
            String location = Location.location.get(randomLocation);
            log.info("Рандомная локация " + randomLocation + " " + location);

            // Рассылаем роли в личные сообщения
            // Шпиону
            sendMessage(userSpy.getId(), "Ты шпион");
            // Мирным
            for(User user : userList) {
                    sendMessage(user.getId(), "Ты мирный.\nЛокация " + location);
            }

            // оповещаем в чате, что игра началась
            EditMessageText editMessage = new EditMessageText();
            editMessage.setText(String.format("Начинаем. Участников: %d", size));
            editMessage.setMessageId(messageIdForEdit);
            editMessage.setChatId(chatId);
            executeMessage(editMessage);

            Thread.sleep(1000);
            sendMessage(chatId, Location.getAllLocation());

            Team.gameRun.put(chatId, true);

            Thread.sleep(1500);
            sendMessage(chatId, "Первый вопрос от " + userStart);

            //Thread.sleep(3000);
            //Thread.sleep(600000);

        } else {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setText(String.format("Команда не набрана. Участников: %d", size));
            editMessage.setMessageId(messageIdForEdit);
            editMessage.setChatId(chatId);
            executeMessage(editMessage);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    public void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        executeMessage(message);
    }

    public void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeMessage(EditMessageText editMessage) {
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeMessage(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
