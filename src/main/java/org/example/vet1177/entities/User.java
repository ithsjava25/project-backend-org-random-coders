package org.example.vet1177.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users") // nödvändigt med raden då User är reserverat ord i PostgreSQL
public class User {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    private String name;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role; //role använder enum OWNER, VET, ADMIN

    // Koppling till bilagor som användaren laddat upp
    // Vi använder inte CascadeType.REMOVE för att skydda medicinsk data om en användare raderas
    @OneToMany(mappedBy = "uploadedBy", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Attachment> uploadedAttachments = new ArrayList<>();

    public User() {
    } //tom konsturktor för JPA

    public User(String name, String email, String passwordHash, Role role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    public List<Attachment> getUploadedAttachments() {
        return java.util.Collections.unmodifiableList(uploadedAttachments);
    }

    public void setUploadedAttachments(List<Attachment> attachments) {
        this.uploadedAttachments.clear();
        if (attachments != null) {
            attachments.forEach(this::addAttachment);
        }
    }

    public void addAttachment(Attachment attachment) {
        if (attachment != null) {
            this.uploadedAttachments.add(attachment);
            // Säkerställ att bilagan pekar på denna användare
            if (attachment.getUploadedBy() != this) {
                attachment.setUploadedBy(this);
            }
        }
    }

    public void removeAttachment(Attachment attachment) {
        if (attachment != null) {
            this.uploadedAttachments.remove(attachment);
            attachment.setUploadedBy(null);
        }
    }
}
