package com.consumer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LibraryEvent {

    @Id
    @GeneratedValue
    private Integer libraryEventId;

    @Enumerated(EnumType.STRING)
    private LibraryEventType libraryEventType;

    @OneToOne(mappedBy = "libraryEvent", cascade = {CascadeType.ALL})
    @ToString.Exclude
    private Book book;

}
