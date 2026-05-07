package com.defragler.fixiqo.entities;

import com.defragler.fixiqo.annotaions.*;

/**
 * Represents a client (customer) in the service center system.
 *
 * <p>This entity stores personal information about a client who submits repair requests.
 * Each client has a unique ID and a phone number for contact.</p>
 */
@Table(name = "Clients")
@Indexes({
      @Index(name = "idx_clients_phone", columns = {"phone_number"}, unique = true)
})
public class Client {
    @Id(autoIncrement = true)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "phone_number", length = 20, nullable = false, unique = true)
    private String phoneNumber;

    public Client() { }

    public Client(String fullName, String phoneNumber) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the unique identifier of the client.
     *
     * @return client ID (primary key)
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the full name of the client.
     *
     * @return full name as a string
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets or updates the client's full name.
     *
     * @param fullName new full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the client's primary phone number.
     *
     * @return phone number string
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets or updates the client's phone number.
     *
     * @param phoneNumber new phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}