package cnt5106c.torrent.messages;
import java.util.BitSet;

public abstract class BitfieldMessage extends ActualMessage
{
	protected static final long serialVersionUID = 5L;
	//final int BITFIELD_SIZE = 302;
	private BitSet bits;
	public BitfieldMessage (String msg)
	{
		if (length != 0)
		{
			//length is in bytes, and we need that many bits
			int bit_len = length * 8;
			bits = new BitSet(bit_len);
					
			// convert bits_int to bits
			int j = 0;
			for (int i = 5; i < 5+length; i++)
			{
				int one_byte = Integer.parseInt(msg.substring(i, i+1));
				for (int k = 0; k < 8; k++)
				{
					// AND with only the highest-bit as 1 (1000 0000)
					if ((one_byte & 0x80) != 0)
						bits.set(j++, 1);
					else
						bits.set(j++, 0);
					one_byte = one_byte << 1;
				}
			}
		}
	}
	public BitSet GetBitField()
	{
		return bits;
	}
	public boolean IsSet(int bit_pos)
	{
		return bits.get(bit_pos);
	}
	public void Set(int bit_pos)
	{
		bits.set(bit_pos);
	}
	public void UnSet(int bit_pos)
	{
		bits.clear(bit_pos);
	}
};
