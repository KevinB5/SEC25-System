package pt.tecnico.sec;

public interface ILibrary {

    public String intentionToSell(String userID, String goodID);

    public String getStateOfGood(String goodID);

    public String transferGood(String user, String buyerID, String goodID);
    
    public String buyGood(String goodID);
    
    public String sellGood(String goodID);

}