package uk.me.eastmans.invoicemanagement;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "invoice")
public class Invoice {

    public static final int DESCRIPTION_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description = "";

    @Temporal(TemporalType.DATE)
    private LocalDate invoiceDate;

    @Column(name = "fileName", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String fileName = "";

    @Column(name = "mimeType", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String mimeType = "";


    @Lob
    private byte[] imageData;

    protected Invoice() { // To keep Hibernate happy
    }

    public Invoice(String description) {
        this.description = description;
        invoiceDate = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {this.description = description;}

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String filename) {
        this.fileName = filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public LocalDate getInvoiceDate() {return invoiceDate;}

    public void setInvoiceDate(LocalDate when) {this.invoiceDate = when;}

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Invoice other = (Invoice) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        // Hashcode should never change during the lifetime of an object. Because of
        // this we can't use getId() to calculate the hashcode. Unless you have sets
        // with lots of entities in them, returning the same hashcode should not be a
        // problem.
        return id == null ? getClass().hashCode() : id.hashCode();
    }

}