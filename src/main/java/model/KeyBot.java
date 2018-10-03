package model;

import logic.GameNim;
import logic.GameRender;
import logic.Level;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

public class KeyBot extends TelegramLongPollingBot {

    private final int ButtonsInRow = 5;
    private final int ButtonsInRowStones = 7;

    GameNim game = null;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            long chat_id = update.getMessage().getChatId();
            switch (update.getMessage().getText()) {
                case "/start":
                    sendMsgButtons("Hello my friend", chat_id);
                    break;
                case "/new":
                    //sendReplyMessage("New game was started", chat_id);
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
                    sendReplyMessage("Game was stopped. To play new game press \"/new\" button or type \'/new\' as message", chat_id);
                    if (game != null) {
                        game.stop();
                        game = null;
                    }
                    break;
                case "/help":
                    sendReplyMessage("No help yet))", chat_id);
                    break;
                case "/cancel":
                    if (game!=null) {
                        game.canceLastMove();
                    } else {
                        sendReplyMessage("No started game", chat_id);
                    }
                    break;
                default:
            }
        } else if (update.hasCallbackQuery()) {
            // Set variables
            String call_data = update.getCallbackQuery().getData();
            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            StringBuilder sb = new StringBuilder();
            EditMessageText new_message = new EditMessageText();
            String answer;
            switch (call_data) {
                case "/easy":
                    if (game != null) sb.append("Previous game corrupted\n");
                    game = new GameNim(Level.EASY);
                    sb.append("Game easy mode started");
                    new_message.setChatId(chat_id)
                            .setMessageId(toIntExact(message_id))
                            .setText(sb.toString());
                    break;
                case "/medium":
                    if (game != null) sb.append("Previous game corrupted\n");
                    game = new GameNim(Level.MEDIUM);
                    sb.append("Game medium mode started");
                    new_message.setChatId(chat_id)
                            .setMessageId(toIntExact(message_id))
                            .setText(sb.toString());
                    break;
                case "/hard":
                    if (game != null) sb.append("Previous game corrupted\n");
                    game = new GameNim(Level.HARD);
                    sb.append("Game medium mode started");
                    new_message.setChatId(chat_id)
                            .setMessageId(toIntExact(message_id))
                            .setText(sb.toString());
                    break;
                case "/cancelCreation":
                    //new_message = game.get
                    break;
                case "r_1":
                    answer = "1";
                    new_message.setChatId(chat_id)
                               .setMessageId(toIntExact(message_id))
                               .setText(answer);
                    new_message.setReplyMarkup(getStoneChoiseKeyboard(game.getStones()[0]));

                    break;
                case "r_2":
                    answer = "2";
                    new_message.setChatId(chat_id)
                            .setMessageId(toIntExact(message_id))
                            .setText(answer);
                    new_message.setReplyMarkup(getStoneChoiseKeyboard(7));

                    break;
                case "s_1":
                    new_message.setChatId(chat_id)
                               .setMessageId(toIntExact(message_id))
                               .setText("get " + call_data + " stones");
                    break;

            }
            try {
                execute(new_message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            SendMessage message = new SendMessage();
            message.setChatId(chat_id)
                    .setText(GameRender.getRender(game));
            message.setReplyMarkup(getRowChoiseKeyboard(game.getStones()));
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendReplyMessage(String message, long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMsgButtons(String msg, long chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(getKeyboadMarkup());
        sendMessage.setChatId(chatId);
        //sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(msg);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup getKeyboadMarkup() {
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

    private InlineKeyboardMarkup getRowChoiseKeyboard(int[] row) {
        int buttonsCount = row.length;
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>());
        int j = 0;
        for (int i=0; i<buttonsCount; i++) {
            if (row[i] > 0) {
                String caption = String.valueOf(i+1);
                rowsInline.get(rowsInline.size()-1).add(new InlineKeyboardButton().setText(caption).setCallbackData("r_"+caption));
                if (j%ButtonsInRow == ButtonsInRow-1) rowsInline.add(new ArrayList<>());
                j++;
            }
        }
        //rowsInline.get(rowsInline.size()-1).add(new InlineKeyboardButton().setText("Help").setCallbackData("/help"));
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private InlineKeyboardMarkup getStoneChoiseKeyboard(int count) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>());
        for (int i=0; i<count; i++) {
            String caption = String.valueOf(i+1);
            rowsInline.get(rowsInline.size()-1).add(new InlineKeyboardButton().setText(caption).setCallbackData("s_"+caption));
            if (i%ButtonsInRowStones == ButtonsInRowStones-1) rowsInline.add(new ArrayList<>());
        }
        rowsInline.get(rowsInline.size()-1).add(new InlineKeyboardButton().setText("Help").setCallbackData("/help"));
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

    @Override
    public String getBotUsername() {
        return "TryWinMe_bot";
    }

    @Override
    public String getBotToken() {
        return "636053551:AAHhFYPJ9abV7KV-PPyVwda0FBVGBH4mIzs";
    }
}
