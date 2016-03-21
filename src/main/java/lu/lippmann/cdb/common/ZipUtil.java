/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipInputStream;


/**
 * ZipUtil.
 * 
 * @author the CADRAL devteam
 */
public class ZipUtil 
{
	private ZipUtil() {}	
	
    public static void unzipFile(final ZipInputStream zipIn,final String filePath) throws IOException 
    {
        final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        final byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) 
        {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public static String unzipFile(final ZipInputStream zipIn) throws IOException 
    {
        final ByteArrayOutputStream bos=new ByteArrayOutputStream();
        final byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) 
        {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
        return new String(bos.toByteArray(),"UTF-8");
    }

    public static String unzipFile(final URL zipUrl) throws IOException
    {
		//final URL zipUrl=new URL(URL);
		final ZipInputStream zin=new ZipInputStream(zipUrl.openStream());			
		zin.getNextEntry();			
		final String csvContent=ZipUtil.unzipFile(zin);			
		zin.close();
		return csvContent;
    }
}
