package cnt5106c.torrent.config;

public class BadFileFormatException extends Exception {

	/**
	 * default serial version UID
	 *	//TODO find out why is this required
	 */
	private static final long serialVersionUID = 1L;
	private String errorMessage;
	
	public BadFileFormatException(String errorMessage)
	{
		super(errorMessage);
		this.errorMessage = errorMessage;
	}
	
	public BadFileFormatException()
	{
		super();
		errorMessage = "unknown";
	}
	
	public String getMessage()
	{
		return errorMessage;
	}
}
