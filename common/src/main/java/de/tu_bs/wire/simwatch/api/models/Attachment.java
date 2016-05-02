package de.tu_bs.wire.simwatch.api.models;

/**
 * Created by mw on 18.04.16.
 */
public class Attachment {

    private String instanceID;
    private String attachmentName;

    public Attachment(String instanceID, String attachmentName) {
        if (instanceID == null) {
            throw new NullPointerException("instanceID is null");
        }
        if (attachmentName == null) {
            throw new NullPointerException("attachmentName is null");
        }
        this.instanceID = instanceID;
        this.attachmentName = attachmentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attachment)) return false;

        Attachment that = (Attachment) o;

        return instanceID.equals(that.instanceID) && attachmentName.equals(that.attachmentName);
    }

    @Override
    public String toString() {
        return "{instanceID='" + instanceID + "', attachmentName='" + attachmentName + "'}";
    }

    @Override
    public int hashCode() {
        int result = instanceID.hashCode();
        result = 31 * result + attachmentName.hashCode();
        return result;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public String getAttachmentName() {
        return attachmentName;
    }
}
