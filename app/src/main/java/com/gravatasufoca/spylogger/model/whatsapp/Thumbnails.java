package com.gravatasufoca.spylogger.model.whatsapp;

import com.gravatasufoca.spylogger.model.EntidadeAbstrata;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bruno on 30/04/17.
 */

@DatabaseTable(tableName = "message_thumbnails")
public class Thumbnails extends EntidadeAbstrata{

    @DatabaseField(dataType=DataType.BYTE_ARRAY)
    private byte[] thumbnail;
    @DatabaseField(dataType= DataType.DATE_LONG)
    private Date timestamp;
    @DatabaseField()
    private String key_remote_jid;
    @DatabaseField()
    private Integer key_from_me;
    @DatabaseField()
    private String key_id;

    @Override
    public Serializable getId() {
        return key_id;
    }
}
