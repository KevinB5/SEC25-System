package hdsNotary;

public interface library extends Remote{

    public boolean intentionToSell(String userID, String goodID);

    public Object[] getStateOfGood(goodID);

    public boolean transferGood(String goodID);


}