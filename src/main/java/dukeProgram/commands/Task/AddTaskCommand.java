package dukeprogram.commands.task;

import dukeprogram.*;
import dukeprogram.commands.Command;
import dukeprogram.facilities.TaskList;
import dukeprogram.ui.UserInterface;
import exceptions.InvalidCommandException;
import exceptions.JobNameException;
import utilities.StringUtilities;

import java.util.Arrays;
import java.util.stream.Collectors;

import static dukeprogram.ui.UserInterface.printInStyle;

public class AddTaskCommand extends Command {

    private final String[] fullCommandParameters;

    public AddTaskCommand(String[] fullCommandParameters) {
        this.fullCommandParameters = fullCommandParameters;
    }

    @Override
    public boolean execute() {
        try {
            return parse().execute();
        } catch (InvalidCommandException ex) {
            UserInterface.printInStyle(ex.getUiMessage().toString());
            return true;
        }
    }

    private Command parse() throws InvalidCommandException {
        return parse("");
    }

    @Override
    public Command parse(String commandString) throws InvalidCommandException {
        if (fullCommandParameters.length < 2) {
            throw new InvalidCommandException(
                    new UiMessage(
                            "Usage: add todo task_name\n"
                                    + "\t\tExample: add todo mytask\n"
                                    + "\t\tUsage: add [event | deadline] task_name /at date\n"
                                    + "\t\tExample: add event myevent /at 22 Aug"));
        }

        switch (fullCommandParameters[1]) {
        case "todo":
            return new CreateToDoTask();

        case "deadline":
            return new CreateDeadlineTask();

        case "event":
            return new CreateEventTask();

        default:
            throw new InvalidCommandException(
                    new UiMessage(
                            String.format("Hmm... %s does not seem to be a valid task type",
                                    fullCommandParameters[1])
                    )
            );
        }
    }

    private static String concatName(String[] input) throws JobNameException {
        String name = Arrays.stream(input).skip(2).collect(Collectors.joining(" "));
        if (name.equals("")) {
            throw new JobNameException(input[0]);
        }
        return name;
    }

    private abstract static class CreateTaskCommand extends Command {

        protected Task task;

        @Override
        public boolean execute() {
            printInStyle(
                    "Got it. I've added this task:",
                    task.toString(),
                    String.format("Now you have %d tasks in the list", TaskList.current().getSize()));
            return true;
        }

        @Override
        public Command parse(String commandString) throws InvalidCommandException {
            throw new InvalidCommandException(this, commandString,
                    new UiMessage("I can't understand more commands from Create!"));
        }
    }

    private class CreateToDoTask extends CreateTaskCommand {

        @Override
        public boolean execute() {
            try {
                task = new ToDo(concatName(fullCommandParameters));
                TaskList.current().add(task);
                super.execute();

            } catch (JobNameException ex) {
                printInStyle(ex.getMessage());
            }
            return true;
        }
    }


    private class CreateDeadlineTask extends CreateTaskCommand {
        @Override
        public boolean execute() {
            String[][] nameAndDate = StringUtilities
                    .splitStringArray(fullCommandParameters, "/by");

            if (nameAndDate.length != 2) {
                printInStyle("Please use /by to set a time");
                return true;
            }

            try {
                task = new Deadline(concatName(nameAndDate[0]),
                        String.join(" ", nameAndDate[1]));
                TaskList.current().add(task);
                super.execute();

            } catch (JobNameException ex) {
                printInStyle(ex.getMessage());
            }
            return true;
        }
    }

    private class CreateEventTask extends CreateTaskCommand {
        @Override
        public boolean execute() {
            String[][] nameAndDate = StringUtilities.splitStringArray(fullCommandParameters, "/at");
            if (nameAndDate.length != 2) {
                printInStyle("Please use /at to set a time");
                return true;
            }

            try {
                task = new Event(concatName(nameAndDate[0]),
                        String.join(" ", nameAndDate[1]));
                TaskList.current().add(task);
                super.execute();

            } catch (JobNameException ex) {
                printInStyle(ex.getMessage());
            }
            return true;
        }
    }
}