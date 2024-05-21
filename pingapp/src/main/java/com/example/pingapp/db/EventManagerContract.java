package com.example.pingapp.db;

import android.provider.BaseColumns;

public final class EventManagerContract {

    /* To prevent someone from accidentally instantiating the contract class,
    make the constructor private. */
    private EventManagerContract() {}


    /* Inner class that defines the table contents */
    public static class SleepEvent implements BaseColumns {
        public static final String TABLE_NAME1 = "HeartRate";
        public static final String TABLE_NAME2 = "Motion";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_EVENT1 = "bpm";
        public static final String COLUMN_NAME_EVENT2_x = "acceleration_x";
        public static final String COLUMN_NAME_EVENT2_y = "acceleration_y";
        public static final String COLUMN_NAME_EVENT2_z = "acceleration_z";


    }

}