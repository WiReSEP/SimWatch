package de.tu_bs.wire.simwatch.simulation;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import de.tu_bs.wire.simwatch.api.models.Attachment;

/**
 * Created by mw on 18.04.16.
 */
public class FileAttachmentKnowledge implements AttachmentKnowledge {

    public static final String ATTACHMENT_KNOWLEDGE_DIR_NAME = "attachmentKnowledge";
    public static final String LAST_MODIFIED_FILE_NAME = "lastModified.txt";
    public static final String FILE_POINTER_FILE_NAME = "filePointers.txt";
    private static final String TAG = "FileAttachmentKnowledge";
    /**
     * Maps an Instance id and an attachmentName to the attachment's last modified date
     */
    private final Map<Attachment, String> attachment2Version;
    /**
     * Maps an Instance id and an attachmentName to the attachment's file location
     */
    private final Map<Attachment, File> attachment2File;
    private final Map<File, Attachment> file2Attachment;
    private File attachmentKnowledgeDir;

    FileAttachmentKnowledge(Context context) {
        attachmentKnowledgeDir = new File(context.getFilesDir(), ATTACHMENT_KNOWLEDGE_DIR_NAME);
        boolean dirExists = attachmentKnowledgeDir.mkdirs();
        if (dirExists && !attachmentKnowledgeDir.isDirectory()) {
            Log.e(TAG, "Simulation directory already exists as a non-directory");
        }
        attachment2Version = readVersions();
        attachment2File = readFilePointers();
        file2Attachment = new HashMap<>();
        for (Attachment attachment : attachment2File.keySet()) {
            file2Attachment.put(attachment2File.get(attachment), attachment);
        }
    }

    private Map<Attachment, String> readVersions() {
        File file = new File(attachmentKnowledgeDir, LAST_MODIFIED_FILE_NAME);
        if (file.exists() && file.canRead()) {
            try {
                Scanner scanner = new Scanner(file);
                scanner.useDelimiter("\\A");
                String str = scanner.next();
                scanner.close();
                Type type = new TypeToken<Map<Attachment, String>>() {
                }.getType();
                try {
                    return new Gson().fromJson(str, type);
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "Broken syntax in last modified knowledge file. Creating empty last modified knowledge", e);
                    return new HashMap<>();
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Couldn't find last modified knowledge file, although it exists", e);
                return new HashMap<>();
            }
        } else {
            return new HashMap<>();
        }
    }

    private Map<Attachment, File> readFilePointers() {
        File file = new File(attachmentKnowledgeDir, FILE_POINTER_FILE_NAME);
        if (file.exists() && file.canRead()) {
            try {
                Scanner scanner = new Scanner(file);
                scanner.useDelimiter("\\A");
                String str = scanner.next();
                scanner.close();
                Type type = new TypeToken<Map<Attachment, File>>() {
                }.getType();
                try {
                    return new Gson().fromJson(str, type);
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "Broken syntax in file location knowledge file. Creating empty file location knowledge", e);
                    return new HashMap<>();
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Couldn't find file location knowledge file, although it exists", e);
                return new HashMap<>();
            }
        } else {
            return new HashMap<>();
        }
    }

    @Override
    public Collection<Attachment> getAllAttachments() {
        synchronized (attachment2Version) {
            return new ArrayList<>(attachment2Version.keySet());
        }
    }

    @Override
    public boolean has(Attachment attachment) {
        synchronized (attachment2Version) {
            return attachment2Version.containsKey(attachment);
        }
    }

    @Override
    public String getVersion(Attachment attachment) {
        synchronized (attachment2Version) {
            return attachment2Version.get(attachment);
        }
    }

    @Override
    public void addAttachment(Attachment attachment, String version, File file) {
        if (attachment == null) {
            throw new NullPointerException("attachment is null");
        }
        if (file == null) {
            throw new NullPointerException("file is null");
        }
        synchronized (attachment2Version) {
            attachment2Version.put(attachment, version);
        }
        synchronized (attachment2File) {
            attachment2File.put(attachment, file);
            file2Attachment.put(file, attachment);
        }
        writeLastModified();
        writeFileLocations();
    }

    @Override
    public void removeAttachment(Attachment attachment) {
        if (attachment != null) {
            synchronized (attachment2Version) {
                attachment2Version.remove(attachment);
            }
            synchronized (attachment2File) {
                file2Attachment.remove(attachment2File.remove(attachment));
            }
            writeLastModified();
            writeFileLocations();
        }
    }

    @Override
    public File getFile(Attachment attachment) {
        synchronized (attachment2File) {
            return attachment2File.get(attachment);
        }
    }

    @Override
    public Attachment getAttachment(File file) {
        synchronized (attachment2File) {
            return file2Attachment.get(file);
        }
    }

    public void writeLastModified() {
        synchronized (attachment2Version) {
            File file = new File(attachmentKnowledgeDir, LAST_MODIFIED_FILE_NAME);
            try {
                if (file.exists() || file.createNewFile()) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    writer.write(gson.toJson(attachment2Version));
                    writer.flush();
                    writer.close();
                } else {
                    throw new IOException("Cannot create file " + file.getName());
                }
            } catch (IOException e) {
                Log.e(TAG, "Couldn't write last modified to file", e);
            }
        }
    }

    public void writeFileLocations() {
        synchronized (attachment2File) {
            File file = new File(attachmentKnowledgeDir, FILE_POINTER_FILE_NAME);
            try {
                if (file.exists() || file.createNewFile()) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    writer.write(gson.toJson(attachment2File));
                    writer.flush();
                    writer.close();
                } else {
                    throw new IOException("Cannot create file " + file.getName());
                }
            } catch (IOException e) {
                Log.e(TAG, "Couldn't write file pointers to file", e);
            }
        }
    }
}
