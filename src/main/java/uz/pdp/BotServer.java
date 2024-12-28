package uz.pdp;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;

import static uz.pdp.Main.bot;

public class BotServer {
    public static void server(Update update) {
        try {
            String text = update.message().text();
            Long chatId = update.message().chat().id();
            User user = findUser(chatId);

            if (text != null && text.equals("/start")) {
                SendMessage sendMessage = new SendMessage(chatId, "Tizimga hush kelibsiz. Boshlash uchun 1 ni bosing");
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup("1");
                sendMessage.replyMarkup(replyKeyboardMarkup);
                bot.execute(sendMessage);
                user.setStatus(Status.CABINET);
            } else {
                if (user.getStatus().equals(Status.CABINET)) {
                    if (text.equals("1")) {
                        if (user.getPayments().isEmpty()) {
                            SendMessage sendMessage = new SendMessage(
                                    chatId,
                                    """
                                            You have no payments yet!
                                            1 - Add payment
                                            2 - Report
                                            """
                            );
                            sendMessage.replyMarkup(new ReplyKeyboardMarkup("1", "2"));
                            bot.execute(sendMessage);
                        } else {
                            StringBuilder builder = new StringBuilder();
                            for (Payment payment : user.getPayments()) {
                                builder.append(payment.getAmount());
                            }
                            SendMessage sendMessage = new SendMessage(
                                    chatId, builder + """
                                    \n
                                    1 - Add payment
                                    2 - Report
                                    """
                            );
                            sendMessage.replyMarkup(new ReplyKeyboardMarkup("1", "2"));
                            bot.execute(sendMessage);
                        }
                        user.setStatus(Status.SYSTEM);
                    }
                } else if (user.getStatus().equals(Status.SYSTEM)) {
                    if (text.equals("1")) {
                        SendMessage sendMessage = new SendMessage(
                                chatId, "Enter amount to pay"
                        );
                        sendMessage.replyMarkup(new ReplyKeyboardRemove());
                        bot.execute(sendMessage);
                        user.setStatus(Status.ADD_PAYMENT);
                    } else if (text.equals("2")) {
                        SendMessage sendMessage = new SendMessage(
                                chatId, reportPayment(user)
                        );
                        sendMessage.replyMarkup(new ReplyKeyboardMarkup("0 - Back"));
                        bot.execute(sendMessage);
                        user.setStatus(Status.CABINET);
                    } else {
                        SendMessage sendMessage = new SendMessage(
                                chatId, "Invalid option!"
                        );
                        user.setStatus(Status.CABINET);
                        bot.execute(sendMessage);
                    }
                } else if (user.getStatus().equals(Status.ADD_PAYMENT)) {
                    Payment payment = user.getCurrentPayment();
                    if (payment == null) {
                        payment = new Payment();
                        user.setCurrentPayment(payment);
                    }

                    if (payment.getNext()) {
                        payment.setAmount(Integer.parseInt(text));
                        payment.setNext(false);
                        payment.setHave(true);
                    }

                    if (payment.getHave()) {
                        payment.setHave(false);
                        SendMessage sendMessage = new SendMessage(
                                chatId, "Choose payment type: \n1-Pay_me\n2-Grapes\n3-Click"
                        );
                        sendMessage.replyMarkup(new ReplyKeyboardMarkup("1", "2", "3"));
                        bot.execute(sendMessage);
                        payment.setCome(true);
                    } else if (payment.getCome()) {
                        switch (text) {
                            case "1":
                                payment.setType(Type.PAY_ME);
                                break;
                            case "2":
                                payment.setType(Type.GRAPES);
                                break;
                            case "3":
                                payment.setType(Type.CLICK);
                                break;
                            default:
                                SendMessage sendMessage = new SendMessage(
                                        chatId, "Invalid option! Please try again!"
                                );
                                return;
                        }
                        payment.setCome(false);
                        SendMessage sendMessage = new SendMessage(
                                chatId, "Payment added successfully! \n 0 -Back"
                        );
                        user.getPayments().add(payment);
                        user.setCurrentPayment(null);
                        sendMessage.replyMarkup(new ReplyKeyboardMarkup("0"));
                        bot.execute(sendMessage);
                        user.setStatus(Status.CABINET);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String reportPayment(User user) {
        if (user.getPayments().isEmpty()) {
            return "No payments yet!";
        }

        int pay_me = 0;
        int click = 0;
        int grapes = 0;

        for (Payment payment : user.getPayments()) {
            if (payment.getType().equals(Type.CLICK)) {
                click += payment.getAmount();
            }
            if (payment.getType().equals(Type.GRAPES)) {
                grapes += payment.getAmount();
            }
            if (payment.getType().equals(Type.PAY_ME)) {
                pay_me += payment.getAmount();
            }
        }
        return "Payme -> " + pay_me + "\nClick -> " + click + "\nUzum -> " + grapes;
    }

    private static User findUser(Long chatId) {
        for (User user : DB.USERS) {
            if (user.getChatId().equals(chatId)) {
                return user;
            }
        }
        User user = new User();
        user.setChatId(chatId);
        DB.USERS.add(user);
        DB.export();
        return user;
    }
}
