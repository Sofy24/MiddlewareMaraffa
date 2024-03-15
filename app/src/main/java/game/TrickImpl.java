package game;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.bson.codecs.pojo.annotations.BsonIgnore;

public class TrickImpl implements Trick{
    // private final Map<String, Pair<Integer, String>> cards = new HashMap<>();
    private final Map<String, String> cards = new LinkedHashMap<>(); // linked to keep the order of the cards
    private final AtomicInteger currentUser;

    private Call call = Call.NONE;
    @BsonIgnore
    private final int numberOfPlayers;

    @BsonIgnore
    private final CardSuit trump;

    public TrickImpl(int numberOfPlayers, final CardSuit trump) {
        this.numberOfPlayers = numberOfPlayers;
        this.trump = trump;
        this.currentUser = new AtomicInteger(0);
    }

    /**@param card added to the trick, if not all the players has already played*/
    @Override
    public void addCard(Card<CardValue, CardSuit> card, String username) {
        // this.cards.put(card.toString(), new Pair<>(this.currentUser.getAndIncrement(), username));
        this.cards.put(String.valueOf(card.getCardValue()) , username);
    }    

    public Call getCall() {
        return call;
    }

    @Override
    public void setCall(Call call, String username) {
        this.call = call;
    }

    public List<String> getCards(){
        return  this.cards.keySet().stream().toList();
    }

    @Override
    public Map<String, String> getCardsAndUsers() {
        return this.cards;
    }   

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public CardSuit getTrump() {
        return trump;
    }

    /**@return true if all the players have played their card*/
    @BsonIgnore
    @Override
    public boolean isCompleted() {
        return this.cards.keySet().size() == this.numberOfPlayers;
    }

    @Override
    public String toString() {
        return "Trick{" +
                "cards=" + cards +
                ", trump=" + trump +
                ", call=" + call +
                '}';
    }
}
