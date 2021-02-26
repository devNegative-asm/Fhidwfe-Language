package ti83packager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
public class Packager {
	public static void to8xp(String filename)
	{
		try {
			FileInputStream in = new FileInputStream(new File(filename));
			String name = filename.split("\\.")[0];
			try {
				/*
				 * ANNOYING FORMATTING
				 *  **TI83F*
				 *  0x1A 0x0A 0x00
				 *  42 useless bytes
				 *  length {
				 * 	0x0B 0x00
				 * 	lengthofnext <-----____
				 * 	0x06                  |
				 * 	8 bytes of name       |
				 * 	0x00                  |
				 * 	0x00                  |
				 * 	lengthofnext (copy of |)
				 * 	{                     |
				 * 		that -2  >________|
				 * 		0xBB
				 * 		0x6D
				 * 		PROGRAM BINARY
				 * 		
				 * 		
				 * 	}
				 * }
				 * checksum
				 */
				
				//BLAST IT!
				
				
				FileOutputStream out = new FileOutputStream(name+".8xp");
				
				DoubleEndedByteBuffer fileData = new DoubleEndedByteBuffer(0,"**TI83F*".getBytes());
				fileData.append(new byte[]{0x1A, 0x0A, 0x00});
				fileData.append(new byte[41]);
				fileData.append((byte)0);
				DoubleEndedByteBuffer code = new DoubleEndedByteBuffer(0x9d95);
				//code.prepend(new byte[]{(byte)0xBB,(byte)0x6D});
				
				int available;
				while((available=in.available())!=0)
				{
					byte[] readin = new byte[available];
					in.read(readin);
					code.append(readin);
				}
				//code.writeHexWithFront(binary);
				
				code.prependLittleEndian((short) code.size());
				
				
				short newLength = (short) code.size();
				code.prependLittleEndian(newLength);
				code.prepend((byte)0x80);
				code.prepend((byte)0);
				for(int i=8;i>0;i--)
					if(name.getBytes().length>=i)
						code.prepend(name.toUpperCase().replaceAll("[^A-Z]","").getBytes()[i-1]);
					else
						code.prepend((byte)0);
				code.prepend((byte)0x06);
				code.prependLittleEndian(newLength);
				code.prepend((byte)0x00);
				code.prepend((byte)0x0D);
				
				fileData.appendLittleEndian((short)code.size());
				fileData.append(code);
				fileData.appendLittleEndian((short)code.checkSum());
				fileData.writeSelf(out);
				
				in.close();
				out.close();
			} catch (IOException e) {
				throw new RuntimeException("cannot create file "+name+".8xp");
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("could not find file "+filename);
		}
	}
}
