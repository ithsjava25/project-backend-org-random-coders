package org.example.vet1177.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "vet_details")
public class Vet {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "license_id", nullable = false, unique = true, length = 50)
    private String licenseId;

    @Column(name = "specialization", length = 255)
    private String specialization;

    @Column(name = "booking_info", length = 500)
    private String bookingInfo;


    public Vet() {
    }

    public Vet(User user, String licenseId, String specialization, String bookingInfo) {
        this.setUser(user);
        this.licenseId = licenseId;
        this.specialization = specialization;
        this.bookingInfo = bookingInfo;
    }

    // Getters och Setters
    public UUID getUserId() {
        return userId;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getBookingInfo() {
        return bookingInfo;
    }

    public void setBookingInfo(String bookingInfo) {
        this.bookingInfo = bookingInfo;
    }
}