package model;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

public class KeyBot extends TelegramLongPollingBot {

    final int ButtonsInRow = 5;
    final int ButtonsInRowStones = 7;

    @Override
    public void onUpdateReceived(Update update) {

        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            if (update.getMessage().getText().equals("/start")) {

                SendMessage message = new SendMessage() // Create a message object object
                        .setChatId(chat_id)
                        .setText("You send long text /staaaaaaaart");

                int[] arr = {1, 2, 4, 5};
                message.setReplyMarkup(getRowChoiseKeyboard(arr));
                try {
                    execute(message); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {

            }

        } else if (update.hasCallbackQuery()) {

            // Set variables
            String call_data = update.getCallbackQuery().getData();
            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            EditMessageText new_message = new EditMessageText();
            switch (call_data) {
                case "ans_1":
                    String answer = "1";
                    new_message.setChatId(chat_id)
                               .setMessageId(toIntExact(message_id))
                               .setText(answer);
                    new_message.setReplyMarkup(getStoneChiseKeyboard(7));

                    break;
                case "/help":
                    new_message.setChatId(chat_id)
                               .setMessageId(toIntExact(message_id))
                               .setText("No help ))");
                case "s_1":
                    new_message.setChatId(chat_id)
                               .setMessageId(toIntExact(message_id))
                               .setText("get " + call_data + " stones");

            }
            try {
                execute(new_message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    private InlineKeyboardMarkup getRowChoiseKeyboard(int[] row) {
        int buttonsCount = row.length;
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>());
        for (int i=0; i<buttonsCount; i++) {
            String caption = String.valueOf(row[i]);
            rowsInline.get(rowsInline.size()-1).add(new InlineKeyboardButton().setText(caption).setCallbackData("ans_"+caption));
            if (i%ButtonsInRow == ButtonsInRow-1) rowsInline.add(new ArrayList<>());
        }
        rowsInline.get(rowsInline.size()-1).add(new InlineKeyboardButton().setText("Help").setCallbackData("/help"));
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private InlineKeyboardMarkup getStoneChiseKeyboard(int count) {
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

    @Override
    public String getBotUsername() {
        return "TryWinMe_bot";
    }

    @Override
    public String getBotToken() {
        return "636053551:AAHhFYPJ9abV7KV-PPyVwda0FBVGBH4mIzs";
    }
}
