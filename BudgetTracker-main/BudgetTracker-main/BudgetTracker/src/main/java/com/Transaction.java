package com;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Transaction {
    private final StringProperty category;
    private final DoubleProperty amount;
    private final StringProperty date;

    public Transaction(String category, double amount, String date) {
        this.category = new SimpleStringProperty(category);
        this.amount = new SimpleDoubleProperty(amount);
        this.date = new SimpleStringProperty(date);
    }

    // Category property


    public String getCategory() {
        return category.get();
    }



    // Amount property


    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    // Date property


    public String getDate() {
        return date.get();
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "category='" + getCategory() + '\'' +
                ", amount=" + getAmount() +
                ", date='" + getDate() + '\'' +
                '}';
    }
}