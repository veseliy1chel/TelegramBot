package com.bot.command;

import com.bot.service.AnswerCallbackButton;
import com.bot.service.SendBotMessageService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ShowCommand implements Command {
    SendBotMessageService sendBotMessageService;
    String word;
    public ShowCommand(SendBotMessageService sendBotMessageService,String word){
        this.sendBotMessageService = sendBotMessageService;
        this.word= word;

    }
    @Override
    public void execute(Update update) {
        sendBotMessageService.answerToButton(AnswerCallbackButton.showWord(update.getCallbackQuery().getId(),word));
    }
}
