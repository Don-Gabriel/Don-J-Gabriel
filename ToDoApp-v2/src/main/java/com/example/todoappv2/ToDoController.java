package com.example.todoappv2;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SelectionMode;
import javafx.application.Platform;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.AWTException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class ToDoController {

    @FXML private ListView<Task> taskListView;
    @FXML private ListView<Task> completedListView;
    @FXML private TextField taskInput, searchBox;
    @FXML private ComboBox<String> priorityBox, reminderBox;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner, minuteSpinner;
    @FXML private Label clockLabel, statsLabel;

    private ObservableList<Task> tasks;
    private ObservableList<Task> completedTasks;
    private FilteredList<Task> filteredTasks;

    private boolean darkMode = false;
    private AudioClip alarmSound;
    private TaskDAO taskDAO = new TaskDAO();

    private TrayIcon trayIcon; // Single tray icon for notifications

    @FXML
    public void initialize() {
        tasks = FXCollections.observableArrayList();
        completedTasks = FXCollections.observableArrayList();

        // Load tasks from DB
        for (Task t : taskDAO.getAllTasks()) {
            if (t.isCompleted()) completedTasks.add(t);
            else tasks.add(t);
        }

        filteredTasks = new FilteredList<>(tasks, t -> true);
        taskListView.setItems(filteredTasks);
        completedListView.setItems(completedTasks);

        priorityBox.getItems().addAll("High", "Medium", "Low");
        priorityBox.setValue("Medium");

        reminderBox.getItems().addAll("10", "15", "30");
        reminderBox.setValue("10");

        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0,23,12));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0,59,0));

        taskListView.setCellFactory(param -> new TaskCell());
        completedListView.setCellFactory(param -> new TaskCell());

        enableDragAndDrop();
        startClock();
        updateStats();

        taskListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        completedListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        alarmSound = new AudioClip(getClass().getResource("/com/example/todoappv2/alarm.wav").toExternalForm());

        setupSystemTray();
    }

    @FXML
    public void addTask() {
        String title = taskInput.getText().trim();
        String priority = priorityBox.getValue();
        LocalDate date = datePicker.getValue();
        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        int reminderMins = Integer.parseInt(reminderBox.getValue());

        if (!title.isEmpty() && date != null) {
            LocalDateTime dueDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
            Task task = new Task(title, priority, dueDateTime);

            taskDAO.addTask(title, priority, dueDateTime);
            tasks.add(task);

            scheduleAlarm(task, reminderMins);

            FadeTransition ft = new FadeTransition(Duration.millis(300), taskListView.lookup(".list-cell:last-child"));
            ft.setFromValue(0); ft.setToValue(1); ft.play();

            taskInput.clear();
            datePicker.setValue(null);
            hourSpinner.getValueFactory().setValue(12);
            minuteSpinner.getValueFactory().setValue(0);

            updateStats();
        }
    }

    private void scheduleAlarm(Task task, int reminderMinutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime alarmTime = task.getDueDate().minusMinutes(reminderMinutes);
        long delay = java.time.Duration.between(now, alarmTime).toMillis();
        if (delay <= 0) return;

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!task.isCompleted()) {
                    alarmSound.play();
                    showSystemNotification(
                            "Task Reminder",
                            task.getTitle() + "\nDue at: " +
                                    task.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    );
                }
            }
        }, delay);
    }


    private void setupSystemTray() {
        if (!SystemTray.isSupported()) return;

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage(
                    getClass().getResource("/com/example/todoappv2/icon.png")
            );
            trayIcon = new TrayIcon(image, "To-Do App");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("To-Do App running");

            // Add tray icon only if not already present
            boolean exists = false;
            for (TrayIcon ti : tray.getTrayIcons()) {
                if (ti.getToolTip().equals("To-Do App running")) {
                    trayIcon = ti;
                    exists = true;
                    break;
                }
            }
            if (!exists) tray.add(trayIcon);

        } catch (AWTException e) {
            e.printStackTrace();
        }
    }


    private void showSystemNotification(String title, String message) {
        if (trayIcon != null) {
            java.awt.EventQueue.invokeLater(() -> {
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            });
        }
    }


    @FXML
    public void deleteTask() {
        ObservableList<Task> selectedPending = FXCollections.observableArrayList(taskListView.getSelectionModel().getSelectedItems());
        ObservableList<Task> selectedCompleted = FXCollections.observableArrayList(completedListView.getSelectionModel().getSelectedItems());

        for (Task t : selectedPending) {
            tasks.remove(t);
            taskDAO.deleteTask(t);
        }
        for (Task t : selectedCompleted) {
            completedTasks.remove(t);
            taskDAO.deleteTask(t);
        }
        updateStats();
    }

    @FXML
    public void toggleTheme() {
        Scene scene = taskListView.getScene();
        if (darkMode) {
            scene.getStylesheets().remove(getClass().getResource("dark.css").toExternalForm());
            darkMode = false;
        } else {
            scene.getStylesheets().add(getClass().getResource("dark.css").toExternalForm());
            darkMode = true;
        }
        taskListView.refresh();
        completedListView.refresh();
    }

    @FXML
    public void searchTasks() {
        String query = searchBox.getText().toLowerCase().trim();
        filteredTasks.setPredicate(task -> task.getTitle().toLowerCase().contains(query));
    }

    private void enableDragAndDrop() {
        taskListView.setOnDragDetected(event -> {
            int index = taskListView.getSelectionModel().getSelectedIndex();
            if (index < 0) return;
            Dragboard db = taskListView.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(index));
            db.setContent(content);
            event.consume();
        });

        taskListView.setOnDragOver(event -> {
            if (event.getGestureSource() != taskListView && event.getDragboard().hasString())
                event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });

        taskListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                int draggedIdx = Integer.parseInt(db.getString());
                Task draggedTask = tasks.remove(draggedIdx);

                int dropIndex = taskListView.getSelectionModel().getSelectedIndex();
                if (dropIndex < 0) dropIndex = tasks.size();

                tasks.add(dropIndex, draggedTask);
                event.setDropCompleted(true);
                taskListView.getSelectionModel().select(dropIndex);
            } else event.setDropCompleted(false);
            event.consume();
        });
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void updateStats() {
        long completed = completedTasks.size();
        statsLabel.setText(completed + " of " + (tasks.size() + completedTasks.size()) + " tasks completed");
    }

    @FXML
    public void moveToCompleted() {
        ObservableList<Task> selected = FXCollections.observableArrayList(taskListView.getSelectionModel().getSelectedItems());
        for (Task t : selected) {
            t.setCompleted(true);
            completedTasks.add(t);
            taskDAO.updateTaskStatus(t);
        }
        tasks.removeAll(selected);
        taskListView.getSelectionModel().clearSelection();
        updateStats();
    }

    @FXML
    public void moveToPending() {
        ObservableList<Task> selected = FXCollections.observableArrayList(completedListView.getSelectionModel().getSelectedItems());
        for (Task t : selected) {
            t.setCompleted(false);
            tasks.add(t);
            taskDAO.updateTaskStatus(t);
        }
        completedTasks.removeAll(selected);
        completedListView.getSelectionModel().clearSelection();
        updateStats();
    }

    class TaskCell extends ListCell<Task> {
        HBox hbox = new HBox(10);
        CheckBox checkBox = new CheckBox();
        Label titleLabel = new Label();
        Label priorityLabel = new Label();
        Label dueLabel = new Label();

        public TaskCell() {
            super();
            hbox.getChildren().addAll(checkBox, priorityLabel, titleLabel, dueLabel);
            checkBox.setOnAction(e -> {
                Task task = getItem();
                if (task != null) {
                    task.setCompleted(checkBox.isSelected());
                    if (task.isCompleted() && tasks.contains(task)) {
                        tasks.remove(task);
                        completedTasks.add(task);
                    } else if (!task.isCompleted() && completedTasks.contains(task)) {
                        completedTasks.remove(task);
                        tasks.add(task);
                    }
                    taskDAO.updateTaskStatus(task);
                    updateStats();
                }
            });
        }

        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);
            if (empty || task == null) {
                setGraphic(null);
            } else {
                titleLabel.setText(task.getTitle());
                dueLabel.setText(task.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                priorityLabel.setText(
                        switch (task.getPriority()) {
                            case "High" -> "🔴 High";
                            case "Medium" -> "🟠 Medium";
                            default -> "🟢 Low";
                        });
                checkBox.setSelected(task.isCompleted());
                if (task.isCompleted()) {
                    titleLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: gray;");
                    dueLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: gray;");
                } else {
                    titleLabel.setStyle("-fx-strikethrough: false; -fx-text-fill: black;");
                    dueLabel.setStyle("-fx-strikethrough: false; -fx-text-fill: black;");
                }
                setGraphic(hbox);
            }
        }
    }
}
