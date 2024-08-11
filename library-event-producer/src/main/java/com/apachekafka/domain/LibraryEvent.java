package com.apachekafka.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class LibraryEvent {
    private Integer libraryEventId;
    private LibraryEventType libraryEventType;
    private Book book;

    public LibraryEvent(Integer o, LibraryEventType libraryEventType, Book book) {
    }
}
