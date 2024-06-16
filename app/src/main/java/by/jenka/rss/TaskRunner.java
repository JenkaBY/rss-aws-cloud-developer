package by.jenka.rss;


import by.jenka.rss.task2.Task2;
import by.jenka.rss.task3.Task3;

public class TaskRunner {


    public static void main(String[] args) {
        System.out.println("-------------   Start app");
        new Task2().run();
        new Task3().run();
        System.out.println("---------------  End app");
    }

}
