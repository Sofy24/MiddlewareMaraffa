package org.example.repository;

import org.example.game.GameSchema;
import org.example.game.Trick;

public abstract class AbstractStatisticManager {
    //TODO matte queste chiamate devo essere sparate e non attesa, o sarebbe comodo che ogni verticle si connetta al db e faccia le sue cose ???
    public abstract void createRecord(GameSchema schema);

    public abstract void updateRecordWithTrick(String recordID, Trick trick);
}
