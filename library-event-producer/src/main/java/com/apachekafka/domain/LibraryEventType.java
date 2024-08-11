package com.apachekafka.domain;

import lombok.Getter;

@Getter
public enum LibraryEventType {
    NEW("NEW"),
    UPDATE("UPDATE");

    LibraryEventType(String update) {
    }
}
