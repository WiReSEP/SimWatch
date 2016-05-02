package de.tu_bs.wire.simwatch.simulation;

import java.io.File;
import java.util.Collection;

import de.tu_bs.wire.simwatch.api.models.Attachment;

/**
 * Created by mw on 18.04.16.
 */
public interface AttachmentKnowledge {

    Collection<Attachment> getAllAttachments();

    boolean has(Attachment attachment);

    String getVersion(Attachment attachment);

    void addAttachment(Attachment attachment, String version, File file);

    void removeAttachment(Attachment attachment);

    File getFile(Attachment attachment);

    Attachment getAttachment(File file);
}
