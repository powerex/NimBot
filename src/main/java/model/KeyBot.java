package model;

import logic.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.toIntExact;

public class KeyBot extends TelegramLongPollingBot {

    private Map<Long, GameNim> sessions = new HashMap<>();
    private Properties property;

    private int selectedRow = -1;

    KeyBot() {
        try {
            property =new Properties();
            property.load(new FileInputStream("src/main/resources/bot.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            long chat_id = update.getMessage().getChatId();
            switch (update.getMessage().getText()) {
                case "/start":
                    sendMsgButtons(chat_id);
                    break;
                case "/new":
                    //sendReplyMessage("New game was started", chat_id);

                    Logger.log(
                            update.getMessage().getFrom().getFirstName(),
                            update.getMessage().getFrom().getLastName(),
                            String.valueOf(update.getMessage().getFrom().getId()));

                    SendMessage new_message = new SendMessage();
                    new_message.setChatId(chat_id)
                            //.setMessageId(update.getCallbackQuery().getMessage().getChatId())
                            .setText("---");
                    new_message.setReplyMarkup(getDifficultKeyboard());
                    try {
                        execute(new_message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;
                case "/stop":
                    sendReplyMessage("Game was stopped. To play new game press \"/new\" button or type \'/new\' as message", chat_id, true);
                    if (sessions.get(chat_id) != null) {
                        sessions.get(chat_id).stop();
                        System.out.println(update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + " stopped the game\n");
                    }
                    break;
                case "/help":
                    sendReplyMessage("You can take any count of doughnuts from only one row at once. \nIf You take a last doughnut - You will win the game.\nGood luck!", chat_id, true);
                    break;
                case "/cancel":
                    if (sessions.get(chat_id)!=null) {
                        sessions.get(chat_id).cancelLastMove();
                        sendReplyMessage("No canceling in free version", chat_id);
                    } else {
                        sendReplyMessage("No started game", chat_id, true);
                    }
                    break;
                default:
            }
        } else if (update.hasCallbackQuery()) {

            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            String call_data = update.getCallbackQuery().getData();
            String answer;
            switch (call_data.substring(0, 2)) {
                case "r_": {
                    EditMessageText message = new EditMessageText();
                    answer = call_data.substring(2);
                    int row = Integer.valueOf(answer);
                    selectedRow = row;
                    message.setChatId(chat_id)
                            .setMessageId(toIntExact(message_id))
                            .setText(answer);
                    message.setReplyMarkup(getStoneChoiceKeyboard(sessions.get(chat_id).getStones()[row - 1]));
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "s_": {
                    EditMessageText message = new EditMessageText();
                    answer = call_data.substring(2);
                    int number = Integer.valueOf(answer);
                    message.setChatId(chat_id)
                            .setMessageId(toIntExact(message_id))
                            .setText("get " + answer + " doughnuts");
                    sessions.get(chat_id).setMove(selectedRow, number);
                    renderGame(chat_id);

                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                    if (!sessions.get(chat_id).isOver()) {
                        Move move = sessions.get(chat_id).rightMove();
                        sendReplyMessage("Take " + move.getNumber() + "doughnut(s) from " + move.getRow() + " row", chat_id);
                        renderGame(chat_id);
                    }

                    if (!sessions.get(chat_id).isOver()) {
                        SendMessage keyBoard = new SendMessage();
                        keyBoard.setChatId(chat_id)
                                .setText("SelectRow");
                        keyBoard.setReplyMarkup(getRowChoiceKeyboard(sessions.get(chat_id).getStones()));
                        try {
                            execute(keyBoard);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else {
                        switch (sessions.get(chat_id).getStatus()) {
                            case WIN:
                                sendReplyMessage("You WIN!!!", chat_id, true);
                                System.out.println(update.getCallbackQuery().getFrom().getFirstName() + " wins!");
                                sessions.put(chat_id, null);
                                break;
                            case LOSE:
                                sendReplyMessage("You LOSE!!!\n Try /new game?", chat_id);
                                sessions.put(chat_id, null);
                                break;
                        }
                    }
                    break;
                }
                default:
                    boolean go = true;
                    switch (call_data) {
                        case "/easy":
                            beginNewGame(Level.EASY, chat_id, message_id);
                            System.out.println("Easy");
                            break;
                        case "/medium":
                            beginNewGame(Level.MEDIUM, chat_id, message_id);
                            System.out.println("Medium");
                            break;
                        case "/hard":
                            beginNewGame(Level.HARD, chat_id, message_id);
                            System.out.println("Hard");
                            break;
                        case "/cancelCreation":
                            SendMessage sm = new SendMessage();
                            sm.setChatId(chat_id).setText("Maybe next time...").setReplyMarkup(getKeyboardMarkup());
                            go = false;
                            try {
                                execute(sm);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    try {
                        if (go) {
                            EditMessageText new_message = new EditMessageText();
                            new_message.setChatId(chat_id)
                                    .setMessageId(toIntExact(message_id))
                                    .setText("-");
                            execute(new_message);
                        }
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private void beginNewGame(Level level, long chat_id, long message_id) {
        StringBuilder sb = new StringBuilder();
        if (sessions.get(chat_id) != null) {
            sb.append("Previous game corrupted\n");
        }
        GameNim gH = new GameNim(level);
        sessions.put(chat_id, gH);
        sb.append("Game started");
        EditMessageText new_message = new EditMessageText();
        new_message.setChatId(chat_id)
                .setMessageId(toIntExact(message_id))
                .setText(sb.toString());
        SendMessage viewH = new SendMessage();
        viewH.setChatId(chat_id)
                .setText(GameRender.getRender(sessions.get(chat_id)));
        try {
            execute(viewH);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        SendMessage keyBoardH = new SendMessage();
        keyBoardH.setChatId(chat_id)
                .setText("SelectRow");
        keyBoardH.setReplyMarkup(getRowChoiceKeyboard(sessions.get(chat_id).getStones()));
        try {
            execute(keyBoardH);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendReplyMessage(String message, long chatId) {
        sendReplyMessage(message, chatId, false);
    }

    private void sendReplyMessage(String message, long chatId, boolean withMainKeyBoard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);
        sendMessage.setText(message);
        if (withMainKeyBoard) sendMessage.setReplyMarkup(getKeyboardMarkup());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMsgButtons(long chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(getKeyboardMarkup());
        sendMessage.setChatId(chatId);
        //sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText("Hello, my friend");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup getKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры

        keyboardFirstRow.add("/new");
        keyboardFirstRow.add("/stop");

        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        // Добавляем кнопки во вторую строчку клавиатуры
        keyboardSecondRow.add("/cancel");
        keyboardSecondRow.add("/help");

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private InlineKeyboardMarkup getRowChoiceKeyboard(int[] row) {
        int buttonsCount = row.length;
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>());
        int j = 0;
        for (int i=0; i<buttonsCount; i++) {
            if (row[i] > 0) {
                String caption = String.valueOf(i+1);
                rowsInline.get(rowsInline.size()-1).add(new InlineKeyboardButton().setText(caption).setCallbackData("r_"+caption));
                int buttonsInRow = 5;
                if (j% buttonsInRow == buttonsInRow -1) rowsInline.add(new ArrayList<>());
                j++;
            }
        }
        //rowsInline.get(rowsInline.size()-1).add(new InlineKeyboardButton().setText("Help").setCallbackData("/help"));
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private InlineKeyboardMarkup getStoneChoiceKeyboard(int count) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>());
        for (int i=0; i<count; i++) {
            String caption = String.valueOf(i+1);
            rowsInline.get(rowsInline.size()-1).add(new InlineKeyboardButton().setText(caption).setCallbackData("s_"+caption));
            int buttonsInRowStones = 7;
            if (i% buttonsInRowStones == buttonsInRowStones -1) rowsInline.add(new ArrayList<>());
        }
        //rowsInline.get(rowsInline.size()-1).add(new InlineKeyboardButton().setText("Help").setCallbackData("/help"));
        // Add it to the message
        keyboardMarkup.setKeyboard(rowsInline);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getDifficultKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>());
        rowsInline.get(0).add(new InlineKeyboardButton().setText("EASY").setCallbackData("/easy"));
        rowsInline.get(0).add(new InlineKeyboardButton().setText("MEDIUM").setCallbackData("/medium"));
        rowsInline.get(0).add(new InlineKeyboardButton().setText("HARD").setCallbackData("/hard"));
        rowsInline.add(new ArrayList<>());
        rowsInline.get(1).add(new InlineKeyboardButton().setText("Cancel").setCallbackData("/cancelCreation"));
        keyboardMarkup.setKeyboard(rowsInline);
        return keyboardMarkup;
    }

    private void renderGame(long chat_id) {
        SendMessage view = new SendMessage();
        view.setChatId(chat_id)
                .setText(GameRender.getRender(sessions.get(chat_id)));
        try {
            execute(view);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return property.getProperty("bot.name");
    }

    @Override
    public String getBotToken() {
        return property.getProperty("bot.token");
    }
}
