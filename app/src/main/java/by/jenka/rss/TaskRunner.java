package by.jenka.rss;


import by.jenka.rss.task3.AllTasks;

public class TaskRunner {

    public static void main(String[] args) {
        System.out.println("-------------   Start app");
        new AllTasks().run();
        System.out.println("---------------  End app");
    }

}
