package com.example.shopkz.model;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;

@Entity
@Table
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String model;
    private Double price;
    private Integer code;
    private Boolean available;
    private String image;
    private String url;
    @Length(max = 1000000)
    private String description;

    public Item(String model, Double price, Integer code, Boolean available, String image, String url, String description) {
        this.model = model;
        this.price = price;
        this.code = code;
        this.available = available;
        this.image = image;
        this.url = url;
        this.description = description;
    }

    public Item() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
