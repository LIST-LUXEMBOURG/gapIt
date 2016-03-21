/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import java.io.*;

import javax.swing.ImageIcon;


/**
 * 
 *
 *
 * @author Olivier PARISOT
 */
public final class FileUtil 
{
	//
	// Constructors
	//

	/**
	 * Private constructor.
	 */
	private FileUtil() {}


	//
	// Static fields
	//

	public static StringBuilder getFileContent(final String fileName)
	{
		final StringBuilder sb=new StringBuilder();

		BufferedReader br=null;
		try 
		{
			final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
			if (is==null) throw new Exception("File '"+fileName+"' not found!");
			br = new BufferedReader(new InputStreamReader(is,"ISO-8859-15"));
			String nextLine="";
			while ((nextLine = br.readLine()) != null)
			{
				sb.append(nextLine);
				sb.append("\n");
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if (br!=null)
			{
				try 
				{
					br.close();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		}
		return sb;
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static ImageIcon getImageFromFile(String fileName){
		try {
			final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			return new ImageIcon(buffer.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get extension of a file
	 * @param f
	 * @return
	 */
	public static String getExtension(File f){
		if(!f.exists()) throw new IllegalStateException("File must exist!");
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
}
