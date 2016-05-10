package io;

import drive.change.ChangeStruct;
import drive.change.NeedNameInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2016-05-05.
 */
public abstract class AbstractChangeService implements NeedNameInterface{
    protected Logger logger = LoggerFactory.getLogger(MoveService.class);
    protected ChangeStruct structure;

    public AbstractChangeService(ChangeStruct structure){
        this.structure = structure;
    }
}
