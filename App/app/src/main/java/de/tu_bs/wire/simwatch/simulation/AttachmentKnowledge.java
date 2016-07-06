package de.tu_bs.wire.simwatch.simulation;

import java.io.File;
import java.util.Collection;

import de.tu_bs.wire.simwatch.api.models.Attachment;

/**
 * Interface for a local storage that saves information about attachments that have been downloaded
 * and stored locally. The information include the identifying instance id and attachment name, the
 * location where each individual attachment is stored, and version information of the attachments.
 * The storage implementing this interface does NOT save the attachments themselves
 */
public interface AttachmentKnowledge {

    /**
     * Reads and returns identifiers for all Attachments, information whereof are stored in the
     * local storage
     *
     * @return A Collection of all known Attachments
     */
    Collection<Attachment> getAllAttachments();

    /**
     * Determines whether the given Attachment currently has its information stored in the storage
     *
     * @param attachment Identifier of the Attachment in question
     * @return true, if the Attachment is in the storage, or false otherwise
     */
    boolean has(Attachment attachment);

    /**
     * Determines what version of the given Attachment is saved locally. Note, that the storage does
     * not specify the format of the version information. The user of this interface must decide on
     * the format of the version String
     *
     * @param attachment Identifier of the Attachment in question
     * @return The String identifying the version of the Attachment stored locally
     */
    String getVersion(Attachment attachment);

    /**
     * Adds information about an Attachment to the storage. Information stored include the
     * Attachment identifier, its version String, and its file location. If the storage already
     * holds information about an Attachment with this identifier, the original data will be
     * overridden by this method call. Implementations of this Interface may or may not allow null
     * as version Strings or file locations. Note, that the storage does not specify the format of
     * the version information. The user of this interface must decide on the format of the version
     * String
     *
     * @param attachment Identifier of the Attachment in question
     * @param version    The version String
     * @param file       The location where the Attachment is saved or should be saved
     */
    void addAttachment(Attachment attachment, String version, File file);

    /**
     * Removes all information of a given Attachment from the storage or does nothing, if the
     * storage contains no such Attachment
     *
     * @param attachment Identifier of the Attachment in question
     * @return true, if the storage was changed by this method call, or false otherwise
     */
    boolean removeAttachment(Attachment attachment);

    /**
     * Determines the location, what the given Attachment is stored locally
     *
     * @param attachment Identifier of the Attachment in question
     * @return The file location of the Attachment or null if the storage has no information of the
     * Attachment
     */
    File getFile(Attachment attachment);

    /**
     * Determines the Attachment that is stored at the given file location
     *
     * @param file The file location in question
     * @return Identifier of the Attachment that is stored at the given location according to the
     * information of this storage, or null if there is no such Attachment
     */
    Attachment getAttachment(File file);
}
