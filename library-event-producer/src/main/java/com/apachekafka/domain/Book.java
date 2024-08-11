package com.apachekafka.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Book {
    private Integer bookId;
    private String bookName;
    private String bookAuthor;
    public Book(Integer i, String dilip, String kafkaUsingSpringBoot) {
    }
}
