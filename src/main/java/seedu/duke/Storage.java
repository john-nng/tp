package seedu.duke;

import seedu.duke.ui.UI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import static seedu.duke.Parser.DAYS;

/**
 * Represents a storage manager that deals with loading tasks from the file and saving tasks in the file.
 */


public class Storage {
    public static final int START_TIME_INDEX = 3;
    public static final int END_TIME_INDEX_INCREMENT = 2;
    public static final int END_TIME_END_INDEX = 16;
    public static final int DESCRIPTION_INDEX = 18;
    public static final int TYPE_INDEX_INCREMENT = 7;

    public static String folderPath = "data";
    public static final String BOX_OUTLINE = "+---------+\n";
    public static final String BOX_OUTLINE_FOR_WEDNESDAY = "+-------------+\n";
    public static final String BOX_OUTLINE_FOR_FRIDAY = "+------+\n";
    public static final String LINE_SEPARATOR =
            ".................................................................................................\n";
    public String filePath;

    public Storage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Gets the file path of the timetable of the user.
     *
     * @return the string that represents the filepath of the file storing the timetable of the user.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Gets the folder path of the folder where all data is stored.
     *
     * @return the string that represents the path of the folder where all data is stored.
     */
    public static String getFolderPath() {
        return folderPath;
    }

    /**
     * Sets the path of the data folder to a new path.
     *
     * @param newPath the string that represents the new path, which should be a relative path.
     */
    public static void setFolderPath(String newPath) {
        folderPath = newPath;
    }

    /**
     * Creates a new folder in the same directory when the application is first launched.
     * Otherwise, print that the folder has already been created.
     */
    public static void createFolder() {
        File folder = new File(folderPath);

        if (!folder.exists()) {
            boolean folderCreated = folder.mkdirs();
            if (folderCreated) {
                System.out.println("Folder created successfully.");
            } else {
                System.out.println("Failed to create folder.");
            }
        } else {
            System.out.println("Folder already exists.");
        }
    }

    /**
     * Add users that are stored in the data folder to the user list, for a returning user.
     *
     * @param userList the list of users added.
     * @throws FileNotFoundException if the data file is not found.
     */
    public static void addExistingUsers(UserList userList) throws FileNotFoundException {
        File directory = new File(folderPath);

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file != null) {
                    String fileName = file.getName();
                    int indexOfDot = fileName.indexOf(".");
                    String userName = fileName.substring(0, indexOfDot);
                    User user = new User(userName);
                    userList.addUser(user);
                    user.getStorage().loadData(user);
                }
            }
        } else {
            UI.printEmptyDirectory();
        }

    }

    /**
     * Loads data from the data file.
     *
     * @param user the user that we want to load data.
     * @throws FileNotFoundException if the data file is not found.
     */
    public void loadData(User user) throws FileNotFoundException {
        File f = new File(filePath);
        String day = "";
        Scanner s = new Scanner(f);
        while (s.hasNext()) {
            String line = s.nextLine();
            // ignore lines for formatting and weeks that have no tasks
            if (line.startsWith("Username") || line.startsWith("+") || line.startsWith(".")
                    || line.isEmpty() || line.equals("No task :)")) {
                continue;
            }
            if (line.startsWith("|")) {
                day = line.substring(1, line.length() - 1).trim();
                continue;
            }
            user.getTimetable().addUserTask(day, extractTaskInfo(line, day));
        }
    }

    /**
     * Creates a data file for a new user when a new user is added.
     */
    public void addUserInFolder() {
        File f = new File(filePath);
        try {
            if (f.createNewFile()) {
                System.out.println("File created: " + f.getName());
            }
        } catch (IOException e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }

    /**
     * Extracts task information from the local text file.
     *
     * @param line the current line in the file.
     * @param day  the day of the week.
     * @return a Task object represented by this line.
     */
    public static Task extractTaskInfo(String line, String day) {
        int indexOfDash = line.indexOf("-");
        String startTime = line.substring(START_TIME_INDEX, indexOfDash).trim();
        String endTime = line.substring(indexOfDash + END_TIME_INDEX_INCREMENT, END_TIME_END_INDEX).trim();
        int indexOfType = line.indexOf("(type:");
        String description = line.substring(DESCRIPTION_INDEX, indexOfType).trim();
        int indexOfRightParenthesis = line.indexOf(")");
        String type = line.substring(indexOfType + TYPE_INDEX_INCREMENT, indexOfRightParenthesis);
        return new Task(description, day, startTime, endTime, type);
    }

    /**
     * Writes the text to data file
     *
     * @param filePath  a relative path giving the location of the data file, relative to the current working directory.
     * @param textToAdd text to write to the file.
     * @param isAppend  whether to append the text or overwrite the whole file.
     * @throws IOException If there is something wrong.
     */
    public static void writeToFile(String filePath, String textToAdd, boolean isAppend) throws IOException {
        FileWriter fw = new FileWriter(filePath, isAppend);
        fw.write(textToAdd);
        fw.close();
    }

    /**
     * Adds task in file.
     *
     * @param user the user that the timetable belongs to.
     */

    public void writeTaskInFile(User user) throws IOException {
        Timetable timetable = user.getTimetable();
        writeToFile(filePath, "Username: " + user.getName() + "\n", false);
        for (String day : DAYS) {
            String outline;
            switch (day) {
            case ("Wednesday"):
                outline = BOX_OUTLINE_FOR_WEDNESDAY;
                break;
            case ("Friday"):
                outline = BOX_OUTLINE_FOR_FRIDAY;
                break;
            default:
                outline = BOX_OUTLINE;
                break;
            }
            writeToFile(filePath, outline, true);
            writeToFile(filePath, "| " + day + " |" + "\n", true);
            writeToFile(filePath, outline, true);

            if (timetable.getWeeklyTasks().get(day).isEmpty()) {
                writeToFile(filePath, "No task :)\n", true);
            } else {
                int taskCount = 1;
                for (Task task : timetable.getWeeklyTasks().get(day)) {
                    writeToFile(filePath, taskCount + ". " + task.getStartTime() + " - " + task.getEndTime() +
                            ": " + task.getDescription() + " (type: " + task.getType() + ")" + "\n", true);
                    taskCount += 1;
                }
            }
            writeToFile(filePath, LINE_SEPARATOR, true);
            writeToFile(filePath, "\n", true);
        }

        System.out.println("Timetable has been written to " + filePath);

    }
}
