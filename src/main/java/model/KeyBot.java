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

                message.setReplyMarkup(getRowChoiseKeyboard(0));
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

            if (call_data.equals("ans_1")) {
                String answer = "1";
                EditMessageText new_message = new EditMessageText()
                        .setChatId(chat_id)
                        .setMessageId(toIntExact(message_id))
                        .setText(answer);
                try {
                    execute(new_message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private InlineKeyboardMarkup getRowChoiseKeyboard(int[] row) {
        int buttonsCount = row.length;
        int rowCount = (buttonsCount + 3 + 1) / 4;

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("1").setCallbackData("ans_1"));
        rowInline1.add(new InlineKeyboardButton().setText("2").setCallbackData("ans_2"));
        rowInline1.add(new InlineKeyboardButton().setText("3").setCallbackData("ans_3"));
        rowInline2.add(new InlineKeyboardButton().setText("4").setCallbackData("ans_4"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
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
