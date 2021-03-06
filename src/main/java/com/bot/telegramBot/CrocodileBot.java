package com.bot.telegramBot;

import com.bot.command.Command;
import com.bot.command.CommandContainer;
import com.bot.service.AnswerCallbackButton;
import com.bot.service.CanStartTimer;
import com.bot.service.GenerateWord;
import com.bot.service.SendBotMessageServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.bot.command.CommandName.*;

@Component
public class CrocodileBot extends TelegramLongPollingBot{
    CanStartTimer timer = new CanStartTimer();
    GenerateWord word = new GenerateWord();



    public static String COMMAND_PREFIX = "/";
    @Value("${bot.username}")
    private String username;

    @Value("${bot.token}")
    private String token;

    private String nameUser="";

    private final CommandContainer commandContainer;

    public CrocodileBot(){
        this.commandContainer = new CommandContainer(new SendBotMessageServiceImpl(this),word.getWord());
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText().trim();
            if(update.getMessage().getChat().isGroupChat()) {
                if (message.startsWith(COMMAND_PREFIX)) {
                    actionCommand(update);
                }
                checkWord(update);
            }
            else commandContainer.retriveCommand("unknown").execute(update);
        }
        else if(update.hasCallbackQuery()){



            checkChangeWord(update);
            if(nameUser.equals("")||nameUser.equals(update.getCallbackQuery().getFrom().getId().toString())) {
                String commandName = update.getCallbackQuery().getData();
                nameUser = update.getCallbackQuery().getFrom().getId().toString();
                commandContainer.retriveCommand(commandName).execute(update);
            }
            else{
                commandContainer.retriveCommand(UNKNOWN_PERSON.getCommandName()).execute(update);
            }

        }
    }

    private void changeWord(){
            word.changeWord();
            commandContainer.setWord(word.getWord(),new SendBotMessageServiceImpl(this));
    }
    private void checkWord(Update update){
        String message = update.getMessage().getText();
        if(message.toLowerCase().contains(word.getWord())&& !update.getMessage().getFrom().getId().toString().equals(nameUser)){
            commandContainer.retriveCommand(GUESS_WORD.getCommandName()).execute(update);
            commandContainer.retriveCommand(START.getCommandName()).execute(update);
            nameUser = "";
            timer.offVotes();
            changeWord();
        }
    }
    private void checkChangeWord(Update update){
        if (update.getCallbackQuery().getData().equals(START_GAME.getCommandName())) {
            timer.enableVotes();
            changeWord();
        }
        if(update.getCallbackQuery().getData().equals(CHANGE_WORD.getCommandName())){
            changeWord();
        }
    }
    public void actionCommand(Update update) {
        String commandIdentifier = update.getMessage().getText().split(" ")[0].toLowerCase();

        if (!commandIdentifier.equals(START.getCommandName()) | timer.votesEnabled()) {

            commandContainer.retriveCommand(commandIdentifier).execute(update);
            if (commandIdentifier.equals(START.getCommandName())) {
                changeWord();
                timer.enableVotes();
            }

        } else {
            commandContainer.retriveCommand(NO.getCommandName()).execute(update);
        }

    }

}
