package pt.tecnico.sec;

public interface ILibrary {

    public boolean intentionToSell(String userID, String goodID);

    public Object[] getStateOfGood(String goodID);

    public boolean transferGood(String goodID);


}