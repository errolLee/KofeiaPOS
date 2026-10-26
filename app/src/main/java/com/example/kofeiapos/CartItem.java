package com.example.kofeiapos;

// One line in the customer's order.
public class CartItem {
    public int productId;
    public String name;
    public double price;
    public int qty;
    public int stock;   // stock available when it was added

    public CartItem(int productId, String name, double price, int qty, int stock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.stock = stock;
    }
}
