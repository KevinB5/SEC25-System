package hdsNotary;

public interface Library {

    public boolean intentionToSell(String userID, String goodID);

    public Object[] getStateOfGood(String goodID);

    public boolean transferGood(String goodID);


}