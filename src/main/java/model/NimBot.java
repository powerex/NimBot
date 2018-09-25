package model;

import logic.GameNim;
import logic.Level;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class NimBot extends TelegramLongPollingBot {

    private Icon icon = new Icon();
    private GameNim gameNim = null;

    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        try {
            telegramBotsApi.registerBot(new NimBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String formMessage() {
        StringBuilder sb = new StringBuilder();

        for (int i=0; i<gameNim.getPreSet().length; i++) {
            for (int j=0; j<gameNim.getPreSet()[i]; j++)
                if (j < gameNim.getStones()[i])
                    sb.append(icon.getIcon("lock"));
                else
                    sb.append(icon.getIcon("doughnut"));
            sb.append("\n");
        }
        return sb.toString();
    }

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            System.out.println(message.getText());

            switch (message.getText()) {
                case "/button":
                    sendMsgButtons(message, "Prompt");
                    break;
                case "/help":
                    sendMsg(message, "Hello");
                    break;
            }

            if (message.getText().equals("/lock")) {
                sendMsg(message, icon.getIcon("lock"));
            }
            else if (message.getText().equals("/unlock")) {
                sendMsg(message, icon.getIcon("unlock"));
                sendMsg(message, "plus data");
            }
            else if (message.getText().equals("/easy")) {
                if (gameNim == null) {
                    gameNim = new GameNim(Level.EASY);
                    sendMsg(message, "start new game");
                    String view = formMessage();
                    sendMsg(message, view);
                }
                else sendMsg(message, "game already started");
            }
            else if (message.getText().equals("/4")) {
                StringBuilder sb = new StringBuilder();
                for (int i=0; i<4; i++) {
                    sb.append(icon.getIcon("doughnut"));
                }
                sendMsg(message, sb.toString());
            }
            else
            {
                System.out.println("I am here");
                System.out.println(message.getText());
                String[] ss = message.getText().split(" ");
                System.out.println(ss[0]);
                System.out.println(ss[1]);
                int[] move = new int[2];
                move[0] = Integer.parseInt(ss[0]);
                System.out.println(move[0]);
                move[1] = Integer.parseInt(ss[1]);
                gameNim.setMove(move[0], move[1]);
                sendMsg(message, formMessage());
            }
        }
    }

    private void sendMsgButtons(Message message, String msg){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        // Создаем клавиуатуру
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры
        keyboardFirstRow.add("Команда 1");
        keyboardFirstRow.add("Команда 2");

        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        // Добавляем кнопки во вторую строчку клавиатуры
        keyboardSecondRow.add("Команда 3");
        keyboardSecondRow.add("Команда 4");

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(msg);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(Message message, String msg) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        //sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(msg);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return "TryWinMe_bot";
    }

    public String getBotToken() {
        return "636053551:AAHhFYPJ9abV7KV-PPyVwda0FBVGBH4mIzs";
    }
}
