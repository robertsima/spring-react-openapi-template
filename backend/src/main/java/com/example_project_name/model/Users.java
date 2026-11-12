package com.example_project_name.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class Users {
    //test entity class
    private String name;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(lombok.AccessLevel.NONE) // id is assigned by the database
    private Long id;
}
