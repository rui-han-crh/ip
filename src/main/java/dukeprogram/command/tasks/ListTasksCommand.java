package dukeprogram.command.tasks;

import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dukeprogram.Duke;
import dukeprogram.command.Command;
import dukeprogram.facilities.TaskList;
import dukeprogram.userinterface.Widget;
import javafx.scene.layout.Region;

/**
 * A ListTaskCommand specifies a command that allows the main program
 * to list out all the tasks presently stored in the task list.
 */
public class ListTasksCommand extends Command {

    /**
     * Creates a ListTasksCommand
     * @param duke the instance of duke that spawned this command
     */
    public ListTasksCommand(Duke duke) {
        super(duke);
    }

    @Override
    public void parse(Iterator<String> elements) {
        printToGui();
    }

    /**
     * Sends a message to the window of the Duke instance to list
     * all the tasks to the GUI window
     */
    public void printToGui() {
        TaskList currentTaskList = duke.getTaskList();

        Stream<Region> tasksLabelStream = IntStream.range(0, currentTaskList.getSize())
                .mapToObj(i -> currentTaskList.get(i).createLabelWidget());

        duke.sendMessage("Here is your task list:",
                new Widget(tasksLabelStream.toArray(Region[]::new)));
    }
}
