package com.example.kofeiapos;

// One drink on the menu.
// hot / iced = price. 0 means "not available in that type".
public class Product {
    public int id;
    public String name;
    public String category;
    public double hot;
    public double iced;
    public int stock;

    public Product(int id, String name, String category, double hot, double iced, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.hot = hot;
        this.iced = iced;
        this.stock = stock;
    }
}
