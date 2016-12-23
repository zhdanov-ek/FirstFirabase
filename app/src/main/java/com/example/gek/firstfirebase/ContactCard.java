package com.example.gek.firstfirebase;

/**
 * Для работы с FireBase описываем класс с простыми полями, обычным конструктором и
 * гетерами с сетерами
 */

public class ContactCard {
    private String name;
    private String phone;

    // Базовый конструктор необходим для  DataSnapshot.getValue(ContactCard.class)
    public ContactCard() {
    }


    public ContactCard(String name, String phone, String autor) {
        this.name = name;
        this.phone = phone;
        this.autor = autor;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    private String autor;







    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
