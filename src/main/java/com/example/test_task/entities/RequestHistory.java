package com.example.test_task.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "request_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "time_of_request", nullable = false, updatable = false)
    private String time;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "explanation")
    private String explanation;

    public RequestHistory(Double latitude, Double longitude, String explanation) {
        this.time = String.valueOf(new Date());
        this.latitude = latitude;
        this.longitude = longitude;
        this.explanation = explanation;
    }

}
