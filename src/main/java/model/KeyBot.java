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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.toIntExact;

public class KeyBot extends TelegramLongPollingBot {

    private Map<Long, GameNim> sessions = new HashMap<>();

    private int selectedRow = -1;
    private int selectedNumber = -1;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            long chat_id = update.getMessage().getChatId();
            switch (update.getMessage().getText()) {
                case "/start":
                    sendMsgButtons("Hello, my friend", chat_id);
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
                    sendReplyMessage("Game was stopped. To play new game press \"/new\" button or type \'/new\' as message", chat_id, true);
                    if (sessions.get(chat_id) != null) {
                        sessions.get(chat_id).stop();
                    }
                    break;
                case "/help":
                    sendReplyMessage("You can take any count of doughnuts from only one row at once. \nIf You take a last doughnut - You will win the game.\nGood luck!", chat_id, true);
                    break;
                case "/cancel":
                    if (sessions.get(chat_id)!=null) {
                        sessions.get(chat_id).canceLastMove();
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

/*            if (game != null) {
                SendMessage keyBoard = new SendMessage();
                keyBoard.setChatId(chat_id)
                        .setText("SelectRow");
                keyBoard.setReplyMarkup(getRowChoiseKeyboard(game.getStones()));
                try {
                    execute(keyBoard);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }*/

            // Set variable
            String call_data = update.getCallbackQuery().getData();
            StringBuilder sb = new StringBuilder();
            //EditMessageText new_message = new EditMessageText();
            String answer;
            if ((call_data.substring(0, 2).equals("r_"))) {
                EditMessageText message = new EditMessageText();
                answer  = call_data.substring(2, call_data.length());
                System.out.println("Suppose is  " + answer);
                int row = Integer.valueOf(answer);
                selectedRow = row;
                        message.setChatId(chat_id)
                        .setMessageId(toIntExact(message_id))
                        .setText(answer);
                message.setReplyMarkup(getStoneChoiseKeyboard(sessions.get(chat_id).getStones()[row-1]));
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }
            else if ((call_data.substring(0, 2).equals("s_"))) {
                EditMessageText message = new EditMessageText();
                String s = call_data.substring(2, call_data.length());
                System.out.println("Number maybe " + s);
                int number = Integer.valueOf(s);
                message.setChatId(chat_id)
                        .setMessageId(toIntExact(message_id))
                        .setText("get " + s + " doughnuts");
                selectedNumber = number;
                sessions.get(chat_id).setMove(selectedRow, selectedNumber);
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
                    keyBoard.setReplyMarkup(getRowChoiseKeyboard(sessions.get(chat_id).getStones()));
                    try {
                        execute(keyBoard);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {

                    switch (sessions.get(chat_id).getStatus()) {
                        case WIN:
                            sendReplyMessage("You WIN!!!", chat_id, true);
                            sessions.put(chat_id, null);
                            break;
                        case LOSE:
                            sendReplyMessage("You LOSE!!!\n Try /new game?", chat_id);
                            sessions.put(chat_id, null);
                            break;
                    }
                }

            } else {
                EditMessageText new_message = new EditMessageText();
                boolean go = true;
                switch (call_data) {
                    case "/easy":
                        if (sessions.get(chat_id) != null) {
                            sb.append("Previous game corrupted\n");
                        }
                        GameNim g = new GameNim(Level.EASY);
                        sessions.put(chat_id, g);
                        sb.append("Game easy mode started");
                        new_message.setChatId(chat_id)
                                .setMessageId(toIntExact(message_id))
                                .setText(sb.toString());

                        SendMessage view = new SendMessage();
                        view.setChatId(chat_id)
                                .setText(GameRender.getRender(sessions.get(chat_id)));
                        try {
                            execute(view);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        SendMessage keyBoard = new SendMessage();
                        keyBoard.setChatId(chat_id)
                                .setText("SelectRow");
                        keyBoard.setReplyMarkup(getRowChoiseKeyboard(sessions.get(chat_id).getStones()));
                        try {
                            execute(keyBoard);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        break;
                    case "/medium":
                        if (sessions.get(chat_id) != null) {
                            sb.append("Previous game corrupted\n");
                        }
                        GameNim gM = new GameNim(Level.MEDIUM);
                        sessions.put(chat_id, gM);
                        sb.append("Game easy mode started");
                        new_message.setChatId(chat_id)
                                .setMessageId(toIntExact(message_id))
                                .setText(sb.toString());

                        SendMessage viewM = new SendMessage();
                        viewM.setChatId(chat_id)
                                .setText(GameRender.getRender(sessions.get(chat_id)));
                        try {
                            execute(viewM);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        SendMessage keyBoardM = new SendMessage();
                        keyBoardM.setChatId(chat_id)
                                .setText("SelectRow");
                        keyBoardM.setReplyMarkup(getRowChoiseKeyboard(sessions.get(chat_id).getStones()));
                        try {
                            execute(keyBoardM);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "/hard":
                        if (sessions.get(chat_id) != null) {
                            sb.append("Previous game corrupted\n");
                        }
                        GameNim gH = new GameNim(Level.HARD);
                        sessions.put(chat_id, gH);
                        sb.append("Game easy mode started");
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
                        keyBoardH.setReplyMarkup(getRowChoiseKeyboard(sessions.get(chat_id).getStones()));
                        try {
                            execute(keyBoardH);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "/cancelCreation":
                        SendMessage sm = new SendMessage();
                        sm.setChatId(chat_id).setText("Maybe next time...").setReplyMarkup(getKeyboadMarkup());
                        go = false;
                        try {
                            execute(sm);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                System.out.println("Active sessions " + sessions.size());
                System.out.println(sessions);
                try {
                   if (go) execute(new_message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

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
        if (withMainKeyBoard) sendMessage.setReplyMarkup(getKeyboadMarkup());
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

    private InlineKeyboardMarkup getStoneChoiseKeyboard(int count) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>());
        for (int i=0; i<count; i++) {
            String caption = String.valueOf(i+1);
            rowsInline.get(rowsInline.size()-1).add(new InlineKeyboardButton().setText(caption).setCallbackData("s_"+caption));
            int buttonsInRowStones = 7;
            if (i% buttonsInRowStones == buttonsInRowStones -1) rowsInline.add(new ArrayList<>());
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
        return "TryWinMe_bot";
    }

    @Override
    public String getBotToken() {
        return "636053551:AAHhFYPJ9abV7KV-PPyVwda0FBVGBH4mIzs";
    }
}
