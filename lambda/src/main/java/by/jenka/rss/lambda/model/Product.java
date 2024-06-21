package by.jenka.rss.lambda.model;

import java.util.UUID;


public class Product {
    private UUID id;
    private String title;
    private String description;
    private int count;
    private double price;

    public Product(UUID id, String title, String description, int count, double price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.count = count;
        this.price = price;
    }


    public UUID id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public int count() {
        return count;
    }

    public double price() {
        return price;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
