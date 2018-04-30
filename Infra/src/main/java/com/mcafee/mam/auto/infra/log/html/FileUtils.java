package com.mcafee.mam.auto.infra.log.html;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class FileUtils
{

	private static Logger logger = Logger.getLogger(FileUtils.class);

	public static void addResourceFiles(String srcFileFolder, String srcFileName, String resFolder)
	{
		String srcFilePath = srcFileFolder + "/" + srcFileName;
		InputStream is = FileUtils.class.getClassLoader().getResourceAsStream(srcFilePath);
		FileOutputStream fs = null;
		try
		{
			if (is == null)
			{
				throw new IllegalStateException("Cannot find resource:" + srcFileFolder + "/" + srcFileName);
			}
			else
			{
				logger.debug("Loading resources from: " + srcFilePath + " to: " + resFolder);
				try
				{
					File resfolderFile = new File(resFolder);
					File resFile = new File(resfolderFile, srcFileName);
					fs = new FileOutputStream(resFile);
					ByteStreams.copy(is, fs);
				}
				catch (Exception ex)
				{
					logger.info(ex);
					throw new IllegalStateException(ex);
				}
			}
		}
		finally
		{
			Closeables.closeQuietly(is);
			Closeables.closeQuietly(fs);
		}
	}

	public static void delete(File file) throws IOException
	{

		if (file.isDirectory())
		{

			// directory is empty, then delete it
			if (file.list().length == 0)
			{
				file.delete();
			}
			else
			{
				// list all the directory contents
				String files[] = file.list();

				for (String temp : files)
				{
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					delete(fileDelete);
				}

				// check the directory again, if empty then delete it
				if (file.list().length == 0)
				{
					file.delete();
				}
			}

		}
		else
		{
			// if file, then delete it
			file.delete();
		}
	}

	/**
	 * * delete log folder if exists and create if not exists
	 * 
	 * @param logFolder
	 */
	/**
	 * * delete log folder if exists and create if not exists
	 * 
	 * @param logFolder
	 */
	public static void init(String rootLogFolder, boolean isSaveArcive)
	{
		try
		{
			createFolderIfNotExists(rootLogFolder);
			File currentFolder = new File(rootLogFolder + "/current");
			if (currentFolder.exists())
			{
				if (isSaveArcive)
				{
				String oldFolder = rootLogFolder + "/old";
				createFolderIfNotExists(oldFolder);
				zipDirectory(currentFolder, "", oldFolder + "/Log_" + getDateForFile() + ".zip", false);
				}
				delete(currentFolder);
				createFolderIfNotExists(rootLogFolder + "/current");
			}
			else
			{
				createFolderIfNotExists(rootLogFolder + "/current");
			}

		}
		catch (IOException ex)
		{
			System.err.println("Failed to init log folder");
			System.err.println(ex.toString());
		}
	}

	public static void createFolderIfNotExists(String folder)
	{
		File fileFolder = new File(folder);
		if (!fileFolder.exists())
		{
			fileFolder.mkdirs();
		}
	}

	public static String getDateForFile()
	{
		return new Date(System.currentTimeMillis()).toString().replaceAll("\\s", "_").replaceAll(":", "_");
	}
	/**
	 * Zip given directory
	 * 
	 * @param directory
	 *            String
	 * @param fileExtention
	 *            String
	 * @param destinationFile
	 *            String
	 * @throws IOException
	 */
	public static void zipDirectory(File directory, String fileExtention, String destinationFile, boolean report) throws IOException
	{

		if (!directory.exists()) { return; }
		Vector<File> logFiles = new Vector<File>(100);
		collectAllFiles(directory, new ExtentionFilter(fileExtention), logFiles);
		if (logFiles.size() == 0) { return; }
		if (report)
		{
			System.out.println("Backup " + logFiles.size() + " files");
		}
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(destinationFile));
		int mod = logFiles.size() / 10;
		if (mod == 0)
		{
			mod = 1;
		}
		try
		{
			for (int i = 0; i < logFiles.size(); i++)
			{
				if (report)
				{
					if ((i % mod) == 0)
					{
						System.out.print(".");
					}
				}
				File f = (File) logFiles.elementAt(i);
				if (f.isFile())
				{
					addZipEntry(zipOut, f, directory);
				}
			}
		}
		finally
		{
			try
			{
				zipOut.close();
			}
			catch (IOException e)
			{
				// e.printStackTrace();
			}
		}
	}

	public static void collectAllFiles(File root, FilenameFilter filter, Vector<File> collectTo)
	{
		File[] list = root.listFiles();
		for (int i = 0; i < list.length; i++)
		{
			if (list[i].isDirectory())
			{
				collectAllFiles(list[i], filter, collectTo);
			}
			else
			{
				if (filter.accept(list[i].getParentFile(), list[i].getName()))
				{
					collectTo.addElement(list[i]);
				}
			}
		}
	}

	private static void addZipEntry(ZipOutputStream zipOut, File zipIn, File root) throws IOException
	{

		BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(zipIn));
		byte buffer[] = new byte[1024];
		int length;

		zipOut.putNextEntry(new ZipEntry(zipIn.getPath().substring(root.getPath().length() + 1)));

		try
		{
			length = inStream.read(buffer);
			while (length != -1)
			{
				zipOut.write(buffer, 0, length);
				length = inStream.read(buffer);
			}
		}
		finally
		{
			zipOut.closeEntry();
			inStream.close();
		}
	}
}
