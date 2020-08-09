package com.artifex.mupdf_test;

/**
 * This file contains an example implementation of the SOSecureFS interface.
 *
 * This implementation uses the standard RandomAccessFile class for file
 * operations.
 *
 * Customers should replace this with their proprietary file API for
 * handling encrypted files.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.SyncFailedException;
import java.lang.ClassCastException;
import java.lang.NullPointerException;
import java.lang.SecurityException;
import java.nio.channels.FileChannel;
import java.nio.channels.ClosedChannelException;
import android.os.Environment;
import android.util.Log;

import com.artifex.solib.SOSecureFS;

public class SecureFS implements SOSecureFS
{
    private final  String   mDebugTag  = "SecureFS";

    /**
     * Encrypted files can be identified by a specific prefix which can
     * then be mapped to an actual file path.
     *
     * In this example file paths starting with 'mSecurePrefix' are deemed
     * to be in the secure container and suitable for handling.
     */
    private static final String mSecurePrefix = "/SECURE";

    /**
     * The physical root of the secure container.
     */
    private static final String mSecurePath =
        Environment.getExternalStoragePublicDirectory(
                                  Environment.DIRECTORY_DOWNLOADS)
                                  .getAbsolutePath() +
                                  File.separator + "Secure";

    //////////////////////////////////////////////////////////////////////////
    // Utility Methods.
    //////////////////////////////////////////////////////////////////////////

    /**
     * Map paths starting with {@link #mSecurePrefix} to their physical
     * location as defined by {@link #mSecurePath}.
     *
     * @param path The path to be mapped.
     *
     * @return The result of the mapping.<br>
     *         On error the path is returned unaltered.
     */
    private String mapSecurePrefixToPath(String path)
    {
        if (! path.startsWith(mSecurePrefix))
        {
            Log.e(mDebugTag, "mapSecurePrefixToPath [" + path + "]: " +
                             "Does not start with secure prefix");

            return path;
        }

        return path.replaceFirst(mSecurePrefix, mSecurePath);
    }

    /**
     * Return the physical root of the secure container.
     *
     * @return The physical path to the secure container.
     */
    public String getSecurePath()
    {
        return mSecurePath;
    }

    /**
     * Return the 'tag' to indicate the file is suitable for decryption.
     *
     * @return The secure 'tag' string.
     */
    public String getSecurePrefix()
    {
        return mSecurePrefix;
    }

    //////////////////////////////////////////////////////////////////////////
    // Methods Required By Interface.
    //////////////////////////////////////////////////////////////////////////

    /**
     * This method determines whether the supplied file path resides
     * within the secure container.<br><br>
     *
     * The path may be a pseudo path on which a mapping can be performed
     * to access the actual file. For example "/SECURE/filename".
     *
     * @param path The file path to be analysed.
     *
     * @return True if the file resides within the secure container.<br>
     *         False otherwise.
     */
    public boolean isSecurePath(final String path)
    {
        return path.startsWith(mSecurePrefix) || path.startsWith(mSecurePath);
    }

    /**
     * This method returns the directory to be used to store temporary
     * files created during file translation/saving.<br><br>
     *
     * This directory must reside within the secure container as identified
     * in {@link #isSecurePath(String)}.
     *
     * @return The (pseudo) path to the temporary directory.
     */
    public final String getTempPath()
    {
        return mSecurePrefix + File.separator + "tmp";
    }

    /**
     * This method returns the relevant attributes of the file located
     * at the supplied path. The path will reference a file within the
     * secure container.<br><br>
     *
     * The attributes should refer to the properties of the decrypted file.
     * <br><br>
     *
     * The (pseudo) path will be constructed using the secure container
     * identifier used in {@link #isSecurePath(String)}.
     *
     * @param path The path to the file to obtain attributes for.
     *
     * @return A reference to a {@link FileAttributes} object.<br>
     *         null on error.
     */
    public FileAttributes getFileAttributes(final String path)
    {
        File file                 = new File(mapSecurePrefixToPath(path));
        FileAttributes attributes = new FileAttributes();

        if (! file.exists())
        {
            return null;
        }

        attributes.length       = file.length();
        attributes.lastModified = file.lastModified() / 1000;

        // Attribute Flags
        attributes.isHidden    = file.isHidden();
        attributes.isDirectory = file.isDirectory();
        attributes.isWriteable = file.canWrite();
        attributes.isSystem    = false;

        return attributes;
    }

    /**
     * This method renames a file within the secure container.<br><br>
     *
     * Both source and destination file paths will reside within the
     * secure container and be constructed using the secure container
     * identifier used in {@link #isSecurePath(String)}.
     *
     * @param src The path to the file to be renamed.
     * @param dst The path to the destination file.
     *
     * @return True on success. False on failure.
     */
    public boolean renameFile(final String src, final String dst)
    {
        File srcFile = new File(mapSecurePrefixToPath(src));
        File dstFile = new File(mapSecurePrefixToPath(dst));

        if (! srcFile.exists())
        {
            return false;
        }

        try
        {
            return srcFile.renameTo(dstFile);
        }
        catch (SecurityException e)
        {
            return false;
        }
    }

    /**
     * This method copies a file to a new location within the secure
     * container..<br><br>
     *
     * Both source and destination file paths will reside within the
     * secure container and be constructed using the secure container
     * identifier used in {@link #isSecurePath(String)}.
     *
     * @param src The path to the file to be copied.
     * @param dst The path to the destination file.
     *
     * @return True on success. False on failure.
     */
    public boolean copyFile(final String src, final String dst)
    {
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        boolean     result     = false;

        try
        {
            File srcFile = new File(mapSecurePrefixToPath(src));
            File dstFile = new File(mapSecurePrefixToPath(dst));

            // Ensure the destination file exists.
            dstFile.createNewFile();

            srcChannel = new FileInputStream(srcFile).getChannel();
            dstChannel = new FileOutputStream(dstFile).getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

            result = true;
        }
        catch (Exception e)
        {
        }

        try
        {
            if (srcChannel != null)
            {
                srcChannel.close();
            }
            if (dstChannel != null)
            {
                dstChannel.close();
            }
        }
        catch (Exception e)
        {
        }

        return result;
    }

    /**
     * This method deletes a file from within the secure container..<br><br>
     *
     * The file path will be constructed using the secure container
     * identifier used in {@link #isSecurePath(String)}.
     *
     * @param path The path to the file to be deleted.
     *
     * @return True on success. False on failure.
     */
    public boolean deleteFile(final String path)
    {
        File file = new File(mapSecurePrefixToPath(path));

        try
        {
            return file.delete();
        }
        catch (SecurityException e)
        {
            return false;
        }
    }

    /**
     * This method tests for the existence of a file within the secure
     * container.<br><br>
     *
     * The file path will be constructed using the secure container
     * identifier used in {@link #isSecurePath(String)}.
     *
     * @param path The path to the file to be checked.
     *
     * @return True if the file exists. False otherwise.
     */
    public boolean fileExists(final String path)
    {
        File file = new File(mapSecurePrefixToPath(path));

        try
        {
            return file.exists();
        }
        catch (SecurityException e)
        {
            return false;
        }
    }

    /**
     * This method recursively deletes the supplied directory, and it's
     * sub-directories, located within the secure container.<br><br>
     *
     * The path will be constructed using the secure container
     * identifier used in {@link #isSecurePath(String)}.
     *
     * @param path The path to the directory to be deleted.
     *
     * @return True on success. False on failure.
     */
    public boolean recursivelyRemoveDirectory(final String path)
    {
        File item = new File(mapSecurePrefixToPath(path) + '/');

        try
        {
            if (item.isDirectory())
            {
                for (File child : item.listFiles())
                {
                    recursivelyRemoveDirectory(child.getPath());
                }
            }

            item.delete();

            return true;
        }
        catch (SecurityException e)
        {
            return false;
        }
        catch (NullPointerException e)
        {
            Log.e(mDebugTag, "recusivelyRemoveDirectory() failed  [" + path +
                             "]: " + "Have storage permissions been granted");

            return false;
        }
    }

    /**
     * This method creates a directory, and all non-existent directories in
     * the supplied path, within the secure container.<br><br>
     *
     * The path will be constructed using the secure container
     * identifier used in {@link #isSecurePath(String)}.
     *
     * @param path The path to the directory to be created.
     *
     * @return True on success. False on failure.
     */
    public boolean createDirectory(final String path)
    {
        File dir = new File(mapSecurePrefixToPath(path));

        try
        {
            return dir.mkdirs();
        }
        catch (SecurityException e)
        {
            return false;
        }
    }

    /**
     * This method creates a file within the secure container.<br><br>
     *
     * The file path will be constructed using the secure container
     * identifier used in {@link #isSecurePath(String)}.
     *
     * @param path The path to the file to be created.
     *
     * @return True on success. False on failure.
     */
    public boolean createFile(final String path)
    {
        File file = new File(mapSecurePrefixToPath(path));

        try
        {
            return file.createNewFile();
        }
        catch (IOException e)
        {
            return false;
        }
        catch (SecurityException e)
        {
            return false;
        }
    }

    /**
     * This method opens an existing file, in the secure container, for
     * reading.<br><br>
     *
     * The file path will be constructed using the secure container
     * identifier used in {@link #isSecurePath(String)}.
     *
     * @param path The path to the file to be opened.
     *
     * @return The file handle object on success.<br>
     *         null on error.
     */
    public Object getFileHandleForReading(final String path)
    {
        try
        {
            return new RandomAccessFile(mapSecurePrefixToPath(path), "r");
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
        catch (SecurityException e)
        {
            return null;
        }
    }

    /**
     * This method opens an existing file, in the secure container, for
     * writing.<br><br>
     *
     * The file path will be constructed using the secure container
     * identifier used in {@link #isSecurePath(String)}.
     *
     * @param path The path to the file to be opened.
     *
     * @return The file handle object on success.<br>
     *         null on error.
     */
    public Object getFileHandleForWriting(final String path)
    {
        try
        {
            return new RandomAccessFile(mapSecurePrefixToPath(path), "rw");
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
        catch (SecurityException e)
        {
            return null;
        }
    }

    /**
     * This method opens an existing file, in the secure container, for
     * updating.<br><br>
     *
     * The file path will be constructed using the secure container
     * identifier used in {@link #isSecurePath(String)}.
     *
     * @param path The path to the file to be opened.
     *
     * @return The file handle object on success.<br>
     *         null on error.
     */
    public Object getFileHandleForUpdating(final String path)
    {
        try
        {
            return new RandomAccessFile(mapSecurePrefixToPath(path), "rw");
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
        catch (SecurityException e)
        {
            return null;
        }
    }

    /**
     * This method sets the file length of a file located within the
     * secure container.
     *
     * @param handle The file handle object to be used..
     *
     * @return True on success. False on failure.
     */
    public boolean setFileLength(final Object handle, final long length)
    {
        try
        {
            RandomAccessFile file = (RandomAccessFile)handle;
            file.setLength(length);

            return true;
        }
        catch (ClassCastException e)
        {
            return false;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    /**
     * This method closes an open file within the secure container.
     *
     * @param handle The file handle object to be closed.
     *
     * @return True on success. False on failure.
     */
    public boolean closeFile(final Object handle)
    {
        try
        {
            RandomAccessFile file = (RandomAccessFile)handle;

            file.close();

            return true;
        }
        catch (ClassCastException e)
        {
            return false;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    /**
     * This method reads data from a file located within the secure container.
     *
     * @param handle The file handle object to be used..
     * @param buf    The buffer to put the read data in.
     *
     * @return The amount of data placed in the buffer.<br>
     *          0 on EOF<br>
     *         -1 on error.
     */
    public int readFromFile(final Object handle, final byte buf[])
    {
        try
        {
            RandomAccessFile file  = (RandomAccessFile)handle;
            int              retVal = file.read(buf);

            if (retVal == -1)
            {
                /* Use 0 to signify EOF */
                return 0;
            }
            else
            {
                return retVal;
            }
        }
        catch (ClassCastException e)
        {
            return -1;
        }
        catch (IOException e)
        {
            return -1;
        }
        catch (NullPointerException e)
        {
            return -1;
        }
    }

    /**
     * This method writes data to a file located within the secure container.
     *
     * @param handle The file handle object to be used.
     * @param buf    The buffer containing the data.
     *
     * @return The amount of data written.
     *         -1 on error.
     */
    public int writeToFile(final Object handle, final byte buf[])
    {
        try
        {
            RandomAccessFile file  = (RandomAccessFile)handle;

            file.write(buf);

            return buf.length;
        }
        catch (ClassCastException e)
        {
            return -1;
        }
        catch (IOException e)
        {
            return -1;
        }
    }

    /**
     * This method forces buffered data to be written to the underlying
     * device.
     *
     * @param handle The file handle object to be used.
     *
     * @return True on success. False on failure.
     */
    public boolean syncFile(final Object handle)
    {
        try
        {
            RandomAccessFile file  = (RandomAccessFile)handle;

            file.getFD().sync();
        }
        catch (ClassCastException e)
        {
            return false;
        }
        catch (SyncFailedException e)
        {
            return false;
        }
        catch (IOException e)
        {
            return false;
        }

        return true;
    }

    /**
     * This method obtains the length of a file within the secure container.
     * <br><br>
     *
     * The length returned is that of the decrypted file.
     *
     * @param handle The file handle object to be used.
     *
     * @return The file length. -1 on error.
     */
    public long getFileLength(final Object handle)
    {
        try
        {
            RandomAccessFile file  = (RandomAccessFile)handle;

            return file.length();
        }
        catch (ClassCastException e)
        {
            return -1;
        }
        catch (IOException e)
        {
            return -1;
        }
    }

    /**
     * This method obtains the offset, from the start of the file , of the
     * file pointer. <br><br>
     *
     * The offset returned relates to the decrypted file.
     *
     * @param handle The file handle object to be used.
     *
     * @return The file pointer offset. -1 on error.
     */
    public long getFileOffset(final Object handle)
    {
        try
        {
            RandomAccessFile file  = (RandomAccessFile)handle;

            return file.getChannel().position();
        }
        catch (ClassCastException e)
        {
            return -1;
        }
        catch (ClosedChannelException e)
        {
            return -1;
        }
        catch (IOException e)
        {
            return -1;
        }
    }

    /**
     * This method moves the file pointer to the requested offset from 0.
     * <br><br>
     *
     * The offset relates to the decrypted file.
     *
     * @param handle The file handle object to be used.
     *
     * @return True on success. False on failure.
     */
    public boolean seekToFileOffset(final Object handle, final long offset)
    {
        try
        {
            RandomAccessFile file  = (RandomAccessFile)handle;

            file.seek(offset);
        }
        catch (ClassCastException e)
        {
            return false;
        }
        catch (IOException e)
        {
            return false;
        }

        return true;
    }
}
