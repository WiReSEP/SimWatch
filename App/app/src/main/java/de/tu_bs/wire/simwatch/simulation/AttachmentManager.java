package de.tu_bs.wire.simwatch.simulation;

import android.content.Context;

import java.io.File;

import de.tu_bs.wire.simwatch.net.AttachmentProvider;
import de.tu_bs.wire.simwatch.net.HTTPAttachmentProvider;

/**
 * Created by mw on 18.04.16.
 */
public class AttachmentManager implements AttachmentDownloadListener {

    public static final String ATTACHMENT_DIR_NAME = "simulations";
    private Context context;
    private File attachmentDir;
    protected static AttachmentManager instance;

    protected AttachmentManager(Context context) {
        this.context = context;
        attachmentDir = new File(context.getFilesDir(), ATTACHMENT_DIR_NAME);
    }

    protected static AttachmentManager getInstance(Context context) {
        if (instance==null) {
            instance=new AttachmentManager(context);
        }
        return instance;
    }

    public void download(String attachmentName) {
        AttachmentProvider attachmentProvider = new HTTPAttachmentProvider(this, context);
        File file = new File(attachmentDir,attachmentName); //todo proper file placement
        attachmentProvider.download(attachmentName, file);
    }

    @Override
    public void onFileChanged(File file) {

    }

    @Override
    public void onFileUnchanged(File file) {

    }

    @Override
    public void onDownloaded(File file) {

    }

    @Override
    public void onCouldNotCheckForChange(File file) {

    }

    @Override
    public void onCouldNotDownload(File file) {

    }
}
