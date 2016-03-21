/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.security.*;
import java.util.Date;

/**
 * 
 * @author
 * 
 */
public class CGraphSnapshot {

	private String snapshotString;
	private Date createdAt;
	
	//cache the hash
	private String cachedHash;

	/**
	 * 
	 * @param s
	 */
	public CGraphSnapshot(String s) {
		this.createdAt = new Date();
		this.snapshotString = s;
	}

	/**
	 * 
	 * @param snapshotString
	 */
	public void setSnapshotString(String snapshotString) {
		this.snapshotString = snapshotString;
	}

	/**
	 * 
	 * @return
	 */
	public String getSnapshotString() {
		return snapshotString;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * 
	 * @return
	 */
	public String getHash() {
		
		//if cached return it
		if (cachedHash !=null) {
			return cachedHash;
		}
		
		byte[] source = snapshotString.getBytes();
		byte[] hash = null;
		try {
			hash = MessageDigest.getInstance("MD5").digest(source);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (hash == null) {
			return null;
		}

		final StringBuilder hashString = new StringBuilder();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(hash[i]);
			if (hex.length() == 1) {
				hashString.append('0');
				hashString.append(hex.charAt(hex.length() - 1));
			} else
				hashString.append(hex.substring(hex.length() - 2));
		}
		
		//cache it to be more efficiient
		cachedHash = hashString.toString();
		
		return cachedHash;
	}
}
